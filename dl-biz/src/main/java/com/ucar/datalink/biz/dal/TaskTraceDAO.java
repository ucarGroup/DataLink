package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.task.TaskExceptionInfo;
import com.ucar.datalink.domain.task.TaskTraceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by djj on 2019/4/24.
 */
public interface TaskTraceDAO {

    Integer insert(TaskTraceInfo taskTraceInfo);

    Integer update(TaskTraceInfo taskTraceInfo);

    List<TaskTraceInfo> findListByTaskId(@Param("taskId") Long taskId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);


}
