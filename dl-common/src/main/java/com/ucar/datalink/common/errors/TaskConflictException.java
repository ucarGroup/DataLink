package com.ucar.datalink.common.errors;

/**
 * Created by lubiao on 2017/5/26.
 */
public class TaskConflictException extends Exception {
    private String taskId;
    private String newWorkerId;
    private String oldWorkerId;
    private String newTaskExecutionId;
    private String oldTaskExecutionId;

    public TaskConflictException(String taskId, String newWorkerId, String oldWorkerId, String newTaskExecutionId, String oldTaskExecutionId) {
        super(String.format(
                        "Task %s has already been assigned to Worker %s with ExecutionId %s ," +
                                "but it still being holden by Worker %s with ExecutionId %s.",
                        taskId,
                        newWorkerId,
                        newTaskExecutionId,
                        oldWorkerId,
                        oldTaskExecutionId)
        );
        this.taskId = taskId;
        this.newWorkerId = newWorkerId;
        this.oldWorkerId = oldWorkerId;
        this.newTaskExecutionId = newTaskExecutionId;
        this.oldTaskExecutionId = oldTaskExecutionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getNewWorkerId() {
        return newWorkerId;
    }

    public String getOldWorkerId() {
        return oldWorkerId;
    }

    public String getNewTaskExecutionId() {
        return newTaskExecutionId;
    }

    public String getOldTaskExecutionId() {
        return oldTaskExecutionId;
    }
}
