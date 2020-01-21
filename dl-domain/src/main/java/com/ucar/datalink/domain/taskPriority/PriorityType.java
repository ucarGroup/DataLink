package com.ucar.datalink.domain.taskPriority;

import com.google.common.collect.Lists;
import com.ucar.datalink.domain.monitor.MonitorCat;

import java.util.List;

public enum PriorityType {

    TASK_ONE_PRIORITY(1, "一级任务"),
    TASK_TWO_PRIORITY(2, "二级任务"),
    TASK_THREE_PRIORITY(3, "三级任务");


    private final int key;
    private final String desc;

    PriorityType(int i, String desc) {
        this.key = i;
        this.desc = desc;
    }

    public int getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public static List<PriorityType> getPriorityTypeList() {
        return Lists.newArrayList(TASK_ONE_PRIORITY, TASK_TWO_PRIORITY, TASK_THREE_PRIORITY);
    }

}
