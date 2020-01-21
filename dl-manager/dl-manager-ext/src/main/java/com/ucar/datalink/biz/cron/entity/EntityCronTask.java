package com.ucar.datalink.biz.cron.entity;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.DoubleCenterDataxService;
import com.ucar.datalink.biz.service.JobControlService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.impl.JobServiceDynamicArgs;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.job.*;
import com.ucar.datalink.util.DataxUtil;
import com.ucar.datalink.util.SyncModifyUtil;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yang.wang09 on 2019-02-13 15:37.
 */
public class EntityCronTask implements Job{

    private static final Logger logger = LoggerFactory.getLogger(EntityCronTask.class);


    //@Autowired
    JobService jobService;

    //@Autowired
    //@Qualifier("dynamic")
    JobControlService jobControlService;

    DoubleCenterDataxService doubleCenterDataxService;

    public EntityCronTask() {
        jobService = DataLinkFactory.getObject(JobService.class);
        jobControlService = DataLinkFactory.getObject(JobServiceDynamicArgs.class);
        doubleCenterDataxService = DataLinkFactory.getObject(DoubleCenterDataxService.class);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String job_name = context.getTrigger().getJobKey().getName();
            logger.info("[EntityCronTask] execute -> "+job_name);
            EntityQuartzJob job = (EntityQuartzJob)context.getJobDetail().getJobDataMap().get(job_name);
            String jobConfigId = job.getJobConfigId()+"";
            String md5 = job.getMd5();
            JobConfigInfo jobConfigInfo = jobService.getJobConfigById(Long.parseLong(jobConfigId));
            String jobName = jobConfigInfo.getJob_name();

            if (DataxUtil.isJobRunning(job_name)) {
                //如果当前这个job还没执行完，就给调用方返回一个 Long.MIN_VALUE，做一个特殊标志
                //待调用方下次再调用 doStart()
                logger.warn("[EntityCronTask]current job not end " + job_name);
                return;
            }

            //如果job关联的数据源正在切机房中，禁止job启动
            if(doubleCenterDataxService.isSwitchLabIng(jobConfigInfo.getJob_src_media_source_id()) || doubleCenterDataxService.isSwitchLabIng(jobConfigInfo.getJob_target_media_source_id())){
                String msg = "当前job(" + jobConfigInfo.getJob_name() + ")关联的数据源正在切机房中，job暂时不能启动，请稍后再试！";
                logger.warn(msg);
                //记录本次提醒到异常表中
                JobExecutionInfo info = new JobExecutionInfo();
                info.setJob_id(jobConfigInfo.getId());
                info.setState(JobExecutionState.ABANDONED);
                info.setStart_time(new Timestamp(System.currentTimeMillis()));
                info.setEnd_time(new Timestamp(System.currentTimeMillis()));
                info.setException(msg);
                jobService.createJobExecution(info);
                return;
            }

            SyncModifyUtil.checkModifyColumn(jobConfigInfo);

            Map<String, String> map = DataxUtil.replaceDynamicParameter(jobConfigInfo, new HashMap<String,String>());
            logger.info("[EntityCronTask]dynamic parameter -> " + map.toString());
            DataxCommand command = new DataxCommand();
            command.setJobId(new Long(jobConfigInfo.getId()));
            command.setJobName(job_name);
            command.setType(DataxCommand.Type.Start);
            if (map != null && map.size() > 0) {
                command.setDynamicParam(true);
                command.setMapParam(map);
            }
            if (StringUtils.isNotBlank(jobConfigInfo.getTiming_parameter())) {
                TimingParameter p = JSONObject.parseObject(jobConfigInfo.getTiming_parameter(), TimingParameter.class);
                command.setJvmArgs(p.getJvmMemory());
            }

            //如果本地没启动datax服务，并且没有填写worker机器ip的话，将Command对象转换成json之前
            //就会报错了，这里方便后续调试加一个datax机器地址
            //info.setTiming_target_worker("1.2.3.4");
            logger.info("[EntityCronTask]pre start -> " + jobConfigInfo.toString());
            String msg = jobControlService.start(command, jobConfigInfo.getTiming_target_worker());
            if(StringUtils.equals(msg,"-1")) {
               logger.error("[EntityCronTask] job start failure");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

}
