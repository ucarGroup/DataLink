package com.ucar.datalink.reader.mysql;

import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2019/7/31.
 */
public class CanalReaderMsg {
    private final long batchId;
    private final String logFileName;
    private final long logFileOffset;
    private final long firstEntryTime;
    private final long payloadSize;
    private final List<RdbEventRecord> records;
    private final Map<String, Object> metaData;

    public CanalReaderMsg(long batchId, String logFileName, long logFileOffset, long firstEntryTime,
                          long payloadSize, List<RdbEventRecord> records) {
        this.batchId = batchId;
        this.logFileName = logFileName;
        this.logFileOffset = logFileOffset;
        this.firstEntryTime = firstEntryTime;
        this.payloadSize = payloadSize;
        this.records = records;
        this.metaData = new HashMap<>();
    }

    public long getBatchId() {
        return batchId;
    }

    public long getLogFileOffset() {
        return logFileOffset;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public long getFirstEntryTime() {
        return firstEntryTime;
    }

    public long getPayloadSize() {
        return payloadSize;
    }

    public List<RdbEventRecord> getRecords() {
        return records;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }
}
