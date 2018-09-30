package com.ucar.datalink.worker.core.runtime.standalone;

import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.NotFoundException;
import com.ucar.datalink.common.errors.TaskConflictException;
import com.ucar.datalink.common.utils.Callback;
import com.ucar.datalink.domain.ClusterConfigState;
import com.ucar.datalink.domain.Position;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.worker.core.runtime.*;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by lubiao on 2018/3/7.
 */
public class StandaloneWorkerKeeper implements Keeper, Runnable, TaskStatusListener {

    private static final Logger log = LoggerFactory.getLogger(StandaloneWorkerKeeper.class);

    private final WorkerConfig workerConfig;
    private final String workerId;
    private final Worker worker;
    private final TaskConfigManager taskConfigManager;
    private final Time time;
    private final AtomicBoolean stopping;
    private final CountDownLatch stopLatch = new CountDownLatch(1);
    private final ExecutorService forwardRequestExecutor;
    private final Queue<KeeperRequest> requests = new PriorityQueue<>();
    private ClusterConfigState configState;
    private Set<String> taskConfigAdds = new HashSet<>();
    private Set<String> taskConfigDeletes = new HashSet<>();
    private Set<String> taskConfigUpdates = new HashSet<>();
    private Set<String> taskTargetStateChanges = new HashSet<>();


    public StandaloneWorkerKeeper(WorkerConfig config,
                                  Worker worker,
                                  Time time,
                                  TaskConfigManager taskConfigManager
    ) {
        this.workerConfig = config;
        this.workerId = worker.workerId();
        this.worker = worker;
        this.time = time;
        this.taskConfigManager = taskConfigManager;
        this.stopping = new AtomicBoolean(false);
        this.configState = ClusterConfigState.EMPTY;
        this.forwardRequestExecutor = Executors.newSingleThreadExecutor();
        taskConfigManager.setUpdateListener(new ConfigUpdateListener());
    }

    @Override
    public void start() {
        Thread thread = new Thread(this, StandaloneWorkerKeeper.class.getSimpleName());
        thread.start();
    }

    @Override
    public void stop() {
        log.info("Standalone Worker Keeper stopping");

        stopping.set(true);
        while (stopLatch.getCount() > 0) {
            try {
                stopLatch.await();
            } catch (InterruptedException e) {
                // ignore, should not happen
            }
        }

        forwardRequestExecutor.shutdown();
        try {
            if (!forwardRequestExecutor.awaitTermination(10000, TimeUnit.MILLISECONDS))
                forwardRequestExecutor.shutdownNow();
        } catch (InterruptedException e) {
            // ignore
        }

        log.info("Standalone Worker Keeper stopped");
    }

    @Override
    public void run() {
        try {
            log.info("Standalone Worker Keeper starting");

            startServices();
            prepare();

            log.info("Standalone Worker Keeper started");

            while (!stopping.get()) {
                tick();
            }

            halt();

            log.info("Standalone Worker Keeper stopped");
        } catch (Throwable t) {
            log.error("Uncaught errors in standalone worker keeper thread, exiting: ", t);
            stopLatch.countDown();
            System.exit(1);
        } finally {
            stopLatch.countDown();
        }
    }

    public void prepare() {
        taskConfigManager.forceRefresh();
        configState = taskConfigManager.snapshot();
        if (configState != null && configState.tasks() != null) {
            configState.tasks().stream().forEach(t -> taskConfigAdds.add(t));
        }
    }

