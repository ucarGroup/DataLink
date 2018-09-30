package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.monitor.TaskMonitorInfo;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskType;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by lubiao on 2017/1/4.
 */
public interface TaskDAO {

    List<TaskInfo> getList();

    List<TaskInfo> getTasksByReaderMediaSourceId(Long readerMediaSourceId);

    List<TaskInfo> getListWithDeleted();

    List<TaskInfo> listByGroupId(Long groupId);

    List<TaskInfo> listByLeaderTaskId(Long leaderTaskId);

    List<TaskInfo> listAllLeaderTasks();

    Date findMaxModifyTime();

    List<TaskInfo> listByCondition(TaskInfo query);

    TaskInfo findById(Long id);

    Integer insert(TaskInfo taskInfo);

    Integer update(TaskInfo taskInfo);

    Integer delete(Long id);

    Integer migrateGroup(@Param(value = "id") Long id, @Param(value = "groupId") Long groupId);

    Integer taskCount();

    List<StatisDetail> getCountByType();

    List<TaskInfo> getTasksByType(@Param(value = "taskType")TaskType taskType);

    List<TaskMonitorInfo> getTaskMonitorInfoList(@Param(value = "taskId") Long taskId, @Param(value = "groupId") Long groupId, @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

    List<TaskInfo> getTaskListByGroupId(Long groupId);
}
