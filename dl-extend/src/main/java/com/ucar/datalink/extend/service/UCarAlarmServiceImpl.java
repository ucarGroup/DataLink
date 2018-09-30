package com.ucar.datalink.extend.service;

import com.ucar.datalink.biz.service.AlarmService;
import com.ucar.datalink.biz.service.MailService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.HttpUtils;
import com.ucar.datalink.domain.mail.MailInfo;
import com.ucar.datalink.domain.mail.MailType;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskStatus;
import com.ucar.datalink.domain.task.TaskStatusMismatchLogInfo;
import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.domain.worker.WorkerInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2018/3/1.
 */
@Service
public class UCarAlarmServiceImpl implements AlarmService {

    private static final Logger logger = LoggerFactory.getLogger(UCarAlarmServiceImpl.class);

    private static final String MAIL_ADDRESS_SUFFIX = "@xxx.com";

    @Value("${extend.isSendSms}")
    private boolean isSendSms;

    @Value("${extend.smsUrl}")
    private String smsUrl;

    @Value("${extend.envName}")
    private String envName;

    @Autowired
    private MailService mailService;

    @Override
    public void alarmDelay(MonitorInfo monitorInfo, long delayTime) {
        String content = AlarmTemplate.buildDelayEmailContent(delayTime, MessageFormat.format("任务[{0}:ID = {1}]出现同步延迟", monitorInfo.getResourceName(), monitorInfo.getResourceId()), envName);
        sendEmail(monitorInfo, content, "DataLink任务延迟");
        if (isSendSms) {
            String smsContent = AlarmTemplate.buildDelaySMSContent(delayTime, MessageFormat.format("任务[{0}:ID = {1}]出现同步延迟", monitorInfo.getResourceName(), monitorInfo.getResourceId()), envName);
            sendSMS(monitorInfo, smsContent, smsUrl);
        }
    }

    @Override
    public void alarmError(MonitorInfo monitorInfo, String errorMsg) {
        String content = AlarmTemplate.buildErrorEmailContent(MessageFormat.format("任务[{0}:ID = {1}]出现异常", monitorInfo.getResourceName(), monitorInfo.getResourceId()), errorMsg, envName);
        sendEmail(monitorInfo, content, "DataLink任务异常");
        if (isSendSms) {
            String smsContent = AlarmTemplate.buildErrorSMSContent(MessageFormat.format("任务[{0}:ID = {1}]出现异常", monitorInfo.getResourceName(), monitorInfo.getResourceId()), envName);
            sendSMS(monitorInfo, smsContent, smsUrl);
        }
    }

    @Override
    public void alarmTaskStatus(MonitorInfo monitorInfo, TaskInfo taskInfo, TargetState targetState, TaskStatus.State actualState) {
        String content = AlarmTemplate.buildTaskStateEmailContent(String.format("任务%s:ID = %s,目标状态%s,实际状态%s", taskInfo.getTaskName(), taskInfo.getId(), targetState, actualState), "", envName);
        sendEmail(monitorInfo, content, "Datalink任务状态异常");
        if (isSendSms) {
            String smsContent = AlarmTemplate.buildTaskStateSMSContent(String.format("任务%s:ID = %s,目标状态%s,实际状态%s", taskInfo.getTaskName(), taskInfo.getId(), targetState, actualState), envName);
            sendSMS(monitorInfo, smsContent, smsUrl);
        }
    }

    @Override
    public void alarmWorkerJvmState(MonitorInfo monitorInfo, Long jvmUsedRate) {
        String content = AlarmTemplate.buildJvmUsedRateEmailContent(jvmUsedRate, MessageFormat.format("机器[ID = {0}]Jvm内存使用超过阈值{1}%", monitorInfo.getResourceId(), monitorInfo.getThreshold()), envName);
        sendEmail(monitorInfo, content, "DataLink机器内存报警");
        if (isSendSms) {
            String smsContent = AlarmTemplate.buildJvmUsedRateSmsContent(jvmUsedRate, MessageFormat.format("机器[ID = {0}]Jvm内存使用超过阈值{1}%", monitorInfo.getResourceId(), monitorInfo.getThreshold()), envName);
            sendSMS(monitorInfo, smsContent, smsUrl);
        }
    }

    @Override
    public void alarmWorkerStatus(MonitorInfo monitorInfo, WorkerInfo workerInfo) {
        String content = AlarmTemplate.buildWorkerStateEmailContent(MessageFormat.format("机器[ID = {0}]状态异常", workerInfo.getId()), envName);
        sendEmail(monitorInfo, content, "DataLink机器状态异常");
        if (isSendSms) {
            String smsContent = AlarmTemplate.buildWorkerStateSmsContent(MessageFormat.format("机器[ID = {0}]状态异常", workerInfo.getId()), envName);
            sendSMS(monitorInfo, smsContent, smsUrl);
        }
    }

