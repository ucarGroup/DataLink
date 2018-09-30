package com.ucar.datalink.worker.core.runtime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.worker.api.probe.ProbeManager;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * <p>
 * Worker runs a (dynamic) set of tasks in a set of threads, doing the work of actually moving
 * data from reader to writer.
 * </p>
 * <p>
 * Since each task has a set of dedicated threads, this is mainly just a container for them.
 * </p>
 *
 * @author lubiao
 */
public class Worker {
    private static final Logger log = LoggerFactory.getLogger(Worker.class);

    private final ExecutorService executor;
    private final Time time;
    private final String workerId;
    private final WorkerConfig config;
    private final TaskPositionManager taskPositionManager;
    private ProbeManager probeManager;

    private HashMap<String, WorkerTask> tasks = new HashMap<>();

    public Worker(String workerId, Time time, WorkerConfig config, TaskPositionManager taskPositionManager, ProbeManager probeManager) {
        this.executor = Executors.newCachedThreadPool(new NamedThreadFactory("Task-Container"));
        this.workerId = workerId;
        this.time = time;
        this.config = config;
        this.taskPositionManager = taskPositionManager;
        this.probeManager = probeManager;
    }

    public void start() {
        log.info("Worker starting");

        taskPositionManager.start();
        String probeBlackList = WorkerConfig.current().getString(WorkerConfig.WORKER_PROBE_BLACKLIST_CONFIG);
        probeManager.start(StringUtils.isNotBlank(probeBlackList) ? Arrays.asList(probeBlackList.split(",")) : Lists.newArrayList());

        log.info("Worker started");
    }

    public void stop() {
        log.info("Worker stopping");

        if (!tasks.isEmpty()) {
            log.warn("Shutting down tasks {} uncleanly; WorkerKeeper should have shut down tasks before the Worker is stopped", tasks.keySet());
            stopAndAwaitTasks();
        }

        taskPositionManager.stop();
        probeManager.stop();

        log.info("Worker stopped");
    }

    public String workerId() {
        return workerId;
    }

    public boolean startTask(String taskId, TaskInfo taskInfo, TaskStatusListener statusListener) {
        try {
            MDC.put(Constants.MDC_TASKID, taskId);

            if (tasks.containsKey(taskInfo.idString())) {
                throw new DatalinkException("Task already exists in this worker: " + taskId);
            }

            final WorkerTask workerTask;
            final String taskExecutionId = UUID.randomUUID().toString();
            try {
                log.info("Creating task {}.", taskId);
                workerTask = new WorkerTaskContainer(config, taskInfo, statusListener,
                        taskPositionManager, taskExecutionId);

                log.info("Initializing task {}, reader parameter is {}, writer parameter is {}.",
                        taskId,
                        taskInfo.getTaskReaderParameter(),
                        taskInfo.getTaskWriterParameter());
                workerTask.initialize(taskInfo);
            } catch (Throwable t) {
                log.error("Failed to start task {}", taskId, t);
                statusListener.onFailure(new TaskStatusEvent(taskId, taskExecutionId, null), t);
                return false;
            }

            log.info("Submitting task {}.", taskId);
            executor.submit(workerTask);

            log.info("Saving task {}.", taskId);
            tasks.put(taskId, workerTask);
            return true;
        } finally {
            MDC.remove(Constants.MDC_TASKID);
        }
    }

    public void stopAndAwaitTask(String taskId) {
        stopAndAwaitTasks(Sets.newHashSet(taskId));
    }

    public void stopAndAwaitTasks() {
        stopAndAwaitTasks(new HashSet<>(tasks.keySet()));
    }

    public Collection<String> stopAndAwaitTasks(Collection<String> ids) {
        final List<String> stoppable = new ArrayList<>(ids.size());
        for (String taskId : ids) {
            try {
                MDC.put(Constants.MDC_TASKID, taskId);

                final WorkerTask task = tasks.get(taskId);
                if (task == null) {
                    log.warn("Ignoring stop request for unowned task {}", taskId);
                    continue;
                }
                stopTask(task);
                stoppable.add(taskId);
            } finally {
                MDC.remove(Constants.MDC_TASKID);
            }
        }
        awaitStopTasks(stoppable);
        return stoppable;
    }

    private void awaitStopTasks(Collection<String> ids) {
        long now = time.milliseconds();
        long deadline = now + config.getLong(WorkerConfig.TASK_SHUTDOWN_GRACEFUL_TIMEOUT_MS_CONFIG);
        for (String id : ids) {
            try {
                MDC.put(Constants.MDC_TASKID, id);
                long remaining = Math.max(0, deadline - time.milliseconds());
                awaitStopTask(tasks.get(id), remaining);
            } finally {
                MDC.remove(Constants.MDC_TASKID);
            }
        }
    }

    private void stopTask(WorkerTask task) {
        log.info("Stopping task {}", task.id());
        task.stop();
    }

    private void awaitStopTask(WorkerTask task, long timeout) {
        if (!task.awaitStop(timeout)) {
            log.error("Graceful stop of task {} failed.", task.id());
            task.cancel();
        }
        tasks.remove(task.id());
    }

    public void setTargetState(String taskId, TargetState state) {
        try {
            MDC.put(Constants.MDC_TASKID, taskId);

            WorkerTask task = tasks.get(taskId);
            if (task != null) {
                log.info("Setting task {} state to {}", taskId, state);
                task.transitionTo(state);
            }
        } finally {
            MDC.remove(Constants.MDC_TASKID);
        }
    }

    public TaskPositionManager getTaskPositionManager() {
        return taskPositionManager;
    }
}
