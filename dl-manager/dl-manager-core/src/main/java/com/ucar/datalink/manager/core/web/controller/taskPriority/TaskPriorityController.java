package com.ucar.datalink.manager.core.web.controller.taskPriority;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.AlarmPriorityService;
import com.ucar.datalink.biz.service.AlarmStrategyService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.alarm.AlarmStrategyInfo;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.taskPriority.PriorityType;
import com.ucar.datalink.domain.alarm.AlarmPriorityInfo;
import com.ucar.datalink.manager.core.web.controller.taskmonitor.TaskMonitorController;
import com.ucar.datalink.manager.core.web.dto.taskPriority.TaskPriorityView;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/taskPriority/")
public class TaskPriorityController {

    private static Logger logger = LoggerFactory.getLogger(TaskMonitorController.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AlarmPriorityService taskPriorityService;

    @Autowired
    private AlarmStrategyService alarmStrategyService;

    @RequestMapping(value = "/list")
    public ModelAndView taskPriorityList() {
        ModelAndView mav = new ModelAndView("taskPriority/list");
        return mav;
    }

    @RequestMapping(value = "/initList")
    @ResponseBody
    public Page<TaskPriorityView> initList(@RequestBody Map<String, String> map) {
        String name = map.get("name");
        Integer priority = Integer.valueOf(map.get("priority"));

        priority = priority == -1 ? null :priority;

        Page<TaskPriorityView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<AlarmPriorityInfo> taskPrioritys = taskPriorityService.getTaskPriorityList(name,priority);

        List<TaskPriorityView> taskPriorityViews = taskPrioritys.stream().map(i -> {
            TaskPriorityView view = new TaskPriorityView();
            view.setId(i.getId());
            view.setName(i.getName());
            view.setPriority(i.getPriority());
            view.setCreateTime(sdf.format(i.getCreateTime()));
            view.setModifyTime(sdf.format(i.getModifyTime()));
            return view;
        }).collect(Collectors.toList());

        PageInfo<AlarmPriorityInfo> pageInfo = new PageInfo<>(taskPrioritys);
        page.setDraw(page.getDraw());
        page.setAaData(taskPriorityViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("taskPriority/add");
        mav.addObject("priorityTypeList", PriorityType.getPriorityTypeList());
        return mav;
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(@ModelAttribute("taskPriorityInfo") AlarmPriorityInfo taskPriorityInfo) {
        try {
            Boolean isSuccess = taskPriorityService.insert(taskPriorityInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(taskPriorityInfo, "006003003", AuditLogOperType.insert.getValue()));
                return "success";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("taskPriority/edit");
        String id = request.getParameter("id");
        AlarmPriorityInfo taskPriorityInfo = new AlarmPriorityInfo();
        if (StringUtils.isNotBlank(id)) {
            taskPriorityInfo = taskPriorityService.getById(Long.valueOf(id));
        }

        mav.addObject("taskPriorityInfo", taskPriorityInfo);
        mav.addObject("priorityTypeList", PriorityType.getPriorityTypeList());
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("taskPriorityInfo") AlarmPriorityInfo taskPriorityInfo) {
        try {
            Boolean isSuccess = taskPriorityService.update(taskPriorityInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(taskPriorityInfo, "006003005", AuditLogOperType.update.getValue()));
                return "success";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(Long id) {
        if (id == null) {
            return "fail";
        }
        try {
            Long idLong = Long.valueOf(id);
            List<AlarmStrategyInfo> list = alarmStrategyService.getAlarmStrategyList(idLong,"");
            if(list!=null && list.size() > 0) {
                return "请先删除策略组下的策略";
            }
            AlarmPriorityInfo info = taskPriorityService.getById(idLong);
            Boolean isSuccess = taskPriorityService.delete(idLong);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(info, "006003006", AuditLogOperType.delete.getValue()));
                return "success";
            } else {
                return "fail";
            }
        } catch (Exception e) {
            logger.error("doStart is fail", e);
        }
        return "fail";
    }

    private static AuditLogInfo getAuditLogInfo(AlarmPriorityInfo info, String menuCode, String operType){
        AuditLogInfo logInfo=new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName(info.getName());
        logInfo.setOperType(operType);
        logInfo.setOperKey(info.getId());
        logInfo.setOperRecord(info.toString());
        return logInfo;
    }
}
