package com.ucar.datalink.manager.core.web.controller.job;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.JobControlService;
import com.ucar.datalink.biz.service.JobScheduleService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobConfigConstant;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobUtil;
import com.ucar.datalink.biz.utils.flinker.DateExpressionEngine;
import com.ucar.datalink.biz.utils.flinker.GetBetweenDate;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.job.JobCommand;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.JobScheduleInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.manager.core.flinker.cron.QuartzManager;
import com.ucar.datalink.manager.core.flinker.cron.entity.EntityCronUtil;
import com.ucar.datalink.manager.core.flinker.cron.entity.EntityQuartzJob;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.dto.job.JobScheduleView;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by yang.wang09 on 2018-07-24 10:44.
 */
@Controller
@RequestMapping(value = "/jobSchedule/")
@LoginIgnore
public class JobScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduleController.class);

    @Autowired
    private JobScheduleService jobScheduleService;

    @Autowired
    private JobService jobService;

    @Autowired
    private MediaSourceService mediaSourceService;

    @Autowired
    @Qualifier("dynamic")
    JobControlService jobControlService;


    private static ExecutorService executorService;

    private static Map<String,Future> fillDataSet = new ConcurrentHashMap<>();

    static {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @RequestMapping(value = "/ScheduleList")
    public ModelAndView jobList() {
        ModelAndView mav = new ModelAndView("jobSchedule/list");
        return mav;
    }


    @RequestMapping(value = "/initSchedule")
    @ResponseBody
    public Page<JobScheduleView> initJobs(@RequestBody Map<String, String> map) {
        int isTiming = Integer.parseInt(map.get("isTiming"));
        String srcType = map.get("srcType");
        String destType = map.get("destType");
        String srcName = getStringByDefault(map.get("srcName"), null);
        String destName = getStringByDefault(map.get("destName"), null);
        String mediaName = getStringByDefault(map.get("mediaName"), null);
        String name = getStringByDefault(map.get("jobName"), null);
        int schedule_state = Integer.parseInt(map.get("schedule_state"));

        if (StringUtils.isBlank(srcType)) {
            srcType = "-1";
        }
        if (StringUtils.isBlank(destType)) {
            destType = "-1";
        }
        if (StringUtils.isNotBlank(srcName)) {
            MediaSourceInfo info = mediaSourceService.getById(Long.parseLong(srcName));
            srcName = info.getName();
        }
        if (StringUtils.isNotBlank(destName)) {
            MediaSourceInfo info = mediaSourceService.getById(Long.parseLong(destName));
            destName = info.getName();
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
                destType = MediaSourceType.SDDL.name().toUpperCase();
                break;
        }

        Page<JobScheduleView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<JobScheduleInfo> jobScheduleInfoList = jobScheduleService.selectBatchJobSchedule(srcType, destType, srcName, destName, mediaName, name, isTiming, schedule_state);
        List<JobScheduleView> jobView = jobScheduleInfoList.stream().map(i -> {
            JobScheduleView view = new JobScheduleView();
            view.setId(i.getId() + "");
            JobConfigInfo jobConfigInfo = jobService.getJobConfigById(i.getJobId());
            MediaSourceInfo srcInfo = mediaSourceService.getById(new Long(jobConfigInfo.getJob_src_media_source_id()));
            MediaSourceInfo targetInfo = mediaSourceService.getById(new Long(jobConfigInfo.getJob_target_media_source_id()));
            if (srcInfo == null) {
                view.setSrc_media_name("none");
            } else {
                view.setSrc_media_name(srcInfo.getName());
            }
            if (targetInfo == null) {
                view.setTarget_media_name("none");
            } else {
                view.setTarget_media_name(targetInfo.getName());
            }
            view.setJob_name(jobConfigInfo.getJob_name());
            view.setMedia_name(jobConfigInfo.getJob_media_name());

            view.setSchedule_cron(i.getCron());
            view.setSchedule_max_retry(i.getRetryNumber() + "");
            view.setSchedule_retry_interval(i.getRetryInterval() + "");
            view.setSchedule_max_runtime(i.getMaxRunningTime() + "");
            if(i.getScheduleState()) {
                view.setSchedule_state("启动");
            } else {
                view.setSchedule_state("暂停");
            }
            if(fillDataSet.containsKey(jobConfigInfo.getJob_name())) {
                view.setFillDataState("true");
            } else {
                view.setFillDataState("false");
            }

            return view;
        }).collect(Collectors.toList());
        PageInfo<JobScheduleInfo> pageInfo = new PageInfo<>(jobScheduleInfoList);
        page.setDraw(page.getDraw());
        page.setAaData(jobView);
        page.setRecordsTotal( (int)pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }


    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("jobConfig/scheduleAdd");
        long id = Long.parseLong(request.getParameter("id"));
        JobScheduleView view = new JobScheduleView();
        view.setJob_id(id+"");
        mav.addObject("scheduleView", view);
        return mav;
    }



    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("scheduleView") JobScheduleView view) {
        try {
            JobScheduleInfo info = new JobScheduleInfo();
            info.setCron(view.getSchedule_cron());
            info.setIsDelete(false);
            info.setIsSuspend(false);
            info.setOnlineState(0);
            info.setIsRetry(Boolean.parseBoolean(view.getSchedule_is_retry()));
            info.setJobId(Long.parseLong(view.getJob_id()));
            info.setMaxRunningTime(Long.parseLong(view.getSchedule_max_runtime()));
            info.setRetryNumber(Integer.parseInt(view.getSchedule_max_retry()));
            info.setRetryInterval(Integer.parseInt(view.getSchedule_retry_interval()));
            info.setScheduleState(Boolean.parseBoolean(view.getSchedule_state()));
            info.setScheduleName(UUID.randomUUID().toString());
            jobScheduleService.create(info);
            long jobScheduleId = jobScheduleService.latestJobScheduleRecord().getId();
            info.setId(jobScheduleId);

            JobConfigInfo jobConfigInfo = jobService.getJobConfigById(info.getJobId());
            EntityQuartzJob job = EntityCronUtil.assembleCronTaskWithMD5(jobConfigInfo, info);
            EntityCronUtil.schdule(job);
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return e.getMessage();
        }
        return "success";
    }


    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("jobSchedule/edit");
        JobScheduleInfo info = new JobScheduleInfo();
        JobConfigInfo configInfo = new JobConfigInfo();
        if (StringUtils.isNotBlank(id)) {
            info = jobScheduleService.getById(Long.parseLong(id));
            configInfo = jobService.getJobConfigById(info.getJobId());

        }
        MediaSourceInfo srcInfo = mediaSourceService.getById(new Long(configInfo.getJob_src_media_source_id()));
        MediaSourceInfo targetInfo = mediaSourceService.getById(new Long(configInfo.getJob_target_media_source_id()));
        JobScheduleView view = new JobScheduleView();
        if (info != null) {
            view.setId(info.getId()+"");
            view.setJob_id(info.getJobId()+"");
            view.setJob_name(configInfo.getJob_name());
            view.setMedia_name(configInfo.getJob_media_name());
            if (srcInfo == null) {
                view.setSrc_media_name("none");
            } else {
                view.setSrc_media_name(srcInfo.getName());
            }
            if (targetInfo == null) {
                view.setTarget_media_name("none");
            } else {
                view.setTarget_media_name(targetInfo.getName());
            }

            view.setSchedule_name(info.getScheduleName());
            view.setSchedule_cron(info.getCron());
            if(info.getIsRetry()) {
                view.setSchedule_is_retry("true");
            } else {
                view.setSchedule_is_retry("false");
            }
            view.setSchedule_max_retry(info.getRetryNumber() + "");
            view.setSchedule_retry_interval(info.getRetryInterval() + "");
            view.setSchedule_max_runtime(info.getMaxRunningTime() + "");
            if(info.getScheduleState()) {
                view.setSchedule_state("true");
            } else {
                view.setSchedule_state("false");
            }
        }
        mav.addObject("scheduleView", view);
        return mav;
    }


    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("scheduleView") JobScheduleView view) {
        try {
            JobScheduleInfo info = new JobScheduleInfo();
            info.setId(Long.parseLong(view.getId()));
            info.setJobId(Long.parseLong(view.getJob_id()));
            info.setScheduleName(view.getSchedule_name());
            info.setCron(view.getSchedule_cron());
            Integer max_retry = Integer.parseInt(view.getSchedule_max_retry());
            if(max_retry > 0) {
                info.setIsRetry(true);
            } else {
                info.setIsRetry(false);
            }
            info.setRetryNumber(max_retry);
            info.setRetryInterval(Integer.parseInt(view.getSchedule_retry_interval()));
            info.setMaxRunningTime(Long.parseLong(view.getSchedule_max_runtime()));
            if("true".equals(view.getSchedule_state())) {
                info.setScheduleState(true);
            } else {
                info.setScheduleState(false);
            }
            info.setOnlineState(1);
            info.setIsSuspend(false);
            jobScheduleService.modify(info);

            if(info.getScheduleState()) {
                JobConfigInfo jobConfigInfo = jobService.getJobConfigById(info.getJobId());
                EntityQuartzJob job = EntityCronUtil.assembleCronTaskWithMD5(jobConfigInfo, info);
                EntityCronUtil.schdule(job);
            } else {
                JobConfigInfo jobConfigInfo = jobService.getJobConfigById(info.getJobId());
                EntityQuartzJob job = EntityCronUtil.assembleCronTask(jobConfigInfo, info);
                EntityCronUtil.pause(job);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return e.getMessage();
        }
        return "success";
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            JobScheduleInfo jobScheduleInfo = jobScheduleService.getById(Long.parseLong(id));
            JobConfigInfo jobConfigInfo = jobService.getJobConfigById(jobScheduleInfo.getJobId());
            jobScheduleService.remove(Long.parseLong(id));
            EntityQuartzJob job = EntityCronUtil.assembleCronTask(jobConfigInfo, jobScheduleInfo);
            if(job == null) {
                logger.error("EntityCronUtil.assembleCronTask job is null, job_config->"+jobConfigInfo.toString()+" job_schedule->"+jobScheduleInfo.toString());
            }
            EntityCronUtil.remove(job);
            return "success";
        } catch (ValidationException e) {
            logger.error("validation job schedule Error.", e);
            return e.getMessage();
        } catch(Exception ex) {
            logger.error("Delete job schedule Error.", ex);
            return ex.getMessage();
        }

    }

    @ResponseBody
    @RequestMapping(value = "/doStop")
    public String doStop(HttpServletRequest request) {

        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            JobScheduleInfo info = new JobScheduleInfo();
            info.setId(Long.parseLong(id));
            info.setScheduleState(false);
            jobScheduleService.modifyState(info);

            JobScheduleInfo scheduleInfo = jobScheduleService.getById(Long.parseLong(id));
            JobConfigInfo configInfo = jobService.getJobConfigById(scheduleInfo.getJobId());
            EntityQuartzJob job = EntityCronUtil.assembleCronTask(configInfo, scheduleInfo);
            //EntityCronUtil.pause(job);
            EntityCronUtil.remove(job);
            return "success";
        } catch (ValidationException e) {
            logger.error("Delete job schedule Error.", e);
            return e.getMessage();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doStart")
    public String doStart(HttpServletRequest request) {
        String id = request.getParameter("jobId");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            JobScheduleInfo info = new JobScheduleInfo();
            info.setId(Long.parseLong(id));
            info.setScheduleState(true);
            jobScheduleService.modifyState(info);

            JobScheduleInfo scheduleInfo = jobScheduleService.getById(Long.parseLong(id));
            JobConfigInfo configInfo = jobService.getJobConfigById(scheduleInfo.getJobId());
            EntityQuartzJob job = EntityCronUtil.assembleCronTaskWithMD5(configInfo, scheduleInfo);
            //EntityCronUtil.resume(job);
            EntityCronUtil.schdule(job);
            return "success";
        } catch (ValidationException e) {
            logger.error("Delete job schedule Error.", e);
            return e.getMessage();
        }
    }


    @ResponseBody
    @RequestMapping(value = "/doStartSync")
    public String doStartSync(HttpServletRequest request) {
        String id = request.getParameter("jobId");
        String name = request.getParameter("jobNameDisp");
        String timeExpress = request.getParameter("timeExpress");
        String timeToContinue = request.getParameter("timeToContinue");
        if (StringUtils.isBlank(id) || StringUtils.isBlank(name) ) {
            return "id is empty";
        }

        try {
            if (FlinkerJobUtil.isJobRunning(name)) {
                logger.info("[JobSechduleController]current job not end " + name);
                return "job still running";
            }
            long jobConfigId = jobService.getJobConfigIDByName(name);
            JobConfigInfo info = jobService.getJobConfigById(jobConfigId);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dateStr = sdf.format(new Date());
            String previousDateStr = DateExpressionEngine.formatDateExpression(timeExpress, dateStr);
            List<String> list = new ArrayList<>();
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            String currentStr = sdf2.format(new Date());
            if(Boolean.parseBoolean(timeToContinue)) {
                list.addAll(GetBetweenDate.getBetweenDate(previousDateStr, currentStr));
            } else {
                list.add(previousDateStr);
            }

            if( checkJobExistInSet(name) ) {
                return "job in running";
            }

            Future f = executorService.submit(new BatchTask(list, jobConfigId+"", name, info));
            fillDataSet.put(name,f);
            return "success";
        }catch(Exception e) {
            logger.error(e.getMessage(),e);
            return e.getMessage();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/canalSync")
    public String canalSync(HttpServletRequest request) {
        try {
            String id = request.getParameter("id");
            String name = request.getParameter("name");
            long jobConfigId = jobService.getJobConfigIDByName(name);
            JobConfigInfo configInfo = jobService.getJobConfigById(jobConfigId);
            String jobConfigName = configInfo.getJob_name();
            Future f = fillDataSet.get(jobConfigName);
            f.cancel(true);
            fillDataSet.remove(jobConfigName);
            return "success";
        }catch(Exception e) {
            logger.error(e.getMessage(),e);
            return e.getMessage();
        }

    }

    private class BatchTask implements Runnable {
        final List<String> list;
        final String id;
        final String name;
        final JobConfigInfo info;
        public BatchTask(List<String> list,String id,String name,JobConfigInfo info) {
            this.list = list;
            this.id = id;
            this.name = name;
            this.info = info;
        }

        @Override
        public void run() {
            try {
                for(String fillDataStr : list) {
                    Map<String, String> map = new HashMap<>();
                    JobCommand command = new JobCommand();
                    command.setJobId(new Long(id));
                    command.setJobName(name);
                    command.setType(JobCommand.Type.Start);
                    map.put(FlinkerJobConfigConstant.DATAX_FILL_DATA, fillDataStr);
                    Map<String, String> dyncParaMap =  FlinkerJobUtil.replaceDyncParaTofillDate(info, map);
                    if (dyncParaMap != null && dyncParaMap.size() > 0) {
                        command.setDynamicParam(true);
                        command.setMapParam(dyncParaMap);
                    }
                    String msg = jobControlService.start(command, info.getTiming_target_worker());

                    //等待10秒，再查询job运行结果，如果成功就继续执行，否则退出
                    Thread.sleep(10 * 1000);
                    if (StringUtils.isNumeric(msg)) {
                        long executeId = Long.parseLong(msg);
                        JobExecutionInfo executionInfo = jobService.getJobExecutionById(executeId);
                        if (StringUtils.equals(executionInfo.getState(), "SUCCESSED")) {
                            //如果成功就继续
                            continue;
                        }
                    } else {
                        //失败，退出
                        break;
                    }
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            } catch(Exception ex) {
                logger.error(ex.getMessage(),ex);
            } finally {
                 fillDataSet.remove(name);
            }
        }

    }


    private static synchronized boolean checkJobExistInSet(String name) {
        if(fillDataSet.containsKey(name)) {
            return true;
        }
        return false;
    }



    @ResponseBody
    @RequestMapping(value = "/toHistory")
    public ModelAndView toHistory(HttpServletRequest request) {
        String id = request.getParameter("id");
        JobScheduleInfo scheduleInfo = jobScheduleService.getById(Long.parseLong(id));
        String name = jobService.getJobConfigNameById(scheduleInfo.getJobId());
        ModelAndView mav = new ModelAndView("jobSchedule/history");
        mav.addObject("job_name", name);
        return mav;
    }



    @ResponseBody
    @RequestMapping(value = "/stopSchedule")
    public String stopSchedule(HttpServletRequest request) {
        try {
            QuartzManager.getInstance().stopSchedule();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(),e);
            return e.getMessage();
        }
        return "success";
    }

    @ResponseBody
    @RequestMapping(value = "/startSchedule")
    public String startSchedule(HttpServletRequest request) {
        try {
            QuartzManager.getInstance().startSchedule();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(),e);
            return e.getMessage();
        }
        return "success";
    }


    private static String getStringByDefault(Object obj, String value) {
        if (obj == null || StringUtils.isBlank(obj.toString())) {
            return value;
        }
        if ("-1".equals(obj.toString())) {
            return null;
        }
        return obj.toString();
    }



}