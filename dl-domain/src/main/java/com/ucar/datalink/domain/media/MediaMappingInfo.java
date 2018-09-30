package com.ucar.datalink.domain.media;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.domain.Storable;
import com.ucar.datalink.domain.task.TaskInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 介质同步映射类，定义同步行为.
 * <p>
 * Created by user on 2017/2/28.
 */
@Alias("mediaMapping")
public class MediaMappingInfo implements Serializable, Storable {

    //------------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------fields mapping to database-----------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private Long id;
    private Long taskId;
    private Long sourceMediaId;
    private Long targetMediaSourceId;
    private String targetMediaName;
    private String targetMediaNamespace;
    private String parameter = "{}";
    private ColumnMappingMode columnMappingMode;
    private Long writePriority;
    private boolean valid;
    private Long interceptorId;
    private String joinColumn;
    private boolean esUsePrefix;
    private String geoPositionConf;
    private String skipIds;
    private Date createTime;
    private Date modifyTime;
    //------------------------------------------------------------------------------------------------------------------
    //------------------------------getter&setter methods for database fields-------------------------------------------
    //------------------------------------------------------------------------------------------------------------------


    public ColumnMappingMode getColumnMappingMode() {
        return columnMappingMode;
    }

    public void setColumnMappingMode(ColumnMappingMode columnMappingMode) {
        this.columnMappingMode = columnMappingMode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getSourceMediaId() {
        return sourceMediaId;
    }

    public void setSourceMediaId(Long sourceMediaId) {
        this.sourceMediaId = sourceMediaId;
    }

    public Long getTargetMediaSourceId() {
        return targetMediaSourceId;
    }

    public void setTargetMediaSourceId(Long targetMediaSourceId) {
        this.targetMediaSourceId = targetMediaSourceId;
    }

    public String getTargetMediaName() {
        return targetMediaName;
    }

    public void setTargetMediaName(String targetMediaName) {
        this.targetMediaName = targetMediaName;
    }

    public String getTargetMediaNamespace() {
        return targetMediaNamespace;
    }

    public void setTargetMediaNamespace(String targetMediaNameSpace) {
        this.targetMediaNamespace = targetMediaNameSpace;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
        if (!StringUtils.isBlank(parameter) && !Objects.equals(parameter, "{}")) {
            parameterObj = JSONObject.parseObject(parameter, Object.class);
        } else {
            parameterObj = null;
        }
    }

    public Long getWritePriority() {
        return writePriority;
    }

    public void setWritePriority(Long writePriority) {
        this.writePriority = writePriority;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Long getInterceptorId() {
        return interceptorId;
    }

    public void setInterceptorId(Long interceptorId) {
        this.interceptorId = interceptorId;
    }

    public String getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(String joinColumn) {
        this.joinColumn = joinColumn;
    }

    public boolean isEsUsePrefix() {
        return esUsePrefix;
    }

    public void setEsUsePrefix(boolean esUsePrefix) {
        this.esUsePrefix = esUsePrefix;
    }

    public String getGeoPositionConf() {
        return geoPositionConf;
    }

    public void setGeoPositionConf(String geoPositionConf) {
        this.geoPositionConf = geoPositionConf;
        if (!StringUtils.isEmpty(geoPositionConf)) {
            this.geoPositionMappings = JSONObject.parseArray(geoPositionConf, GeoPositionMapping.class);
        } else {
            this.geoPositionMappings = null;
        }
    }

    public String getSkipIds() {
        return skipIds;
    }

    public void setSkipIds(String skipIds) {
        this.skipIds = skipIds;
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


    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------fields for business------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private MediaInfo sourceMedia;
    private MediaSourceInfo targetMediaSource;
    private List<MediaColumnMappingInfo> columnMappings = new ArrayList<>(0);
    private TaskInfo taskInfo;
    private List<GeoPositionMapping> geoPositionMappings;
    private transient Object parameterObj;

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------business methods---------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    public MediaInfo getSourceMedia() {
        return sourceMedia;
    }

    public void setSourceMedia(MediaInfo sourceMedia) {
        this.sourceMedia = sourceMedia;
    }

    public MediaSourceInfo getTargetMediaSource() {
        return targetMediaSource;
    }

    public void setTargetMediaSource(MediaSourceInfo targetMediaSource) {
        this.targetMediaSource = targetMediaSource;
    }

    public List<MediaColumnMappingInfo> getColumnMappings() {
        return columnMappings;
    }

    public void setColumnMappings(List<MediaColumnMappingInfo> columnMappings) {
        this.columnMappings = columnMappings;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public List<GeoPositionMapping> getGeoPositionMappings() {
        return geoPositionMappings;
    }

    public <T> T getParameterObj() {
        return (T) parameterObj;
    }

    @Override
    public String toString() {
        return "MediaMappingInfo{" +
                "id=" + id +
                '}';
    }
}
