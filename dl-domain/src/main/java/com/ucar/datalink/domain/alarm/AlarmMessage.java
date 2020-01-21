package com.ucar.datalink.domain.alarm;

import com.ucar.datalink.domain.monitor.MonitorInfo;

import java.util.List;

public class AlarmMessage {

    private MonitorInfo monitorInfo;
    /**
     * 标题
     */
    private String subject;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 接收人
     */
    private List<String> recipient;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public void setRecipient(List<String> recipient) {
        this.recipient = recipient;
    }

    public MonitorInfo getMonitorInfo() {
        return monitorInfo;
    }

    public void setMonitorInfo(MonitorInfo monitorInfo) {
        this.monitorInfo = monitorInfo;
    }
}
