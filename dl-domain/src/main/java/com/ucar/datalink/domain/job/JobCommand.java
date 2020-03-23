package com.ucar.datalink.domain.job;

import java.util.Map;

/**
 * Created by user on 2017/9/21.
 */

public class JobCommand {

    private Type type;
    private String jobName;
    private String jvmArgs;
    private boolean forceStop;
    private Long jobId;
    private Long executeId;
    private boolean debug;
    private Long jobQueueExecutionId=-1L;//默认值-1
    private Long timingJobId=-1L;
    private boolean dynamicParam = false;
    private Map<String,String> mapParam;//动态传参

    public boolean isDynamicParam() {
        return dynamicParam;
    }

    public Long getTimingJobId() {
        return timingJobId;
    }

    public void setTimingJobId(Long timingJobId) {
        this.timingJobId = timingJobId;
    }

    public void setDynamicParam(boolean dynamicParam) {
        this.dynamicParam = dynamicParam;
    }

    public Map<String, String> getMapParam() {
        return mapParam;
    }

    public void setMapParam(Map<String, String> mapParam) {
        this.mapParam = mapParam;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String toString() {
        return "Command [type=" + type + ", jobName=" + jobName + "]";
    }

    public String getJvmArgs() {
        return jvmArgs;
    }

    public void setJvmArgs(String jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    public boolean isForceStop() {
        return forceStop;
    }

    public void setForceStop(boolean forceStop) {
        this.forceStop = forceStop;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Long getJobQueueExecutionId() {
        return jobQueueExecutionId;
    }

    public void setJobQueueExecutionId(Long jobQueueExecutionId) {
        this.jobQueueExecutionId = jobQueueExecutionId;
    }

    public Long getExecuteId() {
        return executeId;
    }

    public void setExecuteId(Long executeId) {
        this.executeId = executeId;
    }

    public static enum Type {
        Start, Stop
    }

}
