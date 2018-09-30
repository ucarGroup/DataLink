package com.ucar.datalink.extend.service;

import com.ucar.datalink.biz.service.MailService;
import com.ucar.datalink.domain.mail.MailInfo;
import com.ucar.datalink.domain.mail.MailType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;


/**
 * Created by lubiao on 2018/3/22.
 */
@Service
public class UCarMailServiceImpl implements MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UCarLoginServiceImpl.class);

    private JavaMailSenderImpl javaMailSender;

    public UCarMailServiceImpl() {
        javaMailSender = new JavaMailSenderImpl();
        //邮箱发送设置
    }

    @Override
    public void sendMail(MailInfo mailInfo) {
        if (mailInfo.getMailType() == MailType.Simple) {
            sendSimpleMail(mailInfo);
        } else {
            sendMimeMail(mailInfo);
        }
    }

    private void sendSimpleMail(MailInfo msg) {
        //组装mailMessage
        LOGGER.info("mailMessage:" + msg);
    }

    private void sendMimeMail(MailInfo msg) {
        LOGGER.info("mailMessage:" + msg);
    }
}
