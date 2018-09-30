package com.ucar.datalink.manager.core.web.dto.mediaSource;

import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;

import java.util.Date;

/**
 * Created by user on 2017/6/19.
 */
public class HBaseMediaSourceView {

    private Long id;
    private String name;
    private String desc;
    private Date createTime;
    private Date modifyTime;
    private HBaseMediaSrcParameter hbaseMediaSrcParameter = new HBaseMediaSrcParameter();
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

    public HBaseMediaSrcParameter getHbaseMediaSrcParameter() {
        return hbaseMediaSrcParameter;
    }

    public void setHbaseMediaSrcParameter(HBaseMediaSrcParameter hbaseMediaSrcParameter) {
        this.hbaseMediaSrcParameter = hbaseMediaSrcParameter;
    }

    public String getZkMediaSourceName() {
        return zkMediaSourceName;
    }

    public void setZkMediaSourceName(String zkMediaSourceName) {
        this.zkMediaSourceName = zkMediaSourceName;
    }
}
