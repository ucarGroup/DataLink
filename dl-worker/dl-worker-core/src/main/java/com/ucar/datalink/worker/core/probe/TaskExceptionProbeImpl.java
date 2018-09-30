package com.ucar.datalink.worker.core.probe;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ucar.datalink.biz.service.TaskExceptionService;
import com.ucar.datalink.biz.utils.RestServerUtils;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import com.ucar.datalink.domain.task.TaskExceptionInfo;
import com.ucar.datalink.worker.api.probe.TaskExceptionProbe;
import com.ucar.datalink.worker.api.probe.index.TaskExceptionProbeIndex;
import com.ucar.datalink.worker.core.runtime.WorkerConfig;
import org.apache.kafka.clients.CommonClientConfigs;
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
public class TaskExceptionProbeImpl implements TaskExceptionProbe {

    private static final Logger logger = LoggerFactory.getLogger(TaskExceptionProbeImpl.class);

    private final Cache<Long, String> cache = CacheBuilder.newBuilder()
            .maximumSize(1000L)
            .expireAfterWrite(60L, TimeUnit.MINUTES)
            .build();

    private volatile boolean running = false;

    private long period = 5000;// 单位ms

    private ScheduledExecutorService taskMonitorExecutor;

    @Autowired
    TaskExceptionService taskExceptionService;

    @Override
    public void start() {
        if (running) {
            return;
        }

        this.taskMonitorExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory(
                MessageFormat.format("Probe-Type-{0}", TaskExceptionProbe.class.getSimpleName())
        ));
        //启动定时监控任务
        taskMonitorExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    String bootMode = WorkerConfig.current().getString(WorkerConfig.WORKER_BOOT_MODE_CONFIG);
                    if (bootMode.equals("distributed")) {
                        send();
                    }
                } catch (Throwable e) {
                    logger.error("Task exception probe failed.", e);
                }
            }
        }, period, period, TimeUnit.MILLISECONDS);

        logger.info("Task exception probe started.");

        this.running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        taskMonitorExecutor.shutdownNow();
        cache.invalidateAll();

        logger.info("Task exception probe stopped.");
    }

    @Override
    public void record(TaskExceptionProbeIndex index) {
        String bootMode = WorkerConfig.current().getString(WorkerConfig.WORKER_BOOT_MODE_CONFIG);
        if (index.isSendDirectly() && bootMode.equals("distributed")) {
            sendDirectly(index);
        } else {
            cache.put(index.getTaskId(), index.getExceptionInfo());
        }
        try {
            TaskExceptionInfo taskExceptionInfo = new TaskExceptionInfo();
            String clientId = WorkerConfig.current().getString(CommonClientConfigs.CLIENT_ID_CONFIG);//???
            taskExceptionInfo.setWorkerId(Long.valueOf(clientId));
            taskExceptionInfo.setTaskId(index.getTaskId());
            taskExceptionInfo.setExceptionDetail(index.getExceptionInfo());
            taskExceptionService.insert(taskExceptionInfo);
        } catch (Exception e) {
            logger.info("Record exception detail failed.", e);
        }
    }

    private void send() throws Exception {
        Map<Long, String> copiedMap = new HashMap<>();
        copiedMap.putAll(cache.asMap());
        cache.invalidateAll();

        RestServerUtils.executeRemote(copiedMap, "/communication/putExceptionAndCache");


    }

    private void sendDirectly(TaskExceptionProbeIndex exceptionMonitorIndex) {
        try {
            Map<Long, String> request = new HashMap<>();
            request.put(exceptionMonitorIndex.getTaskId(), exceptionMonitorIndex.getExceptionInfo());
            RestServerUtils.executeRemote(request, "/communication/putExceptionAndSend");
        } catch (Throwable t) {
            logger.error("exception send failed in direct mode.", t);
        }
    }

}
