package com.ucar.datalink.domain.task;


import com.ucar.datalink.domain.Parameter;

/**
 * Task参数类.
 *
 * Created by lubiao on 2017/2/16.
 */
public class TaskParameter extends Parameter {
    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
