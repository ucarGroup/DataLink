package com.ucar.datalink.extend.service;


import com.alibaba.fastjson.JSON;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.alarm.AlarmMessage;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2018/3/1.
 */
@Service
public class UCarAlarmServiceImpl implements AlarmService{

    private static final Logger logger = LoggerFactory.getLogger(UCarAlarmServiceImpl.class);

    private static final String MAIL_ADDRESS_SUFFIX = "@ucarinc.com";

    private static final List<String> exceptionFilterList = new ArrayList<>();

    static {
        exceptionFilterList.add("com.ucar.datalink.writer.sddl.handler.RecordLoader$IllegalTableException");
    }

    @Value("${extend.isSendSms}")
    private boolean isSendSms;

    @Value("${extend.envName}")
    private String envName;

    @Qualifier("mailService")
    @Autowired
    private UcarAlarmPlugin mailService;

    @Qualifier("smsService")
    @Autowired
    private UcarAlarmPlugin smsService;


    public static Map<String, UcarAlarmPlugin> getAlarmPlugin() {
        return DataLinkFactory.getBeansOfType(UcarAlarmPlugin.class);
    }

    public void alarm(String content,String subject,boolean isSendMobile) {
        mailService.doSend(getEmailAlarmMessage("env:{" + envName + "}\r\n" + content,subject));
        if (isSendSms && isSendMobile) {
            smsService.doSend(getMobileAlarmMessage("env:{" + envName + "}\r\n" + content,subject));
        }
    }

    /**
     * 发送延迟报警
     * @param monitorInfo
     * @param delayTime
     */
    @Override
    public void alarmDelay(MonitorInfo monitorInfo, long delayTime) {

        getAlarmPlugin().entrySet().stream().forEach(i->{
            try {
                UcarAlarmPlugin alarmPlugin = i.getValue();
                AlarmMessage alarmMessage = null;
                boolean isSend = false;
                switch (alarmPlugin.getPluginName()) {
                    case "email" :
                        alarmMessage = getEmailDelayMessage(monitorInfo,delayTime);
                        isSend = true;
                        break;
                    case "SMS" :
                        alarmMessage = getSMSDelayMessage(monitorInfo,delayTime);
                        isSend = monitorInfo.isSMS();
                        break;
                    case "phone" :
                        alarmMessage = getPhoneDelayMessage(monitorInfo,delayTime);
                        isSend = monitorInfo.isPhone();
                        break;
                    case "dingD" :
                        alarmMessage = getEmailDelayMessage(monitorInfo,delayTime);
                        isSend = monitorInfo.isDingD();
                        break;
                    case "threshold" :
                        alarmMessage = getPhoneDelayMessage(monitorInfo,delayTime);
                        isSend = false;
                        break;
                }
                if(isSend ){
                    i.getValue().doSend(alarmMessage);
                }
            }catch (Exception e){
                logger.info("发送警报失败:",e);
            }
        });

    }


    /**
     * 发送异常报警
     * @param monitorInfo
     * @param errorMsg
     */
    @Override
    public void alarmError(MonitorInfo monitorInfo, String errorMsg) {

        getAlarmPlugin().entrySet().stream().forEach(i->{
            try {
                UcarAlarmPlugin alarmPlugin = i.getValue();
                AlarmMessage alarmMessage = null;
                logger.info("error , monitorInfo="+ JSON.toJSONString(monitorInfo));
                boolean isSend = false;
                switch (alarmPlugin.getPluginName()) {
                    case "email" :
                        alarmMessage = getEmailErrorMessage(monitorInfo,errorMsg);
                        isSend = true;
                        break;
                    case "SMS" :
                        alarmMessage = getSMSErrorMessage(monitorInfo);
                        isSend = monitorInfo.isSMS() && !containsSpecialException(errorMsg);
                        break;
                    case "phone" :
                        alarmMessage = getPhoneErrorMessage(monitorInfo);
                        isSend = monitorInfo.isPhone() && !containsSpecialException(errorMsg);
                        break;
                    case "dingD" :
                        alarmMessage = getEmailErrorMessage(monitorInfo,errorMsg);
                        isSend = monitorInfo.isDingD();
                        break;
                    case "threshold" :
                        alarmMessage = getPhoneErrorMessage(monitorInfo);
                        isSend = true && !containsSpecialException(errorMsg);
                        break;
                }
                if(isSend ){
                    i.getValue().doSend(alarmMessage);
                }
            }catch (Exception e){
                logger.info("发送警报失败:",e);
            }

        });
    }

