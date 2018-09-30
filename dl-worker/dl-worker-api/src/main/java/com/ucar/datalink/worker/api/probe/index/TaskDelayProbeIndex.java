package com.ucar.datalink.worker.api.probe.index;

/**
 * 监控指标信息对象
 * Created by lubiao on 17/4/27.
 */
public class TaskDelayProbeIndex extends BaseTaskProbeIndex {

    private Long delayTime = 0L;

    public TaskDelayProbeIndex(Long taskId, Long delayTime) {
        super(taskId);
        this.delayTime = delayTime;
    }

    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }
}
