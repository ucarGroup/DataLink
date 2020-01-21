package com.ucar.datalink.manager.core.web.dto.mediaSource;

import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.kudu.KuduMediaSrcParameter;

import java.util.Date;

/**
 * Created by user on 2017/6/19.
 */
public class KuduMediaSourceView {

    private Long id;
    private String name;
    private String desc;
    private Date createTime;
    private Date modifyTime;
    private KuduMediaSrcParameter kuduMediaSrcParameter = new KuduMediaSrcParameter();
    private Long labId;
    private String labName;

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


    public Long getLabId() {
        return labId;
    }

    public void setLabId(Long labId) {
        this.labId = labId;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public KuduMediaSrcParameter getKuduMediaSrcParameter() {
        return kuduMediaSrcParameter;
    }

    public void setKuduMediaSrcParameter(KuduMediaSrcParameter kuduMediaSrcParameter) {
        this.kuduMediaSrcParameter = kuduMediaSrcParameter;
    }
}
