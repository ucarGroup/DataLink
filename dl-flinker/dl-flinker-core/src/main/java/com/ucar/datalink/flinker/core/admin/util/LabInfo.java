package com.ucar.datalink.flinker.core.admin.util;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yang.wang09 on 2018-11-16 13:39.
 */
public class LabInfo implements Serializable {

    private Long id;
    private String labName;
    private String labDesc;
    private String ipRule;
    private Date createTime;
    private Date modifyTime;
    private String dataSource;
    private String dbIpPort;
    //页面展示用
    private Boolean isCenterLab;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getLabDesc() {
        return labDesc;
    }

    public void setLabDesc(String labDesc) {
        this.labDesc = labDesc;
    }

    public String getIpRule() {
        return ipRule;
    }

    public void setIpRule(String ipRule) {
        this.ipRule = ipRule;
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

    public Boolean getIsCenterLab() {
        return isCenterLab;
    }

    public void setIsCenterLab(Boolean isCenterLab) {
        this.isCenterLab = isCenterLab;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDbIpPort() {
        return dbIpPort;
    }

    public void setDbIpPort(String dbIpPort) {
        this.dbIpPort = dbIpPort;
    }

    @Override
    public String toString() {
        return "LabInfo{" +
                "id=" + id +
                ", labName='" + labName + '\'' +
                ", labDesc='" + labDesc + '\'' +
                ", ipRule='" + ipRule + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", dataSource='" + dataSource + '\'' +
                ", dbIpPort='" + dbIpPort + '\'' +
                ", isCenterLab=" + isCenterLab +
                '}';
    }
}
