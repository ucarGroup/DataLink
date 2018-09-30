package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.worker.api.task.TaskWriter;
import com.ucar.datalink.worker.api.util.statistic.BaseWriterStatistic;
import com.ucar.datalink.worker.api.util.statistic.WriterStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.LockSupport;

/**
 * 负责TaskWriter的生命周期管理.
 * <p>
 * Created by lubiao on 2017/2/14.
 */
public class WorkerTaskWriter extends WorkerTask {
    private static final Logger logger = LoggerFactory.getLogger(WorkerTaskWriter.class);

    private TaskWriter taskWriter;
    private final ArrayBlockingQueue<TaskChunk> queue;
    private final WorkerTaskWriterContext workerTaskWriterContext;
    private final PluginWriterParameter taskWriterParameter;
    private int errorTimes = 0;

    public WorkerTaskWriter(WorkerConfig workerConfig, TaskInfo taskInfo, TaskStatusListener statusListener, ArrayBlockingQueue<TaskChunk> queue,
                            WorkerTaskWriterContext workerTaskWriterContext) {
        super(workerConfig, taskInfo, statusListener, workerTaskWriterContext.taskExecutionId());
        this.queue = queue;
        this.workerTaskWriterContext = workerTaskWriterContext;
        this.taskWriterParameter = workerTaskWriterContext.getWriterParameter();

        boolean autoAddColumnGlobal = workerConfig.getBoolean(WorkerConfig.SYNC_AUTO_ADD_COLUMN_CONFIG);
        boolean autoAddColumnTask = taskWriterParameter.isSyncAutoAddColumn();
        this.taskWriterParameter.setSyncAutoAddColumn(autoAddColumnGlobal && autoAddColumnTask);//通过全局开关设置是否缺字段时自动加字段
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void initialize(TaskInfo taskInfo) {
        this.taskWriter = buildTaskWriter();
        this.taskWriter.initialize(workerTaskWriterContext);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    void execute() {
        synchronized (this) {
            if (isStopping()) {
                return;
            }
            taskWriter.start();
            logger.info("TaskWriter-{} finished initialization and start.", taskWriterParameter.getPluginName());
        }

        while (!isStopping()) {
            try {
                if (shouldPause()) {
                    awaitUnpause();
                    continue;
                }

                TaskChunk taskChunk = queue.take();

                try {
                    taskWriter.prePush();

                    BaseWriterStatistic statistic = workerTaskWriterContext.taskWriterSession().getData(WriterStatistic.KEY);
                    long startTime = System.currentTimeMillis();
                    taskWriter.push(taskChunk.getRecordChunk());
                    statistic.setTimeForTotalPush(System.currentTimeMillis() - startTime);

                    taskWriter.postPush();

                    taskChunk.getCallback().onCompletion(null, null);
                    errorTimes = 0;
                } catch (Exception e) {//目前是对所有异常统一处理，后续视需要可以对异常进一步分类，把某些严重异常直接抛出，不用重试
                    logger.error(String.format("RecordChunk execute error in Writer [%s] ", taskWriterParameter.getPluginName()), e);

                    errorTimes++;

                    if (taskWriterParameter.getRetryMode() == PluginWriterParameter.RetryMode.Always) {
                        taskChunk.getCallback().onCompletion(e, null);
                    } else if (taskWriterParameter.getRetryMode() == PluginWriterParameter.RetryMode.TimesOutDiscard) {
                        if (errorTimes == taskWriterParameter.getMaxRetryTimes()) {
                            logger.error("RecordChunk has been discard due to retry times out.");//TODO,把废弃数据记录到日志或数据库
                            taskChunk.getCallback().onCompletion(null, null);
                            errorTimes = 0;
                        } else {
                            taskChunk.getCallback().onCompletion(e, null);
                        }
                    } else if (taskWriterParameter.getRetryMode() == PluginWriterParameter.RetryMode.TimesOutError) {
                        if (errorTimes == taskWriterParameter.getMaxRetryTimes()) {
                            logger.error(MessageFormat.format("Plugin Writer {0} failed due to retry times out.", taskWriterParameter.getPluginName()), e);
                            throw e;
                        } else {
                            taskChunk.getCallback().onCompletion(e, null);
                        }
                    } else if (taskWriterParameter.getRetryMode() == PluginWriterParameter.RetryMode.NoAndError) {
                        throw e;
                    } else {
                        throw e;
                    }

                    long unit = 1000 * 1000L * 1000L;//1s
                    long parkTime = errorTimes * unit > 30 * unit ? 30 * unit : errorTimes * unit;//最多暂停30s
                    LockSupport.parkNanos(parkTime);//出现错误，暂停1s
                }
            } catch (InterruptedException e) {
                //do nothing
            }
        }

        logger.info("TaskWriter-{} has stopped executing.", taskWriterParameter.getPluginName());
    }

    @Override
    public void stop() {
        //执行stop的线程和execute方法的线程是不同的，存在并发问题，需要加锁控制
        synchronized (this) {
            super.stop();
            if (taskWriter.isStart()) {
                taskWriter.stop();
            }
        }
    }

    @Override
    void close() {
        Thread.interrupted();//如果有中断状态取消中断状态，保证后续操作正常进行
        taskWriter.close();
    }

    @SuppressWarnings({"unchecked"})
    private TaskWriter buildTaskWriter() {
        String className = taskWriterParameter.getPluginClass();
        try {
            //必须使用Thread.currentThread().getContextClassLoader().loadClass(className)获取class对象，这样使用的才是插件的classloader
            //不能用class.forname()方法,该方式使用的是调用类的classloader
            return TaskFactory.newTaskWriter((Class<TaskWriter>) Thread.currentThread().getContextClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            throw new DatalinkException("The Task Reader Class can not found.", e);
        }
    }
}
