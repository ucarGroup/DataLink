package com.ucar.datalink.extend.service;

import com.lucky.upush.dto.BusinessLine;
import com.lucky.upush.dto.email.send.MailMessage;
import com.lucky.upush.dto.sms.send.SendSmsDTO;
import com.lucky.upush.sdk.Upush;
import com.lucky.upush.sdk.model.SmsCategory;
import com.lucky.upush.sdk.model.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * UPushService
 *
 * @author daijunjian
 * @date 2019/11/05
 */
@Service
public class UPushService {

    private static final Logger logger = LoggerFactory.getLogger(UPushService.class);

    @Value("${extend.uPush.url}")
    private String uPushUrl;
    @Value("${extend.uPush.channel.text}")
    private Integer channelText;
    @Value("${extend.uPush.channel.voice}")
    private Integer channelVoice;
    @Value("${extend.uPush.channel.mail}")
    private Integer channelMail;


    @PostConstruct
    public void startup(){
        Upush.init(BusinessLine.UCAR, "datalink", uPushUrl);
    }

    @PreDestroy
    public void shutdown(){
        Upush.shutdown();
    }

    /**
     * 普通短信
     * @param dto
     */
    public void sendSms(SendSmsDTO dto){
        try {
            dto.setChannel(channelText);
            Upush.SMS.send(Transport.HTTP, SmsCategory.TEXT, dto);
        }catch (Exception e){
            logger.error("UPushServer sendSms is error", e);
        }
    }

    /**
     * 语音短信
     * @param dto
     */
    public void sendPhone(SendSmsDTO dto){
        try {
            dto.setChannel(channelVoice);
            Upush.SMS.send(Transport.HTTP, SmsCategory.VOICE, dto);
        }catch (Exception e){
            logger.error("UPushServer sendPhone is error", e);
        }
    }

    /**
     * 邮件发送
     * @param message
     */
    public void sendMail(MailMessage message){
        try {
            message.setChannel(channelMail);
            Upush.EMAIL.send(Transport.HTTP, true, message);
        }catch (Exception e){
            logger.error("UPushServer sendMail is error", e);
        }
    }

}
