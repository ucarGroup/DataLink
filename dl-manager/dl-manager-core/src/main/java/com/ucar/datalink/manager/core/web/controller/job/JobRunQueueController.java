package com.ucar.datalink.manager.core.web.controller.job;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.JobRunQueueService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.impl.JobRunQueueDaemonService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.job.*;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.manager.core.web.dto.job.JobConfigView;
import com.ucar.datalink.manager.core.web.dto.job.JobExecutionView;
import com.ucar.datalink.manager.core.web.dto.job.JobRunQueueView;
import com.ucar.datalink.manager.core.web.util.Page;
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

/**
 * Created by user on 2018/1/17.
 */

//@Controller
//@RequestMapping(value = "/jobRunQueue/")
public class JobRunQueueController {

    private static final Logger logger = LoggerFactory.getLogger(JobRunQueueController.class);

    /**
     * 格式化时间
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";

    /**
     * 队列状态集合
     */
    private static final Set<String> JOB_RUN_QUEUE_STATE = new HashSet<>();



    @Autowired
    JobService jobService;

    @Autowired
    JobRunQueueService queueService;

    @Autowired
    MediaSourceService mediaSourceService;


    static {
        JOB_RUN_QUEUE_STATE.add( JobRunQueueState.FAILED );
        JOB_RUN_QUEUE_STATE.add( JobRunQueueState.PROCESSING );
        JOB_RUN_QUEUE_STATE.add( JobRunQueueState.STOP );
        JOB_RUN_QUEUE_STATE.add( JobRunQueueState.SUCCEEDED );
        JOB_RUN_QUEUE_STATE.add( JobRunQueueState.INIT );
        JOB_RUN_QUEUE_STATE.add( JobRunQueueState.READY);
    }


    @RequestMapping(value = "/toJobRunQueue")
    public ModelAndView toJobRunQueue() {
        ModelAndView mav = new ModelAndView("jobRunQueue/jobRunQueue");
        return mav;
    }

