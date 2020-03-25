package com.ucar.datalink.manager.core.web.controller.monitor;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.monitor.MonitorCat;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import com.ucar.datalink.manager.core.web.dto.monitor.MonitorView;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import org.apache.commons.collections.CollectionUtils;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by csf on 17/4/28.
 */
@Controller
@RequestMapping(value = "/monitor/")
public class MonitorController {

    private static Logger logger = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    UserService userService;
    @Autowired
    WorkerService workerService;
    @Autowired
    GroupService groupService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private TaskConfigService taskConfigService;

    private static AuditLogInfo getAuditLogInfo(MonitorInfo info, String menuCode, String operType) {
        AuditLogInfo logInfo = new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName(info.getResourceName());
        logInfo.setOperType(operType);
        logInfo.setOperKey(info.getId());
        logInfo.setOperRecord(info.toString());
        return logInfo;
    }

    public static void main(String[] arg) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.print(sf.format(new Date()));
    }

    @RequestMapping(value = "/monitorList")
    public ModelAndView monitorList() {
        ModelAndView mav = new ModelAndView("monitor/list");
        mav.addObject("monitorCatList", MonitorCat.getMonitorCatList());
        mav.addObject("groupList", groupService.getAllGroups());
        return mav;
    }

    @RequestMapping(value = "/initMonitor")
    @ResponseBody
    public Page<MonitorView> initMonitor(@RequestBody Map<String, String> map) {
        Integer monitorCat = Integer.valueOf(map.get("monitorCat"));
        Integer monitorType = Integer.valueOf(map.get("monitorType"));
        Long groupId = Long.valueOf(map.get("groupId"));
        Long resourceId = Long.valueOf(map.get("resourceId"));
        Integer isEffective = Integer.valueOf(map.get("isEffective"));
        Page<MonitorView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<MonitorInfo> listMonitor = monitorService.getListForQueryPage(monitorCat == -1L ? null : monitorCat, monitorType == -1L ? null : monitorType, groupId == -1L ? null : groupId, resourceId == -1L ? null : resourceId, isEffective == -1 ? null : isEffective);

        //构造view
        List<MonitorView> monitorViews = listMonitor.stream().map(i -> {
            MonitorView view = new MonitorView();
            view.setId(i.getId());
            view.setMonitorType(i.getMonitorType());
            view.setResourceId(i.getResourceId());
            view.setIntervalTime(i.getIntervalTime());
            view.setIsEffective(i.getIsEffective());
            view.setReceivePeople(i.getReceivePeople());
            view.setModifyTime(i.getModifyTime());
            view.setThreshold(i.getThreshold());
            view.setResourceName(i.getResourceName());
            return view;
        }).collect(Collectors.toList());
        PageInfo<MonitorInfo> pageInfo = new PageInfo<>(listMonitor);
        page.setDraw(page.getDraw());
        page.setAaData(monitorViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("monitor/add");
        List<UserInfo> userList = userService.getList();
        mav.addObject("monitorCatList", MonitorCat.getMonitorCatList());
        mav.addObject("userList", userList);
        return mav;
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(@ModelAttribute("monitorInfo") MonitorInfo monitorInfo) {
        try {
            Boolean isSuccess = monitorService.insert(monitorInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(monitorInfo, "006001003", AuditLogOperType.insert.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("Monitor Info 添加失败",e);
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
            MonitorInfo info = monitorService.getById(idLong);
            Boolean isSuccess = monitorService.delete(idLong);
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

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("monitor/edit");
        List<UserInfo> userList = userService.getList();
        List<TaskInfo> taskList = taskConfigService.getList();
        List<WorkerInfo> workerList = workerService.getList();
        String id = request.getParameter("id");
        MonitorInfo monitorInfo = new MonitorInfo();
        if (StringUtils.isNotBlank(id)) {
            monitorInfo = monitorService.getById(Long.valueOf(id));
        }

        mav.addObject("taskList", taskList);
        mav.addObject("workerList", workerList);
        mav.addObject("userList", userList);
        mav.addObject("monitorCatList", MonitorCat.getMonitorCatList());
        mav.addObject("monitorInfo", monitorInfo);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("monitorInfo") MonitorInfo monitorInfo) {
        try {
            Boolean isSuccess = monitorService.update(monitorInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(monitorInfo, "006001005", AuditLogOperType.update.getValue()));
                return "success";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/doStart")
    public String doStart(Long id) {
        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.setId(id);
        monitorInfo.setIsEffective(1);
        Boolean isSuccess = monitorService.update(monitorInfo);
        if (isSuccess) {
            return "success";
        } else {
            return "fail";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doPause")
    public String doPause(Long id) {
        MonitorInfo monitorInfo = new MonitorInfo();
        monitorInfo.setId(id);
        monitorInfo.setIsEffective(2);
        Boolean isSuccess = monitorService.update(monitorInfo);
        if (isSuccess) {
            return "success";
        } else {
            return "fail";
        }
    }

    @RequestMapping(value = "/doAllStart")
    @ResponseBody
    public String doAllStart() {
        try {
            monitorService.updateIsAlarm(1);
            return "success";
        } catch (Exception e) {
            logger.error("doAllStart is fail", e);
        }
        return "fail";
    }

    @RequestMapping(value = "/doAllStop")
    @ResponseBody
    public String doAllStop() {
        try {
            monitorService.updateIsAlarm(2);
            return "success";
        } catch (Exception e) {
            logger.error("doAllStop is fail", e);
        }
        return "fail";
    }

    @RequestMapping(value = "/getMonitorTypeListByCat")
    @ResponseBody
    @AuthIgnore
    public Map<String, Object> getMonitorTypeListByCat(String monitorCat) {
        Map<String, Object> map = new HashMap<>();
        Integer catKey = Integer.valueOf(monitorCat);
        List<MonitorType> monitorTypeList = MonitorType.getMonitorTypeListByCat(catKey);
        List<Integer> key = new ArrayList<>();
        List<String> desc = new ArrayList<>();
        for (MonitorType monitorType : monitorTypeList) {
            key.add(monitorType.getKey());
            desc.add(monitorType.getDesc());
        }
        map.put("key", key);
        map.put("desc", desc);
        List<Long> resourceId = new ArrayList<>();
        List<String> resourceName = new ArrayList<>();
        if (catKey == 1) {
            List<TaskInfo> taskInfoList = taskConfigService.getList();
            List<TaskInfo> taskList = CollectionUtils.isEmpty(taskInfoList) ? taskInfoList : taskInfoList.stream().filter(t -> t.getLeaderTaskId() == null).collect(Collectors.toList());
            for (TaskInfo task : taskList) {
                resourceId.add(task.getId());
                resourceName.add(task.getTaskName());
            }
        }
        if (catKey == 2) {
            List<WorkerInfo> workerInfoList = workerService.getList();
            for (WorkerInfo workerInfo : workerInfoList) {
                resourceId.add(workerInfo.getId());
                resourceName.add(workerInfo.getWorkerName());
            }
        }
        map.put("resourceId", resourceId);
        map.put("resourceName", resourceName);
        return map;
    }
}
