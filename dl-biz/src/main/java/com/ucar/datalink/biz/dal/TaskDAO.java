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

    List<TaskInfo> listByGroupId(Long groupId);

    List<TaskInfo> listByLeaderTaskId(Long leaderTaskId);

    List<TaskInfo> listAllLeaderTasks();

    Date findMaxModifyTime();

    List<TaskInfo> listByCondition(@Param(value = "readerMediaSourceId") Long readerMediaSourceId, @Param(value = "groupId") Long groupId, @Param(value = "id") Long id, @Param(value = "taskType") TaskType taskType);

    TaskInfo findById(Long id);

    Integer insert(TaskInfo taskInfo);

    Integer update(TaskInfo taskInfo);

    Integer delete(Long id);

    Integer deleteTemp(Long id);

    Integer migrateGroup(@Param(value = "id") Long id, @Param(value = "groupId") Long groupId);

    Integer taskCount();

    List<StatisDetail> getCountByType();

    List<TaskInfo> getTasksByType(@Param(value = "taskType") TaskType taskType);

    List<TaskMonitorInfo> getTaskMonitorInfoList(@Param(value = "taskId") Long taskId, @Param(value = "groupId") Long groupId, @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

    List<TaskInfo> getTaskListByGroupId(Long groupId);

    /**
     * 批量修改taskInfo的信息
     *
     * @param updateTaskList
     */
    void batchUpdateTaskInfo(@Param("taskInfoList") List<TaskInfo> updateTaskList);

    /**
     * 批量插入
     *
     * @param taskInfoList
     */
    void batchInsertTask(@Param("taskInfoList") List<TaskInfo> taskInfoList);
}
