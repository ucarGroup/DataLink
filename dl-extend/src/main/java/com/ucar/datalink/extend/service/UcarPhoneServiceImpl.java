package com.ucar.datalink.extend.service;

import com.lucky.upush.dto.sms.send.SendSmsDTO;
import com.ucar.datalink.biz.service.UcarAlarmPlugin;
import com.ucar.datalink.domain.alarm.AlarmMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 发送电话
 */
@Service("phoneService")
public class UcarPhoneServiceImpl implements UcarAlarmPlugin {
    private static final Logger logger = LoggerFactory.getLogger(UcarPhoneServiceImpl.class);

    /**
     * 电话报警最大次数
     */
    private static final Integer MAX_ALARM_TIMES = 2;

    private static final LoadingCache<String,Integer> alarmTasksTimesCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).
            build(new CacheLoader<String,Integer>(){
                @Override
                public Integer load(String key) throws Exception {
                    return 0;
                }
            });

    @Autowired
    private UPushService uPushService;

    @Override
    public String getPluginName() {
        return "phone";
    }

    @Override
    public void doSend(AlarmMessage message) {
        try{
            Integer times = alarmTasksTimesCache.get(String.valueOf(message.getMonitorInfo().getResourceId())+message.getMonitorInfo().getMonitorType());
            if(times>=MAX_ALARM_TIMES){
                return;
            }
            List<String> phoneNumList = message.getRecipient();
            if (phoneNumList == null || phoneNumList.size() == 0) {
                return;
            }
            SendSmsDTO dto = new SendSmsDTO();
            dto.setMobileList(phoneNumList);
            dto.setMsgContent(message.getContent());
            dto.setMsgUuid(UUID.randomUUID().toString().replace("-", "")); //用途：1.幂等性校验（对于重复的msgUuid，Upush只消费一次），2.消息跟踪（方便调查问题）
            uPushService.sendPhone(dto);
            alarmTasksTimesCache.put(String.valueOf(message.getMonitorInfo().getResourceId())+message.getMonitorInfo().getMonitorType(),++times);
        }catch (Exception e){
          logger.error("sendPhone is error", e);
        }
    }
}
