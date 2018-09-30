package com.ucar.datalink.manager.core.web.controller.task;

import com.google.common.collect.Lists;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.Parameter;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.hbase.HBaseMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.reader.hbase.HBaseReaderParameter;
import com.ucar.datalink.domain.plugin.writer.es.EsWriterParameter;
import com.ucar.datalink.domain.plugin.writer.hbase.HBaseWriterParameter;
import com.ucar.datalink.domain.plugin.writer.hdfs.CommitMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.domain.task.TaskType;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.task.HBaseTaskModel;
import com.ucar.datalink.manager.core.web.dto.task.TaskModel;
import com.ucar.datalink.manager.core.web.dto.task.TaskView;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
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
 * Created by lubiao on 2017/6/15.
 */
@Controller
@RequestMapping(value = "/hbaseTask")
public class HBaseTaskController extends BaseTaskController {

    public static Logger logger = LoggerFactory.getLogger(HBaseTaskController.class);

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

    @RequestMapping(value = "/hbaseTaskList")
    public ModelAndView hbaseTaskList() {
        ModelAndView mav = new ModelAndView("task/hbaseTaskList");
        List<TaskInfo> taskList = taskService.getList();
        mav.addObject("taskList", CollectionUtils.isEmpty(taskList) ? taskList : taskList.stream().filter(t -> t.getLeaderTaskId() == null).collect(Collectors.toList()));
        mav.addObject("groupList", groupService.getAllGroups());
        mav.addObject("mediaSourceList", mediaService.getMediaSourcesByTypes(MediaSourceType.HBASE));
        return mav;
    }

