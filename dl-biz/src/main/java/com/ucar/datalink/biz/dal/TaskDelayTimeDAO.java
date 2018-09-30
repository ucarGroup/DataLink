package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.task.TaskDelayTimeInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by csf on 17/5/1.
 */
public interface TaskDelayTimeDAO {

    Integer insert(TaskDelayTimeInfo alarmInfo);

    public List<TaskDelayTimeInfo> getList();

    List<TaskDelayTimeInfo> getListByTaskIdForQuery(@Param("taskId") Long taskId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

}
