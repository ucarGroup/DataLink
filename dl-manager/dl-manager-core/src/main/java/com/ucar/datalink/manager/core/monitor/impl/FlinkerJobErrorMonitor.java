package com.ucar.datalink.manager.core.monitor.impl;

import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.domain.job.JobExecutionMonitor;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.manager.core.monitor.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by lubiao on 2020/3/19.
 */
@Service
public class FlinkerJobErrorMonitor extends Monitor{

    private static final Logger logger = LoggerFactory.getLogger(FlinkerJobErrorMonitor.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";

    private Date lastExecuteTime = null;

    @Autowired
    private JobService service;

    @Autowired
    AlarmService alarmService;

    @Autowired
    private MonitorService monitorService;


    @Override
    public void doMonitor() {
        List<JobExecutionMonitor> list = getFailureJobExecutionInfo();
        if (list == null || list.size() == 0) {
            return;
        }

        for (JobExecutionMonitor m : list) {
            MonitorInfo monitorInfo = monitorService.getByResourceAndType(m.getResourceId(), MonitorType.FLINKER_EXCEPTION_MONITOR);
            String errMsg = buildDataxErrMsg(m);
            alarmService.alarmFlinkerJobError(monitorInfo, errMsg);
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
