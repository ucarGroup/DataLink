package com.ucar.datalink.worker.core.runtime;

/**
 * Created by lubiao on 2018/4/24.
 */
public class TaskStatusEvent {
    private String taskId;
    private String taskExecutionId;
    private Long startTime;

    public TaskStatusEvent(String taskId, String taskExecutionId, Long startTime) {
        this.taskId = taskId;
        this.taskExecutionId = taskExecutionId;
        this.startTime = startTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskExecutionId() {
        return taskExecutionId;
    }

    public void setTaskExecutionId(String taskExecutionId) {
        this.taskExecutionId = taskExecutionId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
}
