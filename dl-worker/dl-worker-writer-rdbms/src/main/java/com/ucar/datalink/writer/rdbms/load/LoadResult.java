package com.ucar.datalink.writer.rdbms.load;

import com.ucar.datalink.contract.Record;

/**
 * Created by lubiao on 2017/3/14.
 */
public class LoadResult {
    private Throwable throwable;
    private Record record;

    private int totalRecords;
    private long totalSqlTime;
    private long avgSqlTime;

    public LoadResult() {

    }

    public LoadResult(Throwable throwable, Record record) {
        this.throwable = throwable;
        this.record = record;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Record getRecord() {
        return record;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public long getTotalSqlTime() {
        return totalSqlTime;
    }

    public void setTotalSqlTime(long totalSqlTime) {
        this.totalSqlTime = totalSqlTime;
    }

    public long getAvgSqlTime() {
        return avgSqlTime;
    }

    public void setAvgSqlTime(long avgSqlTime) {
        this.avgSqlTime = avgSqlTime;
    }
}
