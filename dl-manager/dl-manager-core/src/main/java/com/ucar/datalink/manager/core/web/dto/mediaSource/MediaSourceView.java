package com.ucar.datalink.manager.core.web.dto.mediaSource;

import com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;

import java.util.Date;

/**
 * Created by csf on 17/4/7.
 */
public class MediaSourceView{

    private Long id;
    private String name;
    private Date createTime;
    private Date modifyTime;
    private RdbMediaSrcParameter rdbMediaSrcParameter = new RdbMediaSrcParameter();
    private BasicDataSourceConfig basicDataSourceConfig = new BasicDataSourceConfig();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RdbMediaSrcParameter getRdbMediaSrcParameter() {
        return rdbMediaSrcParameter;
    }

    public void setRdbMediaSrcParameter(RdbMediaSrcParameter rdbMediaSrcParameter) {
        this.rdbMediaSrcParameter = rdbMediaSrcParameter;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BasicDataSourceConfig getBasicDataSourceConfig() {
        return basicDataSourceConfig;
    }

    public void setBasicDataSourceConfig(BasicDataSourceConfig basicDataSourceConfig) {
        this.basicDataSourceConfig = basicDataSourceConfig;
    }
}
