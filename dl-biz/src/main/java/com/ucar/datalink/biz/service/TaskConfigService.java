package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.monitor.TaskMonitorInfo;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.*;
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
    ActiveTasks getActiveTaskConfigsByGroup(Long groupId);

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
    List<TaskInfo> listTasksForQueryPage(MediaSourceType srcType, Long readerMediaSourceId, Long groupId, Long id, TaskType taskType);

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
     * 增加一个mysql同步任务
     *
     * @param taskInfo
     */
    public TaskInfo addMySqlTask(TaskInfo taskInfo, Boolean isMulticopy);

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
    void deleteTask(long id) throws Exception;

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

    /**
     * 通过同步模式获取任务列表
     *
     * @param taskSyncModeEnum
     * @return
     */
    List<TaskInfo> findListBySyncMode(TaskSyncModeEnum taskSyncModeEnum, Long start, Long pageSize);

    /**
     * 通过同步模式统计任务数
     *
     * @param taskSyncModeEnum
     * @return
     */
    Long countTasksBySyncMode(TaskSyncModeEnum taskSyncModeEnum);

    /**
     * 批量更新任务状态
     *
     * @param taskIdList
     * @param targetState
     * @return
     */
    List<TaskInfo> batchUpdateTaskStatus(List taskIdList,TargetState targetState);

    List<TaskInfo> findAcrossLabList();

    /**
     * 修改hbase主task时，支持从task同步修改
     * @param taskInfo
     * @param taskReaderParameterSource
     * @param taskWriterParameterSource
     * @param sync
     */
    void updateTask(TaskInfo taskInfo, String sync);

    List<TaskInfo> findAcrossLabTaskListByMsList(List<Long> mediaSourceIdList);

    /**
     * 批量查询任务信息
     */
    public List<TaskInfo> findTaskInfoByBatchId(List<Long> taskIdList);


    /**
     * 自动创建task,hdfs接口使用
     * @param srcMediaSourceInfo
     * @param targetMediaSourceInfo
     */
    TaskInfo createTask(MediaSourceInfo srcMediaSourceInfo, MediaSourceInfo targetMediaSourceInfo,Long groupId,String zkServer,String currentEnv) throws CloneNotSupportedException;

    /**
     * 配置task的writer
     * @param findTask
     * @param targetMediaSourceType
     */
    void configTaskWriter(TaskInfo findTask, MediaSourceType targetMediaSourceType);

    List<TaskInfo> findTaskListNoPage(TaskInfo taskInfo);

}
