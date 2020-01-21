package com.ucar.datalink.manager.core.doublecenter;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.Serializable;

public class LabSwitchView implements Serializable {

    private Long id;
    private String version;
    private String businessLine;
    private Integer status;
    private Integer switchProgress;
    private String fromCenter;
    private String targetCenter;
    private Long startTime;
    private Long endTime;

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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
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


}