    @RequestMapping(value = "/initHbaseTaskList")
    @ResponseBody
    public Page<TaskView> initHbaseTaskList(@RequestBody Map<String, String> map) {
        Long readerMediaSourceId = Long.valueOf(map.get("readerMediaSourceId"));
        Long groupId = Long.valueOf(map.get("groupId"));
        Long id = Long.valueOf(map.get("id"));
        List<TaskInfo> taskInfos = taskService.listTasksForQueryPage(
                readerMediaSourceId == -1L ? null : readerMediaSourceId,
                groupId == -1L ? null : groupId,
                id == -1L ? null : id,
                TaskType.HBASE);
        List<TaskView> taskViews = taskInfos.stream().map(i -> {
            TaskView view = new TaskView();
            view.setId(i.getId());
            view.setTaskName(i.getTaskName());
            view.setTaskDesc(i.getTaskDesc());
            view.setTargetState(i.getTargetState());
            view.setGroupId(i.getGroupId());
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

        return new Page<>(taskViews);
    }

    @RequestMapping(value = "/toAddHbaseTask")
    public ModelAndView toAddHbaseTask() {
        ModelAndView mav = new ModelAndView("task/hbaseTaskAdd");
        HBaseTaskModel hbaseTaskModel = new HBaseTaskModel(
                new TaskModel.TaskBasicInfo(null, null, null, null, null),
                getWriterParameters(),
                groupService.getAllGroups(),
                TargetState.getAllStates(),
                mediaService.getMediaSourcesByTypes(MediaSourceType.HBASE),
                buildZkMediaSources(),
                PluginWriterParameter.RetryMode.getAllModes(),
                RdbmsWriterParameter.SyncMode.getAllModes(),
                new HBaseReaderParameter(),
                CommitMode.getAllCommitModes()
        );
        mav.addObject("taskModel", hbaseTaskModel);
        return mav;
    }

    @RequestMapping(value = "/doAddHbaseTask")
    @ResponseBody
    public synchronized String doAddHBaseTask(@RequestBody HBaseTaskModel hbaseTaskModel) {
        try {
            checkRequired(hbaseTaskModel);
            checkZNode(hbaseTaskModel);

            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setGroupId(hbaseTaskModel.getTaskBasicInfo().getGroupId());
            taskInfo.setTaskName(hbaseTaskModel.getTaskBasicInfo().getTaskName());
            taskInfo.setTaskDesc(hbaseTaskModel.getTaskBasicInfo().getTaskDesc());
            taskInfo.setTargetState(hbaseTaskModel.getTaskBasicInfo().getTargetState());
            taskInfo.setReaderMediaSourceId(hbaseTaskModel.getHbaseReaderParameter().getMediaSourceId());
            taskInfo.setTaskType(TaskType.HBASE);
            taskInfo.setTaskReaderParameter(hbaseTaskModel.getHbaseReaderParameter().toJsonString());
            taskInfo.setTaskWriterParameter(Parameter.listToJsonString(
                            hbaseTaskModel.getWriterParameterMap().entrySet()
                                    .stream()
                                    .map(i -> i.getValue())
                                    .collect(Collectors.toList()))
            );
            taskInfo.setTaskParameter("{}");
            taskInfo.setLeaderTaskId(getLeaderTaskId(hbaseTaskModel));
            taskInfo.setIsLeaderTask(isLeaderTask(hbaseTaskModel));
            taskService.addTask(taskInfo);
        } catch (Exception e) {
            logger.error("Add HbaseTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/toUpdateHbaseTask")
    public ModelAndView toUpdateHbaseTask(Long id) {
        ModelAndView mav = new ModelAndView("task/hbaseTaskUpdate");
        TaskInfo taskInfo = taskService.getTask(id);
        Map<String, PluginWriterParameter> writerParameterMap = getWriterParameters();
        taskInfo.getTaskWriterParameterObjs().forEach(i -> writerParameterMap.put(i.getPluginName(), i));
        HBaseTaskModel hbaseTaskModel = new HBaseTaskModel(
                new TaskModel.TaskBasicInfo(
                        id,
                        taskInfo.getTaskName(),
                        taskInfo.getTaskDesc(),
                        taskInfo.getTargetState(),
                        taskInfo.getGroupId()
                ),
                writerParameterMap,
                groupService.getAllGroups(),
                TargetState.getAllStates(),
                mediaService.getMediaSourcesByTypes(MediaSourceType.HBASE),
                buildZkMediaSources(),
                PluginWriterParameter.RetryMode.getAllModes(),
                RdbmsWriterParameter.SyncMode.getAllModes(),
                (HBaseReaderParameter) taskInfo.getTaskReaderParameterObj(),
                CommitMode.getAllCommitModes()
        );
        hbaseTaskModel.setCurrentWriters(taskInfo.getTaskWriterParameterObjs().stream().collect(Collectors.toMap(PluginWriterParameter::getPluginName, i -> "1")));
        mav.addObject("taskModel", hbaseTaskModel);
        return mav;
    }

    @RequestMapping(value = "/doUpdateHbaseTask")
    @ResponseBody
    public synchronized String doUpdateHbaseTask(@RequestBody HBaseTaskModel hbaseTaskModel) {
        try {
            checkRequired(hbaseTaskModel);
            checkZNode(hbaseTaskModel);

            TaskInfo taskInfo = taskService.getTask(hbaseTaskModel.getTaskBasicInfo().getId());
            taskInfo.setId(hbaseTaskModel.getTaskBasicInfo().getId());
            taskInfo.setGroupId(hbaseTaskModel.getTaskBasicInfo().getGroupId());
            taskInfo.setTaskName(hbaseTaskModel.getTaskBasicInfo().getTaskName());
            taskInfo.setTaskDesc(hbaseTaskModel.getTaskBasicInfo().getTaskDesc());
            taskInfo.setTargetState(hbaseTaskModel.getTaskBasicInfo().getTargetState());
            taskInfo.setReaderMediaSourceId(hbaseTaskModel.getHbaseReaderParameter().getMediaSourceId());
            taskInfo.setTaskReaderParameter(hbaseTaskModel.getHbaseReaderParameter().toJsonString());
            taskInfo.setTaskWriterParameter(Parameter.listToJsonString(
                            hbaseTaskModel.getWriterParameterMap().entrySet()
                                    .stream()
                                    .map(i -> i.getValue())
                                    .collect(Collectors.toList()))
            );
            taskService.updateTask(taskInfo);
        } catch (Exception e) {
            logger.error("Update HbaseTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/deleteHbaseTask")
    @ResponseBody
    public String deleteHbaseTask(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            taskService.deleteTask(id);
        } catch (Exception e) {
            logger.error("Delete HbaseTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/getReplZnodeParent")
    @ResponseBody
    @AuthIgnore
    public Map<String, Object> getListenerGroup(Long mediaSourceId) {
        Map<String, Object> map = new HashMap<>();
        MediaSourceInfo hbaseMediaSource = mediaSourceService.getById(mediaSourceId);
        String znodeParent = ((HBaseMediaSrcParameter) hbaseMediaSource.getParameterObj()).getZnodeParent();
        String group = "/hrdl_" + mediaSourceId + "_" + znodeParent.replace("/", "");
        map.put("replZnodeParent", group);
        return map;
    }

    @RequestMapping(value = "/pauseHbaseTask")
    @ResponseBody
    public String pauseHbaseTask(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            taskService.pauseTask(id);
        } catch (Exception e) {
            logger.error("Pause HbaseTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/resumeHbaseTask")
    @ResponseBody
    public String resumeHbaseTask(HttpServletRequest request) {
        try {
            Long id = Long.valueOf(request.getParameter("id"));
            taskService.resumeTask(id);
        } catch (Exception e) {
            logger.error("Pause HbaseTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/restartHbaseTask")
    @ResponseBody
    public String restartHbaseTask(HttpServletRequest request) {
        try {
            sendRestartCommand(request.getParameter("id"), null);
        } catch (Exception e) {
            logger.error("Pause HbaseTask Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    private Map<String, PluginWriterParameter> getWriterParameters() {
        return Lists.newArrayList(new RdbmsWriterParameter(), new EsWriterParameter(), new HdfsWriterParameter(), new HBaseWriterParameter())
                .stream()
                .collect(Collectors.toMap(PluginWriterParameter::getPluginName, i -> i));
    }

    private List<MediaSourceInfo> buildZkMediaSources() {
        List<MediaSourceInfo> list = mediaService.getMediaSourcesByTypes(MediaSourceType.ZOOKEEPER);
        return list == null ?
                Lists.newArrayList() :
                list.stream().
                        filter(i -> ((ZkMediaSrcParameter) i.getParameterObj()).getServers().equals(ManagerConfig.current().getZkServer()))
                        .collect(Collectors.toList());
    }

    private boolean isLeaderTask(HBaseTaskModel taskModel) {
        List<TaskInfo> list = taskService.getTasksByReaderMediaSourceId(taskModel.getHbaseReaderParameter().getMediaSourceId());
        if (CollectionUtils.isNotEmpty(list)) {
            boolean hasLeader = list.stream().filter(i -> {
                HBaseReaderParameter parameter = (HBaseReaderParameter) i.getTaskReaderParameterObj();
                return parameter.getReplZkMediaSourceId().equals(taskModel.getHbaseReaderParameter().getReplZkMediaSourceId()) &&
                        parameter.getReplZnodeParent().equals(taskModel.getHbaseReaderParameter().getReplZnodeParent()) &&
                        i.isLeaderTask();
            }).findFirst().isPresent();
            return !hasLeader;
        }
        return true;
    }

    private Long getLeaderTaskId(HBaseTaskModel taskModel) {
        List<TaskInfo> list = taskService.getTasksByReaderMediaSourceId(taskModel.getHbaseReaderParameter().getMediaSourceId());
        if (CollectionUtils.isNotEmpty(list)) {
            List<TaskInfo> tasksOfThisPeer = list.stream().filter(i -> {
                HBaseReaderParameter parameter = (HBaseReaderParameter) i.getTaskReaderParameterObj();
                return parameter.getReplZkMediaSourceId().equals(taskModel.getHbaseReaderParameter().getReplZkMediaSourceId()) &&
                        parameter.getReplZnodeParent().equals(taskModel.getHbaseReaderParameter().getReplZnodeParent());
            }).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(tasksOfThisPeer)) {
                List<TaskInfo> leaderTasks = tasksOfThisPeer.stream().filter(i -> i.isLeaderTask()).collect(Collectors.toList());
                if (leaderTasks.size() > 1) {
                    throw new ValidationException(String.format("发现[%s]下的Task-Leader的个数大于1，程序可能出现了bug，请排查。", taskModel.getHbaseReaderParameter().getReplZnodeParent()));
                } else if (leaderTasks.size() < 1) {
                    throw new ValidationException(String.format("发现[%s]下的Task-Leader的个数小于1，程序可能出现了bug，请排查。", taskModel.getHbaseReaderParameter().getReplZnodeParent()));
                } else {
                    return leaderTasks.get(0).getId();
                }
            }
        }
        return null;
    }

    private void checkRequired(HBaseTaskModel taskModel) {
        if (StringUtils.isBlank(taskModel.getTaskBasicInfo().getTaskName())) {
            throw new RuntimeException("Task名称为必输项");
        }
        if (StringUtils.isBlank(taskModel.getTaskBasicInfo().getTaskDesc())) {
            throw new RuntimeException("Task描述为必输项");
        }
        if (taskModel.getWriterParameterMap() == null || taskModel.getWriterParameterMap().isEmpty()) {
            throw new RuntimeException("请至少选择一个Writer");
        }
        if (taskModel.getHbaseReaderParameter().getMediaSourceId() == null) {
            throw new RuntimeException("关联数据源为必输项");
        }
        if (taskModel.getHbaseReaderParameter().getReplZkMediaSourceId() == null) {
            throw new RuntimeException("Repl-Zk数据源为必输项");
        }
        if (StringUtils.isBlank(taskModel.getHbaseReaderParameter().getReplZnodeParent())) {
            throw new RuntimeException("ReplZnodeParent为必输项");
        }
    }

    private void checkZNode(HBaseTaskModel taskModel) {
        List<TaskInfo> taskInfos = taskService.getTasksByType(TaskType.HBASE);
        if (CollectionUtils.isNotEmpty(taskInfos)) {
            taskInfos.stream().forEach(t -> {
                HBaseReaderParameter parameter = (HBaseReaderParameter) t.getTaskReaderParameterObj();

                Long hbaseId = taskModel.getHbaseReaderParameter().getMediaSourceId();
                Long replZkMediaSourceId = taskModel.getHbaseReaderParameter().getReplZkMediaSourceId();
                String replZnodeParent = taskModel.getHbaseReaderParameter().getReplZnodeParent();
                if (replZkMediaSourceId.equals(parameter.getReplZkMediaSourceId())
                        && replZnodeParent.equals(parameter.getReplZnodeParent())
                        && !hbaseId.equals(t.getReaderMediaSourceId())) {
                    throw new ValidationException(
                            String.format("在ID为[%s]的Zookeeper集群上,名称为[%s]的ReplZnodeParent已经被Hbase[id=%s]使用，请为Hbase[id=%s]选择其它名称!",
                                    replZkMediaSourceId, replZnodeParent, t.getReaderMediaSourceId(), hbaseId)
                    );
                }
            });
        }
    }
}
