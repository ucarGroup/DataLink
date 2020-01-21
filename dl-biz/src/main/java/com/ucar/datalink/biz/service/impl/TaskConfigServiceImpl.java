package com.ucar.datalink.biz.service.impl;

import com.google.common.collect.Lists;
import com.ucar.datalink.biz.dal.*;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.Parameter;
import com.ucar.datalink.domain.doublecenter.LabEnum;
import com.ucar.datalink.domain.lab.LabInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.meta.HbaseStatus;
import com.ucar.datalink.domain.monitor.MonitorCat;
import com.ucar.datalink.domain.monitor.TaskMonitorInfo;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.reader.hbase.HBaseReaderParameter;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderParameter;
import com.ucar.datalink.domain.plugin.writer.dove.DoveWriterParameter;
import com.ucar.datalink.domain.plugin.writer.es.EsWriterParameter;
import com.ucar.datalink.domain.plugin.writer.fq.FqWriterParameter;
import com.ucar.datalink.domain.plugin.writer.hbase.HBaseWriterParameter;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.domain.plugin.writer.kafka.KafkaWriterParameter;
import com.ucar.datalink.domain.plugin.writer.kudu.KuduWriterParameter;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.domain.plugin.writer.sddl.SddlWriterParameter;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2016/12/6.
 */
@Service
public class TaskConfigServiceImpl implements TaskConfigService {

    private static final Logger logger = LoggerFactory.getLogger(TaskConfigServiceImpl.class);

    private static final String ENV_PROD = "prod";

    /**
     * 同步修改状态，主task修改配置参数，是否同步修改从task,1:表示同步修改
     */
    private static final String SYNC_FLAG = "1";

    private static final Map<MediaSourceType, String> mediaNameMappingMap = new HashMap<>(8);

    static {
        mediaNameMappingMap.put(MediaSourceType.MYSQL, "rdbms");
        mediaNameMappingMap.put(MediaSourceType.SQLSERVER, "rdbms");
        mediaNameMappingMap.put(MediaSourceType.ORACLE, "rdbms");
        mediaNameMappingMap.put(MediaSourceType.POSTGRESQL, "rdbms");
        mediaNameMappingMap.put(MediaSourceType.ELASTICSEARCH, "es");
        mediaNameMappingMap.put(MediaSourceType.HDFS, "hdfs");
        mediaNameMappingMap.put(MediaSourceType.FLEXIBLEQ, "fq");
        mediaNameMappingMap.put(MediaSourceType.DOVE, "dove");
        mediaNameMappingMap.put(MediaSourceType.HBASE, "hbase");
        mediaNameMappingMap.put(MediaSourceType.SDDL, "sddl");
        mediaNameMappingMap.put(MediaSourceType.KUDU, "kudu");
    }

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
    LabDAO labDAO;

