package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.service.JobDaemonService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.utils.RestServerUtils;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.JobExecutionMonitor;
import com.ucar.datalink.util.ConfigReadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yang.wang09 on 2018-10-11 16:29.
 */
public class FailureJobCheckDaemonServiceImpl extends JobDaemonService {

    private static final Logger logger = LoggerFactory.getLogger(FailureJobCheckDaemonServiceImpl.class);

    /**
     * 格式化时间
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";


    @Autowired
    private JobService service;

    private Date lastExecuteTime = null;


    public FailureJobCheckDaemonServiceImpl() {
        logger.info("FailureJobCheckDaemonServiceImpl initialized...");
    }

    @Override
    public void initialized() {
        logger.info("initialized...");
    }

    @Override
    public void destroyed() {

    }


    public void monitorFailureJob() {
        List<JobExecutionMonitor> list = getFailureJobExecutionInfo();
        logger.debug("getFailureJobExecutionInfo -> "+list.toString());
        if(list==null || list.size()==0) {
            return;
        }
        Map<Long, String> request = new HashMap<>();

        for(JobExecutionMonitor m : list) {
            String mailContent = buildDataxErrMsg(m);
            String errBase64 = Base64.getEncoder().encodeToString(mailContent.getBytes());
            request.put(m.getResourceId(), errBase64);
        }
        sendDirectly(request);

    }

    private void sendDirectly(Map<Long, String> map){
        try {
            RestServerUtils.executeRemote(map, "/communication/putDataxExceptionAndSend");
            logger.info("send /communication/putDataxExceptionAndSend");
        } catch (Throwable t) {
            logger.error("exception send failed in direct mode.", t);
        }
    }

    private List<JobExecutionMonitor> getFailureJobExecutionInfo() {
        if(lastExecuteTime == null) {
            lastExecuteTime = new Date();
        }
        Timestamp timestamp = new Timestamp(lastExecuteTime.getTime());
        lastExecuteTime = new Date();
        return service.getAllFailureByMonitorCat(timestamp);
    }


    private String buildDataxErrMsg(JobExecutionMonitor monitor) {
        StringBuilder content = new StringBuilder();
        content.append("hi:").append("<br>").append("<br>");
        String env = ConfigReadUtil.getString("datax.env");
        content.append("\r\n\r\n\r\n").append("当前环境 : ").append(env).append("\r\n");
        content.append("失败的job 信息如下").append("\r\n");
        content.append("\r\n").append("execution id : ").append(monitor.getExecuteId());
        content.append("\r\n").append("job id       : ").append(monitor.getJobconfigId());
        content.append("\r\n").append("job name     : ").append(monitor.getJobName());
        content.append("\r\n").append("work address : ").append(monitor.getWorkerAddress());
        content.append("\r\n").append("start time   : ").append(formatString( monitor.getStartTime() ));
        content.append("\r\n").append("end time     : ").append(formatString( monitor.getEndTime()));
        content.append("\r\n").append("exception    : ").append(monitor.getException());
        return content.toString();
    }

    private String assembleErrMsg(JobExecutionMonitor monitor) {
        StringBuilder content = new StringBuilder();
        content.append("hi:").append("<br>").append("<br>");
        String env = ConfigReadUtil.getString("datax.env");
        content.append("&nbsp &nbsp &nbsp").append("当前环境 : ").append(env).append("<br/>");
        content.append("失败的job 信息如下").append("<br/>");
        content.append("<table border='1'>" +
                "<tr>" +
                "<td>job execute id</td>" +
                "<td>job id</td>" +
                "<td>job name</td>" +
                "<td>work address</td>" +
                "<td>start time</td>" +
                "<td>end time</td>" +
                "<td>exception</td>" +
                "</tr>");
        content.append("<tr>");
        content.append("<td>").append(monitor.getExecuteId()).append("</td>");
        content.append("<td>").append(monitor.getJobconfigId()).append("</td>");;
        content.append("<td>").append(monitor.getJobName()).append("</td>");
        content.append("<td>").append(monitor.getWorkerAddress() == null ? " " : monitor.getWorkerAddress()).append("</td>");
        content.append("<td>").append(formatString(monitor.getStartTime())).append("</td>");
        content.append("<td>").append(formatString(monitor.getEndTime())).append("</td>");
        content.append("<td>").append(monitor.getException() == null ? "" : monitor.getException()).append("</td>");
        content.append("</tr>");
        return content.toString();
    }

    private final String formatString(Timestamp t) {
        if (t == null) {
            return "";
        }
        Date d = new Date(t.getTime());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String result = sdf.format(d);
        return result;
    }


}
