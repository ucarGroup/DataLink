package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.common.errors.TaskExecuteException;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.util.copy.RecordCopier;
import com.ucar.datalink.worker.core.util.classloader.ClassLoaderFactory;
import com.ucar.datalink.worker.core.util.classloader.ClassLoaderSwapper;
import com.ucar.datalink.worker.core.util.classloader.PluginType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 该Worker负责管理多个WorkerTaskWriter
 * <p>
 * Created by lubiao on 2017/2/15.
 */
public class WorkerCombinedTaskWriter extends WorkerTask {
    private static final Logger logger = LoggerFactory.getLogger(WorkerCombinedTaskWriter.class);

    private final ArrayBlockingQueue<TaskChunk> queue;
    private final List<TaskChunk> taskChunksForWriter;
    private final List<WorkerTaskWriterItem> workerTaskWriterItems;
    private final ClassLoaderSwapper classLoaderSwapper;
    private final WorkerTaskContext workerTaskContext;
    private final AtomicBoolean subWorkersStarted;

    public WorkerCombinedTaskWriter(WorkerConfig workerConfig, TaskInfo taskInfo, TaskStatusListener statusListener, ArrayBlockingQueue<TaskChunk> queue, WorkerTaskContext workerTaskContext) {
        super(workerConfig, taskInfo, statusListener, workerTaskContext.taskExecutionId());
        this.queue = queue;
        this.workerTaskContext = workerTaskContext;
        this.taskChunksForWriter = new ArrayList<>();
        this.workerTaskWriterItems = new ArrayList<>();
        this.classLoaderSwapper = ClassLoaderSwapper.newCurrentThreadClassLoaderSwapper();
        this.subWorkersStarted = new AtomicBoolean(false);
        this.initialWorkerTaskWriterItems();
    }

