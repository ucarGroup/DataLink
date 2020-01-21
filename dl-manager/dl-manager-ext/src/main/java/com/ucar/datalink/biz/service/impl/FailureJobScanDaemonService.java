package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.service.JobDaemonService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MailService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.utils.IPUtils;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.mail.MailInfo;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.util.ConfigReadUtil;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by user on 2018/3/22.
 */
//@Service("failureScan")
public class FailureJobScanDaemonService extends JobDaemonService {

    private static final Logger logger = LoggerFactory.getLogger(FailureJobScanDaemonService.class);


    private static final long TIME_PERIOD_BY_MINUTE = 5 * 60 * 1000;

    /**
     * 从第一次扫描到第二次扫之间可能有很小的间隙，这个间隙如果有失败job就会漏掉，所以第二次扫表的时候
     * 再往前衍一点点，10秒
     */
    private static final long TIME_SCAN_SUPPLEMENT_BY_MINUTE = 10 * 1000;

    private static final long DELAY_START_TIME = 1 * 60 * 1000;

    /**
     * 集团邮箱后缀
     */
    private static final String UCAR_MAIL_SUFFIX = "@ucarinc.com";

    /**
     * 邮件标题
     */
    private static final String EMAIL_TITLE = "Job execute failure";

    /**
     * 格式化时间
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";

    private Timer timer;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private JobService service;

    private static AtomicBoolean isIntial = new AtomicBoolean(false);


    public FailureJobScanDaemonService() {

    }




    @Override
    public void initialized() {

    }

    @Override
    public void destroyed() {

    }



    public void monitorFailureJob() {
        List<JobExecutionInfo> list = getFailureJobExecutionInfo();
        if (list == null || list.size() == 0) {
            return;
        }
        List<String> recipient = getUserEamil();
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
                "<td>timing task?</td>" +
                "<td>start time</td>" +
                "<td>end time</td>" +
                "<td>exception</td>" +
                "</tr>");
        for (JobExecutionInfo info : list) {
            content.append("<tr>");
            content.append("<td>").append(info.getId()).append("</td>");
            content.append("<td>").append(info.getJob_id()).append("</td>");
            JobConfigInfo config = getJobConfig(info.getJob_id());
            content.append("<td>").append(config.getJob_name()).append("</td>");
            content.append("<td>").append(info.getWorker_address() == null ? " " : info.getWorker_address()).append("</td>");
            content.append("<td>").append(config.isTiming_yn() ? "yes" : "no").append("</td>");
            content.append("<td>").append(formatString(info.getStart_time())).append("</td>");
            content.append("<td>").append(formatString(info.getEnd_time())).append("</td>");
            content.append("<td>").append(info.getException() == null ? "" : info.getException()).append("</td>");
            content.append("</tr>");
        }
        content.append("</table>");
        MailInfo info = new MailInfo();
        String emailTitle = EMAIL_TITLE;
        if (env != null) {
            emailTitle = emailTitle + "_" + env +"_"+ IPUtils.getHostIp();
        }
        info.setSubject(emailTitle);
        info.setMailContent(content.toString());
        info.setRecipient(recipient);
        logger.info("send mail -> "+content.toString());
        mailService.sendMail(info);
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

    private List<JobExecutionInfo> getFailureJobExecutionInfo() {
        Date current = new Date();
        long currentTime = current.getTime();
        long beforeTime = currentTime - TIME_PERIOD_BY_MINUTE;
        Timestamp startTime = new Timestamp(beforeTime);
        Timestamp endTime = new Timestamp(currentTime);
        //JobService service = DataLinkFactory.getObject(JobService.class);
        return service.getAllFailureJob(startTime, endTime);
    }

    private JobConfigInfo getJobConfig(long job_id) {
        //JobService service = DataLinkFactory.getObject(JobService.class);
        JobConfigInfo info = service.getJobConfigById(job_id);
        if (info == null) {
            return new JobConfigInfo();
        }
        return info;
    }

    private List<String> getUserEamil() {
        //UserService userService = DataLinkFactory.getObject(UserService.class);
        List<UserInfo> users = userService.getUserInfoByReceiveMail();
        List<String> recipient = new ArrayList<>();
        if (users != null && users.size() > 0) {
            for (UserInfo u : users) {
                if (StringUtils.isNotBlank(u.getUcarEmail())) {
                    String email = u.getUcarEmail() + UCAR_MAIL_SUFFIX;
                    recipient.add(email);
                } else {
                    //邮箱都为空就不
                }
            }
        }
        return recipient;
    }


}
