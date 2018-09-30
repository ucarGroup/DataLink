package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.task.TaskStatisticInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2018/2/28.
 */
public interface TaskStatisticDAO {

    Integer insert(TaskStatisticInfo taskStatisticInfo);

    List<TaskStatisticInfo> getListByTaskIdForQuery(@Param("taskId") Long taskId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
