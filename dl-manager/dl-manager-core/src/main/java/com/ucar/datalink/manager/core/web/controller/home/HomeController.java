package com.ucar.datalink.manager.core.web.controller.home;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.statis.HomeStatistic;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import com.ucar.datalink.manager.core.web.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sqq on 2017/6/1.
 */
@Controller
@RequestMapping(value = "/home")
@AuthIgnore
public class HomeController {

    @Autowired
    GroupService groupService;
    @Autowired
    WorkerService workerService;
    @Autowired
    MediaSourceService mediaSourceService;
    @Autowired
    TaskConfigService taskConfigService;
    @Autowired
    private MediaService mediaService;

    @Autowired
    HomeStatisticService homeStatisticService;

    @RequestMapping(value = "/count")
    @ResponseBody
    public Page<StatisDetail> count() {
        Integer groupCount = groupService.groupCount();
        Integer workerCount = workerService.workerCount();
        Integer msCount = mediaSourceService.msCount();
        Integer taskCount = taskConfigService.taskCount();
        Integer mappingCount = mediaService.mappingCount();

        List<StatisDetail> statis = new ArrayList<>();
        StatisDetail statis1 = new StatisDetail();
        statis1.setGroupCount(groupCount);
        statis1.setWorkerCount(workerCount);
        statis1.setMsCount(msCount);
        statis1.setTaskCount(taskCount);
        statis1.setMappingCount(mappingCount);
        statis.add(statis1);
        return new Page<StatisDetail>(statis);
    }

    @ResponseBody
    @RequestMapping(value = "/statis")
    public JSONObject statis(Long groupId) {
        JSONObject statisAll = new JSONObject();
        Integer groupCount = groupService.groupCount();
        Integer workerCount = workerService.workerCount();
        Integer msCount = mediaSourceService.msCount();
        Integer taskCount = taskConfigService.taskCount();
        Integer mappingCount = mediaService.mappingCount();
        statisAll.put("groupCount", groupCount);
        statisAll.put("workerCount", workerCount);
        statisAll.put("msCount", msCount);
        statisAll.put("taskCount", taskCount);
        statisAll.put("mappingCount", mappingCount);

        groupId = groupId == -1L ? null : groupId;
        //设置统计时间为当前时间之前一小时
        Date endTime = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endTime);
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        Date startTime = calendar.getTime();
        List<HomeStatistic> taskSizeStatisticList = homeStatisticService.taskSizeStatistic(groupId, startTime, endTime);
        List<HomeStatistic> taskRecordStatisticList = homeStatisticService.taskRecordStatistic(groupId, startTime, endTime);
        List<HomeStatistic> taskDelayStatisticList = homeStatisticService.taskDelayStatistic(groupId, startTime, endTime);
        List<HomeStatistic> workerJvmUsedStatisticList = homeStatisticService.workerJvmUsedStatistic(groupId, startTime, endTime);
        List<HomeStatistic> workerYoungGCCountStatisticList = homeStatisticService.workerYoungGCCountStatistic(groupId, startTime, endTime);
        List<HomeStatistic> workerNetTrafficStatisticList = homeStatisticService.workerNetTrafficStatistic(groupId, startTime, endTime);

        List<Long> taskSizeList = new ArrayList<>();
        List<String> taskNameList = new ArrayList<>();
        for (HomeStatistic homeStatistic : taskSizeStatisticList) {
            taskSizeList.add(homeStatistic.getTaskSizeStatistic());
            taskNameList.add(homeStatistic.getTaskName());
        }

        List<Long> taskRecordsList = taskRecordStatisticList.stream().map(HomeStatistic::getTaskRecordStatistic).collect(Collectors.toList());

        List<String> taskNameListDelay = new ArrayList<>();
        List<Long> taskDelayTimeList = new ArrayList<>();
        for (HomeStatistic homeStatistic : taskDelayStatisticList) {
            taskDelayTimeList.add(homeStatistic.getTaskDelayStatistic());
            taskNameListDelay.add(homeStatistic.getTaskName());
        }

        List<String> workerNameList = new ArrayList<>();
        List<BigDecimal> workerJvmUsedList = new ArrayList<>();
        for (HomeStatistic homeStatistic : workerJvmUsedStatisticList) {
            workerJvmUsedList.add(homeStatistic.getWorkerJvmUsedStatistic().setScale(2, BigDecimal.ROUND_HALF_UP));
            workerNameList.add(homeStatistic.getWorkerName());
        }
        List<String> workerNameListGC = new ArrayList<>();
        List<BigDecimal> workerYoungGCCountList = new ArrayList<>();
        for (HomeStatistic homeStatistic : workerYoungGCCountStatisticList) {
            workerYoungGCCountList.add(homeStatistic.getWorkerYoungGCCountStatistic().setScale(2, BigDecimal.ROUND_HALF_UP));
            workerNameListGC.add(homeStatistic.getWorkerName());
        }

        List<String> workerNameListNet = new ArrayList<>();
        List<BigDecimal> workerNetTrafficList = new ArrayList<>();
        for (HomeStatistic homeStatistic : workerNetTrafficStatisticList) {
            BigDecimal workerNetTraffic = homeStatistic.getIncomingNetTrafficStatistic();
            BigDecimal workerNetTrafficMbps = new BigDecimal(workerNetTraffic.doubleValue() / 1024 / 1024 * 8).setScale(2, BigDecimal.ROUND_HALF_UP);
            workerNetTrafficList.add(workerNetTrafficMbps);
            workerNameListNet.add(homeStatistic.getWorkerName());
        }

        statisAll.put("taskNameList", taskNameList);
        statisAll.put("taskSizeList", taskSizeList);
        statisAll.put("taskRecordsList", taskRecordsList);
        statisAll.put("taskNameListDelay", taskNameListDelay);
        statisAll.put("taskDelayTimeList", taskDelayTimeList);
        statisAll.put("workerNameList", workerNameList);
        statisAll.put("workerJvmUsedList", workerJvmUsedList);
        statisAll.put("workerNameListGC", workerNameListGC);
        statisAll.put("workerYoungGCCountList", workerYoungGCCountList);
        statisAll.put("workerNameListNet", workerNameListNet);
        statisAll.put("workerNetTrafficList", workerNetTrafficList);

        return statisAll;
    }
}
