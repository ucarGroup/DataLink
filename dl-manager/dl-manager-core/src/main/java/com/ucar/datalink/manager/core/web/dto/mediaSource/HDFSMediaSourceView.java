package com.ucar.datalink.manager.core.web.dto.mediaSource;

import com.ucar.datalink.domain.media.parameter.hdfs.HDFSMediaSrcParameter;

import java.util.Date;

/**
 * Created by sqq on 2017/6/16.
 */
public class HDFSMediaSourceView {
    private Long id;
    private String name;
    private String desc;
    private Date createTime;
    private Date modifyTime;
    private HDFSMediaSrcParameter hdfsMediaSrcParameter = new HDFSMediaSrcParameter();
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

    public HDFSMediaSrcParameter getHdfsMediaSrcParameter() {
        return hdfsMediaSrcParameter;
    }

    public void setHdfsMediaSrcParameter(HDFSMediaSrcParameter hdfsMediaSrcParameter) {
        this.hdfsMediaSrcParameter = hdfsMediaSrcParameter;
    }

    public String getZkMediaSourceName() {
        return zkMediaSourceName;
    }

    public void setZkMediaSourceName(String zkMediaSourceName) {
        this.zkMediaSourceName = zkMediaSourceName;
    }
}
