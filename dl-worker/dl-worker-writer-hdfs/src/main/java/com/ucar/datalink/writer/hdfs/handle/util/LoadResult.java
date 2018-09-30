package com.ucar.datalink.writer.hdfs.handle.util;

/**
 * Created by lubiao on 2017/3/14.
 */
public class LoadResult {

    private int totalRecords;
    private long totalWriteTime;
    private long avgWriteTime;

    public LoadResult() {

    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getTotalWriteTime() {
        return totalWriteTime;
    }

    public void setTotalWriteTime(long totalWriteTime) {
        this.totalWriteTime = totalWriteTime;
    }

    public long getAvgWriteTime() {
        return avgWriteTime;
    }

    public void setAvgWriteTime(long avgWriteTime) {
        this.avgWriteTime = avgWriteTime;
    }
}
