package com.ucar.datalink.manager.core.web.controller.taskmonitor;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.domain.monitor.TaskMonitorInfo;
import com.ucar.datalink.domain.task.TaskDelayTimeInfo;
import com.ucar.datalink.domain.task.TaskExceptionInfo;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskStatisticInfo;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.taskMonitor.TaskMonitorView;
import com.ucar.datalink.manager.core.web.util.Page;
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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by csf on 17/4/28.
 */
@Controller
@RequestMapping(value = "/taskMonitor/")
public class TaskMonitorController {

    private static Logger logger = LoggerFactory.getLogger(TaskMonitorController.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Autowired
    TaskDelayTimeService taskDelayTimeService;

    @Autowired
    TaskConfigService taskService;

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private TaskStatisticService taskStatisticService;

    @Autowired
    private TaskExceptionService taskExceptionService;

    @Autowired
    private GroupService groupService;

    @RequestMapping(value = "/taskMonitorList")
    public ModelAndView taskMonitorList() {
        ModelAndView mav = new ModelAndView("taskMonitor/list");
        mav.addObject("taskList", taskService.getList());
        mav.addObject("groupList", groupService.getAllGroups());
        return mav;
    }

    @RequestMapping(value = "/initTaskMonitor")
    @ResponseBody
    public Page<TaskMonitorView> initMonitor(@RequestBody Map<String, String> map) {
        Long taskId = Long.valueOf(map.get("taskId"));
        Long groupId = Long.valueOf(map.get("groupId"));

        taskId = taskId == -1 ? null : taskId;
        groupId = groupId == -1 ? null :groupId;
        //设置异常展示时间为3分钟
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.add(Calendar.MINUTE, -3);
        Date startTime = calendar.getTime();
        Page<TaskMonitorView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<TaskMonitorInfo> taskMonitorInfos = taskService.getTaskMonitorInfoList(taskId, groupId, startTime, endTime);

        List<TaskMonitorView> monitorViews = taskMonitorInfos.stream().map(i -> {
            TaskMonitorView view = new TaskMonitorView();
            view.setTaskId(i.getTaskId());
            view.setTaskName(i.getTaskName());
            view.setTargetState(i.getTargetState());
            view.setGroupId(i.getGroupId());
            view.setDelayTime(i.getDelayTime() == null ? 0L : i.getDelayTime());
            view.setException(i.getException());
            view.setExceptionId(i.getExceptionId());
            return view;
        }).collect(Collectors.toList());

        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();
        Map<Long, Long> twm = clusterState.getTaskWorkerMapping();
        monitorViews.forEach(t -> t.setWorkerId(twm.get(t.getTaskId())));

        //为View设置TaskStatus属性
        Collection<TaskStatus> allStatus = taskStatusService.getAll();
        allStatus.stream().forEach(s -> monitorViews.forEach(t -> {
                    if (t.getTaskId().toString().equals(s.getId())) {
                        t.setListenedState(s.getState());
                    }
                })
        );

        PageInfo<TaskMonitorInfo> pageInfo = new PageInfo<>(taskMonitorInfos);
        page.setDraw(page.getDraw());
        page.setAaData(monitorViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        return page;
    }

    @RequestMapping(value = "/toTaskStatistic")
    public ModelAndView toTaskStatistic(Long taskId) {
        ModelAndView mav = new ModelAndView("taskMonitor/taskStatistic");
        List<TaskDelayTimeInfo> taskDelayTimeInfoList = new ArrayList<>();
        List<TaskStatisticInfo> taskStatisticList = new ArrayList<>();
        //默认统计时间为当前时间之前一天
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date startTime = calendar.getTime();
        TaskInfo taskInfo = new TaskInfo();
        if (taskId != null) {
            taskInfo = taskService.getTask(taskId);
            taskDelayTimeInfoList = taskDelayTimeService.getListByTaskIdForQuery(taskId, startTime, endTime);
            taskStatisticList = taskStatisticService.getListByTaskIdForQuery(taskId, startTime, endTime);
        }
        List<Long> delayTimeList = new ArrayList<>();
        List<String> delayCreateTimeList = new ArrayList<>();
        for (TaskDelayTimeInfo taskDelayTimeInfo : taskDelayTimeInfoList) {
            delayTimeList.add(taskDelayTimeInfo.getDelayTime());
            Date delayCreateTime = taskDelayTimeInfo.getCreateTime();
            String time = sdf.format(delayCreateTime);
            delayCreateTimeList.add(time);
        }
        List<Long> recordTPSList = new ArrayList<>();
        List<BigDecimal> writeTimePerRecordList = new ArrayList<>();
        List<Long> recordSizeList = new ArrayList<>();
        List<Long> exceptionsPerMinuteList = new ArrayList<>();
        List<Long> readWriteCountPerMinuteList = new ArrayList<>();
        List<String> createTimeList = new ArrayList<>();
        for (TaskStatisticInfo statistic : taskStatisticList) {
            recordTPSList.add(statistic.getRecordsPerMinute());
            writeTimePerRecordList.add(statistic.getWriteTimePerRecord());
            recordSizeList.add(statistic.getSizePerMinute());
            exceptionsPerMinuteList.add(statistic.getExceptionsPerMinute());
            readWriteCountPerMinuteList.add(statistic.getReadWriteCountPerMinute());
            Date createTime = statistic.getCreateTime();
            String time = sdf.format(createTime);
            createTimeList.add(time);
        }
        String delayCreateTimeArr = JSONObject.toJSONString(delayCreateTimeList);
        String createTimeArr = JSONObject.toJSONString(createTimeList);
        mav.addObject("taskInfo", taskInfo);
        mav.addObject("delayTimeList", delayTimeList);
        mav.addObject("delayCreateTimeList", delayCreateTimeArr);
        mav.addObject("recordTPSList", recordTPSList);
        mav.addObject("writeTimePerRecordList", writeTimePerRecordList);
        mav.addObject("recordSizeList", recordSizeList);
        mav.addObject("exceptionsPerMinuteList", exceptionsPerMinuteList);
        mav.addObject("readWriteCountPerMinuteList", readWriteCountPerMinuteList);
        mav.addObject("createTimeList", createTimeArr);
        return mav;
    }

    @RequestMapping(value = "/doSearchTaskStatistic")
    @ResponseBody
    public JSONObject doSearchTaskStatistic(@RequestBody Map<String, String> map) {
        Long taskId = Long.valueOf(map.get("taskId"));
        String start = map.get("startTime");
        String end = map.get("endTime");
        Date startTime = new Date();
        Date endTime = new Date();  //endTime默认为当前时间
        //startTime为空，默认查询endTime之前一天的数据
        if (StringUtils.isBlank(start)) {
            if (!StringUtils.isBlank(end)) {
                endTime = new Date(Long.valueOf(end));
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endTime);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            startTime = calendar.getTime();  //startTime为endTime之前一天
        }
        //startTime不为空，校验时间区间
        if (!StringUtils.isBlank(start)) {
            startTime = new Date(Long.valueOf(start));
            if (!StringUtils.isBlank(end)) {
                endTime = new Date(Long.valueOf(end));
            }
            long st = startTime.getTime();
            long et = endTime.getTime();
            long betweenDays = (long)((et - st) / (1000 * 60 * 60 *24) + 0.5);
            if (betweenDays > 7) {
                Map<String, Object> failMap = new HashMap<>();
                failMap.put("failMessage", "查询日期间隔不能超过7天！");
                return new JSONObject(failMap);
            }
        }
        TaskInfo taskInfo = new TaskInfo();
        List<TaskDelayTimeInfo> taskDelayTimeInfoList = new ArrayList<>();
        List<TaskStatisticInfo> taskStatisticList = new ArrayList<>();
        if (taskId != null) {
            taskInfo = taskService.getTask(taskId);
            taskDelayTimeInfoList = taskDelayTimeService.getListByTaskIdForQuery(taskId, startTime, endTime);
            taskStatisticList = taskStatisticService.getListByTaskIdForQuery(taskId, startTime, endTime);
        }
        List<Long> delayTimeList = new ArrayList<>();
        List<String> delayCreateTimeList = new ArrayList<>();
        for (TaskDelayTimeInfo taskDelayTimeInfo : taskDelayTimeInfoList) {
            delayTimeList.add(taskDelayTimeInfo.getDelayTime());
            Date delayCreateTime = taskDelayTimeInfo.getCreateTime();
            String time = sdf.format(delayCreateTime);
            delayCreateTimeList.add(time);
        }
        List<Long> recordTPSList = new ArrayList<>();
        List<BigDecimal> writeTimePerRecordList = new ArrayList<>();
        List<Long> recordSizeList = new ArrayList<>();
        List<Long> exceptionsPerMinuteList = new ArrayList<>();
        List<Long> readWriteCountPerMinuteList = new ArrayList<>();
        List<String> createTimeList = new ArrayList<>();
        for (TaskStatisticInfo statistic : taskStatisticList) {
            recordTPSList.add(statistic.getRecordsPerMinute());
            writeTimePerRecordList.add(statistic.getWriteTimePerRecord());
            recordSizeList.add(statistic.getSizePerMinute());
            exceptionsPerMinuteList.add(statistic.getExceptionsPerMinute());
            readWriteCountPerMinuteList.add(statistic.getReadWriteCountPerMinute());
            Date createTime = statistic.getCreateTime();
            String time = sdf.format(createTime);
            createTimeList.add(time);
        }
        JSONObject result = new JSONObject();
        result.put("taskInfo", taskInfo);
        result.put("delayTimeList", delayTimeList);
        result.put("delayCreateTimeList", delayCreateTimeList);
        result.put("recordTPSList", recordTPSList);
        result.put("writeTimePerRecordList", writeTimePerRecordList);
        result.put("recordSizeList", recordSizeList);
        result.put("exceptionsPerMinuteList", exceptionsPerMinuteList);
        result.put("readWriteCountPerMinuteList", readWriteCountPerMinuteList);
        result.put("createTimeList", createTimeList);
        return result;
    }

    @RequestMapping(value = "/toTaskException")
    public ModelAndView toTaskException(HttpServletRequest request) {
        Long taskId = Long.valueOf(request.getParameter("taskId"));
        ModelAndView mav = new ModelAndView("taskMonitor/taskException");
        mav.addObject("taskId", taskId);
        return mav;
    }

    @RequestMapping(value = "/initTaskException")
    @ResponseBody
    public Page<TaskExceptionInfo> initTaskException(@RequestBody Map<String, String> map) {
        Long taskId = Long.valueOf(map.get("taskId"));
        String start = map.get("startTime");
        String end = map.get("endTime");
        Date startTime = StringUtils.isBlank(start) ? null : new Date(Long.valueOf(start));
        Date endTime = StringUtils.isBlank(end) ? null : new Date(Long.valueOf(end));
        List<TaskExceptionInfo> taskExceptionList = new ArrayList<>();
        Page<TaskExceptionInfo> page = new Page<>(map);
        if (taskId != null) {
            PageHelper.startPage(page.getPageNum(), page.getLength());
            taskExceptionList = taskExceptionService.getListByTaskIdForQuery(taskId, startTime, endTime);
        }
        PageInfo<TaskExceptionInfo> pageInfo = new PageInfo<>(taskExceptionList);
        page.setDraw(page.getDraw());
        page.setAaData(taskExceptionList);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }


    @RequestMapping(value = "/showException")
    @ResponseBody
    public String showException(Long id) {
        if (id != null) {
            TaskExceptionInfo exceptionInfo = taskExceptionService.getById(id);
            String exc = exceptionInfo.getExceptionDetail();
            if (StringUtils.isNotBlank(exc)) {
                return exc;
            }
        }
        return "";
    }
}
