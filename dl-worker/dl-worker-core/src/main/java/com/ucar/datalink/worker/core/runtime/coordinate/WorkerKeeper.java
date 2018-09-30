package com.ucar.datalink.worker.core.runtime.coordinate;

import com.ucar.datalink.common.*;
import com.ucar.datalink.common.errors.*;
import com.ucar.datalink.domain.ClusterConfigState;
import com.ucar.datalink.domain.Position;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.worker.core.runtime.*;
import com.ucar.datalink.common.utils.Callback;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Worker的监护者.
 * <p>
 * Created by lubiao on 2016/12/5.
 */
public class WorkerKeeper implements Keeper, Runnable, TaskStatusListener {
    private static final Logger log = LoggerFactory.getLogger(WorkerKeeper.class);

    private final String workerId;
    private final Worker worker;
    private final TaskConfigManager taskConfigManager;
    private final TaskStatusManager taskStatusManager;
    private final Time time;
    private final ExecutorService forwardRequestExecutor;
    private final WorkerGroupMember member;
    private final AtomicBoolean stopping;
    private final CountDownLatch stopLatch = new CountDownLatch(1);

    // Track enough information about the current membership state to be able to determine which requests via the API
    // and the parseFrom other nodes are safe to process
    private boolean rebalanceResolved;
    private DatalinkProtocol.Assignment assignment;
    private ClusterConfigState configState;

    // To handle most external requests, like creating or destroying a task, we can use a generic request where
    // the caller specifies all the code that should be executed.
    private final Queue<KeeperRequest> requests = new PriorityQueue<>();

    private Set<String> taskConfigUpdates = new HashSet<>();
    private Set<String> taskTargetStateChanges = new HashSet<>();
    private boolean needsReconfigRebalance;
    private volatile int generation;

    public WorkerKeeper(WorkerConfig config,
                        Time time,
                        Worker worker,
                        TaskStatusManager taskStatusManager,
                        TaskConfigManager taskConfigManager,
                        String restUrl) {
        this(config, worker, worker.workerId(), taskStatusManager, taskConfigManager, null, restUrl, time);
        taskConfigManager.setUpdateListener(new ConfigUpdateListener());
    }

    WorkerKeeper(WorkerConfig config,
                 Worker worker,
                 String workerId,
                 TaskStatusManager taskStatusManager,
                 TaskConfigManager taskConfigManager,
                 WorkerGroupMember member,
                 String restUrl,
                 Time time) {
        this.worker = worker;
        this.workerId = workerId;
        this.taskConfigManager = taskConfigManager;
        this.taskStatusManager = taskStatusManager;
        this.time = time;
        this.member = member != null ? member : new WorkerGroupMember(config, restUrl, this.taskConfigManager, new RebalanceListener(), time);
        this.forwardRequestExecutor = Executors.newSingleThreadExecutor();

        stopping = new AtomicBoolean(false);
        configState = ClusterConfigState.EMPTY;
        rebalanceResolved = true; // If we still need to follow up after a rebalance occurred, starting up tasks
        needsReconfigRebalance = false;
    }

    @Override
    public void start() {
        Thread thread = new Thread(this, WorkerKeeper.class.getSimpleName());
        thread.start();
    }

    @Override
    public void run() {
        try {
            log.info("Worker Keeper starting");

            startServices();

            log.info("Worker Keeper started");

            while (!stopping.get()) {
                tick();
            }

            halt();

            log.info("Worker Keeper stopped");
        } catch (Throwable t) {
            log.error("Uncaught errors in worker keeper thread, exiting: ", t);
            stopLatch.countDown();
            System.exit(1);
        } finally {
            stopLatch.countDown();
        }
    }

