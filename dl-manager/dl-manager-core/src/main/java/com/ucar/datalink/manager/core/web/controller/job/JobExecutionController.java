package com.ucar.datalink.manager.core.web.controller.job;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobUtil;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.domain.job.*;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.manager.core.web.dto.job.JobExecutionView;
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
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by user on 2017/7/14.
 */

@Controller
@RequestMapping(value = "/jobExecution/")
public class JobExecutionController {

    private static final Logger logger = LoggerFactory.getLogger(JobConfigController.class);

    /**
     * 格式化时间
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";

    /**
     * 默认每个页面显示的记录数量
     */
    private static final int DEFAULT_COUNT_PER_PAGE =  25;

    /**
     * 默认查询的起始位置
     */
    private static final int DEFAULT_START_POSTION = 0;


    private static final BigDecimal _1K = new BigDecimal(1*1024);

    private static final BigDecimal _1M = new BigDecimal(1*1024*1024);

    private static final BigDecimal _1G = new BigDecimal(1*1024*1024*1024);

    private static final String KB_PERSECOND = "KB/s";

    private static final String MB_PERSECOND = "MB/s";

    private static final String GB_PERSECOND = "GB/s";

    @Autowired
    JobService jobService;

    @Autowired
    MediaSourceService mediaSourceService;


    @RequestMapping(value = "/jobList")
    public ModelAndView jobList() {
        ModelAndView mav = new ModelAndView("jobExecution/list");
        return mav;
    }

