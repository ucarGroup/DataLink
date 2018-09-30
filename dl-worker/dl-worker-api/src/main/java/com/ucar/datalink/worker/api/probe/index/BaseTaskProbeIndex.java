package com.ucar.datalink.worker.api.probe.index;

/**
 * Created by lubiao on 2018/3/14.
 */
public class BaseTaskProbeIndex extends BaseProbeIndex {
    private long taskId;

    public BaseTaskProbeIndex(long taskId) {
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
}
