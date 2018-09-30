package com.ucar.datalink.worker.api.probe.index;

/**
 * Created by lubiao on 2018/3/14.
 */
public class BaseWorkerProbeIndex extends BaseProbeIndex {
    private long workerId;

    public BaseWorkerProbeIndex(long workerId) {
        this.workerId = workerId;
    }

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }
}