    /**
     * 发送任务状态报警
     * @param monitorInfo
     * @param taskInfo
     * @param targetState
     * @param actualState
     */
    @Override
    public void alarmTaskStatus(MonitorInfo monitorInfo, TaskInfo taskInfo, TargetState targetState, TaskStatus.State actualState) {

        getAlarmPlugin().entrySet().stream().forEach(i->{
            try {
                UcarAlarmPlugin alarmPlugin = i.getValue();
                AlarmMessage alarmMessage = null;
                boolean isSend = false;
                switch (alarmPlugin.getPluginName()) {
                    case "email" :
                        alarmMessage = getEmailTaskStatusMessage(monitorInfo,taskInfo,targetState,actualState);
                        isSend = true;
                        break;
                    case "SMS" :
                        alarmMessage = getSMSTaskStatusMessage(monitorInfo,taskInfo,targetState,actualState);
                        isSend = monitorInfo.isSMS();
                        break;
                    case "phone" :
                        alarmMessage = getPhoneTaskStatusMessage(monitorInfo);
                        isSend = monitorInfo.isPhone();
                        break;
                    case "dingD" :
                        alarmMessage = getEmailTaskStatusMessage(monitorInfo,taskInfo,targetState,actualState);
                        isSend = monitorInfo.isDingD();
                        break;
                    case "threshold" :
                        alarmMessage = getPhoneTaskStatusMessage(monitorInfo);
                        isSend = false;
                        break;
                }
                if(isSend ){
                    i.getValue().doSend(alarmMessage);
                }
            }catch (Exception e){
                logger.info("发送警报失败:",e);
            }

        });
    }




    /**
     *任务状态不匹配报警
     * @param monitorInfo
     * @param taskStatusMismatchLogInfo
     */
    @Override
    public void alarmTaskStatusMismatch(MonitorInfo monitorInfo, TaskStatusMismatchLogInfo taskStatusMismatchLogInfo) {

        getAlarmPlugin().entrySet().stream().forEach(i->{
            try {
                UcarAlarmPlugin alarmPlugin = i.getValue();
                AlarmMessage alarmMessage = null;
                boolean isSend = false;
                switch (alarmPlugin.getPluginName()) {
                    case "email" :
                        alarmMessage = getEmailTaskStatusMismatchMessage(monitorInfo,taskStatusMismatchLogInfo);
                        isSend = true;
                        break;
                    case "SMS" :
                        alarmMessage = getSMSTaskStatusMismatchMessage(monitorInfo,taskStatusMismatchLogInfo);
                        isSend = monitorInfo.isSMS();
                        break;
                    case "phone" :
                        alarmMessage = getPhoneTaskStatusMismatchMessage(monitorInfo);
                        isSend = monitorInfo.isPhone();
                        break;
                    case "dingD" :
                        alarmMessage = getEmailTaskStatusMismatchMessage(monitorInfo,taskStatusMismatchLogInfo);
                        isSend = monitorInfo.isDingD();
                        break;
                    case "threshold" :
                        alarmMessage = getPhoneTaskStatusMessage(monitorInfo);
                        isSend = false;
                        break;
                }
                if(isSend ){
                    i.getValue().doSend(alarmMessage);
                }
            }catch (Exception e) {
                logger.info("发送警报失败:",e);
            }

        });
    }


