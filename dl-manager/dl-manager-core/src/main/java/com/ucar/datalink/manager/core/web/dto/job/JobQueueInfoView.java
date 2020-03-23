package com.ucar.datalink.manager.core.web.dto.job;

/**
 * Created by yang.wang09 on 2019-04-03 20:36.
 */
public class JobQueueInfoView {

    private String id;

    private String queueName;

    private String mail;

    private String queueState;

    private String createTime;

    private String modifyTime;

    private String failToStop;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getQueueState() {
        return queueState;
    }

    public void setQueueState(String queueState) {
        this.queueState = queueState;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getFailToStop() {
        return failToStop;
    }

    public void setFailToStop(String failToStop) {
        this.failToStop = failToStop;
    }
}
