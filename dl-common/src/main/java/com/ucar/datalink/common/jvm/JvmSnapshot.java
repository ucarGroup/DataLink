package com.ucar.datalink.common.jvm;

/**
 * Created by lubiao on 2018/4/2.
 */
public class JvmSnapshot {
    private long startTime;

    private long youngUsed;//新生代已用内存

    private long youngMax;//新生代最大内存

    private long oldUsed;//老年代已用内存

    private long oldMax;//老年代最大内存

    private long youngCollectionCount;//新生代当前累积的垃圾回收次数

    private long oldCollectionCount;//老年代当前累积的垃圾回收次数

    private long youngCollectionTime;//新生代当前累积的垃圾回收时间

    private long oldCollectionTime;//老年代当前累积的垃圾回收时间

    private int currentThreadCount;//当前线程数

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getYoungUsed() {
        return youngUsed;
    }

    public void setYoungUsed(long youngUsed) {
        this.youngUsed = youngUsed;
    }

    public long getYoungMax() {
        return youngMax;
    }

    public void setYoungMax(long youngMax) {
        this.youngMax = youngMax;
    }

    public long getOldUsed() {
        return oldUsed;
    }

    public void setOldUsed(long oldUsed) {
        this.oldUsed = oldUsed;
    }

    public long getOldMax() {
        return oldMax;
    }

    public void setOldMax(long oldMax) {
        this.oldMax = oldMax;
    }

    public long getYoungCollectionCount() {
        return youngCollectionCount;
    }

    public void setYoungCollectionCount(long youngCollectionCount) {
        this.youngCollectionCount = youngCollectionCount;
    }

    public long getOldCollectionCount() {
        return oldCollectionCount;
    }

    public void setOldCollectionCount(long oldCollectionCount) {
        this.oldCollectionCount = oldCollectionCount;
    }

    public long getYoungCollectionTime() {
        return youngCollectionTime;
    }

    public void setYoungCollectionTime(long youngCollectionTime) {
        this.youngCollectionTime = youngCollectionTime;
    }

    public long getOldCollectionTime() {
        return oldCollectionTime;
    }

    public void setOldCollectionTime(long oldCollectionTime) {
        this.oldCollectionTime = oldCollectionTime;
    }

    public int getCurrentThreadCount() {
        return currentThreadCount;
    }

    public void setCurrentThreadCount(int currentThreadCount) {
        this.currentThreadCount = currentThreadCount;
    }
}
