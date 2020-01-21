package com.ucar.datalink.domain.media.parameter.sddl;

import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lubiao on 2017/3/20.
 */
public class SddlMediaSrcParameter extends MediaSrcParameter {
    private List<Long> primaryDbsId = new ArrayList<>();
    private List<Long> secondaryDbsId = new ArrayList<>();
    private Long proxyDbId;

    // ----------sddl_writer-----------
    private String businessName; // 产品线名称BusinessLine
    private String projectName;  // 配置中心里的项目名称
    private String cfProjectName = "sddladmin";// 默认取sddladmin
    private String cfKey;
    private String serverDomain;

    public void setProxyDbId(Long proxyDbId) {
        this.proxyDbId = proxyDbId;
    }

    public Long getProxyDbId() {
        return proxyDbId;
    }

    public List<Long> getPrimaryDbsId() {
        return primaryDbsId;
    }

    public void setPrimaryDbsId(List<Long> primaryDbsId) {
        this.primaryDbsId = primaryDbsId;
    }

    public List<Long> getSecondaryDbsId() {
        return secondaryDbsId;
    }

    public void setSecondaryDbsId(List<Long> secondaryDbsId) {
        this.secondaryDbsId = secondaryDbsId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCfProjectName() {
        return cfProjectName;
    }

    public void setCfProjectName(String cfProjectName) {
        this.cfProjectName = cfProjectName;
    }

    public String getCfKey() {
        return cfKey;
    }

    public void setCfKey(String cfKey) {
        this.cfKey = cfKey;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }
}
