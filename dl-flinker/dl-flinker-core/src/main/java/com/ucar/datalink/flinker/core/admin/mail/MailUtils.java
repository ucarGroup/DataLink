package com.ucar.datalink.flinker.core.admin.mail;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Created by user on 2018/2/1.
 */
public class MailUtils {

    public static void sendMail(MailInfo mailInfo) {
        final Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.zhuanche.10101111.com");
        props.put("mail.user", "sender@zhuanche.10101111.com");
        Session mailSession = Session.getInstance(props);
        try {
            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(props.getProperty("mail.user")));
            message.setSubject(mailInfo.getSubject());
            if (mailInfo.getRecipient() != null && mailInfo.getRecipient().size() != 0) {
                for (String to : mailInfo.getRecipient()) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                }
                MimeMultipart mmp = new MimeMultipart("related");
                MimeBodyPart mbp = new MimeBodyPart();
                mbp.setContent(mailInfo.getMailContent(), "text/html;charset=utf-8");
                mmp.addBodyPart(mbp, 0);
                message.setContent(mmp);
                Transport transport = mailSession.getTransport();
                // 打开连接
                transport.connect("sender", "");
                // 将message对象传递给transport对象，将邮件发送出去
                transport.sendMessage(message, message.getAllRecipients());
                // 关闭连接
                transport.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("send mail failed:", e);
        }

    }
}
