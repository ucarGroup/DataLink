package com.ucar.datalink.biz.service.impl;

import com.google.common.collect.Lists;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.dal.MediaSourceDAO;
import com.ucar.datalink.biz.dal.MonitorDAO;
import com.ucar.datalink.biz.dal.TaskDAO;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.monitor.MonitorCat;
import com.ucar.datalink.domain.monitor.TaskMonitorInfo;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2016/12/6.
 */
@Service
public class TaskConfigServiceImpl implements TaskConfigService {

    private static final Logger logger = LoggerFactory.getLogger(TaskConfigServiceImpl.class);

    /**
     * 同步修改状态，主task修改配置参数，是否同步修改从task,1:表示同步修改
     */
    private static final String SYNC_FLAG = "1";

    @Autowired
    private TaskDAO taskDAO;

    @Autowired
    private MediaSourceDAO mediaSourceDAO;

    @Autowired
    private MediaDAO mediaDAO;

    @Autowired
    private MonitorDAO monitorDAO;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private MediaSourceService mediaSourceService;

    @Override
    public List<TaskInfo> getList() {
        return taskDAO.getList();
    }

    @Override
    public ActiveTasks getActiveTaskConfigsByGroup(Long groupId) {
        Assert.notNull(groupId);

        while (true) {
            long start = System.currentTimeMillis();
            long version = getTaskConfigVersion();
            List<TaskInfo> taskInfos = taskDAO.listByGroupId(groupId);
            logger.info("Time for querying task from db  is : " + (System.currentTimeMillis() - start) + "ms");

            List<TaskInfo> result;
            if (version == -1 || taskInfos == null) {
                return new ActiveTasks(groupId, version, Lists.newArrayList());
            } else {
                result = taskInfos.stream().filter(t -> t.getModifyTime().getTime() <= version).collect(Collectors.toList());
                result.forEach(t -> t.setVersion(version));

                if (result.size() == taskInfos.size()) {
                    return new ActiveTasks(groupId, version, result);
                } else {
                    logger.info("find dirty tasks which exceed version {}.", version);
                }
            }
        }
    }

    @Override
    public Long getTaskConfigVersion() {
        for (int i = 0; i < 3; i++) {
            try {
                Date maxModifyTime = taskDAO.findMaxModifyTime();
                return maxModifyTime == null ? -1L : maxModifyTime.getTime();
            } catch (Throwable t) {
                // 1.连接闪断的情况下存在异常的可能，需要通过重试进行容错处理
                // 2.DB宕机的情况下，也不要向外抛异常，因为DB宕机期间只要不触发rebalance，系统是可以继续运行的
                //TODO,增加报警功能
                logger.error("Get Max ModifyTime Failed!", t);
                try {
                    Thread.sleep(100L);
                } catch (Exception e) {
                }
            }
        }
        throw new DatalinkException("get task config version failed after 3 times.");
    }

    @Override
    public List<TaskInfo> listTasksForQueryPage(Long readerMediaSourceId, Long groupId,
                                                Long id, TaskType taskType) {
        List<TaskInfo> result = taskDAO.listByCondition(readerMediaSourceId, groupId, id, taskType);
        return result == null ? Lists.newArrayList() : result;
    }

    @Override
    public List<TaskInfo> getTasksByReaderMediaSourceId(Long readerMediaSourceId) {
        return taskDAO.getTasksByReaderMediaSourceId(readerMediaSourceId);
    }

    @Override
    public List<TaskInfo> getFollowerTasksForLeaderTask(Long leaderTaskId) {
        return taskDAO.listByLeaderTaskId(leaderTaskId);
    }

    @Override
    public TaskInfo getTask(long id) {
        return taskDAO.findById(id);
    }

    @Override
    @Transactional
    public void addTask(TaskInfo taskInfo) {
        checkTaskName(taskInfo);
        taskDAO.insert(taskInfo);
        if (taskInfo.getLeaderTaskId() == null) {
            monitorService.createAllMonitor(taskInfo.getId(), MonitorCat.TASK_MONITOR);
        }
    }

    @Override
    @Transactional
    public TaskInfo addMySqlTask(TaskInfo taskInfo) {
        checkTaskName(taskInfo);

        taskDAO.insert(taskInfo);
        if (taskInfo.getLeaderTaskId() == null) {
            monitorService.createAllMonitor(taskInfo.getId(), MonitorCat.TASK_MONITOR);
        }
        return taskInfo;
    }