    /**
     * 任务同步状态报警
     * @param monitorInfo
     * @param busyTime
     */
    @Override
    public void alarmTaskSyncStatus(MonitorInfo monitorInfo, Long busyTime) {

        getAlarmPlugin().entrySet().stream().forEach(i->{
            try {
                UcarAlarmPlugin alarmPlugin = i.getValue();
                AlarmMessage alarmMessage = null;
                boolean isSend = false;
                switch (alarmPlugin.getPluginName()) {
                    case "email" :
                        alarmMessage = getEmailTaskSyncStatusMessage(monitorInfo,busyTime);
                        isSend = true;
                        break;
                    case "SMS" :
                        alarmMessage = getSMSTaskSyncStatusMessage(monitorInfo,busyTime);
                        isSend = monitorInfo.isSMS();
                        break;
                    case "phone" :
                        alarmMessage = getPhoneTaskSyncStatusMessage(monitorInfo,busyTime);
                        isSend = monitorInfo.isPhone();
                        break;
                    case "dingD" :
                        alarmMessage = getEmailTaskSyncStatusMessage(monitorInfo,busyTime);
                        isSend = monitorInfo.isDingD();
                        break;
                    case "threshold" :
                        alarmMessage = getPhoneTaskSyncStatusMessage(monitorInfo,busyTime);
                        isSend = false;
                        break;
                }
                if(isSend ){
                    i.getValue().doSend(alarmMessage);
                }
            }catch (Exception e){
                logger.info("发送警报失败:",e);
            }
        });
    }



    @Override
    public void alarmDataxError(MonitorInfo monitorInfo, String errorMsg) {

        getAlarmPlugin().entrySet().stream().forEach(i->{
            try {
                UcarAlarmPlugin alarmPlugin = i.getValue();
                AlarmMessage alarmMessage = null;
                boolean isSend = false;
                switch (alarmPlugin.getPluginName()) {
                    case "email" :
                        alarmMessage = getEmailDataxErrorMessage(monitorInfo,errorMsg);
                        isSend = true;
                        break;
                    case "SMS" :
                        alarmMessage = getSMSDataxErrorMessage(monitorInfo,errorMsg);
                        isSend = monitorInfo.isSMS();
                        break;
                    case "phone" :
                        isSend = false;
                        break;
                    case "dingD" :
                        alarmMessage = getEmailDataxErrorMessage(monitorInfo,errorMsg);
                        isSend = monitorInfo.isDingD();
                        break;
                    case "threshold" :
                        isSend = false;
                        break;
                }
                if(isSend ){
                    i.getValue().doSend(alarmMessage);
                }
            }catch (Exception e){
                logger.info("发送警报失败:",e);
            }

        });
    }




    @Override
    public void alarmWorkerJvmState(MonitorInfo monitorInfo, Long jvmUsedRate) {

        getAlarmPlugin().entrySet().stream().forEach(i->{
            try {
                UcarAlarmPlugin alarmPlugin = i.getValue();
                AlarmMessage alarmMessage = null;
                boolean isSend = false;
                switch (alarmPlugin.getPluginName()) {
                    case "email" :
                        alarmMessage = getEmailWorkerJvmStateMessage(monitorInfo,jvmUsedRate);
                        isSend = true;
                        break;
                    case "SMS" :
                        alarmMessage = getSMSWorkerJvmStateMessage(monitorInfo,jvmUsedRate);
                        isSend = true;
                        break;
                    case "phone" :
                        isSend = false;
                        break;
                    case "dingD" :
                        alarmMessage = getEmailWorkerJvmStateMessage(monitorInfo,jvmUsedRate);
                        isSend = monitorInfo.isDingD();
                        break;
                    case "threshold" :
                        isSend = false;
                        break;
                }
                if(isSend ){
                    i.getValue().doSend(alarmMessage);
                }
            }catch (Exception e){
                logger.info("发送警报失败:",e);
            }

        });
    }



