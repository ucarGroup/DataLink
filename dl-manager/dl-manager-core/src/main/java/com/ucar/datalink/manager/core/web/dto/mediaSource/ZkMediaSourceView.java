package com.ucar.datalink.manager.core.web.dto.mediaSource;

import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;

import java.util.Date;

/**
 * Created by sqq on 2017/5/16.
 */
public class ZkMediaSourceView {
    private Long id;
    private String name;
    private String desc;
    private Date createTime;
    private Date modifyTime;
    private ZkMediaSrcParameter zkMediaSrcParameter = new ZkMediaSrcParameter();

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

    public ZkMediaSrcParameter getZkMediaSrcParameter() {
        return zkMediaSrcParameter;
    }

    public void setZkMediaSrcParameter(ZkMediaSrcParameter zkMediaSrcParameter) {
        this.zkMediaSrcParameter = zkMediaSrcParameter;
    }
}
