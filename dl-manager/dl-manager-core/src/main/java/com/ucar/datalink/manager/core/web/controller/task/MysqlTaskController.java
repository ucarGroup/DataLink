package com.ucar.datalink.manager.core.web.controller.task;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.domain.Parameter;
import com.ucar.datalink.domain.alarm.AlarmPriorityInfo;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.reader.mysql.GroupSinkMode;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderParameter;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.plugin.writer.dove.DoveWriterParameter;
import com.ucar.datalink.domain.plugin.writer.es.EsWriterParameter;
import com.ucar.datalink.domain.plugin.writer.fq.FqWriterParameter;
import com.ucar.datalink.domain.plugin.writer.fq.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.fq.SerializeMode;
import com.ucar.datalink.domain.plugin.writer.hbase.HBaseWriterParameter;
import com.ucar.datalink.domain.plugin.writer.hdfs.CommitMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.domain.plugin.writer.kafka.KafkaWriterParameter;
import com.ucar.datalink.domain.plugin.writer.kudu.KuduWriterParameter;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.domain.task.*;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaMapping.MediaMappingView;
import com.ucar.datalink.manager.core.web.dto.task.MysqlTaskModel;
import com.ucar.datalink.manager.core.web.dto.task.TaskModel;
import com.ucar.datalink.manager.core.web.dto.task.TaskShadowView;
import com.ucar.datalink.manager.core.web.dto.task.TaskView;
import com.ucar.datalink.manager.core.web.util.AuditLogInfoUtil;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2017/1/12.
 */
@Controller
@RequestMapping(value = "/mysqlTask/")
public class MysqlTaskController extends BaseTaskController {

