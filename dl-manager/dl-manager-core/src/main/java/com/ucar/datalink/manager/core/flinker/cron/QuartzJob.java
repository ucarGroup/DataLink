package com.ucar.datalink.manager.core.flinker.cron;

/**
 * Created by yang.wang09 on 2018-07-25 10:38.
 */
public class QuartzJob {
    /**
     * 任务ID
     */
    protected Integer jobId;

    /**
     * 任务名称
     */
    protected String jobName;

    /**
     * 任务分组
     */
    protected String jobGroup;

    /**
     * 任务状态 0禁用 1启用 2删除
     */
    protected Integer jobStatus;

    /**
     * 任务运行时间表达式
     */
    protected String cronExpression;

    protected Long maxRuntime;


    /**
     * @return the jobId
     */
    public Integer getJobId() {
        return jobId;
    }

    /**
     * @param jobId
     *            the jobId to set
     */
    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    /**
     * @return the jobName
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * @param jobName
     *            the jobName to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * @return the jobGroup
     */
    public String getJobGroup() {
        return jobGroup;
    }

    /**
     * @param jobGroup
     *            the jobGroup to set
     */
    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    /**
     * @return the jobStatus
     */
    public Integer getJobStatus() {
        return jobStatus;
    }

    /**
     * @param jobStatus
     *            the jobStatus to set
     */
    public void setJobStatus(Integer jobStatus) {
        this.jobStatus = jobStatus;
    }

    /**
     * @return the cronExpression
     */
    public String getCronExpression() {
        return cronExpression;
    }

    /**
     * @param cronExpression
     *            the cronExpression to set
     */
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Long getMaxRuntime() {
        return maxRuntime;
    }

    public void setMaxRuntime(Long maxRuntime) {
        this.maxRuntime = maxRuntime;
    }


    @Override
    public String toString() {
        return "QuartzJob{" +
                "jobId=" + jobId +
                ", jobName='" + jobName + '\'' +
                ", jobGroup='" + jobGroup + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                '}';
    }
}