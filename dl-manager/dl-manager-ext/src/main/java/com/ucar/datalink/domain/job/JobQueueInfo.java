package com.ucar.datalink.domain.job;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by yang.wang09 on 2019-04-02 15:36.
 */

@Alias("jobQueueInfo")
public class JobQueueInfo implements Serializable, Storable {

    private Long id;

    private String queueName;

    private String mail;

    private String queueState = "EXECUTE";

    private String failToStop;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 修改时间
     */
    private Timestamp modifyTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getQueueState() {
        return queueState;
    }

    public void setQueueState(String queueState) {
        this.queueState = queueState;
    }

    public String getFailToStop() {
        return failToStop;
    }

    public void setFailToStop(String failToStop) {
        this.failToStop = failToStop;
    }

    @Override
    public String toString() {
        return "JobQueueInfo{" +
                "id=" + id +
                ", queueName='" + queueName + '\'' +
                ", mail='" + mail + '\'' +
                ", queueState='" + queueState + '\'' +
                '}';
    }
}
