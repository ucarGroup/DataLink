package com.ucar.datalink.domain.job;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by yang.wang09 on 2018-10-12 10:34.
 */
@Alias("jobexecutionMonitor")
public class JobExecutionMonitor implements Serializable, Storable  {

    private Long executeId;

    private Long jobconfigId;

    private Long resourceId;

    private String jobName;

    private String workerAddress;

    private Timestamp startTime;

    private Timestamp endTime;

    private String exception;

    public Long getJobconfigId() {
        return jobconfigId;
    }

    public void setJobconfigId(Long jobconfigId) {
        this.jobconfigId = jobconfigId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Long getExecuteId() {
        return executeId;
    }

    public void setExecuteId(Long executeId) {
        this.executeId = executeId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getWorkerAddress() {
        return workerAddress;
    }

    public void setWorkerAddress(String workerAddress) {
        this.workerAddress = workerAddress;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "JobExecutionMonitor{" +
                "executeId=" + executeId +
                ", resourceId=" + resourceId +
                ", jobName='" + jobName + '\'' +
                '}';
    }
}
