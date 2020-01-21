package com.ucar.datalink.manager.core.web.dto.mediaSource;

import com.ucar.datalink.domain.media.parameter.fq.FqMediaSrcParameter;

import java.util.Date;

/**
 * Created by sqq on 2017/5/16.
 */
public class FqMediaSourceView {
    private Long id;
    private String name;
    private String desc;
    private Date createTime;
    private Date modifyTime;
    private FqMediaSrcParameter fqMediaSrcParameter = new FqMediaSrcParameter();
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

    public FqMediaSrcParameter getFqMediaSrcParameter() {
        return fqMediaSrcParameter;
    }

    public void setFqMediaSrcParameter(FqMediaSrcParameter fqMediaSrcParameter) {
        this.fqMediaSrcParameter = fqMediaSrcParameter;
    }

    public String getZkMediaSourceName() {
        return zkMediaSourceName;
    }

    public void setZkMediaSourceName(String zkMediaSourceName) {
        this.zkMediaSourceName = zkMediaSourceName;
    }
}
