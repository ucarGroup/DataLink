package com.ucar.datalink.worker.api.probe.index;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Created by lubiao on 17/4/28.
 */
public class TaskExceptionProbeIndex extends BaseTaskProbeIndex {

    private String exceptionInfo;
    /**
     * 标识是否直接发送异常，默认为false
     * 如果为true，则不需要在worker端和manager端缓存，发送一次即可
     * 如果为false，则会在worker端和manager端缓存，直到执行clear操作，才会停止报警
     */
    private boolean sendDirectly;

    public TaskExceptionProbeIndex(Long taskId, Throwable t) {
        super(taskId);
        this.sendDirectly = false;
        this.exceptionInfo = buildExceptionInfo(t);
    }

    public TaskExceptionProbeIndex(Long taskId, String stackTrace) {
        super(taskId);
        this.sendDirectly = false;
        this.exceptionInfo = stackTrace;
    }

    public TaskExceptionProbeIndex(Long taskId, Throwable t, boolean sendDirectly) {
        super(taskId);
        this.sendDirectly = sendDirectly;
        this.exceptionInfo = buildExceptionInfo(t);
    }

    public TaskExceptionProbeIndex(Long taskId, String stackTrace, boolean sendDirectly) {
        super(taskId);
        this.sendDirectly = sendDirectly;
        this.exceptionInfo = stackTrace;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    public boolean isSendDirectly() {
        return sendDirectly;
    }

    public void setSendDirectly(boolean sendDirectly) {
        this.sendDirectly = sendDirectly;
    }

    private String buildExceptionInfo(Throwable t) {
        return ExceptionUtils.getFullStackTrace(t);
    }
}
