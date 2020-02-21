package com.ucar.datalink.flinker.core.admin.mail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2018/2/1.
 */
public class MailInfo {

    private String subject;
    private List<String> recipient =new ArrayList<String>();
    private List<String> attachments =new ArrayList<String>();
    private String mailContent;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public void setRecipient(List<String> recipient) {
        this.recipient = recipient;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public String getMailContent() {
        return mailContent;
    }

    public void setMailContent(String mailContent) {
        this.mailContent = mailContent;
    }


    @Override
    public String toString() {
        return "MailInfo{" +
                "subject='" + subject + '\'' +
                ", recipient=" + recipient +
                ", attachments=" + attachments +
                ", mailContent='" + mailContent + '\'' +
                '}';
    }
}
