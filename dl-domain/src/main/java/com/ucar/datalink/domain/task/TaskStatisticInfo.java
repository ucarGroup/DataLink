package com.ucar.datalink.domain.task;

import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 同步任务性能统计
 * Created by sqq on 2018/2/28.
 */
@Alias("taskStatistic")
public class TaskStatisticInfo implements Serializable, Storable {

    private Long id;
    private Long taskId;
    private long recordsPerMinute;
    private long sizePerMinute;
    private BigDecimal writeTimePerRecord;
    private long exceptionsPerMinute;
    private long readWriteCountPerMinute;
    private Date createTime;
    //查询时间
    private String startTime;
    private String endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public long getRecordsPerMinute() {
        return recordsPerMinute;
    }

    public void setRecordsPerMinute(long recordsPerMinute) {
        this.recordsPerMinute = recordsPerMinute;
    }

    public long getSizePerMinute() {
        return sizePerMinute;
    }

    public void setSizePerMinute(long sizePerMinute) {
        this.sizePerMinute = sizePerMinute;
    }

    public BigDecimal getWriteTimePerRecord() {
        return writeTimePerRecord;
    }

    public void setWriteTimePerRecord(BigDecimal writeTimePerRecord) {
        this.writeTimePerRecord = writeTimePerRecord;
    }

    public long getExceptionsPerMinute() {
        return exceptionsPerMinute;
    }

    public void setExceptionsPerMinute(long exceptionsPerMinute) {
        this.exceptionsPerMinute = exceptionsPerMinute;
    }

    public long getReadWriteCountPerMinute() {
        return readWriteCountPerMinute;
    }

    public void setReadWriteCountPerMinute(long readWriteCountPerMinute) {
        this.readWriteCountPerMinute = readWriteCountPerMinute;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
