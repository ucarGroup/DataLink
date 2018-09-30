package com.ucar.datalink.manager.core.monitor.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.manager.core.monitor.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by csf on 17/5/2.
 */
@Service
public class TaskExceptionMonitor extends Monitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExceptionMonitor.class);

    private static final Cache<Long, String> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .build();

    @Autowired
    MonitorService monitorService;

    @Autowired
    AlarmService alarmService;

    @Override
    public void doMonitor() {
        if (cache.size() == 0) {
            return;
        }

        Map<Long, String> excMap = cache.asMap();
        Iterator<Map.Entry<Long, String>> excIt = excMap.entrySet().iterator();
        while (excIt.hasNext()) {
            Map.Entry<Long, String> excEntry = excIt.next();
            sendAlarm(excEntry.getKey(), excEntry.getValue());
        }
    }

    public void sendException(Map<Long, String> exceptions) {
        exceptions.entrySet().stream().forEach(e -> {
            try {
                sendAlarm(e.getKey(), e.getValue());
            } catch (Throwable t) {
                LOGGER.error("exception send failed in directly mode.", t);
            }
        });
    }

    public void addException(Map<Long, String> map) {
        map.entrySet().stream().forEach(i -> cache.put(i.getKey(), i.getValue()));

    }

    public Map<Long, String> getExceptionMap() {
        Map<Long, String> result = new HashMap<>();
        result.putAll(cache.asMap());
        return result;
    }

    public void clearExceptionMap() {
        cache.invalidateAll();
    }

    private void sendAlarm(Long resourceId, String exceptionInfo) {
        MonitorInfo monitorInfo = monitorService.getByResourceAndType(resourceId, MonitorType.TASK_EXCEPTION_MONITOR);
        if (isAlarm(resourceId, Long.MAX_VALUE, monitorInfo)) {//异常不受阈值控制，有异常就得报警
            alarmService.alarmError(monitorInfo, exceptionInfo);
        }
    }
}
