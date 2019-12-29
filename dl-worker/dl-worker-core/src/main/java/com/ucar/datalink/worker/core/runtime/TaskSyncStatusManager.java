package com.ucar.datalink.worker.core.runtime;

import com.ucar.datalink.biz.service.TaskSyncStatusService;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.domain.task.TaskSyncStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lubiao on 2019/2/18.
 */
public class TaskSyncStatusManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskSyncStatusManager.class);

    private WorkerConfig workerConfig;
    private TaskSyncStatusService taskSyncStatusService;
    private ScheduledExecutorService executor;
    private Map<String, TaskSyncStatus> syncStatusCache;

    private long period = 1000;// 定时持久化频率，单位ms
    private volatile boolean running = false;

    public TaskSyncStatusManager(TaskSyncStatusService taskSyncStatusService, WorkerConfig workerConfig) {
        this.taskSyncStatusService = taskSyncStatusService;
        this.workerConfig = workerConfig;
        this.syncStatusCache = new ConcurrentHashMap<>();
        this.period = workerConfig.getLong(WorkerConfig.TASK_SYNCSTATUS_COMMIT_INTERVAL_MS_CONFIG);
    }

    public void start() {
        if (running) {
            return;
        }

        this.running = true;
        this.executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Task-SyncStatus-Manager"));
        this.executor.scheduleAtFixedRate(() -> {
            syncStatusCache.entrySet().stream().forEach(s -> {
                        try {
                            taskSyncStatusService.updateSyncStatus(s.getKey(), s.getValue());
                        } catch (Throwable t) {
                            logger.error("something goes wrong when update sync status.", t);
                        }
                    }
            );
        }, period, period, TimeUnit.MILLISECONDS);

        logger.info("Task SyncStatus Manager started.");
    }

    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        executor.shutdownNow();

        logger.info("Task SyncStatus Manager stopped.");
    }

    public void updateSyncStatus(String taskId, TaskSyncStatus syncStatus) {
        try {
            syncStatusCache.put(taskId, syncStatus);
        } catch (Throwable t) {
            logger.error("failed to put sync status to cache for task " + taskId, t);
        }
    }

    public void discardSyncStatus(String taskId) {
        try {
            syncStatusCache.remove(taskId);
        } catch (Throwable t) {
            logger.error("failed to remove sync status from cache for task " + taskId, t);
        }
    }
}
