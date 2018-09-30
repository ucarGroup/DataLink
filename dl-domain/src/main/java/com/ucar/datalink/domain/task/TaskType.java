package com.ucar.datalink.domain.task;

/**
 * Created by lubiao on 2017/1/20.
 */
public enum TaskType {
    MYSQL, HBASE;

    public boolean mustRestartWhenPause() {
        return HBASE.equals(this);
    }
}
