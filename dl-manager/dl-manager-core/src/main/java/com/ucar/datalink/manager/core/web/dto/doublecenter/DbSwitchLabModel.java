package com.ucar.datalink.manager.core.web.dto.doublecenter;

import java.util.Date;
import java.util.List;

public class DbSwitchLabModel{

    private String batchId;
    private Integer currentCenter;
    private Integer targetCenter;
    private String environment;//该字段用不着
    private String switchStartTime;
    private Date switchStartTimeDate;//date
    private String token;
    private List<DbInfo> dbList;

    //sddl
    private String groupName;
    private String productName;

    private Long currentTime;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Integer getCurrentCenter() {
        return currentCenter;
    }

    public void setCurrentCenter(Integer currentCenter) {
        this.currentCenter = currentCenter;
    }

    public Integer getTargetCenter() {
        return targetCenter;
    }

    public void setTargetCenter(Integer targetCenter) {
        this.targetCenter = targetCenter;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getSwitchStartTime() {
        return switchStartTime;
    }

    public void setSwitchStartTime(String switchStartTime) {
        this.switchStartTime = switchStartTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<DbInfo> getDbList() {
        return dbList;
    }

    public void setDbList(List<DbInfo> dbList) {
        this.dbList = dbList;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getSwitchStartTimeDate() {
        return switchStartTimeDate;
    }

    public void setSwitchStartTimeDate(Date switchStartTimeDate) {
        this.switchStartTimeDate = switchStartTimeDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }
}
