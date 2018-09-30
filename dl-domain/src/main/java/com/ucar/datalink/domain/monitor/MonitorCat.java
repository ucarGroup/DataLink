package com.ucar.datalink.domain.monitor;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by sqq on 2018/1/17.
 */
public enum MonitorCat {

    TASK_MONITOR(1, "任务监控"),
    WORKER_MONITOR(2, "机器监控");

    private final int key;
    private final String desc;

    MonitorCat(int i, String desc) {
        this.key = i;
        this.desc = desc;
    }

    public int getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public static List<MonitorCat> getMonitorCatList() {
        return Lists.newArrayList(TASK_MONITOR, WORKER_MONITOR);
    }

}
