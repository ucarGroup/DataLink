package com.ucar.datalink.flinker.api.base;

import java.util.Map;

/**
 * Created by csf on 16/12/16.
 */
public class TaskInfo {

    private int taskId;
    /**
     * 所有的数值key-value对 *
     */
    private Map<String, Number> taskInfo;

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Map<String, Number> getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(Map<String, Number> taskInfo) {
        this.taskInfo = taskInfo;
    }

}
