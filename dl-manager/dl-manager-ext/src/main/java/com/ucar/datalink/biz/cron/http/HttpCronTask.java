package com.ucar.datalink.biz.cron.http;

import com.ucar.datalink.biz.utils.HttpUtils;
import com.ucar.datalink.util.ConfigReadUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;

/**
 * Created by yang.wang09 on 2018-07-25 10:57.
 */

public class HttpCronTask implements Job {

    private static final Logger logger = LoggerFactory.getLogger(HttpCronTask.class);

    private static final String REST_URL_PREFIX = ConfigReadUtil.getString("datax.rest.url.prefix");
    private static final String URL = REST_URL_PREFIX +"/jobService/start?CONFIG_ID={0}&JOB_ID_SIGNAL={1}";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String job_name = context.getTrigger().getJobKey().getName();
            logger.info("execute -> "+job_name);
            HttpQuartzJob job = (HttpQuartzJob)context.getJobDetail().getJobDataMap().get(job_name);
            String jobConfigId = job.getJobConfigId()+"";
            String md5 = job.getMd5();
            String restUrl = MessageFormat.format(URL,jobConfigId,md5);
            HttpUtils.doGet(restUrl,new HashMap<String,String>());
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

}
