package com.ucar.datalink.domain.media;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.Storable;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * 介质(数据)源数据类
 * <p>
 * Created by lubiao on 2017/2/28.
 */
@Alias("mediaSource")
public class MediaSourceInfo implements Serializable, Storable {
    //------------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------fields mapping to database-----------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    private Long id;
    private String name;
    private MediaSourceType type;
    private String desc;
    private String parameter;
    private Date createTime;
    private Date modifyTime;
    private Long labId;
    private String labName;
    private MediaSourceType simulateMsType;

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------getter&setter methods for database fields-------------------------------------
    //------------------------------------------------------------------------------------------------------------------

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

    public MediaSourceType getType() {
        return type;
    }

    public void setType(MediaSourceType type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
        if (!StringUtils.isEmpty(parameter)) {
            parameterObj = JSONObject.parseObject(parameter, MediaSrcParameter.class);
        } else {
            parameterObj = null;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MediaSourceInfo that = (MediaSourceInfo) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------fields for business------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private transient MediaSrcParameter parameterObj;

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------business methods---------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public <T extends MediaSrcParameter> T getParameterObj() {
        if (parameterObj == null) {
            throw new DatalinkException("MediaSrcParameter can not be null.");
        }
        return (T) parameterObj;
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

    public MediaSourceType getSimulateMsType() {
        return simulateMsType;
    }

    public void setSimulateMsType(MediaSourceType simulateMsType) {
        this.simulateMsType = simulateMsType;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MediaSourceInfo{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", type=").append(type);
        sb.append(", desc='").append(desc).append('\'');
        sb.append(", parameter='").append(parameter).append('\'');
        sb.append(", createTime=").append(createTime);
        sb.append(", modifyTime=").append(modifyTime);
        sb.append(", labId=").append(labId);
        sb.append(", labName='").append(labName).append('\'');
        sb.append(", simulateMsType=").append(simulateMsType);
        sb.append('}');
        return sb.toString();
    }
}
