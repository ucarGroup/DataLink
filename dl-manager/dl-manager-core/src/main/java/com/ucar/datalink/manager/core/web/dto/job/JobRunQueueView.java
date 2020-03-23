package com.ucar.datalink.manager.core.web.dto.job;

import com.ucar.datalink.domain.job.JobRunQueueState;

/**
 * Created by user on 2018/1/15.
 */
public class JobRunQueueView {

    private long id;

    private String jobIdList;

    private String currentPorcessId = "";

    private String queueState = JobRunQueueState.INIT;

    private int jobCount;

    private int successCount = 0;

    private int failureCount = 0;

    private String createTime;

    private String modifyTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJobIdList() {
        return jobIdList;
    }

    public void setJobIdList(String jobIdList) {
        this.jobIdList = jobIdList;
    }

    public String getCurrentPorcessId() {
        return currentPorcessId;
    }

    public void setCurrentPorcessId(String currentPorcessId) {
        this.currentPorcessId = currentPorcessId;
    }

    public String getQueueState() {
        return queueState;
    }

    public void setQueueState(String queueState) {
        this.queueState = queueState;
    }

    public int getJobCount() {
        return jobCount;
    }

    public void setJobCount(int jobCount) {
        this.jobCount = jobCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }
}
