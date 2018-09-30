package com.ucar.datalink.manager.core.web.dto.mediaSource;

import com.ucar.datalink.domain.media.parameter.es.EsMediaSrcParameter;

import java.util.Date;

/**
 * Created by lubiao on 2017/6/16.
 */
public class EsMediaSourceView {
    private Long id;
    private String name;
    private String desc;
    private Date createTime;
    private Date modifyTime;
    private EsMediaSrcParameter esMediaSrcParameter = new EsMediaSrcParameter();

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

    public EsMediaSrcParameter getEsMediaSrcParameter() {
        return esMediaSrcParameter;
    }

    public void setEsMediaSrcParameter(EsMediaSrcParameter esMediaSrcParameter) {
        this.esMediaSrcParameter = esMediaSrcParameter;
    }
}
