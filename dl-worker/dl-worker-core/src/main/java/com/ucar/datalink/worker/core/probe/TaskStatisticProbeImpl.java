package com.ucar.datalink.worker.core.probe;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.TaskStatisticService;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.domain.task.TaskStatisticInfo;
import com.ucar.datalink.worker.api.probe.TaskStatisticProbe;
import com.ucar.datalink.worker.api.probe.index.TaskStatisticProbeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by sqq on 2018/3/14.
 */
@Service
public class TaskStatisticProbeImpl implements TaskStatisticProbe {

    private static final Logger logger = LoggerFactory.getLogger(TaskStatisticProbeImpl.class);

    private volatile boolean running = false;

    private long period = 1000 * 60;

    private ScheduledExecutorService statisticService;

    private final LoadingCache<Long, List<TaskStatisticProbeIndex>> cache = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .expireAfterWrite(60L, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, List<TaskStatisticProbeIndex>>() {
                @Override
                public List<TaskStatisticProbeIndex> load(Long key) throws Exception {
                    return new LinkedList<>();
                }
            });

    @Autowired
    TaskStatisticService taskStatisticService;

    @Override
    public void record(TaskStatisticProbeIndex index) {
        try {
            List<TaskStatisticProbeIndex> taskStatisticList = cache.getUnchecked(index.getTaskId());
            taskStatisticList.add(index);
        } catch (Exception e) {
            logger.error("task statistic exception.", e);
        }
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        this.statisticService = Executors.newScheduledThreadPool(1, new NamedThreadFactory(
                        MessageFormat.format("Probe-Type-{0}", TaskStatisticProbe.class.getSimpleName()))
        );
        //启动定时任务进行统计
        statisticService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    sendStatisticReset();
                } catch (Throwable e) {
                    logger.error("Task statistic probe failed.", e);
                }
            }
        }, period, period, TimeUnit.MILLISECONDS);

        logger.info("Task statistic probe started.");
        this.running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        statisticService.shutdownNow();
        cache.invalidateAll();
        logger.info("Task statistic probe stopped.");
    }

    private void sendStatisticReset() {
        Map<Long, List<TaskStatisticProbeIndex>> copyStatisticMap = new HashMap<>();
        copyStatisticMap.putAll(cache.asMap());
        Iterator<Map.Entry<Long, List<TaskStatisticProbeIndex>>> it = copyStatisticMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, List<TaskStatisticProbeIndex>> entry = it.next();
            List<TaskStatisticProbeIndex> list = entry.getValue();
            cache.invalidate(entry.getKey());

            if (list == null || list.size() == 0) {
                continue;
            }
            long sumForWriteTime = 0;
            long sumForRecordsCount = 0;
            long sumForRecordsSize = 0;
            long sumForExceptionsCount = 0;
            long sumForReadWriteCount = 0;
            for (TaskStatisticProbeIndex index : list) {
                sumForWriteTime += index.getWriteTimeThrough();
                sumForRecordsCount += index.getRecordsCount();
                sumForRecordsSize += index.getPayloadSize();
                if (index.isExceptionExist()) {
                    sumForExceptionsCount++;
                } else {
                    sumForReadWriteCount++;
                }
            }
            BigDecimal writeTimePerRecord = BigDecimal.valueOf(0.00);
            if (sumForRecordsCount != 0) {
                writeTimePerRecord = new BigDecimal((double) sumForWriteTime / sumForRecordsCount).setScale(2, BigDecimal.ROUND_HALF_UP);
            }

            TaskStatisticInfo taskStatisticInfo = new TaskStatisticInfo();
            taskStatisticInfo.setTaskId(entry.getKey());
            taskStatisticInfo.setRecordsPerMinute(sumForRecordsCount);
            taskStatisticInfo.setSizePerMinute(sumForRecordsSize);
            taskStatisticInfo.setWriteTimePerRecord(writeTimePerRecord);
            taskStatisticInfo.setExceptionsPerMinute(sumForExceptionsCount);
            taskStatisticInfo.setReadWriteCountPerMinute(sumForReadWriteCount);
            taskStatisticService.insert(taskStatisticInfo);
        }
    }
}
