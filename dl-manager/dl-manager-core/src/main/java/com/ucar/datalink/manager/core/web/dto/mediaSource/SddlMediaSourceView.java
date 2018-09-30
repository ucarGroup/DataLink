package com.ucar.datalink.manager.core.web.dto.mediaSource;

import java.util.Date;

/**
 * Created by csf on 17/5/23.
 */
public class SddlMediaSourceView {

    private Long id;

    private String sddlDesc;

    private String sddlName;

    private String primaryRdbId;

    private String secondaryRdbId;

    private String proxyDbId;

    private Date createTime;

    private Date modifyTime;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSddlDesc() {
        return sddlDesc;
    }

    public void setSddlDesc(String sddlDesc) {
        this.sddlDesc = sddlDesc;
    }

    public String getPrimaryRdbId() {
        return primaryRdbId;
    }

    public void setPrimaryRdbId(String primaryRdbId) {
        this.primaryRdbId = primaryRdbId;
    }

    public String getSecondaryRdbId() {
        return secondaryRdbId;
    }

    public void setSecondaryRdbId(String secondaryRdbId) {
        this.secondaryRdbId = secondaryRdbId;
    }

    public String getProxyDbId() {
        return proxyDbId;
    }

    public void setProxyDbId(String proxyDbId) {
        this.proxyDbId = proxyDbId;
    }

    public String getSddlName() {
        return sddlName;
    }

    public void setSddlName(String sddlName) {
        this.sddlName = sddlName;
    }

}
