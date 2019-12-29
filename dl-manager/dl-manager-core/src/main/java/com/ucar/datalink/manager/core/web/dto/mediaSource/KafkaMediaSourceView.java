package com.ucar.datalink.manager.core.web.dto.mediaSource;

import com.ucar.datalink.domain.media.parameter.kafka.KafkaMediaSrcParameter;

import java.util.Date;

public class KafkaMediaSourceView {
    private Long id;
    private String name;
    private String desc;
    private Date createTime;
    private Date modifyTime;
    private KafkaMediaSrcParameter kafkaMediaSrcParameter = new KafkaMediaSrcParameter();

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

    public KafkaMediaSrcParameter getKafkaMediaSrcParameter() {
        return kafkaMediaSrcParameter;
    }

    public void setKafkaMediaSrcParameter(KafkaMediaSrcParameter kafkaMediaSrcParameter) {
        this.kafkaMediaSrcParameter = kafkaMediaSrcParameter;
    }
}