    @Override
    public void updateTask(TaskInfo taskInfo) {
        checkTaskName(taskInfo);
        taskDAO.update(taskInfo);
    }

    @Override
    @Transactional
    public void deleteTask(long id) throws Exception {
        List<TaskInfo> friendTasks = taskDAO.listByLeaderTaskId(id);
        if (CollectionUtils.isNotEmpty(friendTasks)) {
            throw new ValidationException(String.format("任务%s是其它任务的Leader Task，不能删除!", id));
        }

        taskDAO.delete(id);
        mediaDAO.deleteMediaMappingColumnByTaskId(id);//先删除MappingColumn
        mediaDAO.deleteMediaMappingByTaskId(id);//再删除Mapping
        monitorDAO.deleteByResourceIdAndCat(id, MonitorCat.TASK_MONITOR.getKey());
        mediaService.cleanTableMapping(id);//清除Task的映射缓存
    }

    @Override
    public void pauseTask(long id) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setId(id);
        taskInfo.setTargetState(TargetState.PAUSED);
        taskDAO.update(taskInfo);
    }

    @Override
    public void resumeTask(long id) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setId(id);
        taskInfo.setTargetState(TargetState.STARTED);
        taskDAO.update(taskInfo);
    }

    @Override
    public void migrateTaskGroup(Long id, Long targetGroupId) {
        TaskInfo taskInfo = taskDAO.findById(id);

        try {
            taskDAO.deleteTemp(id);
            waitStop(id);
            taskDAO.migrateGroup(id, targetGroupId);//迁到新分组
        } catch (Throwable t) {
            taskDAO.migrateGroup(id, taskInfo.getGroupId());//出现异常，迁回老分组
        }
    }

    @Override
    public Integer taskCount() {
        return taskDAO.taskCount();
    }

    @Override
    public List<StatisDetail> getCountByType() {
        return taskDAO.getCountByType();
    }

    @Override
    public List<TaskInfo> getTasksByType(TaskType taskType) {
        return taskDAO.getTasksByType(taskType);
    }

    @Override
    public List<TaskMonitorInfo> getTaskMonitorInfoList(Long taskId, Long groupId, Date startTime, Date endTime) {
        return taskDAO.getTaskMonitorInfoList(taskId, groupId, startTime, endTime);
    }

    @Override
    public List<TaskInfo> getTaskListByGroupId(Long groupId) {
        return taskDAO.getTaskListByGroupId(groupId);
    }

    private void checkTaskName(TaskInfo taskInfo) {
        String taskName = taskInfo.getTaskName();
        MediaSourceInfo mediaSourceInfo = mediaSourceDAO.getById(taskInfo.getReaderMediaSourceId());
        String prefix = mediaSourceInfo.getName() + "_2_";
        if (!taskName.startsWith(prefix)) {
            throw new ValidationException(String.format("任务名称必须以[%s]为前缀.", prefix));
        }
    }

    private void waitStop(Long taskId) {
        int count = 0;
        while (true) {
            if (count > 10) {
                throw new ValidationException("已超时,Task分组迁移失败!");
            }

            TaskStatus taskStatus = taskStatusService.getStatus(String.valueOf(taskId));
            if (taskStatus != null) {
                try {
                    Thread.sleep(1000);
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }

    }

    /**
     * 支持主leader同步配置参数到所有的follower上
     *
     * @param taskInfo
     * @param sync
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTask(TaskInfo taskInfo, String sync) {
        //修改leader配置
        updateTask(taskInfo);
        //同步所有的follower
        if (!StringUtils.isEmpty(sync) && SYNC_FLAG.equals(sync) && taskInfo.isLeaderTask()) {
            Long leaderId = taskInfo.getId();
            String taskReaderParameter = taskInfo.getTaskReaderParameter();
            String taskWriterParameter = taskInfo.getTaskWriterParameter();
            List<TaskInfo> updateTaskList = new ArrayList<>();
            //查询出所有从task
            List<TaskInfo> taskInfoList = taskDAO.listByLeaderTaskId(leaderId);
            if (taskInfoList != null && taskInfoList.size() > 0) {
                taskInfoList.forEach(t -> {
                    t.setTaskReaderParameter(taskReaderParameter);
                    t.setTaskWriterParameter(taskWriterParameter);
                    updateTaskList.add(t);
                });
                taskDAO.batchUpdateTaskInfo(updateTaskList);
            }
        }
    }
}
