package com.ucar.datalink.biz.utils.flinker.module;

import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.TaskMonitorInfo;

import java.util.List;

/**
 * Created by user on 2017/10/19.
 */
public class JobRestResponse {

    public static final String SUCCESS = "SUCCESS";

    public static final String FAILURED = "FAILED";

    /**
     * 返回的信息，若成功则显示为SUCCESS，否则表示有异常，显示异常内容
     */
    private String msg;

    private String executId;

    private List<JobExecutionInfo> history;

    private JobExecutionInfo state;

    private List<TaskMonitorInfo> task;


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getExecutId() {
        return executId;
    }

    public void setExecutId(String executId) {
        this.executId = executId;
    }

    public List<JobExecutionInfo> getHistory() {
        return history;
    }

    public void setHistory(List<JobExecutionInfo> history) {
        this.history = history;
    }

    public JobExecutionInfo getState() {
        return state;
    }

    public void setState(JobExecutionInfo state) {
        this.state = state;
    }

    public List<TaskMonitorInfo> getTask() {
        return task;
    }

    public void setTask(List<TaskMonitorInfo> task) {
        this.task = task;
    }
}
