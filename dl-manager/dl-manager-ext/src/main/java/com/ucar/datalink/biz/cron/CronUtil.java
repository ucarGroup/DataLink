package com.ucar.datalink.biz.cron;

import com.ucar.datalink.biz.cron.http.HttpQuartzJob;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobScheduleInfo;

/**
 * Created by yang.wang09 on 2019-02-13 16:37.
 */
public class CronUtil {

    public static HttpQuartzJob assembleCronTask(JobConfigInfo info, JobScheduleInfo scheduleInfo) {
        HttpQuartzJob job = new HttpQuartzJob();
        job.setJobConfigId(info.getId());
        job.setCronExpression(scheduleInfo.getCron());
        job.setJobGroup("cron");
        job.setJobName(info.getJob_name() +"@"+ scheduleInfo.getScheduleName());
        return job;
    }

}
