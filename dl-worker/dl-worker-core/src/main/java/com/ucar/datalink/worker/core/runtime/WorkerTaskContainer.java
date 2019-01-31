package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.worker.core.util.classloader.ClassLoaderFactory;
import com.ucar.datalink.worker.core.util.classloader.ClassLoaderSwapper;
import com.ucar.datalink.worker.core.util.classloader.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * WorkerTaskContainer是一个Task的执行入口类，该类负责组织WorkerTaskReader和WorkerCombinedTaskWriter的生命周期管理
 * <p>
 * Created by lubiao on 2017/2/14.
 */
public class WorkerTaskContainer extends WorkerTask {
    private static final Logger logger = LoggerFactory.getLogger(WorkerTaskContainer.class);

    private final ArrayBlockingQueue<TaskChunk> queue;
    private final WorkerTaskContext workerTaskContext;
    private final WorkerTaskReaderContext workerTaskReaderContext;
    private final WorkerTaskReader workerTaskReader;
    private final WorkerCombinedTaskWriter workerCombinedTaskWriter;
    private final ClassLoaderSwapper classLoaderSwapper;
    private final ClassLoader readerClassLoader;
    private final Thread readerThread;
    private final Thread writerThread;
    private final AtomicBoolean subWorkersStarted;

    public WorkerTaskContainer(WorkerConfig workerConfig, TaskInfo taskInfo, TaskStatusListener statusListener, TaskPositionManager taskPositionManager, String executionId) {
        super(workerConfig, taskInfo, statusListener, executionId);

        this.queue = new ArrayBlockingQueue<>(1);
        this.workerTaskContext = new WorkerTaskContext(id(), executionId);
        this.workerTaskReaderContext = new WorkerTaskReaderContext(workerTaskContext, taskPositionManager, taskInfo().getTaskReaderParameterObj());
        this.classLoaderSwapper = ClassLoaderSwapper.newCurrentThreadClassLoaderSwapper();
        this.readerClassLoader = ClassLoaderFactory.getClassLoader(PluginType.Reader, workerTaskReaderContext.getReaderParameter().getPluginName(),
                workerConfig().getString(WorkerConfig.CLASSLOADER_TYPE_CONFIG));
        this.workerTaskReader = newWorkerTaskReader();
        this.workerCombinedTaskWriter = newWorkerCombinedTaskWriter();
        this.readerThread = new Thread(workerTaskReader, MessageFormat.format("Task-{0}-Reader-{1}", id(), workerTaskReaderContext.getReaderParameter().getPluginName()));
        this.readerThread.setContextClassLoader(readerClassLoader);
        this.writerThread = new Thread(workerCombinedTaskWriter, MessageFormat.format("Task-{0}-CombinedWriter", id()));
        this.subWorkersStarted = new AtomicBoolean(false);
    }

    @Override
    public void initialize(TaskInfo taskInfo) {
        try {
            //initialize方法已经是插件执行内容，需使用插件自己的classloader
            this.classLoaderSwapper.setCurrentThreadClassLoader(readerClassLoader);
            this.workerTaskReader.initialize(taskInfo);
        } finally {
            this.classLoaderSwapper.restoreCurrentThreadClassLoader();
        }
        this.workerCombinedTaskWriter.initialize(taskInfo);
    }

    @Override
    void execute() {
        this.readerThread.start();
        this.writerThread.start();
        this.subWorkersStarted.set(true);

        synchronized (this) {
            try {
                this.wait();//等待stop
            } catch (InterruptedException e) {
                //ignore
            }
        }

        // 必须等待WorkerTaskReader和WorkerTaskWriter全部完成，这样才能确保task真正完成资源释放了
        this.workerTaskReader.awaitStop(Long.MAX_VALUE);
        this.workerCombinedTaskWriter.awaitStop(Long.MAX_VALUE);

        // Task最后关闭的时候，需要把PositionManager中缓存的待刷新的Position信息清理掉，否则会导致Position出现脏数据更新
        this.workerTaskReaderContext.positionManager().discardPosition(id());
    }

    @Override
    public void stop() {
        super.stop();

        if (subWorkersStarted.get()) {
            stopWorkerCombinedTaskWriter();
            stopWorkerTaskReader();
        }
    }

    @Override
    public void transitionTo(TargetState state) {
        this.workerTaskReader.transitionTo(state);
        this.workerCombinedTaskWriter.transitionTo(state);
        super.transitionTo(state);
    }

    @Override
    void close() {

    }

    @Override
    public void cancel() {
        this.workerTaskReader.cancel();
        this.workerCombinedTaskWriter.cancel();
        super.cancel();
    }

    private void stopWorkerTaskReader() {
        try {
            //initialize方法已经是插件执行内容，需使用插件自己的classloader
            this.classLoaderSwapper.setCurrentThreadClassLoader(readerClassLoader);
            this.workerTaskReader.stop();
        } finally {
            this.classLoaderSwapper.restoreCurrentThreadClassLoader();
        }
        this.readerThread.interrupt();//中断reader内部阻塞，保证正常结束
    }

    private void stopWorkerCombinedTaskWriter() {
        this.workerCombinedTaskWriter.stop();
        this.writerThread.interrupt();//中断writer内部阻塞，保证正常结束
    }

    private WorkerTaskReader newWorkerTaskReader() {
        return new WorkerTaskReader(
                workerConfig(),
                taskInfo(),
                new TaskStatusListenerAdapter() {
                    @Override
                    public void onFailure(TaskStatusEvent event, Throwable cause) {
                        statusListener.onFailure(event, cause);
                    }
                },
                queue,
                workerTaskReaderContext);
    }

    private WorkerCombinedTaskWriter newWorkerCombinedTaskWriter() {
        return new WorkerCombinedTaskWriter(
                workerConfig(),
                taskInfo(),
                new TaskStatusListenerAdapter() {
                    @Override
                    public void onFailure(TaskStatusEvent event, Throwable cause) {
                        statusListener.onFailure(event, cause);
                    }
                },
                queue,
                workerTaskContext);

    }
}
