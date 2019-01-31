package com.ucar.datalink.domain.plugin.reader.mysql;

import com.ucar.datalink.domain.Position;

import java.net.InetSocketAddress;

/**
 * Created by user on 2017/2/28.
 */
public class MysqlReaderPosition extends Position {
    private static final long serialVersionUID = 5530225131455662581L;

    private boolean included;
    private String journalName;
    private Long position;
    private Long serverId;
    private InetSocketAddress sourceAddress;
    private Long slaveId;
    protected Long timestamp;
    private String latestEffectSyncLogFileName;
    private Long latestEffectSyncLogFileOffset;

    public Long getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(Long slaveId) {
        this.slaveId = slaveId;
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }

    public String getJournalName() {
        return journalName;
    }

    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public InetSocketAddress getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(InetSocketAddress sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLatestEffectSyncLogFileName() {
        return latestEffectSyncLogFileName;
    }

    public void setLatestEffectSyncLogFileName(String latestEffectSyncLogFileName) {
        this.latestEffectSyncLogFileName = latestEffectSyncLogFileName;
    }

    public Long getLatestEffectSyncLogFileOffset() {
        return latestEffectSyncLogFileOffset;
    }

    public void setLatestEffectSyncLogFileOffset(Long latestEffectSyncLogFileOffset) {
        this.latestEffectSyncLogFileOffset = latestEffectSyncLogFileOffset;
    }
}
