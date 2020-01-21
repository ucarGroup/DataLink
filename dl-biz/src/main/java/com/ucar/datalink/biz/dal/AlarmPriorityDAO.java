package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.alarm.AlarmPriorityInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmPriorityDAO {
    List<AlarmPriorityInfo> getTaskPriorityList(@Param(value = "name") String name, @Param(value="priority") Integer priority);

    Integer insert(AlarmPriorityInfo taskPriorityInfo);

    AlarmPriorityInfo getById(Long id);

    Integer update(AlarmPriorityInfo taskPriorityInfo);

    Integer delete(Long id);

    List<AlarmPriorityInfo> getAll();

    List<AlarmPriorityInfo> getPriorityListByIds(@Param("priorityIds") List<Long> priorityIds);
}