    @Override
    public void initialize(TaskInfo taskInfo) {
        for (WorkerTaskWriterItem item : workerTaskWriterItems) {
            try {
                this.classLoaderSwapper.setCurrentThreadClassLoader(item.workerTaskWriterClassLoader);
                item.workerTaskWriter.initialize(taskInfo);
            } finally {
                this.classLoaderSwapper.restoreCurrentThreadClassLoader();
            }
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    void execute() {
        try {
            for (WorkerTaskWriterItem item : workerTaskWriterItems) {
                item.workerTaskWriterThread.start();
            }
            subWorkersStarted.set(true);

            while (!isStopping()) {
                TaskChunk taskChunkFromReader = queue.take();

                taskChunksForWriter.clear();
                for (int i = 0; i < workerTaskWriterItems.size(); i++) {
                    RecordChunk copyChunk;
                    if (i == 0) {
                        copyChunk = taskChunkFromReader.getRecordChunk();//第一个writer直接用传过来的
                    } else {
                        copyChunk = copyRecordChunk(taskChunkFromReader.getRecordChunk());//其它writer用复制的
                    }
                    TaskChunk taskChunkItem = new TaskChunk(copyChunk, new FutureCallback());
                    taskChunksForWriter.add(taskChunkItem);
                }

                for (int i = 0; i < taskChunksForWriter.size(); i++) {
                    TaskChunk taskChunkItem = taskChunksForWriter.get(i);
                    workerTaskWriterItems.get(i).queue.put(taskChunkItem);
                }

                Exception error = null;
                for (TaskChunk chunk : taskChunksForWriter) {
                    try {
                        chunk.getCallback().get();
                    } catch (Exception e) {
                        error = e;
                    }
                }
                if (error != null) {
                    taskChunkFromReader.getCallback().onCompletion(error, null);
                } else {
                    taskChunkFromReader.getCallback().onCompletion(null, null);
                }
            }
        } catch (InterruptedException e) {
            // Ignore and allow to exit.
        } catch (Exception e) {
            throw new TaskExecuteException("something goes wrong in TaskWriter side.", e, id());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (subWorkersStarted.get()) {
            stopWorkerTaskWriters();
        }
    }

    @Override
    void close() {

    }

    @Override
    public void transitionTo(TargetState state) {
        workerTaskWriterItems.forEach(i -> i.workerTaskWriter.transitionTo(state));
        super.transitionTo(state);
    }

    @Override
    public void cancel() {
        workerTaskWriterItems.forEach(i -> i.workerTaskWriter.cancel());
        super.cancel();
    }

    @Override
    public boolean awaitStop(long timeoutMs) {
        boolean result = true;
        for (WorkerTaskWriterItem item : workerTaskWriterItems) {
            long remaining = Math.max(0, timeoutMs);

            long start = System.currentTimeMillis();
            result &= item.workerTaskWriter.awaitStop(remaining);
            timeoutMs = timeoutMs - (System.currentTimeMillis() - start);
        }
        result &= super.awaitStop(Math.max(0, timeoutMs));
        return result;
    }

    @SuppressWarnings("unchecked")
    private RecordChunk copyRecordChunk(RecordChunk recordChunk) {
        logger.debug("RecordChunk Copy Happened in " + getClass().getSimpleName());
        RecordChunk newChunk = recordChunk.copyWithoutRecords();
        newChunk.setRecords(RecordCopier.copyList(recordChunk.getRecords()));
        return newChunk;
    }

    private void initialWorkerTaskWriterItems() {
        //构造WorkerTaskWriterItem列表
        List<PluginWriterParameter> writerParameterList = taskInfo().getTaskWriterParameterObjs();
        for (PluginWriterParameter writerParameter : writerParameterList) {
            ArrayBlockingQueue itemQueue = new ArrayBlockingQueue(1);
            ClassLoader itemClassLoader = ClassLoaderFactory.getClassLoader(PluginType.Writer, writerParameter.getPluginName(),
                    workerConfig().getString(WorkerConfig.CLASSLOADER_TYPE_CONFIG));
            WorkerTaskWriter itemWorkerWriter = newWorkerTaskWriter(writerParameter, itemQueue);
            Thread itemThread = new Thread(itemWorkerWriter, MessageFormat.format("Task-{0}-Wirter-{1}", id(), writerParameter.getPluginName()));

            itemThread.setContextClassLoader(itemClassLoader);

            workerTaskWriterItems.add(new WorkerTaskWriterItem(itemWorkerWriter, itemThread, itemClassLoader, itemQueue, writerParameter));
        }
    }

    private void stopWorkerTaskWriters() {
        for (WorkerTaskWriterItem item : workerTaskWriterItems) {
            try {
                this.classLoaderSwapper.setCurrentThreadClassLoader(item.workerTaskWriterClassLoader);
                item.workerTaskWriter.stop();
            } finally {
                this.classLoaderSwapper.restoreCurrentThreadClassLoader();
            }

            //中断writer内部阻塞，保证正常结束
            item.workerTaskWriterThread.interrupt();
        }
    }

    private WorkerTaskWriter newWorkerTaskWriter(PluginWriterParameter writerParameter, ArrayBlockingQueue queue) {
        return new WorkerTaskWriter(
                workerConfig(),
                taskInfo(),
                new TaskStatusListenerAdapter() {
                    @Override
                    public void onFailure(TaskStatusEvent event, Throwable cause) {
                        statusListener.onFailure(event, cause);
                    }
                },
                queue,
                new WorkerTaskWriterContext(workerTaskContext, writerParameter)
        );
    }

    private class WorkerTaskWriterItem {
        private final WorkerTaskWriter workerTaskWriter;
        private final Thread workerTaskWriterThread;
        private final ClassLoader workerTaskWriterClassLoader;
        private final ArrayBlockingQueue<TaskChunk> queue;
        private final PluginWriterParameter taskWriterParameter;

        public WorkerTaskWriterItem(WorkerTaskWriter workerTaskWriter, Thread workerTaskWriterThread, ClassLoader workerTaskWriterClassLoader, ArrayBlockingQueue<TaskChunk> queue, PluginWriterParameter taskWriterParameter) {
            this.workerTaskWriter = workerTaskWriter;
            this.workerTaskWriterThread = workerTaskWriterThread;
            this.workerTaskWriterClassLoader = workerTaskWriterClassLoader;
            this.queue = queue;
            this.taskWriterParameter = taskWriterParameter;
        }
    }
}
