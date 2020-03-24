package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.JobControlService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobUtil;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.domain.job.JobCommand;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.JobExecutionState;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 2017/9/21.
 */

@Service("dynamic")
public class JobServiceDynamicArgs implements JobControlService {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceDynamicArgs.class);

    @Autowired
    JobService jobService;

    @Override
    public String start(JobCommand command, Object additional) {
        String workerAddress = (String)additional;
        if(StringUtils.isBlank(workerAddress)) {
            workerAddress = FlinkerJobUtil.dynamicChoosenDataxMacheine();
        }
        if(StringUtils.isBlank(workerAddress)) {
            throw new RuntimeException("work address is emtpy");
        }
        command.setDebug(false);

        String json = JSONObject.toJSONString(command);
        logger.info("[JobServiceDynamicArgs]worker address="+workerAddress);
        logger.info("[JobServiceDynamicArgs]json content="+json);
        String address = FlinkerJobUtil.startURL(workerAddress);
        Timestamp start_time = new Timestamp(new Date().getTime());
        String result = URLConnectionUtil.retryPOST(address, json);
        if (result != null && result.contains("failure")) {
            logger.error("invoke datax failure,"+result);
            return "-1";
        }
        long id = command.getJobId();
        JobConfigInfo info = jobService.getJobConfigById(id);
        int count = 0;
        JobExecutionInfo executionInfo = null;
        while (true) {      //这个while循环的目的是判断任务是否启动了，任务启动后会在zookeeper上注册
            if (count > 20) {
                throw new RuntimeException("[JobServiceDynamicArgs]job start check failure,count > 20");
            }
            try {
                Thread.sleep(1000);
                count++;
            } catch (InterruptedException e) {
                throw new RuntimeException("[JobServiceDynamicArgs] job start check failure(interrupted exception)");
            }
            executionInfo = jobService.getJobExecutionById( Long.parseLong(result) );
            //如果获取到的job状态不是 UNEXECUTE 未初始化则返回
            if( executionInfo!=null && !JobExecutionState.UNEXECUTE.equals(executionInfo.getState()) ) {
                break;
            }
        }
        logger.info("[JobServiceDynamicArgs] " + executionInfo.toString());
        return executionInfo.getId()+"";
    }


    @Override
    public List<JobExecutionInfo> history(long id) {
        List<JobExecutionInfo> list = jobService.getJobExecutionByJobId(id);
        return list;
    }

    @Override
    public List<JobExecutionInfo> history(long jobId,long startPage,long count) {
        return jobService.getJobExecutionByJobId(jobId,startPage,count);
    }

    @Override
    public JobExecutionInfo state(long id) {
        return jobService.getJobExecutionById(id);
    }

}
