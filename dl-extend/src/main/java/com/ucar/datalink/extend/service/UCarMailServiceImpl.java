package com.ucar.datalink.extend.service;

import com.ucar.datalink.biz.service.MailService;
import com.ucar.datalink.biz.service.UcarAlarmPlugin;
import com.ucar.datalink.domain.alarm.AlarmMessage;
import com.ucar.datalink.domain.mail.MailInfo;
import com.ucar.datalink.domain.mail.MailType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.lucky.upush.dto.email.send.MailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.lucky.upush.dto.email.enums.ContentType;

/**
 * 发送邮件
 * Created by lubiao on 2018/3/22.
 */
@Service("mailService")
public class UCarMailServiceImpl implements MailService, UcarAlarmPlugin {
    private static final Logger logger = LoggerFactory.getLogger(UCarMailServiceImpl.class);

    @Autowired
    private UPushService uPushService;

    @Override
    public String getPluginName() {
        return "email";
    }

    @Override
    public void sendMail(MailInfo mailInfo) {
        MailMessage message = new MailMessage();
        message.setTitle(mailInfo.getSubject());
        message.setMsgUuid(UUID.randomUUID().toString().replace("-", ""));
        List<String> mailList = Arrays.asList(mailInfo.getRecipient().toArray(new String[mailInfo.getRecipient().size()]));
        message.setMailAddresses(mailList);
        message.setContent(mailInfo.getMailContent());
        if (mailInfo.getMailType() == MailType.Simple) {
            message.setContentType(ContentType.TEXT);
        } else {
            message.setContentType(ContentType.HTML);
        }
        uPushService.sendMail(message);
    }

    @Override
    public void doSend(AlarmMessage message) {
        try {
            List<String> phoneNumList = message.getRecipient();
            if (phoneNumList == null || phoneNumList.size() == 0) {
                return;
            }
            MailInfo mailInfo = new MailInfo();
            mailInfo.setMailContent(message.getContent());
            mailInfo.setRecipient(message.getRecipient());
            mailInfo.setSubject(message.getSubject());
            mailInfo.setMailType(MailType.Simple);
            sendMail(mailInfo);
        } catch (Exception e) {
            logger.error("send mail error", e);
        }
    }
}
