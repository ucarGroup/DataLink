package com.ucar.datalink.domain.lab;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by djj on 2018/8/15.
 */
@Alias("lab")
public class LabInfo implements Serializable, Storable {

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
        final StringBuffer sb = new StringBuffer("LabInfo{");
        sb.append("id=").append(id);
        sb.append(", labName='").append(labName).append('\'');
        sb.append(", labDesc='").append(labDesc).append('\'');
        sb.append(", ipRule='").append(ipRule).append('\'');
        sb.append(", createTime=").append(createTime);
        sb.append(", modifyTime=").append(modifyTime);
        sb.append(", dataSource='").append(dataSource).append('\'');
        sb.append(", dbIpPort='").append(dbIpPort).append('\'');
        sb.append(", isCenterLab=").append(isCenterLab);
        sb.append('}');
        return sb.toString();
    }
}
