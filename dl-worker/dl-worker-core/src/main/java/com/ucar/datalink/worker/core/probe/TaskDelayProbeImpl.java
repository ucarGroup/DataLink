package com.ucar.datalink.worker.core.probe;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.TaskDelayTimeService;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.domain.task.TaskDelayTimeInfo;
import com.ucar.datalink.worker.api.probe.TaskDelayProbe;
import com.ucar.datalink.worker.api.probe.index.TaskDelayProbeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lubiao on 2018/3/14.
 */
@Service
public class TaskDelayProbeImpl implements TaskDelayProbe{

    private static final Logger logger = LoggerFactory.getLogger(TaskDelayProbeImpl.class);

    private final LoadingCache<Long, List<TaskDelayProbeIndex>> cache = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .expireAfterWrite(60L, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, List<TaskDelayProbeIndex>>() {
                @Override
                public List<TaskDelayProbeIndex> load(Long key) throws Exception {
                    return new LinkedList<>();
                }
            });

    private volatile boolean running = false;

    private long period = 1000 * 60;// 单位s

    private ScheduledExecutorService taskMonitorExecutor;

    @Autowired
    private TaskDelayTimeService taskDelayTimeService;

    @Override
    public void record(TaskDelayProbeIndex index) {
        try {
            recordTaskDelayTime(index);
        } catch (Exception e) {
            logger.error("Task delay probe meet exception ", e);
        }
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        this.taskMonitorExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory(
                MessageFormat.format("Probe-Type-{0}", TaskDelayProbe.class.getSimpleName())));
        //启动定时监控任务
        taskMonitorExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    sendDelayReset();
                } catch (Throwable e) {
                    logger.error("Task delay probe failed.", e);
                }
            }
        }, period, period, TimeUnit.MILLISECONDS);

        logger.info("Task delay probe started.");

        this.running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        taskMonitorExecutor.shutdownNow();
        cache.invalidateAll();

        logger.info("Task delay probe stopped.");
    }

    private void recordTaskDelayTime(TaskDelayProbeIndex index) {
        List<TaskDelayProbeIndex> taskTimeList = cache.getUnchecked(index.getTaskId());
        taskTimeList.add(index);
    }

    private void sendDelayReset() {
        Map<Long, List<TaskDelayProbeIndex>> copyDelayTimeMap = new HashMap<>();
        copyDelayTimeMap.putAll(cache.asMap());
        Iterator<Map.Entry<Long, List<TaskDelayProbeIndex>>> it = copyDelayTimeMap.entrySet().iterator();
        while (it.hasNext()) {
            Long sum = 0L;
            Map.Entry<Long, List<TaskDelayProbeIndex>> entry = it.next();
            List<TaskDelayProbeIndex> list = entry.getValue();
            cache.invalidate(entry.getKey());

            if (list == null || list.size() == 0) {
                continue;
            }
            for (TaskDelayProbeIndex time : entry.getValue()) {
                sum += time.getDelayTime();
            }
            Long averageValue = sum / entry.getValue().size();
            taskDelayTimeService.insert(buildDelayTimeInfo(averageValue, entry.getKey()));
        }
    }

    private TaskDelayTimeInfo buildDelayTimeInfo(Long delayTime, Long taskId) {
        TaskDelayTimeInfo taskDelayTimeInfo = new TaskDelayTimeInfo();
        taskDelayTimeInfo.setDelayTime(delayTime);
        taskDelayTimeInfo.setTaskId(taskId);
        return taskDelayTimeInfo;
    }
}
