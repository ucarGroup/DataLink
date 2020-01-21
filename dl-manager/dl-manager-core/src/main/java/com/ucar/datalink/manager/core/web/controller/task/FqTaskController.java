package com.ucar.datalink.manager.core.web.controller.task;

import com.google.common.collect.Lists;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.Parameter;
import com.ucar.datalink.domain.alarm.AlarmPriorityInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.fq.FqMediaSrcParameter;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.reader.fq.FqReaderParameter;
import com.ucar.datalink.domain.plugin.writer.es.EsWriterParameter;
import com.ucar.datalink.domain.plugin.writer.fq.FqWriterParameter;
import com.ucar.datalink.domain.plugin.writer.fq.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.fq.SerializeMode;
import com.ucar.datalink.domain.plugin.writer.hbase.HBaseWriterParameter;
import com.ucar.datalink.domain.plugin.writer.hdfs.CommitMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.domain.task.*;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.task.FqTaskModel;
import com.ucar.datalink.manager.core.web.dto.task.TaskModel;
import com.ucar.datalink.manager.core.web.dto.task.TaskView;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import com.ucar.datalink.manager.core.web.util.AuditLogInfoUtil;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sqq on 2017/6/15.
 */
@Controller
@RequestMapping(value = "/fqTask")
public class FqTaskController extends BaseTaskController{

    public static Logger logger = LoggerFactory.getLogger(FqTaskController.class);

    @Autowired
    TaskConfigService taskService;

    @Autowired
    GroupService groupService;

    @Autowired
    MediaService mediaService;

    @Autowired
    TaskStatusService taskStatusService;

    @Autowired
    MediaSourceService mediaSourceService;

    @Autowired
    LabService labService;

    @Autowired
    private AlarmPriorityService alarmPriorityService;

    @RequestMapping(value = "/fqTaskList")
    public ModelAndView fqTaskList() {
        ModelAndView mav = new ModelAndView("task/fqTaskList");
        List<TaskInfo> taskList = taskService.getList();
        mav.addObject("taskList", CollectionUtils.isEmpty(taskList) ? taskList : taskList.stream().filter(t -> t.getLeaderTaskId() == null).collect(Collectors.toList()));
        mav.addObject("groupList", groupService.getAllGroups());
        mav.addObject("mediaSourceList", mediaService.getMediaSourcesByTypes(MediaSourceType.FLEXIBLEQ));
        return mav;
    }

    @RequestMapping(value = "/initFqTaskList")
    @ResponseBody
    public Page<TaskView> initFqTaskList(@RequestBody Map<String, String> map) {
        Long readerMediaSourceId = Long.valueOf(map.get("readerMediaSourceId"));
        Long groupId = Long.valueOf(map.get("groupId"));
        Long id = Long.valueOf(map.get("id"));
        List<TaskInfo> taskInfos = taskService.listTasksForQueryPage(
                MediaSourceType.FLEXIBLEQ,
                readerMediaSourceId == -1L ? null : readerMediaSourceId,
                groupId == -1L ? null : groupId,
                id == -1L ? null : id,
                TaskType.FLEXIBLEQ);
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

        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();
        Map<Long, Long> taskWorkerMap = clusterState.getTaskWorkerMapping();
        taskViews.forEach(t -> t.setWorkerId(taskWorkerMap.get(t.getId())));

        Collection<TaskStatus> allStatus = taskStatusService.getAll();
        allStatus.stream().forEach(s -> taskViews.forEach(t -> {
            if (t.getId().toString().equals(s.getId())) {
                t.setListenedState(s.getState());
            }
        }));

        //为View设置Position、status相关属性
        taskViews.stream().forEach(i -> {
            i.setCurrentTimeStamp(null);
            i.setCurrentLogFile(null);
            i.setCurrentLogPosition(null);

            //启动时间
            TaskStatus taskStatus = taskStatusService.getStatus(i.getId().toString());
            if(taskStatus != null){
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(taskStatus.getStartTime()));
                i.setStartTime(time);
            }else{
                i.setStartTime("");
            }
        });

