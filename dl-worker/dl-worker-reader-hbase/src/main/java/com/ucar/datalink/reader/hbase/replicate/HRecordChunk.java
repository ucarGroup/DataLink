package com.ucar.datalink.reader.hbase.replicate;

import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.contract.log.hbase.HRecord;

import java.util.List;

/**
 * Created by lubiao on 2017/11/20.
 */
public class HRecordChunk {
    private List<HRecord> records;
    private FutureCallback callback;
    private long payloadSize;

    public HRecordChunk(List<HRecord> records, FutureCallback callback, long payloadSize) {
        this.records = records;
        this.callback = callback;
        this.payloadSize = payloadSize;
    }

    public List<HRecord> getRecords() {
        return records;
    }

    public void setRecords(List<HRecord> records) {
        this.records = records;
    }

    public FutureCallback getCallback() {
        return callback;
    }

    public void setCallback(FutureCallback callback) {
        this.callback = callback;
    }

    public long getPayloadSize() {
        return payloadSize;
    }

    public void setPayloadSize(long payloadSize) {
        this.payloadSize = payloadSize;
    }
}