    @Override
    public void alarmWorkerStatus(MonitorInfo monitorInfo, WorkerInfo workerInfo) {

        getAlarmPlugin().entrySet().stream().forEach(i->{
            try{
                UcarAlarmPlugin alarmPlugin = i.getValue();
                AlarmMessage alarmMessage = null;
                boolean isSend = false;
                switch (alarmPlugin.getPluginName()) {
                    case "email" :
                        alarmMessage = getEmailWorkerStatusMessage(monitorInfo,workerInfo);
                        isSend = true;
                        break;
                    case "SMS" :
                        alarmMessage = getSMSWorkerStatusMessage(monitorInfo,workerInfo);
                        isSend = true;
                        break;
                    case "phone" :
                        isSend = false;
                        break;
                    case "dingD" :
                        alarmMessage = getEmailWorkerStatusMessage(monitorInfo,workerInfo);
                        isSend = monitorInfo.isDingD();
                        break;
                    case "threshold" :
                        isSend = false;
                        break;
                }
                if(isSend ){
                    i.getValue().doSend(alarmMessage);
                }
            }catch (Exception e) {
                logger.info("发送警报失败:",e);
            }

        });
    }


    private AlarmMessage getSMSWorkerStatusMessage(MonitorInfo monitorInfo, WorkerInfo workerInfo) {
        String smsContent = AlarmTemplate.buildWorkerStateSmsContent(MessageFormat.format("机器[ID = {0}]状态异常", workerInfo.getId()), envName);
        String subject = "DataLink机器状态异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(smsContent);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getEmailWorkerStatusMessage(MonitorInfo monitorInfo, WorkerInfo workerInfo) {
        String content = AlarmTemplate.buildWorkerStateEmailContent(MessageFormat.format("机器[ID = {0}]状态异常", workerInfo.getId()), envName);
        String subject = "DataLink机器状态异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getMailAddress(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }


    private AlarmMessage getSMSWorkerJvmStateMessage(MonitorInfo monitorInfo, Long jvmUsedRate) {
        String content = AlarmTemplate.buildJvmUsedRateSmsContent(jvmUsedRate, MessageFormat.format("机器[ID = {0}]Jvm内存使用超过阈值{1}%", monitorInfo.getResourceId(), monitorInfo.getThreshold()), envName);
        String subject = "DataLink机器内存报警 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getEmailWorkerJvmStateMessage(MonitorInfo monitorInfo, Long jvmUsedRate) {
        String content = AlarmTemplate.buildJvmUsedRateEmailContent(jvmUsedRate, MessageFormat.format("机器[ID = {0}]Jvm内存使用超过阈值{1}%", monitorInfo.getResourceId(), monitorInfo.getThreshold()), envName);
        String subject = "DataLink机器内存报警 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getMailAddress(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getSMSDataxErrorMessage(MonitorInfo monitorInfo, String errorMsg) {
        String smsContent = AlarmTemplate.buildDataxErrorSMSContent(
                MessageFormat.format("Datax[{0}:job_id = {1}]同步出错", monitorInfo.getResourceName(), monitorInfo.getResourceId()), envName);
        String subject = "Datax任务异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(smsContent);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getEmailDataxErrorMessage(MonitorInfo monitorInfo, String errorMsg) {
        String subject = "Datax任务异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(errorMsg);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getMailAddress(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getPhoneTaskSyncStatusMessage(MonitorInfo monitorInfo, Long busyTime) {
        String content = MessageFormat.format("ID为{0}的任务{1}同步状态Busy持续时间{2}毫秒",monitorInfo.getResourceId(),monitorInfo.getResourceName().replaceAll("_",""), busyTime);
        String subject = "DataLink任务同步状态为Busy [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getSMSTaskSyncStatusMessage(MonitorInfo monitorInfo, Long busyTime) {
        String smsContent = AlarmTemplate.buildTaskSyncStatusSmsContent(String.format("任务%s:ID = %s,同步状态Busy持续时间:%s毫秒", monitorInfo.getResourceName(), monitorInfo.getResourceId(), busyTime), envName);
        String subject = "DataLink任务同步状态为Busy [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(smsContent);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getEmailTaskSyncStatusMessage(MonitorInfo monitorInfo, Long busyTime) {
        String content = AlarmTemplate.buildTaskSyncStatusEmailContent(String.format("任务%s:ID = %s,同步状态Busy持续时间:%s毫秒", monitorInfo.getResourceName(), monitorInfo.getResourceId(), busyTime), envName);
        String subject = "DataLink任务同步状态为Busy [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getMailAddress(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getPhoneTaskStatusMessage(MonitorInfo monitorInfo) {
        String content = MessageFormat.format("ID为{0}的任务{1}状态异常",monitorInfo.getResourceId(),monitorInfo.getResourceName().replaceAll("_",""));
        String subject = "Datalink任务状态异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getSMSTaskStatusMessage(MonitorInfo monitorInfo, TaskInfo taskInfo, TargetState targetState, TaskStatus.State actualState) {
        String smsContent = AlarmTemplate.buildTaskStateSMSContent(String.format("任务%s:ID = %s,目标状态%s,实际状态%s", taskInfo.getTaskName(), taskInfo.getId(), targetState, actualState), envName);
        String subject = "Datalink任务状态异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(smsContent);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getEmailTaskStatusMessage(MonitorInfo monitorInfo, TaskInfo taskInfo, TargetState targetState, TaskStatus.State actualState) {
        String content = AlarmTemplate.buildTaskStateEmailContent(String.format("任务%s:ID = %s,目标状态%s,实际状态%s", taskInfo.getTaskName(), taskInfo.getId(), targetState, actualState), "", envName);
        String subject = "Datalink任务状态异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getMailAddress(monitorInfo));
        return alarmMessage;
    }

    private AlarmMessage getSMSTaskStatusMismatchMessage(MonitorInfo monitorInfo, TaskStatusMismatchLogInfo taskStatusMismatchLogInfo) {
        String smsContent = AlarmTemplate.buildTaskStatusMismatchSmsContent(String.format("任务%s:ID = %s,本地状态:%s,远程状态:%s", monitorInfo.getResourceName(), monitorInfo.getResourceId(), taskStatusMismatchLogInfo.getLocalStatus(), taskStatusMismatchLogInfo.getRemoteStatus()), envName);
        String subject = "DataLink任务状态不匹配 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(smsContent);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getPhoneTaskStatusMismatchMessage(MonitorInfo monitorInfo) {
        String content = MessageFormat.format("ID为{0}的任务{1}状态不匹配",monitorInfo.getResourceId(),monitorInfo.getResourceName().replaceAll("_",""));
        String subject = "DataLink任务状态不匹配 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getEmailTaskStatusMismatchMessage(MonitorInfo monitorInfo, TaskStatusMismatchLogInfo taskStatusMismatchLogInfo) {
        String content = AlarmTemplate.buildTaskStatusMismatchEmailContent(String.format("任务%s:ID = %s,本地状态:%s,远程状态:%s", monitorInfo.getResourceName(), monitorInfo.getResourceId(), taskStatusMismatchLogInfo.getLocalStatus(), taskStatusMismatchLogInfo.getRemoteStatus()), envName);
        String subject = "DataLink任务状态不匹配 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getMailAddress(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getPhoneDelayMessage(MonitorInfo monitorInfo, long delayTime) {
        String content = MessageFormat.format("ID为{0}的任务{1}同步出现延迟",monitorInfo.getResourceId(),monitorInfo.getResourceName().replaceAll("_",""));
        String subject = "DataLink任务延迟 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getSMSDelayMessage(MonitorInfo monitorInfo,long delayTime) {
        String content = AlarmTemplate.buildDelaySMSContent(delayTime, MessageFormat.format("任务[{0}:ID = {1}]出现同步延迟", monitorInfo.getResourceName(), monitorInfo.getResourceId()), envName);
        String subject = "DataLink任务延迟 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        return alarmMessage;
    }

    private AlarmMessage getEmailDelayMessage(MonitorInfo monitorInfo, long delayTime) {
        String content = AlarmTemplate.buildDelayEmailContent(delayTime, MessageFormat.format("任务[{0}:ID = {1}]出现同步延迟", monitorInfo.getResourceName(), monitorInfo.getResourceId()), envName);
        String subject = "DataLink任务延迟 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getMailAddress(monitorInfo));
        return alarmMessage;
    }

    private AlarmMessage getPhoneErrorMessage(MonitorInfo monitorInfo) {
        String content = MessageFormat.format("ID为{0}的任务{1}同步出现异常",monitorInfo.getResourceId(),monitorInfo.getResourceName().replaceAll("_",""));
        String subject = "DataLink任务异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        alarmMessage.setMonitorInfo(monitorInfo);
        return alarmMessage;
    }

    private AlarmMessage getSMSErrorMessage(MonitorInfo monitorInfo) {
        String content = AlarmTemplate.buildErrorSMSContent(MessageFormat.format("任务[{0}:ID = {1}]出现异常", monitorInfo.getResourceName(), monitorInfo.getResourceId()), envName);
        String subject = "DataLink任务异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum(monitorInfo));
        return alarmMessage;
    }

    private AlarmMessage getEmailErrorMessage(MonitorInfo monitorInfo, String errorMsg) {
        String content = AlarmTemplate.buildErrorEmailContent(MessageFormat.format("任务[{0}:ID = {1}]出现异常", monitorInfo.getResourceName(), monitorInfo.getResourceId()), errorMsg, envName);
        String subject = "DataLink任务异常 [ "+envName+" ]";
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getMailAddress(monitorInfo));
        return alarmMessage;
    }

