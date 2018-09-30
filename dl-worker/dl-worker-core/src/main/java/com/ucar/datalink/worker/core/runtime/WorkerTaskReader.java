package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.errors.TaskClosedException;
import com.ucar.datalink.domain.plugin.PluginReaderParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.worker.api.probe.index.TaskDelayProbeIndex;
import com.ucar.datalink.worker.api.probe.index.TaskExceptionProbeIndex;
import com.ucar.datalink.worker.api.probe.index.TaskStatisticProbeIndex;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskReader;
import com.ucar.datalink.worker.api.util.statistic.ReaderStatistic;
import com.ucar.datalink.worker.api.probe.ProbeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * WorkerTaskReader负责TaskReader生命周期的管理
 * <p>
 * Created by lubiao on 2017/2/14.
 */
public class WorkerTaskReader extends WorkerTask {
    private static final Logger log = LoggerFactory.getLogger(WorkerTaskReader.class);

    private final ArrayBlockingQueue<TaskChunk> queue;
    private final WorkerTaskReaderContext workerTaskReaderContext;
    private final PluginReaderParameter taskReaderParameter;
    private TaskReader taskReader;
    private RecordChunk toSend;

    public WorkerTaskReader(WorkerConfig workerConfig, TaskInfo taskInfo, TaskStatusListener statusListener, ArrayBlockingQueue<TaskChunk> queue,
                            WorkerTaskReaderContext workerTaskReaderContext) {
        super(workerConfig, taskInfo, statusListener, workerTaskReaderContext.taskExecutionId());
        this.queue = queue;
        this.workerTaskReaderContext = workerTaskReaderContext;
        this.taskReaderParameter = workerTaskReaderContext.getReaderParameter();

        boolean ddlSyncGlobal = workerConfig.getBoolean(WorkerConfig.DDL_SYNC_CONFIG);
        boolean ddlSyncTask = taskReaderParameter.isDdlSync();
        this.taskReaderParameter.setDdlSync(ddlSyncGlobal && ddlSyncTask);//通过全局开关设置是否同步ddl操作
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void initialize(TaskInfo taskInfo) {
        taskReader = buildTaskReader();
        taskReader.initialize(workerTaskReaderContext);
    }

    @Override
    void execute() {
        try {
            //启动TaskReader之前判断一下是否是pause状态，pause状态的话暂时先不启动TaskReader,主要原因
            //1.可以降低系统资源的占用
            //2.对于那些mustRestartWhenPause==true的TaskType来说是必须的，如HBASE类型的Task，暂停状态下就不能启动TaskReader，
            //  否则仍然会收到HBase-Master推送的Log,收到Log之后会阻塞住，从实际测试结果来看，此Task会导致所有Task的同步出现严重延迟
            if (shouldPause()) {
                awaitUnpause();
            }

            synchronized (this) {
                if (isStopping()) {
                    return;
                }
                taskReader.start();
                log.info("TaskReader-{} finished initialization and start.", taskReaderParameter.getPluginName());
            }

            while (!isStopping()) {
                if (shouldPause()) {
                    awaitUnpause();
                    continue;
                }

                if (toSend == null) {
                    log.debug("Nothing to send to Task. Polling reader for additional records.");

                    taskReader.prePoll();

                    ReaderStatistic readerStatistic = workerTaskReaderContext.taskReaderSession().getData(ReaderStatistic.KEY);
                    long startTime = System.currentTimeMillis();
                    toSend = taskReader.poll();
                    readerStatistic.setTimeForTotalPoll(System.currentTimeMillis() - startTime);
                }
                if (toSend == null) {
                    continue;
                }

                sendRecords();

            }
        } catch (InterruptedException e) {
            // Ignore and allow to exit.
        }
        log.info("TaskReader-{} has stopped executing.", taskReaderParameter.getPluginName());
    }

    @Override
    public void stop() {
        //执行stop的线程和execute方法的线程是不同的，存在并发问题，需要加锁控制
        synchronized (this) {
            super.stop();
            if (taskReader.isStart()) {
                taskReader.stop();
            }
        }
    }

    @Override
    void close() {
        Thread.interrupted();//如果有中断状态取消中断状态，保证后续操作正常进行
        taskReader.close();
    }

    @SuppressWarnings({"unchecked"})
    private TaskReader buildTaskReader() {
        String className = taskReaderParameter.getPluginClass();
        try {
            //必须使用Thread.currentThread().getContextClassLoader().loadClass(className)获取class对象，这样使用的才是插件的classloader
            //不能用class.forname()方法,该方式使用的是调用类的classloader
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return TaskFactory.newTaskReader((Class<TaskReader>) classLoader.loadClass(className));
        } catch (ClassNotFoundException e) {
            throw new DatalinkException("The Task Reader Class can not found.", e);
        } catch (Exception ex) {
            throw new DatalinkException("", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void sendRecords() throws InterruptedException {
        Throwable error = null;
        try {
            //如果待发送数据为空，无需发送
            if (!toSend.getRecords().isEmpty()) {
                //statistic before
                ReaderStatistic readerStatistic = workerTaskReaderContext.taskReaderSession().getData(ReaderStatistic.KEY);
                long startTime = System.currentTimeMillis();

                //do put and wait
                TaskChunk taskChunk = new TaskChunk(toSend, new FutureCallback<>());
                queue.put(taskChunk);
                taskChunk.getCallback().get();

                //statistic after
                long writeTime = System.currentTimeMillis() - startTime;
                readerStatistic.setTimeForWrite(writeTime);

                //监控延迟时间
                logTaskDelayTime(taskChunk);
            }

            //do commit
            if (!isStopping()) {
                taskReader.commit(toSend);
            } else {
                taskReader.rollback(toSend, new TaskClosedException("Task has closed."));
            }
        } catch (ExecutionException e) {
            log.error("Receive an exception from writer side.", e);

            error = e.getCause();
            taskReader.rollback(toSend, e);
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            if (!toSend.getRecords().isEmpty() || error != null) {
                //任务性能统计
                logTaskStatistic(toSend, error);
            }
        }
        toSend = null;
    }

    private void logTaskDelayTime(TaskChunk taskChunk) {
        long delayTime = System.currentTimeMillis() - taskChunk.getRecordChunk().getFirstEntryTime();
        TaskDelayProbeIndex index = new TaskDelayProbeIndex(Long.valueOf(id()), delayTime);
        ProbeManager.getInstance().getTaskDelayProbe().record(index);
    }

    private void logTaskStatistic(RecordChunk recordChunk, Throwable error) {
        ReaderStatistic readerStatistic = workerTaskReaderContext.taskReaderSession().getData(ReaderStatistic.KEY);

        long timeForWrite = 0;
        int recordsCount = 0;
        long payloadSize = 0;

        if (error == null) {//没有异常时才统计，有异常时(不管什么异常)不统计
            timeForWrite = readerStatistic.getTimeForWrite();
            recordsCount = recordChunk.getRecords().size();
            payloadSize = recordChunk.getPayloadSize();
        }

        boolean loggableError = error != null && !isInterruptedException(error);
        TaskStatisticProbeIndex index = new TaskStatisticProbeIndex(
                Long.valueOf(id()), timeForWrite, recordsCount, payloadSize, loggableError);
        ProbeManager.getInstance().getTaskStatisticProbe().record(index);

        if (loggableError) {
            logTaskException(error);
        }
    }

    private void logTaskException(Throwable e) {
        TaskExceptionProbeIndex index = new TaskExceptionProbeIndex(Long.valueOf(id()), e);
        ProbeManager.getInstance().getTaskExceptionProbe().record(index);
    }

    private boolean isInterruptedException(Throwable t) {
        return (t instanceof InterruptedException) || (t.getCause() != null && isInterruptedException(t.getCause()));
    }
}
