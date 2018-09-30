package com.ucar.datalink.manager.core.web.controller.worker;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.common.collect.Lists;
import com.ucar.datalink.biz.service.GroupService;
import com.ucar.datalink.biz.service.WorkerJvmStateService;
import com.ucar.datalink.biz.service.WorkerService;
import com.ucar.datalink.biz.service.WorkerSystemStateService;
import com.ucar.datalink.common.jvm.JvmSnapshot;
import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.worker.WorkerJvmStateInfo;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.domain.worker.WorkerSystemStateInfo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.work.WorkerView;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by csf on 2017/3/29.
 */
@Controller
@RequestMapping(value = "/worker/")
public class WorkerController {

    private static final Logger logger = LoggerFactory.getLogger(WorkerController.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Autowired
    private GroupService groupService;

    @Autowired
    private WorkerService workerService;

    @Autowired
    private WorkerJvmStateService workerJvmStateService;

    @Autowired
    private WorkerSystemStateService workerSystemStateService;

    @RequestMapping(value = "/workerList")
    public ModelAndView workerList() {
        ModelAndView mav = new ModelAndView("worker/list");
        mav.addObject("groupList", groupService.getAllGroups());
        return mav;
    }

    @RequestMapping(value = "/initWorker")
    @ResponseBody
    public Page<WorkerView> initWork(@RequestBody Map<String, String> map) {
        Long groupId = Long.valueOf(map.get("groupId"));
        List<WorkerInfo> listWorker = workerService.getListForQuery(groupId == -1L ? null : groupId);
        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();
        //构造view
        List<WorkerView> workerViews = listWorker.parallelStream().map(i -> {
            WorkerView view = new WorkerView();
            view.setId(i.getId());
            ClusterState.GroupData groupData = clusterState.getGroupData(i.getGroupId());
            if (groupData != null) {
                List<ClusterState.MemberData> memberList = groupData.getMembers();
                if (memberList != null && memberList.size() > 0) {
                    if (memberList.stream().filter(m -> m.getClientId().equals(i.getId().toString())).findAny().isPresent()) {
                        view.setWorkerState("正常");
                    } else {
                        view.setWorkerState("异常");
                    }
                }
            }
            view.setCreateTime(i.getCreateTime());
            view.setGroupName(i.getGroupName());
            view.setRestPort(i.getRestPort());
            view.setWorkerName(i.getWorkerName());
            view.setWorkerAddress(i.getWorkerAddress());
            view.setModifyTime(i.getModifyTime());
            JvmSnapshot jvmSnapshot = getJvmSnapshot(i.getId());
            if (jvmSnapshot != null) {
                view.setStartTime(DateFormatUtils.format(jvmSnapshot.getStartTime(), "yyyy-MM-dd HH:mm:ss"));
            }
            return view;
        }).collect(Collectors.toList());
        return new Page<WorkerView>(workerViews);
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("worker/add");
        List<GroupInfo> list = groupService.getAllGroups();
        mav.addObject("groupList", list);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("workerInfo") WorkerInfo workerInfo) {
        Boolean isSuccess = workerService.insert(workerInfo);
        if (isSuccess) {
            return "success";
        } else {
            return "fail";
        }
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        WorkerInfo workerInfo = new WorkerInfo();
        ModelAndView mav = new ModelAndView("worker/edit");
        if (StringUtils.isNotBlank(id)) {
            workerInfo = workerService.getById(Long.valueOf(id));
        }

        String javaopts = getJavaOpts(id);
        if (StringUtils.isNotEmpty(javaopts)) {
            workerInfo.setJavaopts(javaopts);
        }

        List<GroupInfo> list = groupService.getAllGroups();
        mav.addObject("groupList", list);
        mav.addObject("workerInfo", workerInfo);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("workInfo") WorkerInfo workInfo) {
        Boolean isSuccess = workerService.update(workInfo);
        if (isSuccess) {
            String javaopts = workInfo.getJavaopts();
            if (StringUtils.isNotEmpty(javaopts)) {
                String updateJavaopts = updateJavaOpts(workInfo.getId().toString(), javaopts);
                if (!"success".equals(updateJavaopts)) {
                    logger.error("update javaopts 失败，请检查！");
                    return "worker基础信息修改完成，但是javaopts参数更新失败! error info：" + updateJavaopts;
                }
            }

            return "success";
        } else {
            return "fail";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        Boolean isSuccess = workerService.delete(Long.valueOf(id));
        if (isSuccess) {
            return "success";
        } else {
            return "fail";
        }
    }

    private String getJavaOpts(String workerId) {
        try {
            if (StringUtils.isNotEmpty(workerId)) {
                WorkerInfo workerInfo = workerService.getById(Long.valueOf(workerId));
                if (workerInfo != null) {
                    String url = "http://" + workerInfo.getWorkerAddress() + ":" + workerInfo.getRestPort() + "/worker/getJavaOpts/" + workerId;

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity request = new HttpEntity(null, headers);

                    Map<String, String> result = new RestTemplate(Lists.newArrayList(new FastJsonHttpMessageConverter())).postForObject(url, request, Map.class);
                    return result.get("content");
                }

                return null;
            }

        } catch (Exception e) {
            return e.getMessage();
        }

        return null;
    }

    private String updateJavaOpts(String workerId, String content) {
        try {
            if (StringUtils.isEmpty(content)) {
                return "fail";
            }
            // TODO 对content内容有效性进行验证

            if (StringUtils.isNotEmpty(workerId)) {
                WorkerInfo workerInfo = workerService.getById(Long.valueOf(workerId));
                if (workerInfo != null) {
                    String url = "http://" + workerInfo.getWorkerAddress() + ":" + workerInfo.getRestPort() + "/worker/updateJavaOpts/" + workerId;

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<String, String> map = new HashMap<>();
                    map.put("content", content);
                    HttpEntity request = new HttpEntity(map, headers);

                    new RestTemplate(Lists.newArrayList(new FastJsonHttpMessageConverter())).postForObject(url, request, Map.class);
                    return "success";
                }

                return "fail";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/restartWorker")
    private String restartWorker(HttpServletRequest requestPara) {
        String workerId = requestPara.getParameter("id");
        if (StringUtils.isNotEmpty(workerId)) {
            WorkerInfo workerInfo = workerService.getById(Long.valueOf(workerId));
            if (workerInfo != null) {
                String url = "http://" + workerInfo.getWorkerAddress() + ":" + workerInfo.getRestPort() + "/worker/restartWorker/" + workerId;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);

                Map<String, String> result = new RestTemplate(Lists.newArrayList(new FastJsonHttpMessageConverter())).postForObject(url, request, Map.class);

                // TODO 重启完成后，记录此次的javaopts参数，添加到表t_dl_worker的字段parameters(定义一个类jvm_parameter.run_java_opts)
                // TODO 此处先不调整，原因是：rebalance时，应该获取worker当前正在运行的配置参数；


                return result.get("content");
            }

            return "fail";
        }

        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/toEditLogback")
    public String toLogback(HttpServletRequest requestPara) {
        String workerId = requestPara.getParameter("id");
        if (StringUtils.isBlank(workerId)) {
            return "fail";
        }
        try {
            WorkerInfo workerInfo = workerService.getById(Long.valueOf(workerId));
            if (workerInfo != null) {
                String url = "http://" + workerInfo.getWorkerAddress() + ":" + workerInfo.getRestPort() + "/worker/toEditLogback/" + workerId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);
                Map<String, String> result = new RestTemplate(Lists.newArrayList(new FastJsonHttpMessageConverter())).postForObject(url, request, Map.class);
                return result.get("content");
            }
            return "fail";
        } catch (Exception e) {
            logger.info("request to edit logback.xml error:", e);
            return e.getMessage();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doEditLogback")
    public String saveLogback(String workerId, String content) {
        logger.info("Receive a request to save logback.xml, with workerId " + workerId + "\r\n with content " + content);

        if (StringUtils.isBlank(content)) {
            return "content can not be null";
        }
        try {
            WorkerInfo workerInfo = workerService.getById(Long.valueOf(workerId));
            if (workerInfo != null) {
                String url = "http://" + workerInfo.getWorkerAddress() + ":" + workerInfo.getRestPort() + "/worker/doEditLogback/" + workerId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                Map<String, String> map = new HashMap<>();
                map.put("content", content);
                HttpEntity request = new HttpEntity(map, headers);
                new RestTemplate(Lists.newArrayList(new FastJsonHttpMessageConverter())).postForObject(url, request, String.class);
                return "success";
            }
            return "fail";
        } catch (Exception e) {
            logger.info("request to save logback.xml error:", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/toWorkerMonitor")
    public ModelAndView toWorkerMonitor(Long id) {
        ModelAndView mav = new ModelAndView("worker/workerMonitor");
        List<WorkerJvmStateInfo> jvmStateList = new ArrayList<>();
        List<WorkerSystemStateInfo> systemStateList = new ArrayList<>();
        //默认统计时间为当前时间之前一天
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date startTime = calendar.getTime();
        WorkerInfo workerInfo = new WorkerInfo();
        if (id != null) {
            workerInfo = workerService.getById(id);
            jvmStateList = workerJvmStateService.getListByWorkerIdForQuery(id, startTime, endTime);
            systemStateList = workerSystemStateService.getListByWorkerIdForQuery(id, startTime, endTime);
        }
        //JVM Monitor
        List<Long> youngUsedList = new ArrayList<>();
        List<Long> youngMaxList = new ArrayList<>();
        List<Long> oldUsedList = new ArrayList<>();
        List<Long> oldMaxList = new ArrayList<>();
        List<Long> youngGCCountList = new ArrayList<>();
        List<Long> oldGCCountList = new ArrayList<>();
        List<Long> youngGCTimeList = new ArrayList<>();
        List<Long> oldGCTimeList = new ArrayList<>();
        List<Integer> threadCountList = new ArrayList<>();
        List<String> createTimeList = new ArrayList<>();
        for (WorkerJvmStateInfo jvmStateInfo : jvmStateList) {
            youngUsedList.add(jvmStateInfo.getYoungMemUsed()/1024/1024);
            youngMaxList.add(jvmStateInfo.getYoungMemMax()/1024/1024);
            oldUsedList.add(jvmStateInfo.getOldMemUsed()/1024/1024);
            oldMaxList.add(jvmStateInfo.getOldMemMax()/1024/1024);
            youngGCCountList.add(jvmStateInfo.getIntervalYoungCollectionCount());
            oldGCCountList.add(jvmStateInfo.getIntervalOldCollectionCount());
            youngGCTimeList.add(jvmStateInfo.getIntervalYoungCollectionTime());
            oldGCTimeList.add(jvmStateInfo.getIntervalOldCollectionTime());
            threadCountList.add(jvmStateInfo.getCurrentThreadCount());
            Date createTime = jvmStateInfo.getCreateTime();
            String time = sdf.format(createTime);
            createTimeList.add(time);
        }
        String createTimeArr = JSONObject.toJSONString(createTimeList);
        //System Monitor
        List<BigDecimal> loadAverageList = new ArrayList<>();
        List<BigDecimal> userCPUUtilizationList = new ArrayList<>();
        List<BigDecimal> sysCPUUtilizationList = new ArrayList<>();
        List<BigDecimal> incomingNetworkTrafficList = new ArrayList<>();
        List<BigDecimal> outgoingNetworkTrafficList = new ArrayList<>();
        List<Long> tcpCurrentEstabList = new ArrayList<>();
        List<String> createTimeSysList = new ArrayList<>();
        for (WorkerSystemStateInfo systemStateInfo : systemStateList) {
            loadAverageList.add(systemStateInfo.getLoadAverage());
            userCPUUtilizationList.add(systemStateInfo.getUserCPUUtilization());
            sysCPUUtilizationList.add(systemStateInfo.getSysCPUUtilization());
            BigDecimal incomingMbps = new BigDecimal((double)systemStateInfo.getIncomingNetworkTraffic()/1024/1024*8).setScale(1, BigDecimal.ROUND_HALF_UP);
            incomingNetworkTrafficList.add(incomingMbps);
            BigDecimal outgoingMbps = new BigDecimal((double)systemStateInfo.getOutgoingNetworkTraffic()/1024/1024*8).setScale(1, BigDecimal.ROUND_HALF_UP);
            outgoingNetworkTrafficList.add(outgoingMbps);
            tcpCurrentEstabList.add(systemStateInfo.getTcpCurrentEstab());
            Date createTime = systemStateInfo.getCreateTime();
            String time = sdf.format(createTime);
            createTimeSysList.add(time);
        }
        String createTimeSysArr = JSONObject.toJSONString(createTimeSysList);

        mav.addObject("workerInfo", workerInfo);
        mav.addObject("youngUsedList", youngUsedList);
        mav.addObject("youngMaxList", youngMaxList);
        mav.addObject("oldUsedList", oldUsedList);
        mav.addObject("oldMaxList", oldMaxList);
        mav.addObject("youngGCCountList", youngGCCountList);
        mav.addObject("oldGCCountList", oldGCCountList);
        mav.addObject("youngGCTimeList", youngGCTimeList);
        mav.addObject("oldGCTimeList", oldGCTimeList);
        mav.addObject("threadCountList", threadCountList);
        mav.addObject("createTimeList", createTimeArr);
        mav.addObject("loadAverageList", loadAverageList);
        mav.addObject("userCPUUtilizationList", userCPUUtilizationList);
        mav.addObject("sysCPUUtilizationList", sysCPUUtilizationList);
        mav.addObject("incomingNetworkTrafficList", incomingNetworkTrafficList);
        mav.addObject("outgoingNetworkTrafficList", outgoingNetworkTrafficList);
        mav.addObject("tcpCurrentEstabList", tcpCurrentEstabList);
        mav.addObject("createTimeSysList", createTimeSysArr);
        return mav;
    }

    @RequestMapping(value = "/doSearchJvmMonitor")
    @ResponseBody
    public JSONObject doSearchJvmMonitor(@RequestBody Map<String, String> map) {
        Long workerId = Long.valueOf(map.get("workerId"));
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
            long betweenDays = (long) ((et - st) / (1000 * 60 * 60 * 24) + 0.5);
            if (betweenDays > 7) {
                Map<String, Object> failMap = new HashMap<>();
                failMap.put("failMessage", "查询日期间隔不能超过7天！");
                return new JSONObject(failMap);
            }
        }
        WorkerInfo worker = new WorkerInfo();
        List<WorkerJvmStateInfo> jvmStateList = new ArrayList<>();
        if (workerId != null) {
            worker = workerService.getById(workerId);
            jvmStateList = workerJvmStateService.getListByWorkerIdForQuery(workerId, startTime, endTime);
        }
        //JVM Monitor
        List<Long> youngUsedList = new ArrayList<>();
        List<Long> youngMaxList = new ArrayList<>();
        List<Long> oldUsedList = new ArrayList<>();
        List<Long> oldMaxList = new ArrayList<>();
        List<Long> youngGCCountList = new ArrayList<>();
        List<Long> oldGCCountList = new ArrayList<>();
        List<Long> youngGCTimeList = new ArrayList<>();
        List<Long> oldGCTimeList = new ArrayList<>();
        List<Integer> threadCountList = new ArrayList<>();
        List<String> createTimeList = new ArrayList<>();
        for (WorkerJvmStateInfo jvmStateInfo : jvmStateList) {
            youngUsedList.add(jvmStateInfo.getYoungMemUsed()/1024/1024);
            youngMaxList.add(jvmStateInfo.getYoungMemMax()/1024/1024);
            oldUsedList.add(jvmStateInfo.getOldMemUsed()/1024/1024);
            oldMaxList.add(jvmStateInfo.getOldMemMax()/1024/1024);
            youngGCCountList.add(jvmStateInfo.getIntervalYoungCollectionCount());
            oldGCCountList.add(jvmStateInfo.getIntervalOldCollectionCount());
            youngGCTimeList.add(jvmStateInfo.getIntervalYoungCollectionTime());
            oldGCTimeList.add(jvmStateInfo.getIntervalOldCollectionTime());
            threadCountList.add(jvmStateInfo.getCurrentThreadCount());
            Date createTime = jvmStateInfo.getCreateTime();
            String time = sdf.format(createTime);
            createTimeList.add(time);
        }

        JSONObject result = new JSONObject();
        result.put("workerInfo", worker);
        result.put("youngUsedList", youngUsedList);
        result.put("youngMaxList", youngMaxList);
        result.put("oldUsedList", oldUsedList);
        result.put("oldMaxList", oldMaxList);
        result.put("youngGCCountList", youngGCCountList);
        result.put("oldGCCountList", oldGCCountList);
        result.put("youngGCTimeList", youngGCTimeList);
        result.put("oldGCTimeList", oldGCTimeList);
        result.put("threadCountList", threadCountList);
        result.put("createTimeList", createTimeList);
        return result;
    }

    @RequestMapping(value = "/doSearchSystemMonitor")
    @ResponseBody
    public JSONObject doSearchSystemMonitor(@RequestBody Map<String, String> map) {
        Long workerId = Long.valueOf(map.get("workerId"));
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
            long betweenDays = (long) ((et - st) / (1000 * 60 * 60 * 24) + 0.5);
            if (betweenDays > 7) {
                Map<String, Object> failMap = new HashMap<>();
                failMap.put("failMessage", "查询日期间隔不能超过7天！");
                return new JSONObject(failMap);
            }
        }
        WorkerInfo worker = new WorkerInfo();
        List<WorkerSystemStateInfo> systemStateList = new ArrayList<>();
        if (workerId != null) {
            worker = workerService.getById(workerId);
            systemStateList = workerSystemStateService.getListByWorkerIdForQuery(workerId, startTime, endTime);
        }
        //System Monitor
        List<BigDecimal> loadAverageList = new ArrayList<>();
        List<BigDecimal> userCPUUtilizationList = new ArrayList<>();
        List<BigDecimal> sysCPUUtilizationList = new ArrayList<>();
        List<BigDecimal> incomingNetworkTrafficList = new ArrayList<>();
        List<BigDecimal> outgoingNetworkTrafficList = new ArrayList<>();
        List<Long> tcpCurrentEstabList = new ArrayList<>();
        List<String> createTimeSysList = new ArrayList<>();
        for (WorkerSystemStateInfo systemStateInfo : systemStateList) {
            loadAverageList.add(systemStateInfo.getLoadAverage());
            userCPUUtilizationList.add(systemStateInfo.getUserCPUUtilization());
            sysCPUUtilizationList.add(systemStateInfo.getSysCPUUtilization());
            BigDecimal incomingMbps = new BigDecimal((double)systemStateInfo.getIncomingNetworkTraffic()/1024/1024*8).setScale(1, BigDecimal.ROUND_HALF_UP);
            incomingNetworkTrafficList.add(incomingMbps);
            BigDecimal outgoingMbps = new BigDecimal((double)systemStateInfo.getOutgoingNetworkTraffic()/1024/1024*8).setScale(1, BigDecimal.ROUND_HALF_UP);
            outgoingNetworkTrafficList.add(outgoingMbps);
            tcpCurrentEstabList.add(systemStateInfo.getTcpCurrentEstab());
            Date createTime = systemStateInfo.getCreateTime();
            String time = sdf.format(createTime);
            createTimeSysList.add(time);
        }

        JSONObject result = new JSONObject();
        result.put("workerInfo", worker);
        result.put("loadAverageList", loadAverageList);
        result.put("userCPUUtilizationList", userCPUUtilizationList);
        result.put("sysCPUUtilizationList", sysCPUUtilizationList);
        result.put("incomingNetworkTrafficList", incomingNetworkTrafficList);
        result.put("outgoingNetworkTrafficList", outgoingNetworkTrafficList);
        result.put("tcpCurrentEstabList", tcpCurrentEstabList);
        result.put("createTimeSysList", createTimeSysList);
        return result;
    }

    private JvmSnapshot getJvmSnapshot(Long workerId) {
        try {
            WorkerInfo workerInfo = workerService.getById(workerId);
            if (workerInfo != null) {
                String url = "http://" + workerInfo.getWorkerAddress() + ":" + workerInfo.getRestPort() + "/worker/getJvmSnapshot/" + workerId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);
                return new RestTemplate(Lists.newArrayList(new FastJsonHttpMessageConverter())).postForObject(url, request, JvmSnapshot.class);
            }
        } catch (Exception e) {
            logger.error("something goes wrong when get jvm snapshot", e);
        }
        return null;
    }
}
