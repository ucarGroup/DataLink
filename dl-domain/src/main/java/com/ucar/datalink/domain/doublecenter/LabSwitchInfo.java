package com.ucar.datalink.domain.doublecenter;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

@Alias("labSwitch")
public class LabSwitchInfo implements Serializable, Storable {

    private Long id;
    private String version;
    private String businessLine;
    private Integer status;
    private Integer switchProgress;
    private String fromCenter;
    private String targetCenter;
    private Date startTime;
    private Date endTime;
    private String virtualMsIds;
    private String originalContent;
    private Date switchStartTime;

    //仅用于页面显示
    private String statusName;
    private String switchProgressName;
    private String labSwitchException;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBusinessLine() {
        return businessLine;
    }

    public void setBusinessLine(String businessLine) {
        this.businessLine = businessLine;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSwitchProgress() {
        return switchProgress;
    }

    public void setSwitchProgress(Integer switchProgress) {
        this.switchProgress = switchProgress;
    }

    public String getFromCenter() {
        return fromCenter;
    }

    public void setFromCenter(String fromCenter) {
        this.fromCenter = fromCenter;
    }

    public String getTargetCenter() {
        return targetCenter;
    }

    public void setTargetCenter(String targetCenter) {
        this.targetCenter = targetCenter;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getSwitchProgressName() {
        return switchProgressName;
    }

    public void setSwitchProgressName(String switchProgressName) {
        this.switchProgressName = switchProgressName;
    }

    public String getLabSwitchException() {
        return labSwitchException;
    }

    public void setLabSwitchException(String labSwitchException) {
        this.labSwitchException = labSwitchException;
    }

    public String getVirtualMsIds() {
        return virtualMsIds;
    }

    public void setVirtualMsIds(String virtualMsIds) {
        this.virtualMsIds = virtualMsIds;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public Date getSwitchStartTime() {
        return switchStartTime;
    }

    public void setSwitchStartTime(Date switchStartTime) {
        this.switchStartTime = switchStartTime;
    }
}
