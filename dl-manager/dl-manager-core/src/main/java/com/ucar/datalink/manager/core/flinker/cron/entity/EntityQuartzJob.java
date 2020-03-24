package com.ucar.datalink.manager.core.flinker.cron.entity;

import com.ucar.datalink.manager.core.flinker.cron.QuartzJob;

/**
 * Created by yang.wang09 on 2019-02-13 15:35.
 */
public class EntityQuartzJob extends QuartzJob {
    private Long jobConfigId;

    private String md5;

    private Integer retryNumber;

    private Integer retryInterval;

    private Long maxRuntime;

    private Long executeId;

    public Long getJobConfigId() {
        return jobConfigId;
    }

    public void setJobConfigId(Long jobConfigId) {
        this.jobConfigId = jobConfigId;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Integer getRetryNumber() {
        return retryNumber;
    }

    public void setRetryNumber(Integer retryNumber) {
        this.retryNumber = retryNumber;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Long getMaxRuntime() {
        return maxRuntime;
    }

    public void setMaxRuntime(Long maxRuntime) {
        this.maxRuntime = maxRuntime;
    }

    public Long getExecuteId() {
        return executeId;
    }

    public void setExecuteId(Long executeId) {
        this.executeId = executeId;
    }
}
