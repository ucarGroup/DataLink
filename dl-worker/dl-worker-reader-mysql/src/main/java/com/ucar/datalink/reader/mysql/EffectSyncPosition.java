package com.ucar.datalink.reader.mysql;

/**
 * Created by lubiao on 2018/12/17.
 */
public class EffectSyncPosition {
    private String latestEffectSyncLogFileName;
    private long latestEffectSyncLogFileOffset;

    public EffectSyncPosition(String latestEffectSyncLogFileName, long latestEffectSyncLogFileOffset) {
        this.latestEffectSyncLogFileName = latestEffectSyncLogFileName;
        this.latestEffectSyncLogFileOffset = latestEffectSyncLogFileOffset;
    }

    public String getLatestEffectSyncLogFileName() {
        return latestEffectSyncLogFileName;
    }

    public void setLatestEffectSyncLogFileName(String latestEffectSyncLogFileName) {
        this.latestEffectSyncLogFileName = latestEffectSyncLogFileName;
    }

    public long getLatestEffectSyncLogFileOffset() {
        return latestEffectSyncLogFileOffset;
    }

    public void setLatestEffectSyncLogFileOffset(long latestEffectSyncLogFileOffset) {
        this.latestEffectSyncLogFileOffset = latestEffectSyncLogFileOffset;
    }
}
