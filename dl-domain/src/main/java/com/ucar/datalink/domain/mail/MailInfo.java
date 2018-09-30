package com.ucar.datalink.domain.mail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csf on 17/5/3.
 */
public class MailInfo {

    private String subject;
    private List<String> recipient;
    private String mailContent;
    private MailType mailType;

    public MailInfo() {
        this.mailType = MailType.Html;
        this.recipient = new ArrayList<>();
    }

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

    public String getMailContent() {
        return mailContent;
    }

    public void setMailContent(String mailContent) {
        this.mailContent = mailContent;
    }

    public MailType getMailType() {
        return mailType;
    }

    public void setMailType(MailType mailType) {
        this.mailType = mailType;
    }
}
