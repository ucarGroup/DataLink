package com.ucar.datalink.worker.api.probe.index;

import java.util.Date;

/**
 * Created by sqq on 2018/1/17.
 */
public class WorkerJvmStateProbeIndex extends BaseWorkerProbeIndex {

    public WorkerJvmStateProbeIndex(Long workerId) {
        super(workerId);
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
