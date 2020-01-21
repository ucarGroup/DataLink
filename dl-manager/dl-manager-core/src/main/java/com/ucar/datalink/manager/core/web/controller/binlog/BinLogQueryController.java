package com.ucar.datalink.manager.core.web.controller.binlog;

import com.ucar.datalink.biz.service.GroupService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.biz.service.TaskPositionService;
import com.ucar.datalink.biz.service.TaskStatusService;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.domain.task.TaskType;
import com.ucar.datalink.manager.core.web.dto.binlog.BinLogView;
import com.ucar.datalink.manager.core.web.util.Page;
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

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BinLog查询
 *
 * @author daijunjian
 * @date 2019/11/01
 */
@Controller
@RequestMapping(value = "/binLogQuery/")
public class BinLogQueryController {

    private static final Logger logger = LoggerFactory.getLogger(BinLogQueryController.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private TaskConfigService taskService;

    @Autowired
    @Qualifier("taskPositionServiceZkImpl")
    private TaskPositionService taskPositionService;

    @Autowired
    private TaskStatusService taskStatusService;

    @RequestMapping(value = "/binLogQueryList")
    public ModelAndView groupList() {
        ModelAndView mav = new ModelAndView("binLog/binLogQueryList");
        mav.addObject("groupList", groupService.getAllGroups());
        int twoMin = 2 * 60 * 1000;
        mav.addObject("currentTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis() - twoMin)));
        return mav;
    }

    @RequestMapping(value = "/initDatas")
    @ResponseBody
    public Page<BinLogView> initDatas(@RequestBody Map<String, String> map) {

        String startTime = map.get("startTime");
        String endTime = map.get("endTime");
        String targetState = map.get("targetState");
        String groupId = map.get("groupId");

        Long startTimeTemp = null;
        Long endTimeTemp = null;
        if(StringUtils.isNotBlank(startTime)){
            startTimeTemp = Long.valueOf(startTime);
        }
        if(StringUtils.isNotBlank(endTime)){
            endTimeTemp = Long.valueOf(endTime);
        }

        //查询任务
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskType(TaskType.MYSQL);
        if(StringUtils.isNotBlank(targetState)){
            taskInfo.setTargetState(TargetState.valueOf(targetState));
        }
        if(StringUtils.isNotBlank(groupId)){
            taskInfo.setGroupId(Long.valueOf(groupId));
        }
        List<TaskInfo> taskInfoList = taskService.findTaskListNoPage(taskInfo);

        //构造view
        List<BinLogView> binLogViewList = taskInfoList.stream().map(i -> {
            BinLogView view = new BinLogView();
            view.setId(i.getId());
            view.setTaskName(i.getTaskName());
            view.setGroupId(i.getGroupId());
            view.setTargetState(taskInfo.getTargetState().name());
            if(taskInfo.getTaskPriorityId() == null){
                view.setTaskPriority(-1);
            }else{
                view.setTaskPriority(taskInfo.getTaskPriorityId());
            }
            return view;
        }).collect(Collectors.toList());


        //为View设置Position属性
        binLogViewList.stream().forEach(i -> {
            MysqlReaderPosition position = (MysqlReaderPosition) taskPositionService.getPosition(i.getId().toString());
            if (position != null) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(position.getTimestamp()));
                i.setCurrentTimeStamp(time + " (" + position.getTimestamp() + ")");
                i.setCurrentTime(new Date(position.getTimestamp()));
            }
        });

        final Long startTimeDate = startTimeTemp;
        final Long endTimeDate = endTimeTemp;
        List<BinLogView> filterList = binLogViewList.stream().filter( binLogView -> {

            if(binLogView.getCurrentTime() == null){
                return false;
            }
            if(startTimeDate == null && endTimeDate == null){
                return true;
            }else if(startTimeDate != null && endTimeDate != null){
                return binLogView.getCurrentTime().getTime() > startTimeDate && binLogView.getCurrentTime().getTime() < endTimeDate;
            }else if(startTimeDate != null){
                return binLogView.getCurrentTime().getTime() > startTimeDate;
            }else{
                return binLogView.getCurrentTime().getTime() < endTimeDate;
            }
        }).collect(Collectors.toList());

        //为View设置TaskStatus属性
        Collection<TaskStatus> allStatus = taskStatusService.getAll();
        allStatus.stream().forEach(s ->
                filterList.forEach(t -> {
                    if (t.getId().toString().equals(s.getId())) {
                        t.setActualState(s.getState().name());
                    }
                })
        );

        Collections.sort(filterList, new Comparator<BinLogView>() {
            @Override
            public int compare(BinLogView o1, BinLogView o2) {
                if(o1.getCurrentTime().getTime() > o2.getCurrentTime().getTime()){
                    return 1;
                }else if(o1.getCurrentTime().getTime() < o2.getCurrentTime().getTime()){
                    return -1;
                }else {
                    return 0;
                }
            }
        });


        Page<BinLogView> pageInfo = new Page<BinLogView>(filterList);
        return pageInfo;
    }


}