    @Override
    public void alarmTaskStatusMismatch(MonitorInfo monitorInfo, TaskStatusMismatchLogInfo taskStatusMismatchLogInfo) {
        String content = AlarmTemplate.buildTaskStatusMismatchEmailContent(String.format("任务%s:ID = %s,本地状态:%s,远程状态:%s", monitorInfo.getResourceName(), monitorInfo.getResourceId(), taskStatusMismatchLogInfo.getLocalStatus(), taskStatusMismatchLogInfo.getRemoteStatus()), envName);
        sendEmail(monitorInfo, content, "DataLink任务状态不匹配");
        if (isSendSms) {
            String smsContent = AlarmTemplate.buildTaskStatusMismatchSmsContent(String.format("任务%s:ID = %s,本地状态:%s,远程状态:%s", monitorInfo.getResourceName(), monitorInfo.getResourceId(), taskStatusMismatchLogInfo.getLocalStatus(), taskStatusMismatchLogInfo.getRemoteStatus()), envName);
            sendSMS(monitorInfo, smsContent, smsUrl);
        }
    }

    public void sendEmail(String content, String subject) {
        try {
            MailInfo mailInfo = buildMailInfo(getMailAddress(), content, subject);
            mailService.sendMail(mailInfo);
        } catch (Exception e) {
            logger.error("send mail error", e);
        }
    }

    public void sendEmail(MonitorInfo monitorInfo, String content, String subject) {
        try {
            MailInfo mailInfo = buildMailInfo(getMailAddress(monitorInfo), content, subject);
            mailService.sendMail(mailInfo);
        } catch (Exception e) {
            logger.error("send mail error", e);
        }
    }

