package com.ucar.datalink.domain.monitor;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by csf on 17/4/26.
 */
public enum MonitorType {

    TASK_DELAY_MONITOR(1, "任务延迟监控", MonitorCat.TASK_MONITOR),
    TASK_EXCEPTION_MONITOR(2, "任务异常监控", MonitorCat.TASK_MONITOR),
    TASK_STATUS_MONITOR(3, "任务状态监控", MonitorCat.TASK_MONITOR),
    WORKER_RUNNING_STATE_MONITOR(4, "worker运行状态", MonitorCat.WORKER_MONITOR),
    WORKER_JVM_STATE_MONITOR(5, "workerJVM状态", MonitorCat.WORKER_MONITOR),
    TASK_STATUS_MISMATCH_MONITOR(6, "任务状态冲突监控", MonitorCat.TASK_MONITOR);

    private final int key;
    private final String desc;
    private final MonitorCat monitorCat;

    MonitorType(int i, String desc, MonitorCat monitorCat) {
        this.key = i;
        this.desc = desc;
        this.monitorCat = monitorCat;
    }

    public int getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public MonitorCat getMonitorCat() {
        return monitorCat;
    }

    public static List<MonitorType> getMonitorTypeListByCat(int catKey) {
        if (catKey == 1) {
            return Lists.newArrayList(TASK_DELAY_MONITOR, TASK_EXCEPTION_MONITOR, TASK_STATUS_MONITOR, TASK_STATUS_MISMATCH_MONITOR);
        } else if (catKey == 2) {
            return Lists.newArrayList(WORKER_JVM_STATE_MONITOR, WORKER_RUNNING_STATE_MONITOR);
        } else {
            return Lists.newArrayList(TASK_DELAY_MONITOR, TASK_EXCEPTION_MONITOR, TASK_STATUS_MONITOR, TASK_STATUS_MISMATCH_MONITOR, WORKER_JVM_STATE_MONITOR, WORKER_RUNNING_STATE_MONITOR);
        }
    }
}