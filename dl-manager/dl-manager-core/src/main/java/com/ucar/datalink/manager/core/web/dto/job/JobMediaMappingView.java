package com.ucar.datalink.manager.core.web.dto.job;

/**
 * Created by user on 2017/10/25.
 */
public class JobMediaMappingView {
    private long id;

    private String src_media;

    private String target_media;

    private String src_mapping;

    private String target_mapping;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSrc_media() {
        return src_media;
    }

    public void setSrc_media(String src_media) {
        this.src_media = src_media;
    }

    public String getTarget_media() {
        return target_media;
    }

    public void setTarget_media(String target_media) {
        this.target_media = target_media;
    }

    public String getSrc_mapping() {
        return src_mapping;
    }

    public void setSrc_mapping(String src_mapping) {
        this.src_mapping = src_mapping;
    }

    public String getTarget_mapping() {
        return target_mapping;
    }

    public void setTarget_mapping(String target_mapping) {
        this.target_mapping = target_mapping;
    }
}