    private static Logger logger = LoggerFactory.getLogger(MysqlTaskController.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Autowired
    private GroupService groupService;

    @Autowired
    private TaskConfigService taskService;

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    LabService labService;

    @Autowired
    @Qualifier("taskPositionServiceZkImpl")
    private TaskPositionService taskPositionService;

    @Autowired
    TaskSyncStatusService taskSyncStatusService;

    @Autowired
    private AlarmPriorityService alarmPriorityService;


    @RequestMapping(value = "/mysqlTaskList")
    public ModelAndView listMysqlTasks() {
        ModelAndView mav = new ModelAndView("task/mysqlTaskList");
        List<TaskInfo> taskList = taskService.getList();
        mav.addObject("srcTypeList", MediaSourceType.getMysqlTaskSrcTypes());
        mav.addObject("taskList", CollectionUtils.isEmpty(taskList) ? taskList : taskList.stream().filter(t -> t.getTaskType() == TaskType.MYSQL).collect(Collectors.toList()));
        mav.addObject("groupList", groupService.getAllGroups());
        mav.addObject("mediaSourceList", mediaService.getMediaSourcesByTypes(MediaSourceType.MYSQL, MediaSourceType.SDDL, MediaSourceType.VIRTUAL));
        return mav;
    }

    @RequestMapping(value = "/toAddMysqlTask")
    public ModelAndView toAddMysqlTask() {
        ModelAndView mav = new ModelAndView("task/mysqlTaskAdd");
        MysqlReaderParameter mysqlReaderParameter = new MysqlReaderParameter();
        mysqlReaderParameter.setMultiplexingRead(ManagerConfig.current().getMultiplexingRead());
        List<AlarmPriorityInfo> alarmPriorityInfoList = alarmPriorityService.getAll();
        MysqlTaskModel mysqlTaskModel = new MysqlTaskModel(
                new TaskModel.TaskBasicInfo(null, null, null, null, null, null, null,null),
                getWriterParameters(),
                groupService.getAllGroups(),
                TargetState.getAllStates(),
                findMediaSourcesBySyncMode(TaskSyncModeEnum.singleLabSync, null),
                PluginWriterParameter.RetryMode.getAllModes(),
                RdbmsWriterParameter.SyncMode.getAllModes(),
                CommitMode.getAllCommitModes(),
                Lists.newArrayList(EventType.INSERT, EventType.UPDATE, EventType.DELETE),
                GroupSinkMode.getAll(),
                SerializeMode.getAllSerializeModes(),
                PartitionMode.getAllPartitionModes(),
                mysqlReaderParameter,
                labService.findLabList(),
                TaskSyncModeEnum.toList(),
                null
        );
        mav.addObject("taskModel", mysqlTaskModel);
        mav.addObject("alarmPriorityInfoList",alarmPriorityInfoList);
        return mav;
    }

    @RequestMapping(value = "/findMediaSourcesBySyncMode")
    @ResponseBody
    public Map<String, Object> findMediaSourcesBySyncMode(String taskSyncMode, Long sourceLabId) {
        Map<String, Object> map = new HashMap<>();
        TaskSyncModeEnum syncModeEnum = TaskSyncModeEnum.getEnumByCode(taskSyncMode);
        map.put("mediaSourceList", findMediaSourcesBySyncMode(syncModeEnum, sourceLabId));
        return map;
    }

    private List<MediaSourceInfo> findMediaSourcesBySyncMode(TaskSyncModeEnum syncModeEnum, Long sourceLabId) {
        List<MediaSourceInfo> mediaSourceInfoList;
        //真实类型
        List<MediaSourceType> types = new ArrayList<MediaSourceType>();
        types.add(MediaSourceType.MYSQL);
        types.add(MediaSourceType.SDDL);

        if (TaskSyncModeEnum.singleLabSync.equals(syncModeEnum)) {
            mediaSourceInfoList = mediaService.findMediaSourcesForSingleLab(types);
        } else {
            mediaSourceInfoList = mediaService.findMediaSourcesForAcrossLab(sourceLabId, types);
        }
        return mediaSourceInfoList;
    }

    @RequestMapping(value = "/doAddMysqlTask")
    @ResponseBody
    public String doAddMysqlTask(@RequestBody MysqlTaskModel mysqlTaskModel) {
        try {
            checkRequired(mysqlTaskModel);

            if (mysqlTaskModel.getIsMultiCopy() && (mysqlTaskModel.getWriterParameterMap().size() != 1 || (!mysqlTaskModel.getWriterParameterMap().containsKey("writer-es")))) {
                throw new RuntimeException("多路复制，Writer只能选择es");
            }

            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setGroupId(mysqlTaskModel.getTaskBasicInfo().getGroupId());
            taskInfo.setTaskName(mysqlTaskModel.getTaskBasicInfo().getTaskName());
            taskInfo.setTaskDesc(mysqlTaskModel.getTaskBasicInfo().getTaskDesc());
            taskInfo.setTargetState(mysqlTaskModel.getTaskBasicInfo().getTargetState());

            taskInfo.setLabId(mysqlTaskModel.getTaskBasicInfo().getLabId());
            taskInfo.setTaskSyncMode(mysqlTaskModel.getTaskBasicInfo().getTaskSyncMode());

            taskInfo.setReaderMediaSourceId(mysqlTaskModel.getMysqlReaderParameter().getMediaSourceId());
            taskInfo.setTaskType(TaskType.MYSQL);
            taskInfo.setTaskReaderParameter(mysqlTaskModel.getMysqlReaderParameter().toJsonString());
            taskInfo.setTaskWriterParameter(Parameter.listToJsonString(
                    mysqlTaskModel.getWriterParameterMap().entrySet()
                            .stream()
                            .map(i -> i.getValue())
                            .collect(Collectors.toList()))
            );
            //单机房同步
            if (TaskSyncModeEnum.getEnumByCode(mysqlTaskModel.getTaskBasicInfo().getTaskSyncMode()).equals(TaskSyncModeEnum.singleLabSync)) {
                taskInfo.setTaskParameter("{}");
            } else {
                taskInfo.setTaskParameter(mysqlTaskModel.getTaskParameter().toJsonString());
            }

            if(mysqlTaskModel.getTaskBasicInfo().getAlarmPriorityId() != null) {
                AlarmPriorityInfo alarmPriorityInfo = alarmPriorityService.getById(mysqlTaskModel.getTaskBasicInfo().getAlarmPriorityId());
                taskInfo.setAlarmPriorityId(mysqlTaskModel.getTaskBasicInfo().getAlarmPriorityId());
                taskInfo.setTaskPriorityId(alarmPriorityInfo.getPriority());
            }

            TaskInfo t = taskService.addMySqlTask(taskInfo, mysqlTaskModel.getIsMultiCopy());
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromTaskInfo(t
                    , "004010102", AuditLogOperType.insert.getValue()));
        } catch (Exception e) {
            logger.error("Add MysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/toUpdateMysqlTask")
    public ModelAndView toUpdateMysqlTask(Long id) {
        ModelAndView mav = new ModelAndView("task/mysqlTaskUpdate");

        TaskInfo taskInfo = taskService.getTask(id);
        Map<String, PluginWriterParameter> writerParameterMap = getWriterParameters();
        taskInfo.getTaskWriterParameterObjs().forEach(i -> writerParameterMap.put(i.getPluginName(), i));
        List<AlarmPriorityInfo> alarmPriorityInfoList = alarmPriorityService.getAll();
        MysqlTaskModel mysqlTaskModel = new MysqlTaskModel(
                new TaskModel.TaskBasicInfo(
                        id,
                        taskInfo.getTaskName(),
                        taskInfo.getTaskDesc(),
                        taskInfo.getTargetState(),
                        taskInfo.getGroupId(),
                        taskInfo.getLabId(),
                        taskInfo.getTaskSyncMode(),
                        taskInfo.getAlarmPriorityId()
                ),
                writerParameterMap,
                groupService.getAllGroups().stream().filter(i -> i.getId().equals(taskInfo.getGroupId())).collect(Collectors.toList()),
                TargetState.getAllStates(),
                findMediaSourcesBySyncMode(TaskSyncModeEnum.getEnumByCode(taskInfo.getTaskSyncMode()), taskInfo.getTaskParameterObj().getSourceLabId()),
                PluginWriterParameter.RetryMode.getAllModes(),
                RdbmsWriterParameter.SyncMode.getAllModes(),
                CommitMode.getAllCommitModes(),
                Lists.newArrayList(EventType.INSERT, EventType.UPDATE, EventType.DELETE),
                GroupSinkMode.getAll(),
                SerializeMode.getAllSerializeModes(),
                PartitionMode.getAllPartitionModes(),
                (MysqlReaderParameter) taskInfo.getTaskReaderParameterObj(),
                labService.findLabList(),
                TaskSyncModeEnum.toList(),
                taskInfo.getTaskParameterObj()
        );
        mysqlTaskModel.setCurrentWriters(taskInfo.getTaskWriterParameterObjs().stream().collect(Collectors.toMap(PluginWriterParameter::getPluginName, i -> "1")));
        mav.addObject("taskModel", mysqlTaskModel);
        mav.addObject("alarmPriorityInfoList",alarmPriorityInfoList);
        return mav;
    }

    @RequestMapping(value = "/doUpdateMysqlTask")
    @ResponseBody
    public String doUpdateMysqlTask(@RequestBody MysqlTaskModel mysqlTaskModel) {
        try {
            checkRequired(mysqlTaskModel);

            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setId(mysqlTaskModel.getTaskBasicInfo().getId());
            taskInfo.setGroupId(mysqlTaskModel.getTaskBasicInfo().getGroupId());
            taskInfo.setTaskName(mysqlTaskModel.getTaskBasicInfo().getTaskName());
            taskInfo.setTaskDesc(mysqlTaskModel.getTaskBasicInfo().getTaskDesc());
            taskInfo.setTargetState(mysqlTaskModel.getTaskBasicInfo().getTargetState());

            taskInfo.setLabId(mysqlTaskModel.getTaskBasicInfo().getLabId());
            taskInfo.setTaskSyncMode(mysqlTaskModel.getTaskBasicInfo().getTaskSyncMode());

            taskInfo.setReaderMediaSourceId(mysqlTaskModel.getMysqlReaderParameter().getMediaSourceId());
            taskInfo.setTaskReaderParameter(mysqlTaskModel.getMysqlReaderParameter().toJsonString());
            taskInfo.setTaskWriterParameter(Parameter.listToJsonString(
                    mysqlTaskModel.getWriterParameterMap().entrySet()
                            .stream()
                            .map(i -> i.getValue())
                            .collect(Collectors.toList()))
            );

            //单机房同步
            if (TaskSyncModeEnum.getEnumByCode(mysqlTaskModel.getTaskBasicInfo().getTaskSyncMode()).equals(TaskSyncModeEnum.singleLabSync)) {
                taskInfo.setTaskParameter("{}");
            } else {
                taskInfo.setTaskParameter(mysqlTaskModel.getTaskParameter().toJsonString());
            }

            if(mysqlTaskModel.getTaskBasicInfo().getAlarmPriorityId() != null) {
                AlarmPriorityInfo alarmPriorityInfo = alarmPriorityService.getById(mysqlTaskModel.getTaskBasicInfo().getAlarmPriorityId());
                taskInfo.setAlarmPriorityId(mysqlTaskModel.getTaskBasicInfo().getAlarmPriorityId());
                taskInfo.setTaskPriorityId(alarmPriorityInfo.getPriority());
            }

            taskService.updateTask(taskInfo);
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromTaskInfo(taskInfo, "004010104", AuditLogOperType.update.getValue()));
        } catch (Exception e) {
            logger.error("Update MysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/deleteMysqlTask")
    @ResponseBody
    public String deleteMysqlTask(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            TaskInfo task = taskService.getTask(id);
            taskService.deleteTask(id);
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromTaskInfo(task, "004010105", AuditLogOperType.delete.getValue()));
        } catch (Exception e) {
            logger.error("Delete MysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/pauseMysqlTask")
    @ResponseBody
    public String pauseMysqlTask(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            taskService.pauseTask(id);
            TaskInfo task = taskService.getTask(id);
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromTaskInfo(task, "004010106", AuditLogOperType.other.getValue()));
        } catch (Exception e) {
            logger.error("Pause MysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/resumeMysqlTask")
    @ResponseBody
    public String resumeMysqlTask(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            taskService.resumeTask(id);
            TaskInfo task = taskService.getTask(id);
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromTaskInfo(task
                    , "004010107", AuditLogOperType.other.getValue()));
        } catch (Exception e) {
            logger.error("Pause MysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/toRestartMysqlTask")
    public ModelAndView toRestartMysqlTask(String id) {
        ModelAndView mav = new ModelAndView("task/mysqlTaskRestart");
        mav.addObject("id", id);
        return mav;
    }

    @RequestMapping(value = "/doRestartMysqlTask")
    @ResponseBody
    public String doRestartMysqlTask(@RequestBody Map<String, String> restartParams) {
        try {
            String ids = restartParams.get("id");
            boolean resetPosition = Boolean.valueOf(restartParams.get("resetPosition"));
            String[] idArray = ids.split(",");
            for (String id : idArray) {
                if (resetPosition) {
                    Long newTimeStamps = Long.valueOf(restartParams.get("newTimeStamps"));
                    MysqlReaderPosition position = (MysqlReaderPosition) taskPositionService.getPosition(id);
                    if (position == null) {
                        throw new ValidationException("还未开始消费，无法重置位点");
                    } else {
                        position.setTimestamp(newTimeStamps);
                        position.setSourceAddress(new InetSocketAddress("0.0.0.0", position.getSourceAddress().getPort()));
                        sendRestartCommand(id, position);
                    }
                } else {
                    sendRestartCommand(id, null);
                }

                TaskInfo task = taskService.getTask(Long.valueOf(id));
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromTaskInfo(task
                        , "004010109", AuditLogOperType.other.getValue()));
            }
        } catch (Exception e) {
            logger.error("Restart MysqlTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/mysqlTaskDatas")
    @ResponseBody
    public Page<TaskView> mysqlTaskDatas(@RequestBody Map<String, String> map) {
        String srcType = map.get("srcType");
        Long readerMediaSourceId = Long.valueOf(map.get("readerMediaSourceId"));
        Long groupId = Long.valueOf(map.get("groupId"));
        Long id = Long.valueOf(map.get("id"));
        Page<TaskView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<TaskInfo> taskInfos = taskService.listTasksForQueryPage(
                srcType.equals("-1") ? null : MediaSourceType.valueOf(srcType),
                readerMediaSourceId == -1L ? null : readerMediaSourceId,
                groupId == -1L ? null : groupId,
                id == -1L ? null : id,
                TaskType.MYSQL);

        //构造view
        List<TaskView> taskViews = taskInfos.stream().map(i -> {
            TaskView view = new TaskView();
            view.setId(i.getId());
            view.setTaskName(i.getTaskName());
            view.setTaskDesc(i.getTaskDesc());
            view.setTargetState(i.getTargetState());
            view.setGroupId(i.getGroupId());
            view.setLabName(StringUtils.isNotBlank(i.getLabName()) ? i.getLabName() : "");
            view.setTaskSyncMode(TaskSyncModeEnum.getEnumByCode(i.getTaskSyncMode()).getName());
            view.setTaskPriorityId(i.getTaskPriorityId()!=null?String.valueOf(i.getTaskPriorityId()):"-1");
            return view;
        }).collect(Collectors.toList());

        //为view设置workerId属性
        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();
        Map<Long, Long> twm = clusterState.getTaskWorkerMapping();
        taskViews.forEach(t -> t.setWorkerId(twm.get(t.getId())));

        //为View设置TaskStatus属性
        Collection<TaskStatus> allStatus = taskStatusService.getAll();
        allStatus.stream().forEach(s ->
                taskViews.forEach(t -> {
                    if (t.getId().toString().equals(s.getId())) {
                        t.setListenedState(s.getState());
                    }
                })
        );

        //为View设置Position、status相关属性
        taskViews.stream().forEach(i -> {
            MysqlReaderPosition position = (MysqlReaderPosition) taskPositionService.getPosition(i.getId().toString());
            if (position != null) {
                i.setCurrentLogFile(position.getJournalName());
                i.setCurrentLogPosition(position.getPosition());
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(position.getTimestamp()));
                i.setCurrentTimeStamp(time + " (" + position.getTimestamp() + ")");

                InetSocketAddress inetSocketAddress = position.getSourceAddress();
                InetAddress inetAddress = inetSocketAddress.getAddress();
                String ip = inetAddress.getHostAddress();
                i.setReaderIp(ip);

                //最后binlog文件名
                if (StringUtils.isNotBlank(position.getLatestEffectSyncLogFileName())) {
                    i.setLatestEffectSyncLogFileName(position.getLatestEffectSyncLogFileName());
                } else {
                    i.setLatestEffectSyncLogFileName("");
                }
                //最后binlog位点
                i.setLatestEffectSyncLogFileOffset(position.getLatestEffectSyncLogFileOffset() == null ? "" : String.valueOf(position.getLatestEffectSyncLogFileOffset()));
            } else {
                i.setReaderIp("");
                i.setLatestEffectSyncLogFileName("");
                i.setLatestEffectSyncLogFileOffset("");
            }
            //设置影子位点
            if(position!=null && position.getShadowPosition() != null) {
                MysqlReaderPosition shadowPosition = position.getShadowPosition();
                String shadowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(shadowPosition.getTimestamp()));
                i.setShadowCurrentTimeStamp(shadowTime + " (" + shadowPosition.getTimestamp() + ")");
                i.setShadowLatestEffectSyncLogFileName(shadowPosition.getLatestEffectSyncLogFileName() == null ? "" : String.valueOf(shadowPosition.getLatestEffectSyncLogFileName()));
                i.setShadowLatestEffectSyncLogFileOffset(shadowPosition.getLatestEffectSyncLogFileOffset() == null ? "" : String.valueOf(shadowPosition.getLatestEffectSyncLogFileOffset()));
            }else {
                i.setShadowCurrentTimeStamp("");
                i.setShadowLatestEffectSyncLogFileName("");
                i.setShadowLatestEffectSyncLogFileOffset("");
            }
            //启动时间
            TaskStatus taskStatus = taskStatusService.getStatus(i.getId().toString());
            if (taskStatus != null) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(taskStatus.getStartTime()));
                i.setStartTime(time);
            } else {
                i.setStartTime("");
            }
            i.setDetail("");

            //任务同步状态
            TaskSyncStatus taskSyncStatus = taskSyncStatusService.getSyncStatus(String.valueOf(i.getId()));
            if (taskSyncStatus != null) {
                i.setTaskSyncStatus(taskSyncStatus.getState().name());
            } else {
                i.setTaskSyncStatus("");
            }

        });
        PageInfo<TaskInfo> pageInfo = new PageInfo<>(taskInfos);
        page.setDraw(page.getDraw());
        page.setAaData(taskViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    private Map<String, PluginWriterParameter> getWriterParameters() {
        return Lists.newArrayList(new RdbmsWriterParameter(), new FqWriterParameter(), new DoveWriterParameter(),new EsWriterParameter(), new HdfsWriterParameter(), new HBaseWriterParameter(), new KuduWriterParameter(),new KafkaWriterParameter())
                .stream()
                .collect(Collectors.toMap(PluginWriterParameter::getPluginName, i -> i));
    }

    private void checkRequired(MysqlTaskModel taskModel) {
        if (StringUtils.isBlank(taskModel.getTaskBasicInfo().getTaskName())) {
            throw new RuntimeException("Task名称为必输项");
        }
        if (StringUtils.isBlank(taskModel.getTaskBasicInfo().getTaskDesc())) {
            throw new RuntimeException("Task描述为必输项");
        }
        if (StringUtils.isBlank(taskModel.getTaskBasicInfo().getTaskSyncMode())) {
            throw new RuntimeException("机房同步模式为必输项");
        }
        if (taskModel.getWriterParameterMap() == null || taskModel.getWriterParameterMap().isEmpty()) {
            throw new RuntimeException("请至少选择一个Writer");
        }
        if (TaskSyncModeEnum.getEnumByCode(taskModel.getTaskBasicInfo().getTaskSyncMode()).equals(TaskSyncModeEnum.acrossLabSync)) {
            if (taskModel.getTaskParameter().getSourceLabId() == null || taskModel.getTaskParameter().getTargetLabId() == null) {
                throw new RuntimeException("跨机房同步，源机房和目标机房必填");
            }
            if (taskModel.getTaskParameter().getSourceLabId().intValue() == taskModel.getTaskParameter().getTargetLabId().intValue()) {
                throw new RuntimeException("跨机房同步，源机房和目标机房不能相同");
            }
        }
    }

}
