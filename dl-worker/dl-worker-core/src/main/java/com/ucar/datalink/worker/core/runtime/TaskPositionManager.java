package com.ucar.datalink.worker.core.runtime;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.TaskPositionService;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.domain.Position;
import com.ucar.datalink.worker.api.position.PositionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Task数据同步位点管理器，PositionManager接口的默认实现.
 * <p>
 * <p>
 * Created by lubiao on 2017/2/23.
 */
public class TaskPositionManager implements PositionManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskPositionManager.class);

    private TaskPositionService taskPositionService;
    private WorkerConfig workerConfig;
    private ScheduledExecutorService executor;
    private Set<String> updatePositionTasks;
    private LoadingCache<String, Position> updatedPositions;
    private NullPosition nullPosition = new NullPosition();

    private long period = 1000;// 定时持久化频率，单位ms
    private volatile boolean running = false;

    public TaskPositionManager(TaskPositionService taskPositionService, WorkerConfig workerConfig) {
        this.taskPositionService = taskPositionService;
        this.workerConfig = workerConfig;
        this.period = workerConfig.getLong(WorkerConfig.POSITION_COMMIT_INTERVAL_MS_CONFIG);
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        this.running = true;
        this.executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("Task-Position-Manager"));
        this.updatePositionTasks = Collections.synchronizedSet(new HashSet<>());
        this.updatedPositions = CacheBuilder.newBuilder().build(new CacheLoader<String, Position>() {
            @Override
            public Position load(String taskId) throws Exception {
                Position position = taskPositionService.getPosition(taskId);
                if (position == null) {
                    return nullPosition;
                } else {
                    return position;
                }
            }
        });
        // 启动定时工作任务
        this.executor.scheduleAtFixedRate(() -> {
            synchronized (TaskPositionManager.this) {
                List<String> tasks = new ArrayList<>(updatePositionTasks);
                for (String taskId : tasks) {
                    // 定时将内存中的最新值持久化到存储，多次变更只刷一次
                    flush(taskId);
                }
            }
        }, period, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        executor.shutdownNow();
        updatePositionTasks.clear();
        updatedPositions.invalidateAll();
    }

    @Override
    public boolean isStart() {
        return running;
    }

    @Override
    public void updatePosition(String taskId, Position position) {
        if (running) {
            updatedPositions.put(taskId, position);
            updatePositionTasks.add(taskId);
        }
    }

    @Override
    public void updatePositionNow(String taskId, Position position) {
        if (running) {
            synchronized (this) {
                updatePosition(taskId, position);
                flush(taskId);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Position> T getPosition(String taskId) {
        return (T) taskPositionService.getPosition(taskId);
    }

    @Override
    public void discardPosition(String taskId) {
        synchronized (this) {
            updatePositionTasks.remove(taskId);
            updatedPositions.invalidate(taskId);
            logger.info("Position is discarded for task " + taskId);
        }
    }

    private void flush(String taskId) {
        if (running) {
            try {
                Position position = getUpdatedPosition(taskId);
                if (position != null) {
                    taskPositionService.updatePosition(taskId, position);
                }
                updatePositionTasks.remove(taskId);//概率上存在刚刚add，就被remove的情况，但从宏观上看没有任何问题
            } catch (Throwable e) {
                // ignore
                logger.error("period update position for task " + taskId + " failed!", e);
            }
        }
    }

    private <T extends Position> T getUpdatedPosition(String taskId) {
        Position position = updatedPositions.getUnchecked(taskId);
        if (position == nullPosition) {
            return null;
        } else {
            return (T) position;
        }
    }

    static class NullPosition extends Position {

    }
}
