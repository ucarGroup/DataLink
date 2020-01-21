package com.ucar.datalink.manager.core.web.controller.alarmStrategy;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.AlarmStrategyService;
import com.ucar.datalink.biz.service.AlarmPriorityService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.alarm.AlarmStrategyInfo;
import com.ucar.datalink.domain.alarm.StrategyConfig;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.monitor.MonitorCat;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.domain.alarm.AlarmPriorityInfo;
import com.ucar.datalink.manager.core.web.controller.taskmonitor.TaskMonitorController;
import com.ucar.datalink.manager.core.web.dto.taskMonitor.AlarmStrategyView;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/alarmStrategy/")
public class AlarmStrategyController {

    private static Logger logger = LoggerFactory.getLogger(TaskMonitorController.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AlarmStrategyService alarmStrategyService;
    @Autowired
    private AlarmPriorityService taskPriorityService;

    @RequestMapping(value = "/list")
    public ModelAndView alarmStrategyList() {
        ModelAndView mav = new ModelAndView("alarmStrategy/list");
        List<AlarmPriorityInfo> alarmPriorityInfoList = taskPriorityService.getAll();
        mav.addObject("alarmPriorityInfoList",alarmPriorityInfoList);
        return mav;
    }

    @RequestMapping(value = "/initList")
    @ResponseBody
    public Page<AlarmStrategyView> initList(@RequestBody Map<String, String> map) {
        Long priorityId = Long.valueOf(map.get("priorityId"));
        String name = map.get("name");
        priorityId = priorityId == -1 ? null :priorityId;
        Page<AlarmStrategyView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<AlarmStrategyInfo> alarmStrategys = alarmStrategyService.getAlarmStrategyList(priorityId,name);

        List<Long> priorityIds = new ArrayList<>();
        alarmStrategys.forEach(i->{ priorityIds.add(i.getPriorityId()); });

        Map<String, AlarmPriorityInfo> taskPriorityInfoMap = new HashMap<>(16);
        List<AlarmPriorityInfo> priorityList = taskPriorityService.getPriorityListByIds(priorityIds);
        priorityList.forEach(i->{taskPriorityInfoMap.put(String.valueOf(i.getId()),i);});

        List<AlarmStrategyView> taskPriorityViews = alarmStrategys.stream().map(i -> {
            AlarmStrategyView view = new AlarmStrategyView();
            view.setId(i.getId());
            view.setPriorityId(i.getPriorityId());
            view.setMonitorType(MonitorType.getTaskMonitorType(i.getMonitorType()).getDesc());
            view.setConfig(i.getConfig());
            view.setCreateTime(sdf.format(i.getCreateTime()));
            view.setModifyTime(sdf.format(i.getModifyTime()));
            view.setName(i.getName());
            view.setPriorityName(taskPriorityInfoMap.get(String.valueOf(i.getPriorityId())).getName());
            return view;
        }).collect(Collectors.toList());

        PageInfo<AlarmStrategyInfo> pageInfo = new PageInfo<>(alarmStrategys);
        page.setDraw(page.getDraw());
        page.setAaData(taskPriorityViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("alarmStrategy/add");
        List<AlarmPriorityInfo> taskPriorityInfos = taskPriorityService.getAll();
        mav.addObject("monitorTypeList", MonitorType.getMonitorTypeListByCat(MonitorCat.TASK_MONITOR.getKey()));
        mav.addObject("taskPriorityInfos",taskPriorityInfos);
        return mav;
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(HttpServletRequest request) {
        try {
            AlarmStrategyInfo alarmStrategyInfo = getAlarmStrategyInfo(request);

            Boolean isSuccess = alarmStrategyService.insert(alarmStrategyInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(alarmStrategyInfo, "006003003", AuditLogOperType.insert.getValue()));
                return "success";
            }
            return "fail";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("alarmStrategy/edit");
        String id = request.getParameter("id");
        AlarmStrategyInfo alarmStrategyInfo = new AlarmStrategyInfo();
        if (StringUtils.isNotBlank(id)) {
            alarmStrategyInfo = alarmStrategyService.getById(Long.valueOf(id));
        }
        List<AlarmPriorityInfo> taskPriorityInfos = taskPriorityService.getAll();
        mav.addObject("alarmStrategyInfo", JSONObject.toJSON(alarmStrategyInfo));
        mav.addObject("taskPriorityInfos", taskPriorityInfos);
        mav.addObject("monitorTypeList", MonitorType.getMonitorTypeListByCat(MonitorCat.TASK_MONITOR.getKey()));

        return mav;
    }

    @RequestMapping(value = "/doEdit")
    @ResponseBody
    public String doEdit(HttpServletRequest request) {
        try {
            String id = request.getParameter("id");
            AlarmStrategyInfo alarmStrategyInfo = getAlarmStrategyInfo(request);
            alarmStrategyInfo.setId(Long.valueOf(id));
            Boolean isSuccess = alarmStrategyService.update(alarmStrategyInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(alarmStrategyInfo, "006003003", AuditLogOperType.insert.getValue()));
                return "success";
            }
            return "fail";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(Long id) {
        if (id == null) {
            return "fail";
        }
        try {
            Long idLong = Long.valueOf(id);
            AlarmStrategyInfo info = alarmStrategyService.getById(idLong);
            Boolean isSuccess = alarmStrategyService.delete(idLong);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(info, "006001006", AuditLogOperType.delete.getValue()));
                return "success";
            } else {
                return "fail";
            }
        } catch (Exception e) {
            logger.error("doStart is fail", e);
        }
        return "fail";
    }

    private AlarmStrategyInfo getAlarmStrategyInfo(HttpServletRequest request) {

        String name = request.getParameter("name");
        String monitorType = request.getParameter("monitorType");
        String priorityId = request.getParameter("priorityId");
        String[] timeRanges = request.getParameterValues("timeRange");
        String[] thresholds = request.getParameterValues("threshold");
        String[] intervalTimes = request.getParameterValues("intervalTime");
        String[] isPhones = request.getParameterValues("isPhone");
        String[] isDingDs = request.getParameterValues("isDingD");
        String[] isSMSs = request.getParameterValues("isSMS");
        String[] others = request.getParameterValues("other");

        List<StrategyConfig> configList = new ArrayList<>();
        for (int i=0;i<timeRanges.length;i++) {
            StrategyConfig config = new StrategyConfig();
            config.setTimeRange(timeRanges[i]);
            config.setThreshold(Integer.valueOf(thresholds[i]));
            config.setIntervalTime(Long.valueOf(intervalTimes[i]));
            config.setPhone(isPhones[i].equals("1")?true:false);
            config.setDingD(isDingDs[i].equals("1")?true:false);
            config.setSMS(isSMSs[i].equals("1")?true:false);
            config.setOtherConfig(others[i]);
            configList.add(config);
        }

        AlarmStrategyInfo alarmStrategyInfo = new AlarmStrategyInfo();
        alarmStrategyInfo.setName(name);
        alarmStrategyInfo.setStrategys(configList);
        alarmStrategyInfo.setPriorityId(Long.valueOf(priorityId));
        alarmStrategyInfo.setMonitorType(Integer.valueOf(monitorType));
        return alarmStrategyInfo;

    }

    private static AuditLogInfo getAuditLogInfo(AlarmStrategyInfo info, String menuCode, String operType){
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