    public void tick() {
        // Process any external requests
        final long now = time.milliseconds();
        long nextRequestTimeoutMs = Long.MAX_VALUE;
        while (true) {
            final KeeperRequest next;
            synchronized (this) {
                next = requests.peek();
                if (next == null) {
                    break;
                } else if (now >= next.at()) {
                    requests.poll();
                } else {
                    nextRequestTimeoutMs = next.at() - now;
                    break;
                }
            }

            try {
                next.action().call();
                next.callback().onCompletion(null, null);
            } catch (Throwable t) {
                next.callback().onCompletion(t, null);
            }
        }

        // Process any configuration updates
        if (taskConfigManager.tryRefresh()) {
            configState = taskConfigManager.snapshot();
        }

        Set<String> taskConfigAddsCopy = null;
        Set<String> taskConfigDeletesCopy = null;
        Set<String> taskConfigUpdatesCopy = null;
        Set<String> taskTargetStateChangesCopy = null;
        synchronized (this) {
            if (!taskConfigAdds.isEmpty() || !taskConfigDeletes.isEmpty() || !taskConfigUpdates.isEmpty() || !taskTargetStateChanges.isEmpty()) {
                if (!taskConfigAdds.isEmpty()) {
                    taskConfigAddsCopy = taskConfigAdds;
                    taskConfigAdds = new HashSet<>();
                }

                if (!taskConfigDeletes.isEmpty()) {
                    taskConfigDeletesCopy = taskConfigDeletes;
                    taskConfigDeletes = new HashSet<>();
                }

                if (!taskConfigUpdates.isEmpty()) {
                    taskConfigUpdatesCopy = taskConfigUpdates;
                    taskConfigUpdates = new HashSet<>();
                }

                if (!taskTargetStateChanges.isEmpty()) {
                    taskTargetStateChangesCopy = taskTargetStateChanges;
                    taskTargetStateChanges = new HashSet<>();
                }
            }
        }

        if (taskConfigAddsCopy != null)
            processTaskConfigAdds(taskConfigAddsCopy);

        if (taskConfigDeletesCopy != null)
            processTaskConfigDeletes(taskConfigDeletesCopy);

        if (taskConfigUpdatesCopy != null)
            processTaskConfigUpdates(taskConfigUpdatesCopy);

        if (taskTargetStateChangesCopy != null)
            processTargetStateChanges(taskTargetStateChangesCopy);

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            //do nothing
        }
    }

    protected void startServices() {
        this.worker.start();//轮询线程：1、记录task-position；2、启动monitor
        this.taskConfigManager.start();//初始化：刷新任务和版本
    }

    protected void stopServices() {
        this.taskConfigManager.stop();
        this.worker.stop();
    }

    // public for testing
    public void halt() {
        synchronized (this) {
            // Clean up any jobs and tasks that are still running.
            log.info("Stopping jobs and tasks that are still assigned to the worker");
            worker.stopAndAwaitTasks();

            stopServices();
        }
    }

    @Override
    public void restartTask(String taskId, Position position, Callback<Void> callback) {
        addRequest(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (configState.taskConfig(taskId) == null) {
                    callback.onCompletion(new NotFoundException("Unknown task: " + taskId), null);
                    return null;
                } else {
                    try {
                        worker.stopAndAwaitTask(taskId);
                        if (position != null) {
                            worker.getTaskPositionManager().updatePositionNow(taskId, position);
                        }
                        if (startTask(taskId)) {
                            callback.onCompletion(null, null);
                        } else {
                            callback.onCompletion(new DatalinkException("Failed to start task: " + taskId), null);
                        }
                    } catch (Throwable t) {
                        callback.onCompletion(t, null);
                    }
                }
                return null;
            }
        }, forwardErrorCallback(callback));
    }

    private void addRequest(Callable<Void> action, Callback<Void> callback) {
        addRequest(0, action, callback);
    }

    private void addRequest(long delayMs, Callable<Void> action, Callback<Void> callback) {
        KeeperRequest req = new KeeperRequest(time.milliseconds() + delayMs, action, callback);
        requests.add(req);
    }

    private Callback<Void> forwardErrorCallback(final Callback<?> callback) {
        return new Callback<Void>() {
            @Override
            public void onCompletion(Throwable error, Void result) {
                if (error != null) {
                    callback.onCompletion(error, null);
                }
            }
        };
    }

    private void processTaskConfigAdds(Set<String> taskConfigAdds) {
        for (String taskId : taskConfigAdds) {
            log.info("Handling task config add by starting task {}", taskId);
            startTask(taskId);
        }
    }

    private void processTaskConfigDeletes(Set<String> taskConfigDeletes) {
        for (String taskId : taskConfigDeletes) {
            log.info("Handling task config delete by stoppinng task {}", taskId);
            stopTask(taskId);
        }
    }

    private void processTaskConfigUpdates(Set<String> taskConfigUpdates) {
        for (String taskId : taskConfigUpdates) {
            log.info("Handling task config update by restarting task {}", taskId);
            stopTask(taskId);
            startTask(taskId);
        }
    }

    private void processTargetStateChanges(Set<String> taskTargetStateChanges) {
        for (String taskId : taskTargetStateChanges) {
            TargetState targetState = configState.taskConfig(taskId).getTargetState();
            if (TargetState.PAUSED.equals(targetState) && configState.taskConfig(taskId).getTaskType().mustRestartWhenPause()) {
                log.info("Handling task state change to {} by restarting task {}", targetState, taskId);
                stopTask(taskId);
                startTask(taskId);
            } else {
                log.info("Handling task state change by transiting state to {} for task {}", targetState, taskId);
                worker.setTargetState(taskId, targetState);
            }
        }
    }

    private boolean startTask(String taskId) {
        log.info("Starting task {}", taskId);
        return worker.startTask(
                taskId,
                configState.taskConfig(taskId),
                this
        );
    }

    private void stopTask(String taskId) {
        log.info("Stopping task {}", taskId);
        worker.stopAndAwaitTask(taskId);
    }

    @Override
    public void onPrepare(TaskStatusEvent event) throws TaskConflictException {
        TaskStatus taskStatus = new TaskStatus(event.getTaskId(), event.getTaskExecutionId(), TaskStatus.State.PREPARING,
                workerId, 0, event.getStartTime());
        log.info("Task prepare event is coming,task status changed to :" + taskStatus);
    }

    @Override
    public void onStartup(TaskStatusEvent event) {
        TaskStatus taskStatus = new TaskStatus(event.getTaskId(), event.getTaskExecutionId(), TaskStatus.State.RUNNING,
                workerId, 0, event.getStartTime());
        log.info("Task startup event is coming,task status changed to :" + taskStatus);
    }

    @Override
    public void onPause(TaskStatusEvent event) {
        TaskStatus taskStatus = new TaskStatus(event.getTaskId(), event.getTaskExecutionId(), TaskStatus.State.PAUSED,
                workerId, 0, event.getStartTime());
        log.info("Task pause event is coming, task status changed to :" + taskStatus);
    }

    @Override
    public void onResume(TaskStatusEvent event) {
        TaskStatus taskStatus = new TaskStatus(event.getTaskId(), event.getTaskExecutionId(), TaskStatus.State.RUNNING,
                workerId, 0, event.getStartTime());
        log.info("Task resume event is coming,task status changed to :" + taskStatus);
    }

    @Override
    public void onFailure(TaskStatusEvent event, Throwable cause) {
        TaskStatus taskStatus = new TaskStatus(event.getTaskId(), event.getTaskExecutionId(), TaskStatus.State.FAILED,
                workerId, 0, event.getStartTime(), trace(cause));
        log.info("Task failure event is coming,task status changed to :" + taskStatus);
    }

    @Override
    public void onShutdown(TaskStatusEvent event) {
        log.info("Task shutdown event is coming,task status changed to : Shutdown");
    }

    private String trace(Throwable t) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(output));
        try {
            return output.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public class ConfigUpdateListener implements TaskConfigUpdateListener {
        @Override
        public void onTaskConfigRemove(String taskId) {
            log.info("task {} config removed", taskId);
            synchronized (StandaloneWorkerKeeper.this) {
                taskConfigDeletes.add(taskId);
            }
        }

        @Override
        public void onTaskConfigAdd(String taskId) {
            log.info("task {} config added", taskId);
            synchronized (StandaloneWorkerKeeper.this) {
                taskConfigAdds.add(taskId);
            }
        }

        @Override
        public void onTaskConfigUpdate(String taskId) {
            log.info("task {} config updated", taskId);
            synchronized (StandaloneWorkerKeeper.this) {
                taskConfigUpdates.add(taskId);
            }
        }

        @Override
        public void onTaskStateChanged(String taskId) {
            log.info("task {} target state change", taskId);
            synchronized (StandaloneWorkerKeeper.this) {
                taskTargetStateChanges.add(taskId);
            }
        }
    }
}