    public void tick() {
        // The main loop does two primary things: 1) drive the group membership protocol, responding to rebalance events
        // as they occur, and 2) handle external requests targeted at the leader. All the "real" work of the keeper is
        // performed in this thread, which keeps synchronization straightforward at the cost of some operations possibly
        // blocking up this thread (especially those in callbacks due to rebalance events).

        try {
            member.ensureActive();
            // Ensure we're in a good state in our group. If not restart and everything should be setup to rejoin
            if (!handleRebalanceCompleted()) {
                return;
            }
        } catch (WakeupException e) {
            // May be due to a request parseFrom another thread, or might be stopping. If the latter, we need to check the
            // flag immediately. If the former, we need to re-run the ensureActive call since we can't handle requests
            // unless we're in the group.
            return;
        }

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
        Set<String> taskConfigUpdatesCopy = null;
        Set<String> taskTargetStateChangesCopy = null;
        synchronized (this) {
            if (needsReconfigRebalance || !taskConfigUpdates.isEmpty() || !taskTargetStateChanges.isEmpty()) {
                if (needsReconfigRebalance) {
                    // Task reconfigs require a rebalance. Request the rebalance, clean out state, and then restart
                    // this loop, which will then ensure the rebalance occurs without any other requests being
                    // processed until it completes.
                    member.requestRejoin();
                    // Any task config updates or target state changes will be addressed during the rebalance too
                    taskConfigUpdates.clear();
                    taskTargetStateChanges.clear();
                    needsReconfigRebalance = false;
                    return;
                } else {
                    if (!taskConfigUpdates.isEmpty()) {
                        taskConfigUpdatesCopy = taskConfigUpdates;
                        taskConfigUpdates = new HashSet<String>();
                    }

                    if (!taskTargetStateChanges.isEmpty()) {
                        // Similarly for target state changes which can cause tasks to be restarted
                        taskTargetStateChangesCopy = taskTargetStateChanges;
                        taskTargetStateChanges = new HashSet<String>();
                    }
                }
            }
        }

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

    private void processTaskConfigUpdates(Set<String> taskConfigUpdates) {
        // If we only have task config updates, we can just restart the updated tasks that are
        // currently assigned to this worker.
        Set<String> localTasks = assignment == null ? Collections.emptySet() : new HashSet(assignment.tasks());
        for (String taskId : taskConfigUpdates) {
            if (!localTasks.contains(taskId)) {
                continue;
            }
            log.info("Handling task config update by restarting task {}", taskId);
            stopTask(taskId);
            startTask(taskId);
        }
    }

    private void processTargetStateChanges(Set<String> taskTargetStateChanges) {
        // If we only have task state updates, we can just transit TargetState for tasks that are
        // currently assigned to this worker.
        Set<String> localTasks = assignment == null ? Collections.emptySet() : new HashSet(assignment.tasks());
        for (String taskId : taskTargetStateChanges) {
            if (!localTasks.contains(taskId)) {
                continue;
            }
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

    // public for testing
    public void halt() {
        synchronized (this) {
            // Clean up any tasks that are still running.
            log.info("Stopping tasks that are still assigned to the worker");
            worker.stopAndAwaitTasks();

            member.stop();

            // Explicitly fail any outstanding requests so they actually get a response and get an understandable reason
            // for their failure
            while (!requests.isEmpty()) {
                KeeperRequest request = requests.poll();
                request.callback().onCompletion(new DatalinkException("Worker is shutting down"), null);
            }

            stopServices();
        }
    }

    @Override
    public void stop() {
        log.info("Worker Keeper stopping");

        stopping.set(true);
        member.wakeup();
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

        log.info("Worker Keeper stopped");
    }

    public int generation() {
        return generation;
    }


    // Should only be called parseFrom work thread, so synchronization should not be needed
    private boolean isLeader() {
        return assignment != null && member.memberId().equals(assignment.leader());
    }

    /**
     * Get the URL for the leader's REST interface, or null if we do not have the leader's URL yet.
     */
    private String leaderUrl() {
        if (assignment == null)
            return null;
        return assignment.leaderUrl();
    }

    /**
     * Handler post-assignment operations, either trying to resolve issues that kept assignment parseFrom completing, getting
     * this node into sync and its work started.
     *
     * @return false if we couldn't finish
     */
    private boolean handleRebalanceCompleted() {
        if (this.rebalanceResolved) {
            return true;
        }

        // We need to handle a variety of cases after a rebalance:
        // 1. Assignment failed
        //  1a. We are the leader for the round. We will be leader again if we rejoin now, so we need to catch up before
        //      even attempting to. If we can't we should drop out of the group because we will block everyone parseFrom making
        //      progress. We can backoff and try rejoining later.
        //  1b. We are not the leader. We might need to catch up. If we're already caught up we can rejoin immediately,
        //      otherwise, we just want to wait reasonable amount of time to catch up and rejoin if we are ready.
        // 2. Assignment succeeded.
        //  2a. We are caught up on configs. Awesome! We can proceed to run our assigned work.
        //  2b. We need to try to catch up - try reading configs for reasonable amount of time.

        boolean needsRefreshConfig = false;
        boolean needsRejoin = false;
        if (assignment.failed()) {
            needsRejoin = true;
            if (isLeader()) {
                log.warn("Join group completed, but assignment failed and we are the leader. Getting the latest config and retrying.");
                needsRefreshConfig = true;
            } else if (configState.version() < assignment.version()) {
                log.warn("Join group completed, but assignment failed and we lagging. Getting the latest config and retrying.");
                needsRefreshConfig = true;
            } else {
                log.warn("Join group completed, but assignment failed. We were up to date, so just retrying.");
            }
        } else {
            if (configState.version() < assignment.version()) {
                log.warn("Catching up to assignment's config version.");
                needsRefreshConfig = true;
            }
        }

        if (needsRefreshConfig) {
            refreshConfig();
        }

        if (needsRejoin) {
            member.requestRejoin();
            return false;
        }

        // Should still validate that they match since we may have gone *past* the required version, in which case we
        // should *not* start any tasks and rejoin
        if (configState.version() != assignment.version()) {
            log.info("Current config state version {} does not match group assignment {}. Forcing rebalance.", configState.version(), assignment.version());
            member.requestRejoin();
            return false;
        }

        startWork();

        // We only mark this as resolved once we've actually started work, which allows us to correctly track whether
        // what work is currently active and running. If we bail early, the main tick loop + having requested rejoin
        // guarantees we'll attempt to rejoin before executing this method again.
        rebalanceResolved = true;
        return true;
    }


    /**
     * refresh to get the latest config.
     */
    private void refreshConfig() {
        log.info("Current config state version {} is behind group assignment {}, try to get the latest configs.", configState.version(), assignment.version());
        taskConfigManager.forceRefresh();
        configState = taskConfigManager.snapshot();
        log.info("Finished get the latest config and updated config snapshot, new config version is: {}", configState.version());
    }

    private void startWork() {
        // Start assigned tasks
        log.info("Starting tasks using config version {}", assignment.version());
        for (String taskId : assignment.tasks()) {
            startTask(taskId);
        }
        log.info("Finished starting tasks");
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

    private void addRequest(Callable<Void> action, Callback<Void> callback) {
        addRequest(0, action, callback);
    }

    private void addRequest(long delayMs, Callable<Void> action, Callback<Void> callback) {
        KeeperRequest req = new KeeperRequest(time.milliseconds() + delayMs, action, callback);
        requests.add(req);
        if (requests.peek() == req) {
            member.wakeup();
        }
    }

    public synchronized void restartTask(final String taskId, final Position position, final Callback<Void> callback) {
        addRequest(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (checkRebalanceNeeded(callback)) {
                    return null;
                }

                if (configState.taskConfig(taskId) == null) {
                    callback.onCompletion(new NotFoundException("Unknown task: " + taskId), null);
                    return null;
                }

                if (assignment.tasks().contains(taskId)) {
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
                } else {
                    callback.onCompletion(new NotAssignedException("Cannot restart task since it is not assigned to this member"), null);
                }
                return null;
            }
        }, forwardErrorCallback(callback));
    }

    private static final Callback<Void> forwardErrorCallback(final Callback<?> callback) {
        return new Callback<Void>() {
            @Override
            public void onCompletion(Throwable error, Void result) {
                if (error != null) {
                    callback.onCompletion(error, null);
                }
            }
        };
    }

    private boolean checkRebalanceNeeded(Callback<?> callback) {
        // Raise an error if we are expecting a rebalance to begin. This prevents us parseFrom forwarding requests
        // based on stale leadership or assignment information
        if (needsReconfigRebalance) {
            callback.onCompletion(new RebalanceNeededException("Request cannot be completed because a rebalance is expected"), null);
            return true;
        }
        return false;
    }

    protected void startServices() {
        this.worker.start();//轮询线程：1、记录task-position；2、启动monitor
        this.taskConfigManager.start();//初始化：刷新任务和版本
        this.taskStatusManager.start();//订阅IZkStateListener(更新zk里task状态)
    }

    protected void stopServices() {
        this.taskConfigManager.stop();
        this.taskStatusManager.stop();
        this.worker.stop();
    }

    @Override
    public void onPrepare(TaskStatusEvent event) throws TaskConflictException {
        taskStatusManager.addStatus(new TaskStatus(event.getTaskId(), event.getTaskExecutionId(),
                TaskStatus.State.PREPARING, workerId, generation(), event.getStartTime()));
    }

    @Override
    public void onStartup(TaskStatusEvent event) {
        taskStatusManager.updateStatus(new TaskStatus(event.getTaskId(), event.getTaskExecutionId(),
                TaskStatus.State.RUNNING, workerId, generation(), event.getStartTime()));
    }

    @Override
    public void onFailure(TaskStatusEvent event, Throwable cause) {
        taskStatusManager.updateStatus(new TaskStatus(event.getTaskId(), event.getTaskExecutionId(),
                TaskStatus.State.FAILED, workerId, generation(), event.getStartTime(), trace(cause)));
    }

    @Override
    public void onShutdown(TaskStatusEvent event) {
        taskStatusManager.removeStatus(event.getTaskId());
    }

    @Override
    public void onResume(TaskStatusEvent event) {
        taskStatusManager.updateStatus(new TaskStatus(event.getTaskId(), event.getTaskExecutionId(),
                TaskStatus.State.RUNNING, workerId, generation(), event.getStartTime()));
    }

    @Override
    public void onPause(TaskStatusEvent event) {
        taskStatusManager.updateStatus(new TaskStatus(event.getTaskId(), event.getTaskExecutionId(),
                TaskStatus.State.PAUSED, workerId, generation(), event.getStartTime()));
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
            synchronized (WorkerKeeper.this) {
                needsReconfigRebalance = true;
            }
        }

        @Override
        public void onTaskConfigAdd(String taskId) {
            log.info("task {} config added", taskId);
            synchronized (WorkerKeeper.this) {
                needsReconfigRebalance = true;
            }
        }

        @Override
        public void onTaskConfigUpdate(String taskId) {
            log.info("task {} config updated", taskId);
            synchronized (WorkerKeeper.this) {
                taskConfigUpdates.add(taskId);
            }
        }

        @Override
        public void onTaskStateChanged(String taskId) {
            log.info("task {} target state change", taskId);
            synchronized (WorkerKeeper.this) {
                taskTargetStateChanges.add(taskId);
            }
        }
    }

    // Rebalances are triggered internally parseFrom the group member, so these are always executed in the work thread.
    public class RebalanceListener implements WorkerRebalanceListener {
        @Override
        public void onAssigned(DatalinkProtocol.Assignment assignment, int generation) {
            // This callback just logs the info and saves it. The actual response is handled in the main loop, which
            // ensures the group member's logic for rebalancing can complete, potentially long-running steps to
            // catch up (or backoff if we fail) not executed in a callback, and so we'll be able to invoke other
            // group membership actions (e.g., we may need to explicitly leave the group if we cannot handle the
            // assigned tasks).
            log.info("Joined group and got assignment: {}", assignment);
            synchronized (WorkerKeeper.this) {
                WorkerKeeper.this.assignment = assignment;
                WorkerKeeper.this.generation = generation;
                rebalanceResolved = false;
            }
        }

        @Override
        public void onRevoked(String leader, Collection<String> tasks) {
            log.info("Rebalance started");

            // Note that since we don't reset the assignment, we don't revoke leadership here. During a rebalance,
            // it is still important to have a leader that can write configs, versions, etc.

            if (rebalanceResolved) {

                // TODO: Parallelize this. We should be able to request all tasks to stop, then wait on all of
                // them to finish
                // TODO: Technically we don't have to stop tasks at all until we know they've really been removed parseFrom
                // this worker. Instead, we can let them continue to run but buffer any update requests (which should be
                // rare anyway). This would avoid a steady stream of start/stop, which probably also includes lots of
                // unnecessary repeated connections to the source/sink system.
                // TODO: We need to at least commit task positions, but if we could commit offsets & pause them instead of
                // stopping them then state could continue to be reused when the task remains on this worker. For example,
                // this would avoid having to close a connection and then reopen it when the task is assigned back to this
                // worker again.
                worker.stopAndAwaitTasks(tasks);
                log.info("Finished stopping tasks in preparation for rebalance");
            } else {
                log.info("Wasn't unable to resume work after last rebalance, can skip stopping tasks");
            }
        }
    }

}
