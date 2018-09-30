package com.ucar.datalink.worker.api.probe.index;


/**
 * Created by sqq on 2018/2/28.
 */
public class TaskStatisticProbeIndex extends BaseTaskProbeIndex {

    private long writeTimeThrough;
    private int recordsCount;
    private long payloadSize;
    private boolean exceptionExist;

    public TaskStatisticProbeIndex(Long taskId, long writeTimeThrough, int recordsCount, long payloadSize, boolean exceptionExist) {
        super(taskId);
        this.setWriteTimeThrough(writeTimeThrough);
        this.setRecordsCount(recordsCount);
        this.setPayloadSize(payloadSize);
        this.setExceptionExist(exceptionExist);
    }

    public long getWriteTimeThrough() {
        return writeTimeThrough;
    }

    public void setWriteTimeThrough(long writeTimeThrough) {
        this.writeTimeThrough = writeTimeThrough;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }

    public long getPayloadSize() {
        return payloadSize;
    }

    public void setPayloadSize(long payloadSize) {
        this.payloadSize = payloadSize;
    }

    public boolean isExceptionExist() {
        return exceptionExist;
    }

    public void setExceptionExist(boolean exceptionExist) {
        this.exceptionExist = exceptionExist;
    }
}
