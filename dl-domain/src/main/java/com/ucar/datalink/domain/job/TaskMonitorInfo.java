package com.ucar.datalink.domain.job;

/**
 * Created by user on 2017/11/6.
 */
public class TaskMonitorInfo {

    private String id;

    private String delayTime ="0";

    private String taskId;

    private String isErr = "false";

    private String taskName;

    private String currentBinlogTimeStamp = "";

    private String schema;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(String delayTime) {
        this.delayTime = delayTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getIsErr() {
        return isErr;
    }

    public void setIsErr(String isErr) {
        this.isErr = isErr;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getCurrentBinlogTimeStamp() {
        return currentBinlogTimeStamp;
    }

    public void setCurrentBinlogTimeStamp(String currentBinlogTimeStamp) {
        this.currentBinlogTimeStamp = currentBinlogTimeStamp;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
