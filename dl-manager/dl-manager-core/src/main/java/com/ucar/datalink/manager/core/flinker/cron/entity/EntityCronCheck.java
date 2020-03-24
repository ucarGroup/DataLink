package com.ucar.datalink.manager.core.flinker.cron.entity;

import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.JobExecutionState;
import com.ucar.datalink.manager.core.flinker.cron.QuartzManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by yang.wang09 on 2019-02-13 15:45.
 */
public class EntityCronCheck implements Job {

    private static final Logger logger = LoggerFactory.getLogger(EntityCronCheck.class);

    private JobService jobService = DataLinkFactory.getObject(JobService.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String job_name = context.getTrigger().getJobKey().getName();
        logger.info("execute -> "+job_name);
        EntityQuartzJob job = (EntityQuartzJob)context.getJobDetail().getJobDataMap().get(job_name);
        long executionId = job.getExecuteId();
        JobExecutionInfo executionInfo = jobService.getJobExecutionById(executionId);

        //如果成功，自己删除自己
        if(JobExecutionState.SUCCEEDED.equalsIgnoreCase(executionInfo.getState())) {
            QuartzManager.getInstance().deleteCheckJob(job);
            return;
        }
        //如果不是失败状态也删除这个任务
        if( !JobExecutionState.FAILED.equalsIgnoreCase(executionInfo.getState()) ) {
            QuartzManager.getInstance().deleteCheckJob(job);
            return;
        }

        //创建一个重试的 定时任务，然后再自己删除自己
        QuartzManager.getInstance().deleteCheckJob(job);
        EntityQuartzJob retryJob = EntityCronUtil.cloneWithExecuteId(job, executionId);




    }



}
