package com.ucar.datalink.extend.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.UcarAlarmPlugin;
import com.ucar.datalink.domain.alarm.AlarmMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UcarThresholdServiceImpl implements UcarAlarmPlugin {
    private static final Logger logger = LoggerFactory.getLogger(UcarThresholdServiceImpl.class);

    @Qualifier("phoneService")
    @Autowired
    private UcarAlarmPlugin phoneService;

    private static final Integer ALARM_THRESHOLD = 5;

    private static final LoadingCache<String,Integer> alarmThresholdCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).
            build(new CacheLoader<String, Integer>() {
                @Override
                public Integer load(String key) throws Exception {
                    return 0;
                }
            });

    @Override
    public String getPluginName() {
        return "threshold";
    }

    @Override
    public void doSend(AlarmMessage message) {
        try {
            Integer threshold = alarmThresholdCache.get(String.valueOf(message.getMonitorInfo().getResourceId())+message.getMonitorInfo().getMonitorType());
            if(threshold >= ALARM_THRESHOLD) {
                phoneService.doSend(message);
            }
            alarmThresholdCache.put(String.valueOf(message.getMonitorInfo().getResourceId())+message.getMonitorInfo().getMonitorType(),++threshold);
        } catch (Exception e) {
            logger.error("sendPhone is error", e);
        }
    }


}
