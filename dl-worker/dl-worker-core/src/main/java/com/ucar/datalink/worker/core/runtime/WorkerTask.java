package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.errors.TaskConflictException;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.worker.api.probe.ProbeManager;
import com.ucar.datalink.worker.api.probe.index.TaskExceptionProbeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * Handles processing for an individual task. This interface only provides the basic methods
 * used by {@link Worker} to manage the tasks. Implementations combine a user-specified TaskReader
 * or TaskWriter to create a data flow.
 * <p>
 * Note on locking: since the task runs in its own thread, special care must be taken to ensure
 * that state transitions are reported correctly, in particular since some state transitions are
 * asynchronous (e.g. pause/resume). For example, changing the state to paused could cause a race
 * if the task fails at the same time. To protect from these cases, we synchronize status updates
 * using the WorkerTask's monitor.
 * <p>
 * Created by lubiao on 2016/12/21.
 */
public abstract class WorkerTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(WorkerTask.class);

    private final String id;
    private final String executionId;
    private final TaskInfo taskInfo;
    private final WorkerConfig workerConfig;
    private final AtomicBoolean stopping;   // indicates whether the Worker has asked the task to stop
    private final CountDownLatch shutdownLatch;
    private final AtomicBoolean cancelled;  // indicates whether the Worker has cancelled the task (e.g. because of slow shutdown)
    private final AtomicReference<TargetState> targetState;
    protected final TaskStatusListener statusListener;
    private Long startTime;//不是WorkerTask线程的启动时间，而是经过prepare阶段后正式开始接管任务的时间

    public WorkerTask(WorkerConfig workerConfig, TaskInfo taskInfo, TaskStatusListener statusListener, String executionId) {
        this.workerConfig = workerConfig;
        this.taskInfo = taskInfo;
        this.targetState = new AtomicReference<>(taskInfo.getTargetState());
        this.statusListener = statusListener;
        this.executionId = executionId;

        this.id = taskInfo.idString();
        this.stopping = new AtomicBoolean(false);
        this.shutdownLatch = new CountDownLatch(1);
        this.cancelled = new AtomicBoolean(false);
    }

    public String id() {
        return id;
    }

    public String executionId() {
        return executionId;
    }

    public Long startTime() {
        return startTime;
    }

    public TaskInfo taskInfo() {
        return taskInfo;
    }

    public WorkerConfig workerConfig() {
        return this.workerConfig;
    }

    public abstract void initialize(TaskInfo taskInfo);

    private void triggerStop() {
        synchronized (this) {
            this.stopping.set(true);

            // wakeup any threads that are waiting for unpause
            this.notifyAll();
        }
    }

    /**
     * Stop this task parseFrom processing messages. This method does not block, it only triggers
     * shutdown. Use #{@link #awaitStop} to block until completion.
     * <p>
     * important：如果需要override该方法，请自行保证幂等性，该方法存在被重复调用的可能性.
     */
    public void stop() {
        triggerStop();
    }

    /**
     * Cancel this task. This won't actually stop it, but it will prevent the state parseFrom being
     * updated when it eventually does shutdown.
     */
    public void cancel() {
        this.cancelled.set(true);
    }

    /**
     * Wait for this task to finish stopping.
     *
     * @param timeoutMs time in milliseconds to await stop
     * @return true if successful, false if the timeout was reached
     */
    public boolean awaitStop(long timeoutMs) {
        try {
            return shutdownLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    abstract void execute();

    abstract void close();

    protected boolean isStopping() {
        return stopping.get();
    }

    private void doClose() {
        try {
            close();
        } catch (Throwable t) {
            log.error("Task {} threw an uncaught and unrecoverable errors during shutdown", id, t);
            throw t;
        }
    }

    private boolean doPrepare() {
        try {
            MDC.put(Constants.MDC_TASKID, id());
            long start = System.currentTimeMillis();
            while (true) {
                if (stopping.get()) {
                    return false;
                }

                try {
                    synchronized (this) {
                        statusListener.onPrepare(new TaskStatusEvent(id, executionId, startTime));
                        return true;
                    }
                } catch (TaskConflictException e) {
                    if (System.currentTimeMillis() - start > workerConfig.getInt(WorkerConfig.ZK_SESSION_TIMEOUT_MS_CONFIG)) {
                        //如果重试时间已经超过了zk的会话超时时间，则触发报警
                        ProbeManager.getInstance().getTaskExceptionProbe()
                                .record(new TaskExceptionProbeIndex(Long.valueOf(id), e));
                    }

                    log.error(e.getMessage(), e);
                    LockSupport.parkNanos(1000 * 1000L * 1000L);//暂停一秒，继续重试
                }
            }
        } catch (Throwable t) {
            log.error("Task {} encounter an error in preparing stage.", id, t);
            throw t;
        } finally {
            MDC.remove(Constants.MDC_TASKID);
        }
    }

    private void doRun() {
        try {
            MDC.put(Constants.MDC_TASKID, id());
            synchronized (this) {
                if (stopping.get())
                    return;

                if (targetState.get() == TargetState.PAUSED) {
                    statusListener.onPause(new TaskStatusEvent(id, executionId, startTime));
                } else {
                    statusListener.onStartup(new TaskStatusEvent(id, executionId, startTime));
                }
            }

            execute();
        } catch (Throwable t) {
            log.error("Task {} threw an uncaught and unrecoverable errors", id, t);
            log.error("Task is being killed and will not recover until manually restarted");
            throw t;
        } finally {
            try {
                doClose();
            } finally {
                MDC.remove(Constants.MDC_TASKID);
            }
        }
    }

    private void onShutdown() {
        synchronized (this) {
            try {
                statusListener.onShutdown(new TaskStatusEvent(id, executionId, startTime));
            } finally {
                shutdownLatch.countDown();
            }
        }
    }

    protected void onFailure(Throwable t) {
        synchronized (this) {
            // if we were cancelled, skip the status update since the task may have already been
            // started somewhere else
            if (!cancelled.get()) {
                statusListener.onFailure(new TaskStatusEvent(id, executionId, startTime), t);

                // we should wait until accepting the trigger stop command
                if (!stopping.get()) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        //do nothing
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        if (doPrepare()) {
            try {
                startTime = System.currentTimeMillis();
                doRun();
            } catch (Throwable t) {
                onFailure(t);

                if (t instanceof Error)
                    throw t;
            } finally {
                onShutdown();
            }
        }
    }

    public boolean shouldPause() {
        return this.targetState.get() == TargetState.PAUSED;
    }

    /**
     * Await task resumption.
     *
     * @return true if the task's target state is not paused, false if the task is shutdown before resumption
     * @throws InterruptedException
     */
    protected boolean awaitUnpause() throws InterruptedException {
        synchronized (this) {
            while (targetState.get() == TargetState.PAUSED) {
                if (stopping.get())
                    return false;
                this.wait();
            }
            return true;
        }
    }

    public void transitionTo(TargetState state) {
        synchronized (this) {
            // ignore the state change if we are stopping
            if (stopping.get())
                return;

            TargetState oldState = this.targetState.getAndSet(state);
            if (state != oldState) {
                if (state == TargetState.PAUSED) {
                    statusListener.onPause(new TaskStatusEvent(id, executionId, startTime));
                } else if (state == TargetState.STARTED) {
                    statusListener.onResume(new TaskStatusEvent(id, executionId, startTime));
                    this.notifyAll();
                } else
                    throw new IllegalArgumentException("Unhandled target state " + state);
            }
        }
    }
}