    private AlarmMessage getEmailAlarmMessage(String content, String subject) {
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getMailAddress());
        return alarmMessage;
    }

    private AlarmMessage getMobileAlarmMessage(String content, String subject) {
        AlarmMessage alarmMessage = new AlarmMessage();
        alarmMessage.setContent(content);
        alarmMessage.setSubject(subject);
        alarmMessage.setRecipient(getPhoneNum());
        return alarmMessage;
    }

    private List<String> getMailAddress() {
        return getAlarmUsers().stream().map(i -> i.getUcarEmail() + MAIL_ADDRESS_SUFFIX).collect(Collectors.toList());
    }

    private List<String> getMailAddress(MonitorInfo monitorInfo) {
        return getAlarmUsers(monitorInfo).stream().map(i -> i.getUcarEmail() + MAIL_ADDRESS_SUFFIX).collect(Collectors.toList());
    }

    private boolean containsSpecialException(String content) {
        for (String exceptionStr : exceptionFilterList) {
            if (content.contains(exceptionStr)) {
                return true;
            }
        }
        return false;
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
        private static final String DATAX_TEMPLATE = "{0},env:{1}";

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

        public static String buildDataxErrorSMSContent(String msg, String envName) {
            return MessageFormat.format(DATAX_TEMPLATE, msg, envName);
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

        public static String buildTaskSyncStatusEmailContent(String msg, String envName) {
            return MessageFormat.format(EMAIL_TEMPLATE, envName, "TaskSyncStatus-Busy", msg);
        }

        public static String buildTaskSyncStatusSmsContent(String msg, String envName) {
            return MessageFormat.format(SMS_TEMPLATE, envName, "TaskSyncStatus-Busy", msg);
        }
    }

}
