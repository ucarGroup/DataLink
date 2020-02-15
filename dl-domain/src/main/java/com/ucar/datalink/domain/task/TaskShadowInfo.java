package com.ucar.datalink.domain.task;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.Storable;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by lubiao on 2019/8/5.
 */
@Alias("taskShadow")
public class TaskShadowInfo implements Serializable, Storable {
    private Long id;
    private Long taskId;
    private State state;
    private String parameter;
    private String note;
    private Date createTime;
    private Date modifyTime;

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

    public State getState() {
        return state;
    }

    public void setState(State state) {
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


    private transient TaskShadowParameter parameterObj;
    public TaskShadowParameter getParameterObj() {
        if (parameterObj == null) {
            throw new DatalinkException("Task-shadow-parameter can not be null.");
        }
        return parameterObj;
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

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------fields for business------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    private TaskInfo taskInfo;

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }
}
