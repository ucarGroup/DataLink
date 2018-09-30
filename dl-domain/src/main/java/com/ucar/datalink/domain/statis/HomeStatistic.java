package com.ucar.datalink.domain.statis;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by sqq on 2018/4/17.
 */
@Alias("homeStatistic")
public class HomeStatistic implements Serializable, Storable {
    private Long taskId;
    private String taskName;
    private Long groupId;
    private Long workerId;
    private String workerName;
    private Long taskSizeStatistic;
    private Long taskRecordStatistic;
    private Long taskDelayStatistic;
    private BigDecimal workerJvmUsedStatistic;
    private BigDecimal workerYoungGCCountStatistic;
    private BigDecimal incomingNetTrafficStatistic;

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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public Long getTaskSizeStatistic() {
        return taskSizeStatistic;
    }

    public void setTaskSizeStatistic(Long taskSizeStatistic) {
        this.taskSizeStatistic = taskSizeStatistic;
    }

    public Long getTaskRecordStatistic() {
        return taskRecordStatistic;
    }

    public void setTaskRecordStatistic(Long taskRecordStatistic) {
        this.taskRecordStatistic = taskRecordStatistic;
    }

    public Long getTaskDelayStatistic() {
        return taskDelayStatistic;
    }

    public void setTaskDelayStatistic(Long taskDelayStatistic) {
        this.taskDelayStatistic = taskDelayStatistic;
    }

    public BigDecimal getWorkerJvmUsedStatistic() {
        return workerJvmUsedStatistic;
    }

    public void setWorkerJvmUsedStatistic(BigDecimal workerJvmUsedStatistic) {
        this.workerJvmUsedStatistic = workerJvmUsedStatistic;
    }

    public BigDecimal getWorkerYoungGCCountStatistic() {
        return workerYoungGCCountStatistic;
    }

    public void setWorkerYoungGCCountStatistic(BigDecimal workerYoungGCCountStatistic) {
        this.workerYoungGCCountStatistic = workerYoungGCCountStatistic;
    }

    public BigDecimal getIncomingNetTrafficStatistic() {
        return incomingNetTrafficStatistic;
    }

    public void setIncomingNetTrafficStatistic(BigDecimal incomingNetTrafficStatistic) {
        this.incomingNetTrafficStatistic = incomingNetTrafficStatistic;
    }
}
