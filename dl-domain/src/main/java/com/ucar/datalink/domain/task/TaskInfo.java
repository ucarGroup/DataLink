package com.ucar.datalink.domain.task;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.Storable;
import com.ucar.datalink.domain.plugin.PluginReaderParameter;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Task配置类
 * <p>
 * Created by lubiao on 2016/12/20.
 */
@Alias("task")
public class TaskInfo implements Serializable, Storable {

    //------------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------fields mapping to database-----------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private Long id;
    private String taskName;
    private String taskDesc;
    private String taskParameter;
    private Long readerMediaSourceId;
    private String taskReaderParameter;
    private String taskWriterParameter;
    private TargetState targetState;
    private TaskType taskType;
    private Long groupId;
    private Date createTime;
    private Date modifyTime;
    private boolean isDelete;
    private Long leaderTaskId;
    private boolean isLeaderTask;

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------getter&setter methods for database fields-------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getTaskParameter() {
        return taskParameter;
    }

    public void setTaskParameter(String taskParameter) {
        this.taskParameter = taskParameter;
        if (!StringUtils.isEmpty(taskParameter)) {
            taskParameterObj = JSONObject.parseObject(taskParameter, TaskParameter.class);
            taskParameterObj.setTaskId(id);
        } else {
            taskParameterObj = null;
        }
    }

    public Long getReaderMediaSourceId() {
        return readerMediaSourceId;
    }

    public void setReaderMediaSourceId(Long readerMediaSourceId) {
        this.readerMediaSourceId = readerMediaSourceId;
    }

    public String getTaskReaderParameter() {
        return taskReaderParameter;
    }

    public void setTaskReaderParameter(String taskReaderParameter) {
        this.taskReaderParameter = taskReaderParameter;
        if (!StringUtils.isEmpty(taskReaderParameter)) {
            taskReaderParameterObj = JSONObject.parseObject(taskReaderParameter, PluginReaderParameter.class);
        } else {
            taskReaderParameterObj = null;
        }
    }

    public String getTaskWriterParameter() {
        return taskWriterParameter;
    }

    public void setTaskWriterParameter(String taskWriterParameter) {
        this.taskWriterParameter = taskWriterParameter;
        if (!StringUtils.isEmpty(taskWriterParameter)) {
            taskWriterParameterObjs = JSONObject.parseArray(taskWriterParameter, PluginWriterParameter.class);
        } else {
            taskWriterParameterObjs = null;
        }
    }

    public TargetState getTargetState() {
        return targetState;
    }

    public void setTargetState(TargetState taskState) {
        this.targetState = taskState;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public boolean isDelete() {
        return isDelete;
    }

    public void setIsDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    public Long getLeaderTaskId() {
        return leaderTaskId;
    }

    public void setLeaderTaskId(Long leaderTaskId) {
        this.leaderTaskId = leaderTaskId;
    }

    public boolean isLeaderTask() {
        return isLeaderTask;
    }

    public void setIsLeaderTask(boolean isLeaderTask) {
        this.isLeaderTask = isLeaderTask;
    }

//------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------fields for business------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private transient Long version;//version代表的是整个分组配置的版本
    private transient PluginReaderParameter taskReaderParameterObj;
    private transient List<PluginWriterParameter> taskWriterParameterObjs;
    private transient TaskParameter taskParameterObj;

    //------------------------------------------------------------------------------------------------------------------
    //-----------------------------------------business methods---------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    public String idString() {
        return String.valueOf(id);
    }

    public Long getModifyTimeMillSeconds() {
        return modifyTime.getTime();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public PluginReaderParameter getTaskReaderParameterObj() {
        if (taskReaderParameterObj == null) {
            throw new DatalinkException("Task-reader-parameter can not be null.");
        }
        return taskReaderParameterObj;
    }

    public List<PluginWriterParameter> getTaskWriterParameterObjs() {
        if (taskWriterParameterObjs == null) {
            throw new DatalinkException("Task-writer-parameter can not be null.");
        }
        return taskWriterParameterObjs;
    }

    public TaskParameter getTaskParameterObj() {
        if (taskParameterObj == null) {
            throw new DatalinkException("Task-parameter can not be null.");
        }
        return taskParameterObj;
    }
}
