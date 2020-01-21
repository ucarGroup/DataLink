package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.alarm.AlarmStrategyInfo;
import com.ucar.datalink.domain.alarm.StrategyConfig;

import java.util.List;

public interface AlarmStrategyService {
    List<AlarmStrategyInfo> getAlarmStrategyList(Long priorityId,String name);

    Boolean insert(AlarmStrategyInfo alarmStrategyInfo);

    AlarmStrategyInfo getById(Long id);

    Boolean update(AlarmStrategyInfo alarmStrategyInfo);

    Boolean delete(Long id);

    AlarmStrategyInfo getByTaskIdAndType(Long taskId, Integer monitorType);

    StrategyConfig getStrategyConfig(List<StrategyConfig> strategys);
}
