package com.ucar.datalink.extend.service;

import com.lucky.upush.dto.sms.send.SendSmsDTO;
import com.ucar.datalink.biz.service.UcarAlarmPlugin;
import com.ucar.datalink.biz.utils.HttpUtils;
import com.ucar.datalink.domain.alarm.AlarmMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 发送短信
 */
@Service("smsService")
public class UcarSMSServiceImpl implements UcarAlarmPlugin {
    private static final Logger logger = LoggerFactory.getLogger(UcarSMSServiceImpl.class);

    @Autowired
    private UPushService uPushService;

    @Override
    public String getPluginName() {
        return "SMS";
    }

    @Override
    public void doSend(AlarmMessage message) {
        try {
            if (message.getRecipient() == null || message.getRecipient().size() == 0) {
                return;
            }
            SendSmsDTO dto = new SendSmsDTO();
            dto.setMobileList(message.getRecipient());
            dto.setMsgContent(message.getContent());
            dto.setMsgUuid(UUID.randomUUID().toString().replace("-", "")); //用途：1.幂等性校验（对于重复的msgUuid，Upush只消费一次），2.消息跟踪（方便调查问题）
            uPushService.sendSms(dto);
        } catch (Exception e) {
            logger.error("sendSMS is error", e);
        }
    }
}
