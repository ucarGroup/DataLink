package com.ucar.datalink.manager.core.doublecenter;

import java.math.BigInteger;

/**
 * 位点收集类
 */
public class PositionGather {

    private Long taskId;
    private String taskName;

    private String latestEffectSyncLogFileNameBefore;
    private Long latestEffectSyncLogFileOffsetBefore;

    private String latestEffectSyncLogFileNameAfter;
    private Long latestEffectSyncLogFileOffsetAfter;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getLatestEffectSyncLogFileNameBefore() {
        return latestEffectSyncLogFileNameBefore;
    }

    public void setLatestEffectSyncLogFileNameBefore(String latestEffectSyncLogFileNameBefore) {
        this.latestEffectSyncLogFileNameBefore = latestEffectSyncLogFileNameBefore;
    }

    public Long getLatestEffectSyncLogFileOffsetBefore() {
        return latestEffectSyncLogFileOffsetBefore;
    }

    public void setLatestEffectSyncLogFileOffsetBefore(Long latestEffectSyncLogFileOffsetBefore) {
        this.latestEffectSyncLogFileOffsetBefore = latestEffectSyncLogFileOffsetBefore;
    }

    public String getLatestEffectSyncLogFileNameAfter() {
        return latestEffectSyncLogFileNameAfter;
    }

    public void setLatestEffectSyncLogFileNameAfter(String latestEffectSyncLogFileNameAfter) {
        this.latestEffectSyncLogFileNameAfter = latestEffectSyncLogFileNameAfter;
    }

    public Long getLatestEffectSyncLogFileOffsetAfter() {
        return latestEffectSyncLogFileOffsetAfter;
    }

    public void setLatestEffectSyncLogFileOffsetAfter(Long latestEffectSyncLogFileOffsetAfter) {
        this.latestEffectSyncLogFileOffsetAfter = latestEffectSyncLogFileOffsetAfter;
    }
}
