package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.monitor.TaskMonitorInfo;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskType;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by lubiao on 2016/12/5.
 */
public interface TaskConfigService {

    /**
     * 获取所有task
     *
     * @return
     */
    List<TaskInfo> getList();

    /**
     * 获取指定的readerMediaSourceId对应的Task列表
     *
     * @return
     */
    List<TaskInfo> getTasksByReaderMediaSourceId(Long readerMediaSourceId);

    /**
     * 获取某个分组下的所有active-task-config，如果没有配置则返回一个Empty-List.
     * active means "isDelete=fasle".
     *
     * @param groupId 分组id
     * @return
     */
    List<TaskInfo> getActiveTaskConfigsByGroup(Long groupId);

    /**
     * 获取某个LeaderTask的FollowerTask列表
     *
     * @return
     */
    List<TaskInfo> getFollowerTasksForLeaderTask(Long leaderTaskId);

    /**
     * 获取所有task的最大修改时间(包含已删除的task),该时间即为TaskConfig的版本
     * 如果当前没有任何task配置，则版本号为-1
     *
     * @return 版本号
     */
    Long getTaskConfigVersion();


    /**
     * 为Task查询页面提供服务
     *
     * @return
     */
    List<TaskInfo> listTasksForQueryPage(Long readerMediaSourceId, Long groupId, Long id, TaskType taskType);

    /**
     * 通过Id获取TaskInfo
     *
     * @param id
     * @return
     */
    TaskInfo getTask(long id);

    /**
     * 增加一个同步任务
     *
     * @param taskInfo
     */
    void addTask(TaskInfo taskInfo);

    /**
     * 更新Task配置内容
     *
     * @param taskInfo
     */
    void updateTask(TaskInfo taskInfo);

    /**
     * 删除同步任务
     *
     * @param id
     */
    void deleteTask(long id);

    /**
     * 暂停Task
     *
     * @param id
     */
    void pauseTask(long id);

    /**
     * 对Task进行组迁移
     *
     * @param id
     * @param targetGroupId
     */
    void migrateTaskGroup(Long id, Long targetGroupId);

    /**
     * 停止暂停，恢复运行
     *
     * @param id
     */
    void resumeTask(long id);

    /**
     * 获取所有的同步任务数量
     *
     * @return
     */
    Integer taskCount();

    /**
     * 获取某种类型Task的数量
     *
     * @return
     */
    List<StatisDetail> getCountByType();

    /**
     * 获取某种类型Task的列表
     *
     * @param taskType
     * @return
     */
    List<TaskInfo> getTasksByType(TaskType taskType);

    /**
     * 获取Task监控列表信息
     *
     * @param taskId
     * @param groupId
     * @param startTime
     * @param endTime
     * @return
     */
    List<TaskMonitorInfo> getTaskMonitorInfoList(@Param(value = "taskId") Long taskId, @Param(value = "groupId") Long groupId, @Param(value = "startTime") Date startTime, @Param(value = "endTime") Date endTime);

    /**
     * 获取某分组的Task列表
     * @param groupId
     * @return
     */
    List<TaskInfo> getTaskListByGroupId(Long groupId);
}