    private MailInfo buildMailInfo(List<String> recipient, String content, String subject) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.setSubject(subject);
        mailInfo.setMailContent(content);
        mailInfo.setRecipient(recipient);
        mailInfo.setMailType(MailType.Simple);
        return mailInfo;
    }

    private List<String> getMailAddress() {
        return getAlarmUsers().stream().map(i -> i.getUcarEmail() + MAIL_ADDRESS_SUFFIX).collect(Collectors.toList());
    }

    private List<String> getMailAddress(MonitorInfo monitorInfo) {
        return getAlarmUsers(monitorInfo).stream().map(i -> i.getUcarEmail() + MAIL_ADDRESS_SUFFIX).collect(Collectors.toList());
    }

    public void sendSMS(String content, String smsUrl) {
        try {
            List<String> phoneNumList = getPhoneNum();
            if (phoneNumList == null || phoneNumList.size() == 0) {
                return;
            }
            for (String phoneNum : phoneNumList) {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", phoneNum);
                params.put("content", URLEncoder.encode(content, "utf-8"));
                HttpUtils.doGet(smsUrl, params);
            }
        } catch (Exception e) {
            logger.error("sendSMS is error", e);
        }
    }

    public void sendSMS(MonitorInfo monitorInfo, String content, String smsUrl) {
        try {
            List<String> phoneNumList = getPhoneNum(monitorInfo);
            if (phoneNumList == null || phoneNumList.size() == 0) {
                return;
            }
            for (String phoneNum : phoneNumList) {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", phoneNum);
                params.put("content", URLEncoder.encode(content, "utf-8"));
                HttpUtils.doGet(smsUrl, params);
            }
        } catch (Exception e) {
            logger.error("sendSMS is error", e);
        }
    }

    private List<String> getPhoneNum() {
        return getAlarmUsers().stream().map(UserInfo::getPhone).collect(Collectors.toList());
    }

    private List<String> getPhoneNum(MonitorInfo monitorInfo) {
        return getAlarmUsers(monitorInfo).stream().map(UserInfo::getPhone).collect(Collectors.toList());
    }

    private List<UserInfo> getAlarmUsers(MonitorInfo monitorInfo) {
        Map<Long, UserInfo> userMaps = DataLinkFactory.getObject(UserService.class).getUserWidthLocalCache();
        Map<Long, UserInfo> alarmUserMaps = new HashMap<>();

        if (StringUtils.isNotBlank(monitorInfo.getReceivePeople())) {
            String[] array = StringUtils.split(monitorInfo.getReceivePeople(), ",");
            for (int i = 0; i < array.length; i++) {
                Long id = Long.valueOf(array[i]);
                alarmUserMaps.put(id, userMaps.get(id));
            }
        }

        userMaps.entrySet().stream().forEach(i -> {
            if (isSuper(i.getValue())) {
                alarmUserMaps.put(i.getValue().getId(), i.getValue());
            }
        });

        List<UserInfo> list = userMaps.values().stream().filter(i -> i.getIsAlarm()).collect(Collectors.toList());
        return list;
    }

    /**
     * //是否超级管理员
     */
    public Boolean isSuper(UserInfo userInfo) {
        for (RoleInfo roleInfo : userInfo.getRoleInfoList()){
            if (Objects.equals(RoleType.SUPER.toString(), roleInfo.getCode())) {
                return true;
            }
        }
        return false;
    }

    private List<UserInfo> getAlarmUsers() {
        Map<Long, UserInfo> userMaps = DataLinkFactory.getObject(UserService.class).getUserWidthLocalCache();
        Map<Long, UserInfo> alarmUserMaps = new HashMap<>();

        userMaps.entrySet().stream().forEach(i -> {
            if (isSuper(i.getValue())) {
                alarmUserMaps.put(i.getValue().getId(), i.getValue());
            }
        });

        return userMaps.values().stream().filter(i -> i.getIsAlarm()).collect(Collectors.toList());
    }

    static class AlarmTemplate {
        private static final String EMAIL_TEMPLATE = "env:{0}\r\n" + "type:{1}\r\n" + "msg:{2}\r\n";
        private static final String SMS_TEMPLATE = "env:{0}," + "type:{1}," + "msg:{2}";

        private static final String ERROR_EMAIL_TEMPLATE = EMAIL_TEMPLATE + "stack:{3}";
        private static final String ERROR_SMS_TEMPLATE = SMS_TEMPLATE;

        private static final String DELAY_EMAIL_TEMPLATE = EMAIL_TEMPLATE + "delaytime:{3}毫秒";
        private static final String DELAY_SMS_TEMPLATE = SMS_TEMPLATE + "delaytime:{3}毫秒";

        private static final String JVM_OVER_THRESHOLD_EMAIL_TEMPLATE = EMAIL_TEMPLATE + "jvmOverThreshold:{3}%";
        private static final String JVM_OVER_THRESHOLD_SMS_TEMPLATE = SMS_TEMPLATE + "jvmOverThreshold:{3}%";

        public static String buildErrorEmailContent(String msg, String errorMsg, String envName) {
            return MessageFormat.format(ERROR_EMAIL_TEMPLATE, envName, "exception", msg, errorMsg);
        }

        public static String buildErrorSMSContent(String msg, String envName) {
            return MessageFormat.format(ERROR_SMS_TEMPLATE, envName, "exception", msg);
        }

        public static String buildDelayEmailContent(Long delaytime, String msg, String envName) {
            return MessageFormat.format(DELAY_EMAIL_TEMPLATE, envName, "delay", msg, delaytime);
        }

        public static String buildDelaySMSContent(Long delaytime, String msg, String envName) {
            return MessageFormat.format(DELAY_SMS_TEMPLATE, envName, "delay", msg, delaytime);
        }

        public static String buildTaskStateEmailContent(String msg, String errorMsg, String envName) {
            return MessageFormat.format(EMAIL_TEMPLATE, envName, "taskStateMismatch", msg);
        }

        public static String buildTaskStateSMSContent(String msg, String envName) {
            return MessageFormat.format(SMS_TEMPLATE, envName, "taskStateMismatch", msg);
        }

        public static String buildJvmUsedRateEmailContent(Long jvmUsedRate, String msg, String envName) {
            return MessageFormat.format(JVM_OVER_THRESHOLD_EMAIL_TEMPLATE, envName, "jvmOverThreshold", msg, jvmUsedRate);
        }

        public static String buildJvmUsedRateSmsContent(Long jvmUsedRate, String msg, String envName) {
            return MessageFormat.format(JVM_OVER_THRESHOLD_SMS_TEMPLATE, envName, "jvmOverThreshold", msg, jvmUsedRate);
        }

        public static String buildWorkerStateEmailContent(String msg, String envName) {
            return MessageFormat.format(EMAIL_TEMPLATE, envName, "workerStateAbnormal", msg);
        }

        public static String buildWorkerStateSmsContent(String msg, String envName) {
            return MessageFormat.format(SMS_TEMPLATE, envName, "workerStateAbnormal", msg);
        }

        public static String buildTaskStatusMismatchEmailContent(String msg, String envName) {
            return MessageFormat.format(EMAIL_TEMPLATE, envName, "TaskStatusMismatchLog", msg);
        }

        public static String buildTaskStatusMismatchSmsContent(String msg, String envName) {
            return MessageFormat.format(SMS_TEMPLATE, envName, "TaskStatusMismatchLog", msg);
        }
    }

}