    @RequestMapping(value = "/initJobQueue")
    @ResponseBody
    public Page<JobRunQueueView> initJobQueue(@RequestBody Map<String, String> map) {
        String state = map.get("stateType");
        List<JobRunQueueInfo> list = new ArrayList<>();
        if("-1".equals(state)) {
            state = null;
        }
        list = queueService.getAllJobRunQueueInfoByState(state);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Page<JobRunQueueView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        List<JobRunQueueView> jobView = list.stream().map(i -> {
            JobRunQueueView view = new JobRunQueueView();
            view.setId(i.getId());
            view.setJobIdList(i.getJobIdList());
            view.setCurrentPorcessId(i.getCurrentPorcessId());
            view.setJobCount(i.getJobCount());
            view.setSuccessCount( parseListCount(i.getSuccessList()) );
            view.setFailureCount( parseListCount(i.getFailureList()) );
            view.setQueueState(i.getQueueState());
            String start_time = sdf.format(new Date(i.getCreateTime().getTime()));
            String end_time = sdf.format(new Date(i.getModifyTime().getTime()));
            view.setCreateTime(start_time);
            view.setModifyTime(end_time);
            return view;
        }).collect(Collectors.toList());
        PageInfo<JobRunQueueInfo> pageInfo = new PageInfo<>(list);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal( (int)pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }


    @RequestMapping(value = "/doDeleteJobQueue")
    @ResponseBody
    public String doDeleteJobQueue(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            queueService.removeJobRunQueueInfo(Long.parseLong(id));
            return "success";
        } catch (ValidationException e) {
            logger.error("Delete job queue failure.", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/toJobRunQueueConfigInfo")
    @ResponseBody
    public ModelAndView toJobRunQueueConfigInfo(@ModelAttribute("queue_id") String queue_id) {
        ModelAndView mav = new ModelAndView("jobRunQueue/jobRunConfigInfo");
        mav.addObject("queue_id", queue_id);
        return mav;
    }


    @RequestMapping(value = "/toEdit")
    @ResponseBody
    public ModelAndView toEdit(@ModelAttribute("queue_id") String queue_id) {
        JobRunQueueInfo info = queueService.getJobRunQueueInfoById(Long.parseLong(queue_id));
        ModelAndView mav = new ModelAndView("jobRunQueue/jobRunQueueEdit");
        JobRunQueueView view = new JobRunQueueView();
        view.setId(info.getId());
        view.setJobIdList(info.getJobIdList());
        mav.addObject("view", view);
        return mav;
    }



    @RequestMapping(value = "/initJobQueueConfigInfo")
    @ResponseBody
    public Page<JobConfigView> initJobQueueConfigInfo(@RequestBody Map<String, String> map) {
        long queueId = Long.parseLong(map.get("queue_id"));
        JobRunQueueInfo info = queueService.getJobRunQueueInfoById(queueId);
        String idList = info.getJobIdList();
        List<JobConfigInfo> list = new ArrayList<>();
        String[] arr = idList.split(",");
        for(String id : arr) {
            JobConfigInfo configInfo = jobService.getJobConfigById(Long.parseLong(id));
            list.add(configInfo);
        }

        Page<JobConfigView> page = new Page<>(map);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        List<JobConfigView> jobView = list.stream().map(i -> {
            JobConfigView view = new JobConfigView();
            String create_time = sdf.format(new Date(i.getCreate_time().getTime()));
            view.setCreate_time(create_time);
            view.setId(i.getId());
            view.setIs_delete(i.is_delete());
            view.setJob_content(i.getJob_content());
            view.setJob_desc(i.getJob_desc());
            view.setJob_media_name(i.getJob_media_name());
            view.setJob_name(i.getJob_name());

            MediaSourceInfo srcInfo = mediaSourceService.getById(new Long(i.getJob_src_media_source_id()));
            MediaSourceInfo targetInfo = mediaSourceService.getById(new Long(i.getJob_target_media_source_id()));
            if (srcInfo == null) {
                view.setJob_src_media_source_name("none");
            } else {
                view.setJob_src_media_source_name(srcInfo.getName());
            }
            if (targetInfo == null) {
                view.setJob_target_media_source_name("none");
            } else {
                view.setJob_target_media_source_name(targetInfo.getName());
            }
            view.setModify_time(i.getModify_time());
            if (i.getTiming_expression() == null || "".equals(i.getTiming_expression())) {
                view.setTiming_expression("none");
            } else {
                view.setTiming_expression(i.getTiming_expression());
            }
            view.setTiming_on_yn(i.isTiming_on_yn());

            if (i.getTiming_parameter() == null || "".equals(i.getTiming_parameter())) {
                view.setTiming_parameter("none");
            } else {
                view.setTiming_parameter(i.getTiming_parameter());
            }
            if (i.getTiming_target_worker() == null || "".equals(i.getTiming_target_worker())) {
                view.setTiming_target_worker("none");
            } else {
                view.setTiming_parameter(i.getTiming_parameter());
            }

            if (JobConfigInfo.TIMING_TRANSFER_TYPE_FULL.equals(i.getTiming_transfer_type())) {
                view.setTiming_transfer_type("全量");
            } else {
                view.setTiming_transfer_type("增量");
            }
            view.setTiming_yn(Boolean.toString(i.isTiming_yn()));
            return view;
        }).collect(Collectors.toList());
        PageInfo<JobConfigInfo> pageInfo = new PageInfo<>(list);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }


    @RequestMapping(value = "/doOpenOrCloseQueue")
    @ResponseBody
    public String doOpenOrCloseQueue(HttpServletRequest request) {
        String state = request.getParameter("state");
        if("open".equalsIgnoreCase(state)) {
            JobRunQueueDaemonService.startJobRunQueue();
            return "success";
        }
        else if("close".equalsIgnoreCase(state)) {
            JobRunQueueDaemonService.stopJobRunQueue();
            return "success";
        }
        else {
            //未知状态
            logger.warn("unkown state->"+state);
            return "failure";
        }

    }

    @RequestMapping(value = "/doUpdateState")
    @ResponseBody
    public String doUpdateState(HttpServletRequest request) {
        String id = request.getParameter("id");
        String state = request.getParameter("state");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        if(StringUtils.isBlank(state) || !JOB_RUN_QUEUE_STATE.contains(state)) {
            return "fail";
        }
        try {
            queueService.modifyJobRunQueueState(state,Long.parseLong(id));
            return "success";
        } catch (ValidationException e) {
            logger.error("update job queue state failure.", e);
            return e.getMessage();
        }
    }


    @RequestMapping(value = "/doUpdateJobRunQueueById")
    @ResponseBody
    public String doUpdateJobRunQueueById(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }

        try {
            JobRunQueueInfo info = queueService.getJobRunQueueInfoById(Long.parseLong(id));
            JobRunQueueInfo newInfo = new JobRunQueueInfo();
            newInfo.setId(info.getId());
            newInfo.setCurrentPorcessId("");
            newInfo.setJobCount(info.getJobCount());
            newInfo.setSuccessList("");
            newInfo.setFailureList("");
            newInfo.setQueueState(JobRunQueueState.READY);
            queueService.modifyJobRunQueueInfo(newInfo);
            return "success";
        } catch (ValidationException e) {
            logger.error("update job queue state failure.", e);
            return e.getMessage();
        }
    }


    @RequestMapping(value = "/doUpdateInitStateJobQueue")
    @ResponseBody
    public String doUpdateInitStateJobQueue(@ModelAttribute("view") JobRunQueueView view) {
        try {
            long id = view.getId();
            String jobIdList = view.getJobIdList();
            JobRunQueueInfo newInfo = new JobRunQueueInfo();
            newInfo.setId(id);
            newInfo.setJobIdList(jobIdList);
            newInfo.setCurrentPorcessId("");
            if(StringUtils.isNotBlank(jobIdList)) {
                String[] ids = jobIdList.split(",");
                for(String s : ids) {
                    JobConfigInfo jci = jobService.getJobConfigById(Long.parseLong(s));
                    if(null == jci) {
                        return "job config id="+s+" not exist!";
                    }
                }
                int length = jobIdList.split(",").length;
                newInfo.setJobCount(length);
            } else {
                return "job id list is empty!";
            }
            newInfo.setSuccessList("");
            newInfo.setFailureList("");
            newInfo.setQueueState(JobRunQueueState.INIT);
            queueService.modifyJobRunQueueConfig(newInfo);
            return "success";
        } catch (ValidationException e) {
            logger.error("update job queue state failure.", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/toExecutionInfo")
    @ResponseBody
    public ModelAndView toExecutionInfo(@ModelAttribute("queue_id") String queue_id) {
        ModelAndView mav = new ModelAndView("jobRunQueue/jobRunExecutionInfo");
        mav.addObject("queue_id", queue_id);
        return mav;
    }


    @RequestMapping(value = "/initJobQueueExecutionInfo")
    @ResponseBody
    public Page<JobExecutionView> initJobQueueExecutionInfo(@RequestBody Map<String, String> map) {
        long queueId = Long.parseLong(map.get("queue_id"));
        JobRunQueueInfo info = queueService.getJobRunQueueInfoById(queueId);
        List<Long> processList = parseJobExecutionList( info.getCurrentPorcessId() );
        List<Long> successList = parseJobExecutionList( info.getSuccessList() );
        List<Long> failureList = parseJobExecutionList( info.getFailureList() );
        processList.addAll(successList);
        processList.addAll(failureList);

        List<JobExecutionInfo> list = new ArrayList<>();
        for(Long lo : processList) {
            JobExecutionInfo i = jobService.getJobExecutionById(lo);
            list.add(i);
        }

        boolean isCheckAbandoned = false;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Page<JobExecutionView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<JobExecutionView> jobView = list.stream().map(i -> {
            String jobConfigName = jobService.getJobConfigNameById(i.getJob_id());
            JobExecutionView view = new JobExecutionView();
            if ( StringUtils.isNotBlank(jobConfigName) ) {
                view.setJob_name(jobConfigName);
            } else {
                view.setJob_name("none");
            }
            view.setByte_speed_per_second( JobExecutionController.formatNumberToString(i.getByte_speed_per_second()) );
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
            view.setPercentage( JobExecutionController.percentage_100_toString(i.getPercentage()) );
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
            view.setWait_reader_time( JobExecutionController.rounding(i.getWait_reader_time()));
            view.setWait_writer_time( JobExecutionController.rounding(i.getWait_writer_time()) );
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
            return view;
        }).collect(Collectors.toList());
        PageInfo<JobExecutionInfo> pageInfo = new PageInfo<>(list);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        //如果是获取所有类型，或者是获取RUNNING类型，则需要检查当前任务状态
        //如果任务状态是RUNNING，但是在zookeeper中已经不存在了，则废弃这个任务
//        if(isCheckAbandoned) {
//            Set<String> tasks = FlinkerJobUtil.getDataxRunningTask();
//            for(JobExecutionView view : jobView) {
//                if(JobExecutionState.RUNNING.equals(view.getState()) && !tasks.contains(view.getJob_name()) ) {
//                    view.setAbandonedValue(1);
//                }
//            }
//        }

        return page;
    }


    @RequestMapping(value = "/doTop")
    @ResponseBody
    public String doTop(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            queueService.modifyJobRunueueTopTime(Long.parseLong(id));
            return "success";
        } catch (ValidationException e) {
            logger.error("doTop job queue failure.", e);
            return e.getMessage();
        }
    }



    public static int parseListCount(String idList) {
        if(StringUtils.isBlank(idList)) {
            return 0;
        }
        return idList.split(",").length;
    }


    public static List<Long> parseJobExecutionList(String listStr) {
        List<Long> list = new ArrayList<>();
        if(null!=listStr && !"".equals(listStr)) {
            String[] arr = listStr.split(",");
            for(String s : arr) {
                long executionId = Long.parseLong( s.split("-")[1] );
                list.add(executionId);
            }
        }
        return list;
    }


}
