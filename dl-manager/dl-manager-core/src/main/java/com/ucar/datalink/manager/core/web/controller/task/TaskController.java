package com.ucar.datalink.manager.core.web.controller.task;

import com.ucar.datalink.biz.service.GroupService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2018/1/9.
 */
@Controller
@RequestMapping(value = "/task/")
public class TaskController extends BaseTaskController {

    private static Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private TaskConfigService taskService;

    @RequestMapping(value = "/toGroupMigrate")
    public ModelAndView toGroupMigrate(Long id) {
        ModelAndView mav = new ModelAndView("task/taskMigrate");

        TaskInfo taskInfo = taskService.getTask(id);
        GroupInfo currentGroup = groupService.getById(taskInfo.getGroupId());
        List<GroupInfo> groupInfos = groupService.getAllGroups();

        mav.addObject("taskId", id);
        mav.addObject("currentGroupId", currentGroup.getId());
        mav.addObject("currentGroupName", currentGroup.getGroupName());
        mav.addObject("groupList", groupInfos.stream().filter(i -> !i.getId().equals(currentGroup.getId())).collect(Collectors.toList()));

        return mav;
    }

    @RequestMapping(value = "/doGroupMigrate")
    @ResponseBody
    public String doGroupMigrate(@RequestBody Map<String, Long> migrateParams) {
        try {
            Long taskId = migrateParams.get("taskId");
            Long targetGroupId = migrateParams.get("targetGroupId");
            taskService.migrateTaskGroup(taskId, targetGroupId);
        } catch (Exception e) {
            logger.error("Migrate Task Group Failed.", e);
            return "操作失败:" + e.getMessage();
        }
        return "success";
    }

    @RequestMapping(value = "/getTaskListByGroupId")
    @ResponseBody
    @AuthIgnore
    public Map<String, Object> getTaskListByGroupId(long groupId) {
        Map<String, Object> map = new HashMap<>();
        List<TaskInfo> taskListByGroupId;
        if (groupId == -1L) {
            taskListByGroupId =taskService.getList();
        } else {
            taskListByGroupId = taskService.getTaskListByGroupId(groupId);
        }
        List<Long> taskIds = new ArrayList<>();
        List<String> taskNames = new ArrayList<>();
        for (TaskInfo taskInfo : taskListByGroupId) {
            taskIds.add(taskInfo.getId());
            taskNames.add(taskInfo.getTaskName());
        }
        map.put("taskIds", taskIds);
        map.put("taskNames", taskNames);
        return map;
    }
}
