package com.ucar.datalink.manager.core.web.controller.job;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.JobQueueService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.utils.flinker.JobQueueScanUtil;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.job.*;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import com.ucar.datalink.manager.core.web.dto.job.JobExecutionView;
import com.ucar.datalink.manager.core.web.dto.job.JobQueueInfoView;
import com.ucar.datalink.manager.core.web.dto.job.JobQueueView;
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
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yang.wang09 on 2019-04-02 14:14.
 */

@Controller
@RequestMapping(value = "/jobQueue/")
public class JobQueueController {

    private static final Logger logger = LoggerFactory.getLogger(JobQueueController.class);

    @Autowired
    JobQueueService jobQueueService;

    @Autowired
    JobService jobService;

    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";

    private static final String CR_LF = "/r/n";


    @RequestMapping(value = "/toJobQueue")
    public ModelAndView toJobRunQueue() {
        ModelAndView mav = new ModelAndView("jobQueue/jobQueue");
        return mav;
    }

    @RequestMapping(value = "/toCreateQueue")
    @ResponseBody
    public ModelAndView doCreateQueue() {
        ModelAndView mav = new ModelAndView("jobQueue/create");
        return mav;
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(HttpServletRequest request) {
        String jobQueueName = request.getParameter("job_queue_name");
        String mail = request.getParameter("job_queue_mail");
        String failToStop = request.getParameter("job_fail_to_stop");
        JobQueueInfo info = new JobQueueInfo();
        info.setQueueName(jobQueueName);
        info.setMail(mail);
        info.setFailToStop(failToStop);
        long time = System.currentTimeMillis();
        info.setCreateTime(new Timestamp(time));
        info.setModifyTime(new Timestamp(time));
        jobQueueService.createJobQueueInfo(info);
        return "success";
    }

    @RequestMapping(value = "/allQueueInfo")
    @ResponseBody
    public List<String> allQueueInfo(HttpServletRequest request) {
        List<JobQueueInfo> queuInfoList = jobQueueService.allJobQueueInfo();
        List<String> list = new ArrayList<>();
        queuInfoList.forEach(i->{
            list.add(i.getQueueName());
        });
        return list;
    }


    @RequestMapping(value = "/initJobQueueInfoList")
    @ResponseBody
    public Page<JobQueueInfoView> initJobQueueInfoList(@RequestBody Map<String, String> map) {
        Page<JobQueueInfoView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<JobQueueInfo> queueInfoList = jobQueueService.allJobQueueInfo();

        List<JobQueueInfoView> jobView =  new ArrayList<>();
        queueInfoList.forEach(i->{
            JobQueueInfoView view = new JobQueueInfoView();
            view.setId(i.getId()+"");
            view.setQueueName(i.getQueueName());
            view.setMail(i.getMail());
            view.setQueueState(i.getQueueState());
            view.setCreateTime(i.getCreateTime().toString());
            view.setModifyTime(i.getModifyTime().toString());
            view.setFailToStop(i.getFailToStop()==null?"false":i.getFailToStop());
            jobView.add(view);
        });

        PageInfo<JobQueueInfo> pageInfo = new PageInfo<>(queueInfoList);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal( (int)pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }




    @ResponseBody
    @RequestMapping(value = "/QueueListInfo")
    @AuthIgnore
    public Map<String, Object> QueueListInfo() {
        List<JobQueueInfo> queuInfoList = jobQueueService.allJobQueueInfo();
        Map<String, Object> map = new HashMap<>();
        List<String> num = new ArrayList<>();
        List<String> val = new ArrayList<>();
        for (JobQueueInfo info : queuInfoList) {
            num.add(info.getId() + "");
            val.add(info.getQueueName());
        }
        map.put("num", num);
        map.put("val", val);
        return map;
    }



    @RequestMapping(value = "/allExecuteJobQueueInfo")
    @ResponseBody
    public List<String> allExecuteJobQueueInfo(HttpServletRequest request) {
        List<JobQueueInfo> queuInfoList = jobQueueService.allExecuteJobQueueInfo();
        List<String> list = new ArrayList<>();
        queuInfoList.forEach(i->{
            list.add(i.getQueueName());
        });
        return list;
    }


    /**
     * 加入队列
     *      支持批量加入
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/doPushQueue")
    @ResponseBody
    public String doPushQueue(HttpServletRequest request) {

        String jobIDStr = request.getParameter("queue_form_start_jobId");
        String queueName = request.getParameter("queueName");

        JobQueueInfo queueInfo = jobQueueService.getJobQueueInfoByName(queueName);
        //校验
        String[] jobIdArr = jobIDStr.split(",");
        for (String jobId : jobIdArr){
            JobConfigInfo configInfo = jobService.getJobConfigById(Long.parseLong(jobId));
            //检查这个jobID是否在job指定的job队列中了
            JobQueue q = jobQueueService.getJobQueueByJobJobNameAndQueueId(configInfo.getJob_name(), queueInfo.getId());
            if(q != null) {
                return "job id ->"+jobIDStr+" already in queue "+queueName;
            }
        }

        //加入
        for (String jobId : jobIdArr){
            JobConfigInfo configInfo = jobService.getJobConfigById(Long.parseLong(jobId));
            JobQueue queue = new JobQueue();
            queue.setJobName(configInfo.getJob_name());
            queue.setQueueId(queueInfo.getId());
            queue.setTableName(configInfo.getJob_media_name());
            queue.setJobState(JobExecutionState.UNEXECUTE);

            long time = System.currentTimeMillis();
            queue.setCreateTime(new Timestamp(time));
            queue.setModifyTime(new Timestamp(time));
            jobQueueService.createJobQueue(queue);
        }

        return "success";
    }



    @RequestMapping(value = "/initJobQueue")
    @ResponseBody
    public Page<JobQueueView> initJobQueue(@RequestBody Map<String, String> map) {

        //SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Page<JobQueueView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        String queueName = map.get("srcName");
        List<JobQueue> jobQueues = null;
        if(StringUtils.isNotBlank(queueName) && !StringUtils.equals("-1",queueName)) {
            long qid = Long.parseLong(queueName);
            jobQueues = jobQueueService.allJobQueueForQueueId(qid);
        }
        else {
            jobQueues = jobQueueService.allJobQueue();
        }


        List<JobQueueView> jobView =  new ArrayList<>();
        jobQueues.forEach(i->{
            JobQueueView view = new JobQueueView();
            view.setId(i.getId()+"");
            Long jobConfigId = jobService.getJobConfigIDByName(i.getJobName());
            view.setJobName(i.getJobName());
            view.setJobId(jobConfigId+"");
            JobQueueInfo jobQueueInfo = jobQueueService.getJobQueueInfoById(i.getQueueId());
            view.setQueueName(jobQueueInfo.getQueueName());
            view.setTableName(i.getTableName());
            view.setCreateTime(i.getCreateTime().toString());
            view.setModifyTime(i.getModifyTime().toString());
            view.setJobState(i.getJobState());
            jobView.add(view);
        });

        PageInfo<JobQueue> pageInfo = new PageInfo<>(jobQueues);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal( (int)pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        return page;
    }


    @RequestMapping(value = "/doOpenOrCloseQueue")
    @ResponseBody
    public String doOpenOrCloseQueue(HttpServletRequest request) {
        String state = request.getParameter("state");
        if("open".equalsIgnoreCase(state)) {
            JobQueueScanUtil.startScan();
            return "success";
        }
        else if("close".equalsIgnoreCase(state)) {
            JobQueueScanUtil.pauseScan();
            return "success";
        }
        else {
            //未知状态
            logger.warn("unkown state->"+state);
            return "failure";
        }

    }

    @RequestMapping(value = "/doJobQueueInfoState")
    @ResponseBody
    public String doJobQueueInfoState(HttpServletRequest request) {
        boolean isRunning = JobQueueScanUtil.isRunning();
        if(isRunning) {
            return "true";
        }
        return "false";
    }





    @RequestMapping(value = "/toShowQueueInfoList")
    @ResponseBody
    public ModelAndView toShowQueueInfoList() {
        ModelAndView mav = new ModelAndView("jobQueue/queueInfoList");
        return mav;
    }

    @RequestMapping(value = "/toHistoryInfo")
    @ResponseBody
    public ModelAndView toExecutionInfo(@ModelAttribute("queue_id") String queue_id) {
        ModelAndView mav = new ModelAndView("jobQueue/jobHistoryInfo");
        mav.addObject("queue_id", queue_id);
        return mav;
    }





    @RequestMapping(value = "/initJobQueueExecutionInfo")
    @ResponseBody
    public Page<JobExecutionView> initJobQueueExecutionInfo(@RequestBody Map<String, String> map) {
        long queueId = Long.parseLong(map.get("queue_id"));
        JobQueue queue = jobQueueService.getJobQueueById(queueId);
        String jobName = queue.getJobName();
        long jobId = jobService.getJobConfigIDByName(jobName);

        List<JobExecutionInfo> list = jobService.getJobExecutionByJobId(jobId);
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
            jobQueueService.deleteJobQueue(Long.parseLong(id));
            return "success";
        } catch (ValidationException e) {
            logger.error("Delete job queue failure.", e);
            return e.getMessage();
        }
    }


    @RequestMapping(value = "/doDeleteJobQueueInfo")
    @ResponseBody
    public String doDeleteJobQueueInfo(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            //获取队列下的所有任务，遍历任务并删除这些任务，最后删除队列
            List<JobQueue> queues = jobQueueService.allJobQueueForQueueId(Long.parseLong(id));
            queues.forEach(i->{
                jobQueueService.deleteJobQueue(i.getId());
            });
            jobQueueService.deleteJobQueueInfo(Long.parseLong(id));
            return "success";
        } catch (ValidationException e) {
            logger.error("Delete job queue info failure.", e);
            return e.getMessage();
        }
    }


    @RequestMapping(value = "/showCurrentState")
    @ResponseBody
    public String showCurrentState(HttpServletRequest request) {
        try {
            StringBuilder sb = new StringBuilder();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(baos);
            PrintWriter pw = new PrintWriter(osw);
            String jobName = JobQueueScanUtil.currentProcessJobName();
            if(StringUtils.isBlank(jobName)) {
                jobName = "没有job";
            }
            pw.println("当前正在运行的job->"+jobName);
            JobQueue q = jobQueueService.getJobQueueByName(jobName);
            if(q == null) {
                pw.println("获取不到其他信息");
                pw.close();
                return new String(baos.toByteArray());
            }
            JobQueueInfo info = jobQueueService.getJobQueueInfoById(q.getQueueId());
            if(info == null) {
                pw.println("获取不到队列信息");
                pw.close();
                return new String(baos.toByteArray());
            }
            List<JobQueue> list = jobQueueService.allJobQueueForQueueId(info.getId());
            if(list==null || list.size()==0) {
                pw.println("队列中没有其他任务");
                pw.close();
                return new String(baos.toByteArray());
            }
            pw.println("队列中的任务：");
            list.forEach(i -> {
                pw.println(i.getJobName()+"    "+i.getJobState());
            });
            pw.close();
            return new String(baos.toByteArray());
        } catch (Exception e) {
            logger.error("show current state failure.", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/editJobQueueInfo")
    @ResponseBody
    public ModelAndView editJobQueueInfo(@ModelAttribute("id") String queue_id) {
        ModelAndView mav = new ModelAndView("jobQueue/editJobQueueInfo");
        JobQueueInfo info = jobQueueService.getJobQueueInfoById(Long.parseLong(queue_id));

        mav.addObject("queueId", info.getId());
        mav.addObject("queueName", info.getQueueName());
        mav.addObject("mail", info.getMail());
        if(StringUtils.isBlank(info.getFailToStop())) {
            mav.addObject("failToStop","false");
        }else {
            Boolean b = Boolean.parseBoolean(info.getFailToStop());
            mav.addObject("failToStop",b.toString());
        }
        return mav;
    }



    @RequestMapping(value = "/doEditJobQueueInfo")
    @ResponseBody
    public String doEditJobQueueInfo(HttpServletRequest request) {
        String id = request.getParameter("queue_id");
        String mail = request.getParameter("job_queue_mail");
        String failToStop = request.getParameter("job_fail_to_stop");
        JobQueueInfo q = jobQueueService.getJobQueueInfoById(Long.parseLong(id));
        q.setMail(mail);
        q.setFailToStop(failToStop);
        jobQueueService.modifyJobQueueInfo(q);
        return "success";
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
