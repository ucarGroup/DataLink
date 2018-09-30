package com.ucar.datalink.common.errors;

/**
 * Created by lubiao on 2017/2/17.
 */
public class TaskExecuteException extends DatalinkException{
    private String taskId;

    public TaskExecuteException(String message, Throwable cause, String taskId) {
        super(message, cause);
        this.taskId = taskId;
    }

    public TaskExecuteException(String message, String taskId) {
        super(message);
        this.taskId = taskId;
    }

    public TaskExecuteException(Throwable cause, String taskId) {
        super(cause);
        this.taskId = taskId;
    }

    public TaskExecuteException(String taskId) {
        this.taskId = taskId;
    }
}
