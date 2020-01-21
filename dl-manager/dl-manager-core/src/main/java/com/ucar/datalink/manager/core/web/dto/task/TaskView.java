package com.ucar.datalink.manager.core.web.dto.task;

import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskStatus;

/**
 * Task视图类.
 * Created by lubiao on 2017/2/22.
 */
public class TaskView {
    private Long id;
    private String taskName;
    private String taskDesc;
    private TargetState targetState;
    private Long groupId;
    private Long workerId;//当前运行该Task的WorkerId
    private TaskStatus.State listenedState = TaskStatus.State.UNASSIGNED;//Task的实际运行状态,默认值设置为UNASSIGNED
    private String currentTimeStamp = "NULL";
    private Long currentLogPosition;
    private String currentLogFile = "NULL";
    private String startTime;//task启动时间
    private String readerIp;//reader地址
    private String detail;//详情
    private String labName;
    private String taskSyncMode;

    private String latestEffectSyncLogFileName;
    private String latestEffectSyncLogFileOffset;

    private String taskSyncStatus;

    private String shadowCurrentTimeStamp; //影子位点当前时间
    private String shadowLatestEffectSyncLogFileName;//影子位点最后binlog文件
    private String shadowLatestEffectSyncLogFileOffset;//影子位点最后binlog位点
    private String taskPriorityId;

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

    public TargetState getTargetState() {
        return targetState;
    }

    public void setTargetState(TargetState targetState) {
        this.targetState = targetState;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public TaskStatus.State getListenedState() {
        return listenedState;
    }

    public void setListenedState(TaskStatus.State listenedState) {
        this.listenedState = listenedState;
    }

    public String getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    public void setCurrentTimeStamp(String currentTimeStamp) {
        this.currentTimeStamp = currentTimeStamp;
    }

    public Long getCurrentLogPosition() {
        return currentLogPosition;
    }

    public void setCurrentLogPosition(Long currentLogPosition) {
        this.currentLogPosition = currentLogPosition;
    }

    public String getCurrentLogFile() {
        return currentLogFile;
    }

    public void setCurrentLogFile(String currentLogFile) {
        this.currentLogFile = currentLogFile;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getReaderIp() {
        return readerIp;
    }

    public void setReaderIp(String readerIp) {
        this.readerIp = readerIp;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getLabName() {
        return labName;
    }

    public void setLabName(String labName) {
        this.labName = labName;
    }

    public String getTaskSyncMode() {
        return taskSyncMode;
    }

    public void setTaskSyncMode(String taskSyncMode) {
        this.taskSyncMode = taskSyncMode;
    }

    public String getLatestEffectSyncLogFileName() {
        return latestEffectSyncLogFileName;
    }

    public void setLatestEffectSyncLogFileName(String latestEffectSyncLogFileName) {
        this.latestEffectSyncLogFileName = latestEffectSyncLogFileName;
    }

    public String getLatestEffectSyncLogFileOffset() {
        return latestEffectSyncLogFileOffset;
    }

    public void setLatestEffectSyncLogFileOffset(String latestEffectSyncLogFileOffset) {
        this.latestEffectSyncLogFileOffset = latestEffectSyncLogFileOffset;
    }

    public String getTaskSyncStatus() {
        return taskSyncStatus;
    }

    public void setTaskSyncStatus(String taskSyncStatus) {
        this.taskSyncStatus = taskSyncStatus;
    }

    public String getShadowCurrentTimeStamp() {
        return shadowCurrentTimeStamp;
    }

    public void setShadowCurrentTimeStamp(String shadowCurrentTimeStamp) {
        this.shadowCurrentTimeStamp = shadowCurrentTimeStamp;
    }

    public String getShadowLatestEffectSyncLogFileName() {
        return shadowLatestEffectSyncLogFileName;
    }

    public void setShadowLatestEffectSyncLogFileName(String shadowLatestEffectSyncLogFileName) {
        this.shadowLatestEffectSyncLogFileName = shadowLatestEffectSyncLogFileName;
    }

    public String getTaskPriorityId() {
        return taskPriorityId;
    }

    public void setTaskPriorityId(String taskPriorityId) {
        this.taskPriorityId = taskPriorityId;
    }

    public String getShadowLatestEffectSyncLogFileOffset() {
        return shadowLatestEffectSyncLogFileOffset;
    }

    public void setShadowLatestEffectSyncLogFileOffset(String shadowLatestEffectSyncLogFileOffset) {
        this.shadowLatestEffectSyncLogFileOffset = shadowLatestEffectSyncLogFileOffset;
    }
}
