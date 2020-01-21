package com.ucar.datalink.manager.core.web.dto.mediaSource;

import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.virtual.VirtualMediaSrcParameter;

import java.util.Date;

/**
 * Created by user on 2018/8/19.
 */
public class VirtualMediaSourceView {

    private Long id;
    private String name;
    private String desc;
    private Date createTime;
    private Date modifyTime;
    private VirtualMediaSrcParameter mediaSrcParameter = new VirtualMediaSrcParameter();
    private MediaSourceType simulateMsType;

    private String realDbIds;
    private String realDbNames;

    private String currentLab;

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

    public VirtualMediaSrcParameter getMediaSrcParameter() {
        return mediaSrcParameter;
    }

    public void setMediaSrcParameter(VirtualMediaSrcParameter mediaSrcParameter) {
        this.mediaSrcParameter = mediaSrcParameter;
    }

    public MediaSourceType getSimulateMsType() {
        return simulateMsType;
    }

    public void setSimulateMsType(MediaSourceType simulateMsType) {
        this.simulateMsType = simulateMsType;
    }

    public String getRealDbIds() {
        return realDbIds;
    }

    public void setRealDbIds(String realDbIds) {
        this.realDbIds = realDbIds;
    }

    public String getRealDbNames() {
        return realDbNames;
    }

    public void setRealDbNames(String realDbNames) {
        this.realDbNames = realDbNames;
    }

    public String getCurrentLab() {
        return currentLab;
    }

    public void setCurrentLab(String currentLab) {
        this.currentLab = currentLab;
    }
}
