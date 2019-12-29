package com.ucar.datalink.manager.core.web.dto.task;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.task.TaskShadowInfo;
import com.ucar.datalink.domain.task.TaskShadowParameter;
import org.apache.commons.lang.StringUtils;

public class TaskShadowView {

    private Long id;
    private Long taskId;
    private String taskName;
    private TaskShadowInfo.State state;
    private String parameter;
    private String note;
    private String createTime;
    private String modifyTime;
    private String mappingIds;
    private String resetTime;
    private transient TaskShadowParameter parameterObj;

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

    public TaskShadowInfo.State getState() {
        return state;
    }

    public void setState(TaskShadowInfo.State state) {
        this.state = state;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
        if (!StringUtils.isEmpty(parameter)) {
            parameterObj = JSONObject.parseObject(parameter, TaskShadowParameter.class);
        } else {
            parameterObj = null;
        }
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public TaskShadowParameter getParameterObj() {
        if (parameterObj == null) {
            throw new DatalinkException("Task-shadow-parameter can not be null.");
        }
        return parameterObj;
    }

    public String getMappingIds() {
        return mappingIds;
    }

    public void setMappingIds(String mappingIds) {
        this.mappingIds = mappingIds;
    }

    public String getResetTime() {
        return resetTime;
    }

    public void setResetTime(String resetTime) {
        this.resetTime = resetTime;
    }

    public static enum State {
        /**
         * 初始化
         */
        INIT,
        /**
         * 执行中
         */
        EXECUTING,
        /**
         * 已完成
         */
        COMPLETE,
        /**
         * 已废弃
         */
        DISCARD
    }

}
