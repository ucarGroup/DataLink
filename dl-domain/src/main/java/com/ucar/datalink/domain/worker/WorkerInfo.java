package com.ucar.datalink.domain.worker;

import java.util.Date;

/**
 * Created by lubiao on 2017/1/16.
 */
public class WorkerInfo {
    private Long id;
    private String workerName;
    private String workerDesc;
    private String workerAddress;
    private Long groupId;
    private String groupName;
    private Integer restPort;
    private Date createTime;
    private Date modifyTime;

    private String javaopts;
    private Long labId;
    private String labName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerDesc() {
        return workerDesc;
    }

    public void setWorkerDesc(String workerDesc) {
        this.workerDesc = workerDesc;
    }

    public String getWorkerAddress() {
        return workerAddress;
    }

    public void setWorkerAddress(String workerAddress) {
        this.workerAddress = workerAddress;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getRestPort() {
        return restPort;
    }

    public void setRestPort(Integer restPort) {
        this.restPort = restPort;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getJavaopts() {
        return javaopts;
    }

    public void setJavaopts(String javaopts) {
        this.javaopts = javaopts;
    }

    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WorkerInfo{");
        sb.append("id=").append(id);
        sb.append(", workerName='").append(workerName).append('\'');
        sb.append(", workerDesc='").append(workerDesc).append('\'');
        sb.append(", workerAddress='").append(workerAddress).append('\'');
        sb.append(", groupId=").append(groupId);
        sb.append(", groupName='").append(groupName).append('\'');
        sb.append(", restPort=").append(restPort);
        sb.append(", createTime=").append(createTime);
        sb.append(", modifyTime=").append(modifyTime);
        sb.append(", javaopts='").append(javaopts).append('\'');
        sb.append(", labId=").append(labId);
        sb.append(", labName='").append(labName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