    @Autowired
    DoubleCenterService doubleCenterService;

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
    public List<TaskInfo> listTasksForQueryPage(MediaSourceType srcType, Long readerMediaSourceId, Long groupId, Long id, TaskType taskType) {
        List<TaskInfo> result = taskDAO.listByCondition(srcType, readerMediaSourceId, groupId, id, taskType);
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
    public TaskInfo addMySqlTask(TaskInfo taskInfo, Boolean isMulticopy) {
        checkTaskName(taskInfo);
        //多路复制
        if (isMulticopy) {
            //leader
            TaskInfo taskInfoLeader = new TaskInfo();
            BeanUtils.copyProperties(taskInfo, taskInfoLeader);
            taskInfoLeader.setTaskName(taskInfo.getTaskName() + "_0");

            //中心机房作为leader的id
            LabInfo labInfo = labDAO.getLabByName(doubleCenterService.getCenterLab(Constants.WHOLE_SYSTEM));
            taskInfoLeader.setLabId(labInfo.getId());
            taskInfoLeader.setIsLeaderTask(true);
            taskDAO.insert(taskInfoLeader);

            //follow
            //取非中心机房
            List<LabInfo> list = labDAO.findLabList();
            LabInfo noCenterLab = null;
            for (LabInfo info : list) {
                if (info.getId().longValue() != labInfo.getId().longValue()) {
                    noCenterLab = info;
                    break;
                }
            }
            TaskInfo taskInfoFollow = new TaskInfo();
            BeanUtils.copyProperties(taskInfo, taskInfoFollow);
            taskInfoFollow.setTaskName(taskInfo.getTaskName() + "_1");
            taskInfoFollow.setLeaderTaskId(taskInfoLeader.getId());
            taskInfoFollow.setLabId(noCenterLab.getId());
            taskDAO.insert(taskInfoFollow);

            monitorService.createAllMonitor(taskInfoLeader.getId(), MonitorCat.TASK_MONITOR);

            return taskInfoLeader;
        } else {
            taskDAO.insert(taskInfo);
            if (taskInfo.getLeaderTaskId() == null) {
                monitorService.createAllMonitor(taskInfo.getId(), MonitorCat.TASK_MONITOR);
            }
            return taskInfo;
        }
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
     * 通过同步模式获取任务列表
     *
     * @param taskSyncModeEnum
     * @return
     */
    public List<TaskInfo> findListBySyncMode(TaskSyncModeEnum taskSyncModeEnum, Long start, Long pageSize) {
        return taskDAO.findListBySyncMode(taskSyncModeEnum.getCode(), start, pageSize);
    }

    /**
     * 通过同步模式统计任务数
     *
     * @param taskSyncModeEnum
     * @return
     */
    public Long countTasksBySyncMode(TaskSyncModeEnum taskSyncModeEnum) {
        return taskDAO.countTasksBySyncMode(taskSyncModeEnum.getCode());
    }

    /**
     * 批量更新任务状态
     *
     * @param taskIdList
     * @param targetState
     * @return
     */
    public List<TaskInfo> batchUpdateTaskStatus(List taskIdList, TargetState targetState) {
        return taskDAO.batchUpdateTaskStatus(taskIdList, targetState);
    }

    public List<TaskInfo> findAcrossLabList() {
        return taskDAO.findAcrossLabList();
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

    @Override
    public List<TaskInfo> findAcrossLabTaskListByMsList(List<Long> mediaSourceIdList) {

        //结果
        List<TaskInfo> resultList = new ArrayList<TaskInfo>();
        //查询夸机房任务
        int count = mediaSourceIdList.size();
        int pageSize = 1000;//步长
        int totalPage = (count + pageSize - 1) / pageSize;
        for (int i = 0; i < totalPage; i++) {
            int start = i * pageSize;
            int end = (i + 1) * pageSize;
            if (end > count) {
                end = count;
            }
            List<Long> tempList = mediaSourceIdList.subList(start, end);
            //停止
            List<TaskInfo> list = taskDAO.findAcrossLabTaskListByMsList(tempList);
            resultList.addAll(list);
        }
        return resultList;
    }

    /**
     * 批量查询任务信息
     */
    @Override
    public List<TaskInfo> findTaskInfoByBatchId(List<Long> taskIdList) {

        //结果
        List<TaskInfo> resultList = new ArrayList<TaskInfo>();
        //查询夸机房任务
        int count = taskIdList.size();
        int pageSize = 1000;//步长
        int totalPage = (count + pageSize - 1) / pageSize;
        for (int i = 0; i < totalPage; i++) {
            int start = i * pageSize;
            int end = (i + 1) * pageSize;
            if (end > count) {
                end = count;
            }
            List<Long> tempList = taskIdList.subList(start, end);
            //停止
            List<TaskInfo> list = taskDAO.findTaskInfoByBatchId(tempList);
            resultList.addAll(list);
        }
        return resultList;
    }

    @Override
    @Transactional
    public TaskInfo createTask(MediaSourceInfo srcMediaSourceInfo, MediaSourceInfo targetMediaSourceInfo, Long groupId, String zkServer,
                               String currentEnv) throws CloneNotSupportedException {
        MediaSourceInfo srcRealMediaSourceInfo = mediaService.getRealDataSource(srcMediaSourceInfo);
        MediaSourceType targetMediaSourceType = mediaService.getRealDataSource(targetMediaSourceInfo).getType();
        String taskName = "";
        if (srcRealMediaSourceInfo.getType() == MediaSourceType.HBASE) {
            return null;
            //taskName = srcMediaSourceInfo.getName()+"_2_"+mediaNameMappingMap.get(targetMediaSourceType);
        }

        if(ENV_PROD.equals(currentEnv)){
            taskName = srcMediaSourceInfo.getName()+"_2_"+targetMediaSourceType.name().toLowerCase();
        }else {
            taskName = srcMediaSourceInfo.getName()+"_2_all";
        }

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setGroupId(groupId);
        taskInfo.setTaskName(taskName);
        taskInfo.setTaskDesc(taskName);
        taskInfo.setTargetState(TargetState.STARTED);
        taskInfo.setTaskSyncMode(TaskSyncModeEnum.singleLabSync.getCode());
        taskInfo.setReaderMediaSourceId(srcMediaSourceInfo.getId());

        configTaskReader(taskInfo, srcMediaSourceInfo, srcRealMediaSourceInfo.getType(), zkServer);
        configTaskWriter(taskInfo, targetMediaSourceType);
        //单机房同步
        taskInfo.setTaskParameter("{}");

        //保存task
        taskDAO.insert(taskInfo);

        monitorService.createAllMonitor(taskInfo.getId(), MonitorCat.TASK_MONITOR);

        //如果是habse 则要判断hbase的HReginServer个数，创建相应个数的task
        if (srcRealMediaSourceInfo.getType() == MediaSourceType.HBASE) {
            HbaseStatus hbaseStatus = HBaseUtil.getClusterStatus(srcRealMediaSourceInfo);
            int size = hbaseStatus.getServersSize();
            List<TaskInfo> taskInfoList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                TaskInfo taskInfoBak = taskInfo.clone();
                taskInfoBak.setTaskName(taskInfo.getTaskName().replaceAll("_0", "_" + i));
                taskInfoBak.setTaskDesc(taskInfoBak.getTaskName());
                taskInfoBak.setIsLeaderTask(false);
                taskInfoBak.setLeaderTaskId(taskInfo.getId());
                taskInfoList.add(taskInfo);
                taskDAO.batchInsertTask(taskInfoList);
            }
        }
        return taskInfo;
    }

    public void configTaskReader(TaskInfo taskInfo, MediaSourceInfo srcMediaSourceInfo, MediaSourceType srcMediaSourceType, String zkServer) {
        if (srcMediaSourceType == MediaSourceType.MYSQL || srcMediaSourceType == MediaSourceType.SDDL) {
            MysqlReaderParameter mysqlReaderParameter = new MysqlReaderParameter();
            taskInfo.setTaskType(TaskType.MYSQL);
            mysqlReaderParameter.setMediaSourceId(srcMediaSourceInfo.getId());
            taskInfo.setTaskReaderParameter(mysqlReaderParameter.toJsonString());
        } else if (srcMediaSourceType == MediaSourceType.HBASE) {
            HBaseReaderParameter hbaseReaderParameter = new HBaseReaderParameter();
            Long srcMediaSourceId = srcMediaSourceInfo.getId();
            if (srcMediaSourceInfo.getType() == MediaSourceType.VIRTUAL) {
                srcMediaSourceInfo = mediaSourceService.findRealSignleByMsIdAndLab(srcMediaSourceInfo.getId(), LabEnum.logicA.getCode());
            }
            String znodeParent = ((HBaseMediaSrcParameter) srcMediaSourceInfo.getParameterObj()).getZnodeParent();
            String group = "/hrdl_" + srcMediaSourceInfo.getId() + "_" + znodeParent.replace("/", "");

            //设置task名称
            taskInfo.setTaskName(taskInfo.getTaskName() + "_0");
            taskInfo.setIsLeaderTask(true);
            taskInfo.setTaskType(TaskType.HBASE);
            hbaseReaderParameter.setMediaSourceId(srcMediaSourceId);
            hbaseReaderParameter.setReplZkMediaSourceId(mediaService.buildZkMediaSources(zkServer).get(0).getId());
            hbaseReaderParameter.setReplZnodeParent(group);
            taskInfo.setTaskReaderParameter(hbaseReaderParameter.toJsonString());
        }/*else if (srcMediaSourceType == MediaSourceType.FLEXIBLEQ){
            String topic = ((FqMediaSrcParameter) srcMediaSourceInfo.getParameterObj()).getTopic();
            String group = topic + "_group";
            FqReaderParameter fqReaderParameter = new FqReaderParameter();
            taskInfo.setTaskType(TaskType.FLEXIBLEQ);
            fqReaderParameter.setMediaSourceId(srcMediaSourceInfo.getId());
            fqReaderParameter.setGroup(group);

        }*/
    }

    /**
     * 配置task的writer参数，使用默认值
     *
     * @param findTask
     * @param targetMediaSourceType
     */
    @Override
    public void configTaskWriter(TaskInfo findTask, MediaSourceType targetMediaSourceType) {
        List<PluginWriterParameter> writerParameterList = new ArrayList<>();
        if (!StringUtils.isEmpty(findTask.getTaskWriterParameter())) {
            writerParameterList = findTask.getTaskWriterParameterObjs();
        }
        if (targetMediaSourceType.isRdbms()) {
            writerParameterList.add(new RdbmsWriterParameter());
        } else if (MediaSourceType.HDFS == targetMediaSourceType) {
            writerParameterList.add(new HdfsWriterParameter());
        } else if (MediaSourceType.ELASTICSEARCH == targetMediaSourceType) {
            writerParameterList.add(new EsWriterParameter());
        } else if (MediaSourceType.FLEXIBLEQ == targetMediaSourceType) {
            writerParameterList.add(new FqWriterParameter());
        } else if (MediaSourceType.HBASE == targetMediaSourceType) {
            writerParameterList.add(new HBaseWriterParameter());
        } else if (MediaSourceType.SDDL == targetMediaSourceType) {
            writerParameterList.add(new SddlWriterParameter());
        } else if (MediaSourceType.KUDU == targetMediaSourceType) {
            writerParameterList.add(new KuduWriterParameter());
        } else if (MediaSourceType.KAFKA == targetMediaSourceType) {
            writerParameterList.add(new KafkaWriterParameter());
        }else if (MediaSourceType.DOVE == targetMediaSourceType) {
            writerParameterList.add(new DoveWriterParameter());
        }
        findTask.setTaskWriterParameter(Parameter.listToJsonString(writerParameterList));
    }

    @Override
    public List<TaskInfo> findTaskListNoPage(TaskInfo taskInfo){
        return taskDAO.findTaskListNoPage(taskInfo);
    }

}
