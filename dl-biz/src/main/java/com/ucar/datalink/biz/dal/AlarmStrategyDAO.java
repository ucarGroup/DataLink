package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.alarm.AlarmStrategyInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmStrategyDAO {
    List<AlarmStrategyInfo> getAlarmStrategyList(@Param("priorityId") Long priorityId,@Param("name") String name);

    Integer insert(AlarmStrategyInfo alarmStrategyInfo);

    AlarmStrategyInfo getById(Long id);

    Integer update(AlarmStrategyInfo alarmStrategyInfo);

    Integer delete(Long id);

    AlarmStrategyInfo getByTaskIdAndType(@Param("taskId") Long taskId,@Param("monitorType") Integer monitorType);
}
