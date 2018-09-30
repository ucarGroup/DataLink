package com.ucar.datalink.manager.core.web.dto.mediaMapping;

import com.ucar.datalink.domain.media.ColumnMappingMode;

import java.util.Date;

/**
 * Created by csf on 17/4/16.
 */
public class MediaMappingView {

    private Long id;
    private Long taskId;
    private String taskName;
    //source
    private Long srcMediaId;
    private String srcMediaName;
    private Long srcMediaSourceId;
    private String srcMediaNamespace;
    private String srcMediaSourceName;
    //target
    private Long targetMediaSourceId;
    private String targetMediaName;
    private String targetMediaNamespace;
    private String targetMediaSourceName;
    //other
    private String parameter;
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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getSrcMediaSourceId() {
        return srcMediaSourceId;
    }

    public void setSrcMediaSourceId(Long srcMediaSourceId) {
        this.srcMediaSourceId = srcMediaSourceId;
    }

    public String getSrcMediaName() {
        return srcMediaName;
    }

    public void setSrcMediaName(String srcMediaName) {
        this.srcMediaName = srcMediaName;
    }

    public String getSrcMediaNamespace() {
        return srcMediaNamespace;
    }

    public void setSrcMediaNamespace(String srcMediaNamespace) {
        this.srcMediaNamespace = srcMediaNamespace;
    }

    public String getSrcMediaSourceName() {
        return srcMediaSourceName;
    }

    public void setSrcMediaSourceName(String srcMediaSourceName) {
        this.srcMediaSourceName = srcMediaSourceName;
    }

    public String getTargetMediaSourceName() {
        return targetMediaSourceName;
    }

    public void setTargetMediaSourceName(String targetMediaSourceName) {
        this.targetMediaSourceName = targetMediaSourceName;
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

    public Long getSrcMediaId() {
        return srcMediaId;
    }

    public void setSrcMediaId(Long srcMediaId) {
        this.srcMediaId = srcMediaId;
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

    public void setTargetMediaNamespace(String targetMediaNamespace) {
        this.targetMediaNamespace = targetMediaNamespace;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public ColumnMappingMode getColumnMappingMode() {
        return columnMappingMode;
    }

    public void setColumnMappingMode(ColumnMappingMode columnMappingMode) {
        this.columnMappingMode = columnMappingMode;
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
}
