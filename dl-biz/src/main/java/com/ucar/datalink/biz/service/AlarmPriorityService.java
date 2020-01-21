package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.alarm.AlarmPriorityInfo;

import java.util.List;

public interface AlarmPriorityService {
    List<AlarmPriorityInfo> getTaskPriorityList(String name, Integer priority);

    public Boolean insert(AlarmPriorityInfo taskPriorityInfo);

    AlarmPriorityInfo getById(Long id);

    Boolean update(AlarmPriorityInfo taskPriorityInfo);

    Boolean delete(Long id);

    List<AlarmPriorityInfo> getAll();

    List<AlarmPriorityInfo> getPriorityListByIds(List<Long> priorityIds);
}
