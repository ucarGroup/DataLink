package com.ucar.datalink.manager.core.flinker.cron.entity;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.JobControlService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.impl.JobServiceDynamicArgs;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobUtil;
import com.ucar.datalink.domain.job.JobCommand;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.TimingParameter;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yang.wang09 on 2019-02-13 15:37.
 */
public class EntityCronTask implements Job{

    private static final Logger logger = LoggerFactory.getLogger(EntityCronTask.class);

    JobService jobService;

    JobControlService jobControlService;

    public EntityCronTask() {
        jobService = DataLinkFactory.getObject(JobService.class);
        jobControlService = DataLinkFactory.getObject(JobServiceDynamicArgs.class);
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

            if (FlinkerJobUtil.isJobRunning(job_name)) {
                //如果当前这个job还没执行完，就给调用方返回一个 Long.MIN_VALUE，做一个特殊标志
                //待调用方下次再调用 doStart()
                logger.warn("[EntityCronTask]current job not end " + job_name);
                return;
            }

            Map<String, String> map = FlinkerJobUtil.replaceDynamicParameter(jobConfigInfo, new HashMap<String, String>());
            logger.info("[EntityCronTask]dynamic parameter -> " + map.toString());
            JobCommand command = new JobCommand();
            command.setJobId(new Long(jobConfigInfo.getId()));
            command.setJobName(job_name);
            command.setType(JobCommand.Type.Start);
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
