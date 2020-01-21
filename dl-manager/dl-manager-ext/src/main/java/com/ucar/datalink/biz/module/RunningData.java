package com.ucar.datalink.biz.module;

/**
 * Created by user on 2017/8/30.
 */
public class RunningData {

    private Long jobId;
    private String ip;
    private int pid;
    private String jobName;
    private String state;
    private Long jobQueueExecutionId;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getJobQueueExecutionId() {
        return jobQueueExecutionId;
    }

    public void setJobQueueExecutionId(Long jobQueueExecutionId) {
        this.jobQueueExecutionId = jobQueueExecutionId;
    }

    @Override
    public String toString() {
        return "RunningData [jobId=" + jobId + ", ip=" + ip + ", pid=" + pid + ", jobName=" + jobName + ", state=" + state + ", jobQueueExecutionId="
                + jobQueueExecutionId + "]";
    }

}
