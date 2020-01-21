package com.ucar.datalink.manager.core.web.dto.mediaSource;


import com.ucar.datalink.domain.media.parameter.dove.DoveMediaSrcParameter;

import java.util.Date;

/**
 * Created by liuyifan
 */
public class DoveMediaSourceView {
    private Long id;
    private String name;
    private String desc;
    private Date createTime;
    private Date modifyTime;
    private DoveMediaSrcParameter doveMediaSrcParameter = new DoveMediaSrcParameter();
    private String zkMediaSourceName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public DoveMediaSrcParameter getDoveMediaSrcParameter() {
        return doveMediaSrcParameter;
    }

    public void setDoveMediaSrcParameter(DoveMediaSrcParameter doveMediaSrcParameter) {
        this.doveMediaSrcParameter = doveMediaSrcParameter;
    }

    public String getZkMediaSourceName() {
        return zkMediaSourceName;
    }

    public void setZkMediaSourceName(String zkMediaSourceName) {
        this.zkMediaSourceName = zkMediaSourceName;
    }
}
