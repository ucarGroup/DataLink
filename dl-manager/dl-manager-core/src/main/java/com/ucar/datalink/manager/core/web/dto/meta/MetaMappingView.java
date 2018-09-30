package com.ucar.datalink.manager.core.web.dto.meta;

/**
 * Created by user on 2017/11/3.
 */
public class MetaMappingView {

    private long id;

    private String srcMediaSourceType;

    private String targetMediaSourceType;

    private String srcMappingType;

    private String targetMappingType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSrcMediaSourceType() {
        return srcMediaSourceType;
    }

    public void setSrcMediaSourceType(String srcMediaSourceType) {
        this.srcMediaSourceType = srcMediaSourceType;
    }

    public String getTargetMediaSourceType() {
        return targetMediaSourceType;
    }

    public void setTargetMediaSourceType(String targetMediaSourceType) {
        this.targetMediaSourceType = targetMediaSourceType;
    }

    public String getSrcMappingType() {
        return srcMappingType;
    }

    public void setSrcMappingType(String srcMappingType) {
        this.srcMappingType = srcMappingType;
    }

    public String getTargetMappingType() {
        return targetMappingType;
    }

    public void setTargetMappingType(String targetMappingType) {
        this.targetMappingType = targetMappingType;
    }
}
