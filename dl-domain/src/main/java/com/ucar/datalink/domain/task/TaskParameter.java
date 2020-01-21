package com.ucar.datalink.domain.task;


import com.ucar.datalink.domain.Parameter;

/**
 * Task参数类.
 *
 * Created by lubiao on 2017/2/16.
 */
public class TaskParameter extends Parameter {
    private Long taskId;
    private Long sourceLabId;
    private Long targetLabId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getSourceLabId() {
        return sourceLabId;
    }

    public void setSourceLabId(Long sourceLabId) {
        this.sourceLabId = sourceLabId;
    }

    public Long getTargetLabId() {
        return targetLabId;
    }

    public void setTargetLabId(Long targetLabId) {
        this.targetLabId = targetLabId;
    }
}
