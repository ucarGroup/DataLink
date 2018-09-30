package com.ucar.datalink.domain.worker;


import com.ucar.datalink.domain.Storable;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sqq on 2018/1/18.
 */
@Alias("workerJvmState")
public class WorkerJvmStateInfo implements Serializable, Storable {

    private Long id;
    /**
     * worker机器Id
     */
    private Long workerId;
    /**
     * worker机器ip
     */
    private String host;

    /**
     * 老年代已使用堆内存
     */
    private long oldMemUsed;
    /**
     * 老年代最大堆可用内存
     */
    private long oldMemMax;
    /**
     * 新生代最大可用内存
     */
    private long youngMemMax;
    /**
     * 新生代已使用内存
     */
    private long youngMemUsed;
    /**
     * 统计时间
     */
    private Date createTime;
    /**
     * 老年代一分钟内的垃圾回收次数
     */
    private long intervalOldCollectionCount;
    /**
     * 新生代一分钟内垃圾回收次数
     */
    private long intervalYoungCollectionCount;
    /**
     * 新生代一分钟内垃圾回收时间
     */
    private long intervalYoungCollectionTime;
    /**
     * 老年代一分钟内的垃圾回收时间
     */
    private long intervalOldCollectionTime;
    /**
     * 当前线程数
     */
    private int currentThreadCount;
    /**
     * 查询时间段
     */
    private String startTime;

    private String endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public long getOldMemUsed() {
        return oldMemUsed;
    }

    public void setOldMemUsed(long oldMemUsed) {
        this.oldMemUsed = oldMemUsed;
    }

    public long getOldMemMax() {
        return oldMemMax;
    }

    public void setOldMemMax(long oldMemMax) {
        this.oldMemMax = oldMemMax;
    }

    public long getYoungMemMax() {
        return youngMemMax;
    }

    public void setYoungMemMax(long youngMemMax) {
        this.youngMemMax = youngMemMax;
    }

    public long getYoungMemUsed() {
        return youngMemUsed;
    }

    public void setYoungMemUsed(long youngMemUsed) {
        this.youngMemUsed = youngMemUsed;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getIntervalOldCollectionCount() {
        return intervalOldCollectionCount;
    }

    public void setIntervalOldCollectionCount(long intervalOldCollectionCount) {
        this.intervalOldCollectionCount = intervalOldCollectionCount;
    }

    public long getIntervalYoungCollectionCount() {
        return intervalYoungCollectionCount;
    }

    public void setIntervalYoungCollectionCount(long intervalYoungCollectionCount) {
        this.intervalYoungCollectionCount = intervalYoungCollectionCount;
    }

    public long getIntervalYoungCollectionTime() {
        return intervalYoungCollectionTime;
    }

    public void setIntervalYoungCollectionTime(long intervalYoungCollectionTime) {
        this.intervalYoungCollectionTime = intervalYoungCollectionTime;
    }

    public long getIntervalOldCollectionTime() {
        return intervalOldCollectionTime;
    }

    public void setIntervalOldCollectionTime(long intervalOldCollectionTime) {
        this.intervalOldCollectionTime = intervalOldCollectionTime;
    }

    public int getCurrentThreadCount() {
        return currentThreadCount;
    }

    public void setCurrentThreadCount(int currentThreadCount) {
        this.currentThreadCount = currentThreadCount;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "WorkerJvmStateInfo{" +
                "id=" + id +
                ", workerId=" + workerId +
                ", host=" + host +
                ", oldMemMax=" + oldMemMax +
                ", oldMemUsed=" + oldMemUsed +
                ", youngMemMax=" + youngMemMax +
                ", youngMemUsed=" + youngMemUsed +
                ", createTime=" + createTime +
                ", intervalOldCollectionCount=" + intervalOldCollectionCount +
                ", intervalYoungCollectionCount=" + intervalYoungCollectionCount +
                ", intervalOldCollectionTime=" + intervalOldCollectionTime +
                ", intervalYoungCollectionTime=" + intervalYoungCollectionTime +
                ", currentThreadCount=" + currentThreadCount +
                '}';
    }
}
