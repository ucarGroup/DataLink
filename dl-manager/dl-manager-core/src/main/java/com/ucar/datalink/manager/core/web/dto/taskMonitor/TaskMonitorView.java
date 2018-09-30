package com.ucar.datalink.manager.core.web.dto.taskMonitor;

import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskStatus;

/**
 * Created by csf on 17/5/1.
 */
public class TaskMonitorView {

    private Long taskId;

    private String taskName;

    private Long delayTime = 0L;

    private String exception="";

    private Long exceptionId;

    private Long groupId;

    private TargetState targetState;

    private TaskStatus.State listenedState = TaskStatus.State.UNASSIGNED;//Task的实际运行状态,默认值设置为UNASSIGNED

    private Long workerId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Long getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(Long exceptionId) {
        this.exceptionId = exceptionId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public TargetState getTargetState() {
        return targetState;
    }

    public void setTargetState(TargetState targetState) {
        this.targetState = targetState;
    }

    public TaskStatus.State getListenedState() {
        return listenedState;
    }

    public void setListenedState(TaskStatus.State listenedState) {
        this.listenedState = listenedState;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    @Override
    public String toString() {
        return "TaskMonitorView{" +
                ", taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", delayTime=" + delayTime +
                ", exception='" + exception + '\'' +
                ", exceptionId=" + exceptionId +
                ", groupId=" + groupId +
                ", targetState=" + targetState +
                ", listenedState=" + listenedState +
                ", workerId=" + workerId +
                '}';
    }
}