        return new Page<TaskView>(taskViews);
    }

    @RequestMapping(value = "/toAddFqTask")
    public ModelAndView toAddFqTask() {
        ModelAndView mav = new ModelAndView("task/fqTaskAdd");
        FqTaskModel fqTaskModel = new FqTaskModel(
                new TaskModel.TaskBasicInfo(null, null, null, null, null,null,null,null),
                getWriterParameters(),
                groupService.getAllGroups(),
                TargetState.getAllStates(),
                mediaService.getMediaSourcesByTypes(MediaSourceType.FLEXIBLEQ),
                mediaService.getMediaSourcesByTypes(MediaSourceType.MYSQL, MediaSourceType.SDDL, MediaSourceType.SQLSERVER, MediaSourceType.ORACLE, MediaSourceType.HBASE, MediaSourceType.HDFS, MediaSourceType.ELASTICSEARCH, MediaSourceType.POSTGRESQL),
                PluginWriterParameter.RetryMode.getAllModes(),
                RdbmsWriterParameter.SyncMode.getAllModes(),
                new FqReaderParameter(),
                CommitMode.getAllCommitModes(),
                SerializeMode.getAllSerializeModes(),
                PartitionMode.getAllPartitionModes(),
                labService.findLabList(),
                TaskSyncModeEnum.toList()
        );
        List<AlarmPriorityInfo> alarmPriorityInfoList = alarmPriorityService.getAll();
        mav.addObject("taskModel", fqTaskModel);
        mav.addObject("alarmPriorityInfoList",alarmPriorityInfoList);
        return mav;
    }

    @RequestMapping(value = "/doAddFqTask")
    @ResponseBody
    public String doAddFqTask(@RequestBody FqTaskModel fqTaskModel) {
        try {
            checkRequired(fqTaskModel);
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setGroupId(fqTaskModel.getTaskBasicInfo().getGroupId());
            taskInfo.setTaskName(fqTaskModel.getTaskBasicInfo().getTaskName());
            taskInfo.setTaskDesc(fqTaskModel.getTaskBasicInfo().getTaskDesc());
            taskInfo.setTargetState(fqTaskModel.getTaskBasicInfo().getTargetState());

            taskInfo.setLabId(fqTaskModel.getTaskBasicInfo().getLabId());
            taskInfo.setTaskSyncMode(fqTaskModel.getTaskBasicInfo().getTaskSyncMode());

            taskInfo.setReaderMediaSourceId(fqTaskModel.getFqReaderParameter().getMediaSourceId());
            taskInfo.setTaskType(TaskType.FLEXIBLEQ);
            taskInfo.setTaskReaderParameter(fqTaskModel.getFqReaderParameter().toJsonString());
            taskInfo.setTaskWriterParameter(Parameter.listToJsonString(
                            fqTaskModel.getWriterParameterMap().entrySet()
                                    .stream()
                                    .map(i -> i.getValue())
                                    .collect(Collectors.toList()))
            );
            taskInfo.setTaskParameter("{}");

            if(fqTaskModel.getTaskBasicInfo().getAlarmPriorityId()!=null) {
                AlarmPriorityInfo alarmPriorityInfo = alarmPriorityService.getById(fqTaskModel.getTaskBasicInfo().getAlarmPriorityId());
                taskInfo.setAlarmPriorityId(fqTaskModel.getTaskBasicInfo().getAlarmPriorityId());
                taskInfo.setTaskPriorityId(alarmPriorityInfo.getPriority());
            }

            taskService.addTask(taskInfo);
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromTaskInfo(taskInfo,"004010303", AuditLogOperType.insert.getValue()));
        } catch (Exception e) {
            logger.error("Add FqTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/toUpdateFqTask")
    public ModelAndView toUpdateFqTask(Long id) {
        ModelAndView mav = new ModelAndView("task/fqTaskUpdate");
        TaskInfo taskInfo = taskService.getTask(id);
        List<AlarmPriorityInfo> alarmPriorityInfoList = alarmPriorityService.getAll();
        Map<String, PluginWriterParameter> writerParameterMap = getWriterParameters();
        taskInfo.getTaskWriterParameterObjs().forEach(i -> writerParameterMap.put(i.getPluginName(), i));
        FqTaskModel fqTaskModel = new FqTaskModel(
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
                groupService.getAllGroups(),
                TargetState.getAllStates(),
                mediaService.getMediaSourcesByTypes(MediaSourceType.FLEXIBLEQ),
                mediaService.getMediaSourcesByTypes(MediaSourceType.MYSQL, MediaSourceType.SDDL, MediaSourceType.SQLSERVER, MediaSourceType.ORACLE, MediaSourceType.HBASE, MediaSourceType.HDFS, MediaSourceType.ELASTICSEARCH, MediaSourceType.POSTGRESQL),
                PluginWriterParameter.RetryMode.getAllModes(),
                RdbmsWriterParameter.SyncMode.getAllModes(),
                (FqReaderParameter) taskInfo.getTaskReaderParameterObj(),
                CommitMode.getAllCommitModes(),
                SerializeMode.getAllSerializeModes(),
                PartitionMode.getAllPartitionModes(),
                labService.findLabList(),
                TaskSyncModeEnum.toList()
        );
        fqTaskModel.setCurrentWriters(taskInfo.getTaskWriterParameterObjs().stream().collect(Collectors.toMap(PluginWriterParameter::getPluginName, i -> "1")));
        mav.addObject("taskModel", fqTaskModel);
        mav.addObject("alarmPriorityInfoList",alarmPriorityInfoList);
        return mav;
    }

    @RequestMapping(value = "/doUpdateFqTask")
    @ResponseBody
    public String doUpdateFqTask(@RequestBody FqTaskModel fqTaskModel) {
        try {
            checkRequired(fqTaskModel);
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setId(fqTaskModel.getTaskBasicInfo().getId());
            taskInfo.setGroupId(fqTaskModel.getTaskBasicInfo().getGroupId());
            taskInfo.setTaskName(fqTaskModel.getTaskBasicInfo().getTaskName());
            taskInfo.setTaskDesc(fqTaskModel.getTaskBasicInfo().getTaskDesc());
            taskInfo.setTargetState(fqTaskModel.getTaskBasicInfo().getTargetState());

            taskInfo.setLabId(fqTaskModel.getTaskBasicInfo().getLabId());
            taskInfo.setTaskSyncMode(fqTaskModel.getTaskBasicInfo().getTaskSyncMode());

            taskInfo.setReaderMediaSourceId(fqTaskModel.getFqReaderParameter().getMediaSourceId());
            taskInfo.setTaskReaderParameter(fqTaskModel.getFqReaderParameter().toJsonString());
            taskInfo.setTaskWriterParameter(Parameter.listToJsonString(
                            fqTaskModel.getWriterParameterMap().entrySet()
                                    .stream()
                                    .map(i -> i.getValue())
                                    .collect(Collectors.toList()))
            );

            if(fqTaskModel.getTaskBasicInfo().getAlarmPriorityId() != null){
                AlarmPriorityInfo alarmPriorityInfo = alarmPriorityService.getById(fqTaskModel.getTaskBasicInfo().getAlarmPriorityId());
                taskInfo.setAlarmPriorityId(fqTaskModel.getTaskBasicInfo().getAlarmPriorityId());
                taskInfo.setTaskPriorityId(alarmPriorityInfo.getPriority());
            }

            taskService.updateTask(taskInfo);
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromTaskInfo(taskInfo,"004010305", AuditLogOperType.update.getValue()));
        } catch (Exception e) {
            logger.error("Update FqTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/deleteFqTask")
    @ResponseBody
    public String deleteFqTask(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            TaskInfo taskInfo = taskService.getTask(id);
            taskService.deleteTask(id);
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromTaskInfo(taskInfo,"004010306", AuditLogOperType.delete.getValue()));
        } catch (Exception e) {
            logger.error("Delete FqTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/getListenerGroup")
    @ResponseBody
    @AuthIgnore
    public Map<String, Object> getListenerGroup(Long mediaSourceId) {
        Map<String, Object> map = new HashMap<>();
        MediaSourceInfo fqMediaSource = mediaSourceService.getById(mediaSourceId);
        String topic = ((FqMediaSrcParameter) fqMediaSource.getParameterObj()).getTopic();
        String group = topic + "_group";
        map.put("group", group);
        return map;
    }

    @RequestMapping(value = "/pauseFqTask")
    @ResponseBody
    public String pauseFqTask(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            taskService.pauseTask(id);
        } catch (Exception e) {
            logger.error("Pause FqTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/resumeFqTask")
    @ResponseBody
    public String resumeFqTask(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            taskService.resumeTask(id);
        } catch (Exception e) {
            logger.error("Pause FqTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    private Map<String, PluginWriterParameter> getWriterParameters() {
        return Lists.newArrayList(new RdbmsWriterParameter(), new FqWriterParameter(), new EsWriterParameter(), new HdfsWriterParameter(),new HBaseWriterParameter())
                .stream()
                .collect(Collectors.toMap(PluginWriterParameter::getPluginName, i -> i));
    }

    private void checkRequired(FqTaskModel taskModel) {
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
        if (taskModel.getFqReaderParameter().getMediaSourceId() == null) {
            throw new RuntimeException("关联数据源为必输项");
        }
        if (taskModel.getFqReaderParameter().getOriginalMediaSourceId() == null) {
            throw new RuntimeException("原始数据源为必输项");
        }
    }
}