    @RequestMapping(value = "/initJob")
    @ResponseBody
    public Page<JobExecutionView> initJobs(@RequestBody Map<String, String> map) {
        String stateType = map.get("stateType");
        String srcType = map.get("srcType");
        String destType = map.get("destType");
        String srcName = getStringByDefault(map.get("srcName"), null);
        String destName = getStringByDefault( map.get("destName"), null);
        String mediaName = getStringByDefault( map.get("mediaName"), null);
        String name = getStringByDefault(map.get("jobName"), null);
        String executionId = map.get("executionId");
        String startTime = map.get("startTime");
        String endTime = map.get("endTime");

        JobExecutionInfo singleJobExecutionInfo = null;
        if(StringUtils.isNotBlank(executionId)){
            singleJobExecutionInfo = jobService.getJobExecutionById(Long.parseLong(executionId));
        }

        if(StringUtils.isBlank(srcType)) {
            srcType = "-1";
        }
        if(StringUtils.isBlank(destType)) {
            destType = "-1";
        }
        if(StringUtils.isNotBlank(srcName)) {
            MediaSourceInfo info = mediaSourceService.getById(Long.parseLong(srcName));
            srcName = info.getName();
        }
        if(StringUtils.isNotBlank(destName)) {
            MediaSourceInfo info = mediaSourceService.getById(Long.parseLong(destName));
            destName = info.getName();
        }
        if(StringUtils.isBlank(startTime)) {
            startTime = null;
        }
        if(StringUtils.isBlank(endTime)) {
            endTime = null;
        }
        boolean isCheckAbandoned = false;
        boolean isCheckStart = false;

        long job_id = -1;
        if (StringUtils.isNotBlank(name)) {
            job_id = jobService.getJobConfigIDByName(name);
            stateType = "-1";
            srcType = "-1";
            destType = "-1";
        }

        switch (srcType) {
            case "-1":
                srcType = null;
                break;
            case "ElasticSearch":
                srcType = MediaSourceType.ELASTICSEARCH.name().toUpperCase();
                break;
            case "HBase":
                srcType = MediaSourceType.HBASE.name().toUpperCase();
                break;
            case "HDFS":
                srcType = MediaSourceType.HDFS.name().toUpperCase();
                break;
            case "MySql":
                srcType = MediaSourceType.MYSQL.name().toUpperCase();
                break;
            case "SqlServer":
                srcType = MediaSourceType.SQLSERVER.name().toUpperCase();
                break;
            case "PostgreSql":
                srcType = MediaSourceType.POSTGRESQL.name().toUpperCase();
                break;
            case "SDDL":
                srcType = MediaSourceType.SDDL.name().toUpperCase();
                break;
        }
        switch (destType) {
            case "-1":
                destType = null;
                break;
            case "ElasticSearch":
                destType = MediaSourceType.ELASTICSEARCH.name().toUpperCase();
                break;
            case "HBase":
                destType = MediaSourceType.HBASE.name().toUpperCase();
                break;
            case "HDFS":
                destType = MediaSourceType.HDFS.name().toUpperCase();
                break;
            case "MySql":
                destType = MediaSourceType.MYSQL.name().toUpperCase();
                break;
            case "SqlServer":
                destType = MediaSourceType.SQLSERVER.name().toUpperCase();
                break;
            case "PostgreSql":
                destType = MediaSourceType.POSTGRESQL.name().toUpperCase();
                break;
            case "SDDL":
                srcType = MediaSourceType.SDDL.name().toUpperCase();
                break;
        }
        switch (stateType) {
            case "-1":
//                set.add(JobExecutionState.ABANDONED);
//                set.add(JobExecutionState.FAILED);
//                set.add(JobExecutionState.KILLED);
//                set.add(JobExecutionState.RUNNING);
//                set.add(JobExecutionState.SUCCEEDED);
//                set.add(JobExecutionState.UNEXECUTE);
                isCheckAbandoned = true;
                isCheckStart = true;
                stateType = null;
                break;
            case "UNEXECUTE":
                stateType = JobExecutionState.UNEXECUTE;
                isCheckStart = true;
                break;
            case "RUNNING":
                stateType = JobExecutionState.RUNNING;
                isCheckAbandoned = true;
                break;
            case "KILLED":
                stateType = JobExecutionState.KILLED;
                isCheckStart = true;
                break;
            case "FAILED":
                stateType = JobExecutionState.FAILED;
                isCheckStart = true;
                break;
            case "SUCCEEDED":
                stateType = JobExecutionState.SUCCEEDED;
                isCheckStart = true;
                break;
            case "ABANDONED":
                stateType = JobExecutionState.ABANDONED;
                isCheckStart = true;
                break;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Page<JobExecutionView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        List<JobExecutionInfo> jobExecutionInfos = null;
        if(singleJobExecutionInfo == null) {
            jobExecutionInfos = jobService.queryJobExecutionStateByOptimized(stateType,job_id,srcType,srcName,destType,destName,mediaName,startTime,endTime);
        } else {
            jobExecutionInfos = new ArrayList<>();
            jobExecutionInfos.add(singleJobExecutionInfo);
        }
        List<JobExecutionView> jobView = jobExecutionInfos.stream().map(i -> {
            String jobConfigName = jobService.getJobConfigNameById(i.getJob_id());
            JobExecutionView view = new JobExecutionView();
            if ( StringUtils.isNotBlank(jobConfigName) ) {
                view.setJob_name(jobConfigName);
            } else {
                view.setJob_name("none");
            }
            view.setByte_speed_per_second( formatNumberToString(i.getByte_speed_per_second()) );
            String start_time = sdf.format(new Date(i.getStart_time().getTime()));
            String end_time = "";
            if(i.getEnd_time()!=null) {
                end_time = sdf.format(new Date(i.getEnd_time().getTime()));
            }
            view.setStart_time(start_time);
            view.setEnd_time(end_time);
            view.setException(i.getException());
            view.setId(i.getId());
            view.setJob_id(i.getJob_id());
            view.setJob_queue_execution_id(i.getJob_queue_execution_id());
            view.setOriginal_configuration(i.getOriginal_configuration());
            //view.setPercentage( (i.getPercentage()*100)+"%" );
            view.setPercentage( percentage_100_toString(i.getPercentage()) );
            view.setPid(i.getPid());
            view.setRecord_speed_per_second( i.getRecord_speed_per_second()+"records/s" );
            if(StringUtils.isBlank(i.getState())) {
                view.setState(JobExecutionState.UNEXECUTE);
            } else {
                view.setState(i.getState());
            }
            view.setTask_communication_info(i.getTask_communication_info());
            view.setTotal_error_records(i.getTotal_error_records());
            view.setTotal_record(i.getTotal_record());
            view.setWait_reader_time( rounding(i.getWait_reader_time()) );
            view.setWait_writer_time( rounding(i.getWait_writer_time()) );
            if(StringUtils.isBlank(i.getWorker_address())) {
                view.setWorker_address("");
            } else {
                view.setWorker_address(i.getWorker_address());
            }
            view.setAbandonedValue(0);
            if(JobExecutionState.ABANDONED.equals(view.getState()) || JobExecutionState.FAILED.equals(view.getState()) ||
                    JobExecutionState.KILLED.equals(view.getState()) || JobExecutionState.SUCCEEDED.equals(view.getState()) || JobExecutionState.UNEXECUTE.equals(view.getState())) {
               view.setStartValue(1);
            }
            try {
                String json = FlinkerJobUtil.formatJson(i.getOriginal_configuration());
                view.setOriginal_configuration(json);
            } catch(Exception e) {
                view.setOriginal_configuration(i.getOriginal_configuration());
            }

            return view;
        }).collect(Collectors.toList());
        PageInfo<JobExecutionInfo> pageInfo = new PageInfo<>(jobExecutionInfos);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal( (int)pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        //如果是获取所有类型，或者是获取RUNNING类型，则需要检查当前任务状态
        //如果任务状态是RUNNING，但是在zookeeper中已经不存在了，则废弃这个任务
        if(isCheckAbandoned) {
            Set<String> tasks = FlinkerJobUtil.getDataxRunningTask();
            for(JobExecutionView view : jobView) {
                if(JobExecutionState.RUNNING.equals(view.getState()) && !tasks.contains(view.getJob_name()) ) {
                    view.setAbandonedValue(1);
                }
            }
        }
        return page;
    }


    @ResponseBody
    @RequestMapping(value = "/doStart")
    public String doStart(HttpServletRequest request) {
        String id = request.getParameter("jobId");
        String jobName = request.getParameter("jobNameDisp");
        String jvmArgsXms = request.getParameter("jvmArgsXms");
        String jvmArgsXmx = request.getParameter("jvmArgsXmx");
        String worker = request.getParameter("worker");
        String isDebug = request.getParameter("isDebug");

        if (StringUtils.isBlank(id)) {
            logger.warn("id is null");
            return "fail";
        }
        if(StringUtils.isBlank(jobName)) {
            logger.warn("job name is null");
            return "fail";
        }
        if(StringUtils.isBlank(worker)) {
            logger.warn("worker address is null");
            return "fail";
        }

        JobCommand command = new JobCommand();
        command.setJobId(new Long(id));
        command.setJobName(jobName);
        command.setType(JobCommand.Type.Start);
        if("yes".equalsIgnoreCase(isDebug)) {
            command.setDebug(true);
        }
        if (!StringUtils.isBlank(jvmArgsXms) && !StringUtils.isBlank(jvmArgsXmx)) {
            command.setJvmArgs(MessageFormat.format("-Xms{0} -Xmx{1}", jvmArgsXms, jvmArgsXmx));
        }
        String json = JSONObject.toJSONString(command);
        //发送一个HTTP请求到 DataX服务器
        String address = FlinkerJobUtil.startURL(worker);
        String result = URLConnectionUtil.retryPOST(address,json);
        if(result!=null && result.contains("success")) {
            return "success";
        } else {
            return "fail";
        }
    }


    @ResponseBody
    @RequestMapping(value = "/doStop")
    public String doStop(HttpServletRequest request) {
        String id = request.getParameter("id");
        String jobName = request.getParameter("jobName");
        if (StringUtils.isBlank(id) || StringUtils.isBlank(jobName)) {
            logger.warn("id or job_name is null");
            return "fail";
        }
        JobRunningData data = FlinkerJobUtil.getRunningData(jobName);
        if(StringUtils.isBlank(data.getIp())) {
            logger.warn("cannot found ip, current job maybe stop "+jobName);
            return "fail";
        }

        JobExecutionInfo executionInfo = jobService.getJobExecutionById(Long.parseLong(id));
        JobCommand command = new JobCommand();
        command.setJobId( executionInfo.getJob_id() );
        command.setJobName(jobName);
        command.setType(JobCommand.Type.Stop);
        String json = JSONObject.toJSONString(command);
        //发送一个HTTP请求到 DataX服务器
        String address = FlinkerJobUtil.stopURL(data.getIp());
        String result = URLConnectionUtil.retryPOST(address, json);
        if(result!=null && result.contains("success")) {
            return "success";
        } else {
            return "fail";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doForceStop")
    public String doForceStop(HttpServletRequest request) {
        String id = request.getParameter("id");
        String jobName = request.getParameter("jobName");
        if (StringUtils.isBlank(id) || StringUtils.isBlank(jobName)) {
            logger.warn("id or job_name is null");
            return "fail";
        }
        JobRunningData data = FlinkerJobUtil.getRunningData(jobName);
        if(StringUtils.isBlank(data.getIp())) {
            logger.warn("cannot found ip, current job maybe stop "+jobName);
            return "fail";
        }

        JobExecutionInfo executionInfo = jobService.getJobExecutionById(Long.parseLong(id));
        JobConfigInfo info = jobService.getJobConfigById( executionInfo.getJob_id() );
        JobCommand command = new JobCommand();
        command.setJobId( executionInfo.getJob_id() );
        command.setJobName(jobName);
        command.setType(JobCommand.Type.Stop);
        command.setForceStop(true);
        String json = JSONObject.toJSONString(command);
        //发送一个HTTP请求到 DataX服务器
        String address = FlinkerJobUtil.forceStopURL(data.getIp());
        String result = URLConnectionUtil.retryPOST(address, json);

        int count = 0;
        boolean isError = false;
        while (true) {
            if (count > 10) {
                throw new RuntimeException("job关闭失败");
            }
            if (FlinkerJobUtil.isJobRunning(info.getJob_name()) ) {
                try {
                    Thread.sleep(1000);
                    count++;
                } catch (InterruptedException e) {
                    isError = true;
                    logger.error(e.getMessage(),e);
                }
            } else {
                jobService.modifyJobExecutionState(JobExecutionState.KILLED, "current job in zk is stop",
                        Long.parseLong(id));
                break;
            }
        }
        if(result!=null && result.contains("success") && !isError) {
            return "success";
        } else {
            return "fail";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doDiscard")
    public String doDiscard(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        jobService.modifyJobExecutionState(JobExecutionState.ABANDONED, "",Long.parseLong(id));
        return "success";
    }


    @ResponseBody
    @RequestMapping(value = "/doStat")
    public String doStat(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "";
        }
        JobExecutionInfo info = jobService.getJobExecutionById(Long.parseLong(id));
        return info.getException();
    }


    @ResponseBody
    @RequestMapping(value = "/doConfig")
    public String doConfig(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "";
        }
        JobExecutionInfo info = jobService.getJobExecutionById(Long.parseLong(id));
        String json = info.getOriginal_configuration();
        try {
            json = FlinkerJobUtil.formatJson(json);
        }catch(Exception e) {}
        return json;
    }



    private static String getStringByDefault(Object obj,String value) {
        if(obj==null || StringUtils.isBlank(obj.toString())) {
            return value;
        }
        if("-1".equals(obj.toString())) {
            return null;
        }
        return obj.toString();
    }


    public static String percentage_100_toString(double percentage) {
        BigDecimal db = new BigDecimal(percentage);
        String str = db.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        return str + "%";
    }

    public static String formatNumberToString(long number) {
        String format = "";
        if(number < JobExecutionView._1K) {
            format = number+"/s";
        }
        else if(number>JobExecutionView._1K && number<JobExecutionView._1M) {
            BigDecimal tmp = new BigDecimal(number);
            format = tmp.divide(_1K).setScale(2,BigDecimal.ROUND_HALF_UP).toString() + KB_PERSECOND;
        }
        else if(number>JobExecutionView._1M && number<JobExecutionView._1G) {
            BigDecimal tmp = new BigDecimal(number);
            format = tmp.divide(_1M).setScale(2,BigDecimal.ROUND_HALF_UP).toString() + MB_PERSECOND;
        }
        else {
            BigDecimal tmp = new BigDecimal(number);
            format = tmp.divide(_1G).setScale(2,BigDecimal.ROUND_HALF_UP).toString() + GB_PERSECOND;
        }
        return format;
    }


    public static double rounding(double number) {
        BigDecimal bd = new BigDecimal(number);
        return bd.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static int parseStringToInt(Object obj, int defaultNum) {
        if(obj == null) {
            return defaultNum;
        }
        String str = obj.toString();
        try {
            return Integer.parseInt(str);
        } catch(Exception e) {
            return defaultNum;
        }
    }


}
