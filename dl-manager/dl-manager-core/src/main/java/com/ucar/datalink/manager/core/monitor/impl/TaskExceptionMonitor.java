package com.ucar.datalink.manager.core.monitor.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.service.AlarmStrategyService;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.domain.alarm.AlarmStrategyInfo;
import com.ucar.datalink.domain.alarm.StrategyConfig;
import com.ucar.datalink.domain.monitor.MonitorCat;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.manager.core.monitor.Monitor;
import com.ucar.datalink.manager.core.monitor.MonitorManager;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

    private static final LoadingCache<String, ExceptionCount> exceptionFilterCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1,TimeUnit.HOURS)
            .build(new CacheLoader<String, ExceptionCount>() {
                @Override
                public ExceptionCount load(String key) throws Exception {
                    return new ExceptionCount();
                }
            });

    @Autowired
    MonitorService monitorService;

    @Autowired
    AlarmService alarmService;

    @Autowired
    private AlarmStrategyService alarmStrategyService;

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

    public void sendDataxException(Map<Long, String> exceptions) {
        exceptions.entrySet().stream().forEach(e -> {
            try {
                byte[] decoderByte = Base64.getDecoder().decode(e.getValue());
                String errMsg = new String(decoderByte);
                sendDataxAlarm(e.getKey(), errMsg);
            } catch (Throwable t) {
                LOGGER.error("datax exception send failed in directly mode.", t);
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
        if(monitorInfo == null) {
            return;
        }
        monitorService.initMonitor(monitorInfo);
        AlarmStrategyInfo alarmStrategyInfo = alarmStrategyService.getByTaskIdAndType(monitorInfo.getResourceId(),monitorInfo.getMonitorType());
        if(alarmStrategyInfo != null) {
            StrategyConfig config = alarmStrategyService.getStrategyConfig(alarmStrategyInfo.getStrategys());
            monitorService.copyStrategy(config,monitorInfo);
        }
        if(isExceptionFilter(monitorInfo,exceptionInfo)) {//过滤异常
            return;
        }
        if (isAlarm(resourceId, Long.MAX_VALUE, monitorInfo)) {//异常不受阈值控制，有异常就得报警
            alarmService.alarmError(monitorInfo, exceptionInfo);
        }
    }

    private boolean isExceptionFilter(MonitorInfo monitorInfo, String exceptionInfo) {
        String otherConfig = monitorInfo.getOtherConfig();
        if (StringUtils.isEmpty(otherConfig)) {
            return false;
        }
        ExceptionConfig exceptionConfig = JSONObject.parseObject(otherConfig, ExceptionConfig.class);
        for (ExceptionFilter exceptionFilter : exceptionConfig.getExceptionFilters()) {
            if (!exceptionInfo.contains(exceptionFilter.getException())) {
                continue;
            }
            ExceptionCount exceptionCount = exceptionFilterCache.getUnchecked(monitorInfo.getResourceId()+exceptionFilter.getException());
            if(exceptionCount.getCount() == 0) {
                exceptionFilterCache.put(monitorInfo.getResourceId()+exceptionFilter.getException(),exceptionCount.countPlus());
                return true;
            }
            Long countTime = System.currentTimeMillis() - exceptionCount.getInitTime();
            if(countTime >= exceptionFilter.getDelayTime()*60*1000) {
                if(exceptionCount.getCount() < countTime/(ManagerConfig.current().getMonitorCheckIntervalTime()* 2*1000)) {//总的次数少于50%，不发报交警，重新初始化缓存数据
                    exceptionFilterCache.put(monitorInfo.getResourceId()+exceptionFilter.getException(),new ExceptionCount());
                    return true;
                }else {
                    exceptionFilterCache.put(monitorInfo.getResourceId()+exceptionFilter.getException(),exceptionCount.countPlus());
                    return false;
                }
            }else {
                exceptionFilterCache.put(monitorInfo.getResourceId()+exceptionFilter.getException(),exceptionCount.countPlus());
                return true;
            }
        }
        return false;
    }



    private void sendDataxAlarm(Long resourceId, String exceptionInfo) {
        List<MonitorInfo> list = monitorService.getListForQueryPage(MonitorCat.DATAX_MONITOR.getKey(), MonitorType.DATAX_EXCEPTION_MONITOR.getKey(), null, resourceId, 1);
        if (list == null || list.size() == 0) {
            LOGGER.info("monitorService get datax exception empty");
            return;
        }
        MonitorInfo monitorInfo = list.get(0);
        LOGGER.info("send datax alarm -> " + monitorInfo);
        //if (isAlarm(resourceId, Long.MAX_VALUE, monitorInfo)) {//异常不受阈值控制，有异常就得报警
            alarmService.alarmDataxError(monitorInfo, exceptionInfo);
        //}
    }


    private static class ExceptionConfig {
        private List<ExceptionFilter> exceptionFilters;

        public List<ExceptionFilter> getExceptionFilters() {
            return exceptionFilters;
        }

        public void setExceptionFilters(List<ExceptionFilter> exceptionFilters) {
            this.exceptionFilters = exceptionFilters;
        }
    }

    private static class ExceptionFilter {
        private String exception;
        private Integer delayTime;

        public String getException() {
            return exception;
        }

        public void setException(String exception) {
            this.exception = exception;
        }

        public Integer getDelayTime() {
            return delayTime;
        }

        public void setDelayTime(Integer delayTime) {
            this.delayTime = delayTime;
        }
    }

    private static class ExceptionCount {
        private Long initTime = System.currentTimeMillis();
        private Integer count = 0;

        public Long getInitTime() {
            return initTime;
        }

        public void setInitTime(Long initTime) {
            this.initTime = initTime;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public ExceptionCount countPlus() {
            this.setCount(++count);
            return this;
        }
    }

}
