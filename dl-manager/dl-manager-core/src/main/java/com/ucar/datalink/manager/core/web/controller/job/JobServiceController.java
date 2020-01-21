package com.ucar.datalink.manager.core.web.controller.job;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.job.DataxJobConfigConstant;
import com.ucar.datalink.biz.module.JobRestResponse;
import com.ucar.datalink.biz.module.RunningData;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.task.TaskDelayTimeInfo;
import com.ucar.datalink.domain.job.*;
import com.ucar.datalink.domain.mail.MailInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskType;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.except.DynamicParamException;
import com.ucar.datalink.manager.core.monitor.impl.TaskExceptionMonitor;
import com.ucar.datalink.manager.core.server.ManagerConfig;

import com.ucar.datalink.util.ConfigReadUtil;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.util.DataxUtil;
import com.ucar.datalink.util.SyncModifyUtil;
import com.ucar.datalink.util.SyncUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by user on 2017/9/21.
 */

@Controller
@RequestMapping(value = "/jobService/")
@LoginIgnore
public class JobServiceController {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceController.class);

    private static final String HTTP_HEADER_USERNAME = "JOB_SERVICE_USERNAME";

    private static final String HTTP_HEADER_PASSWORD = "JOB_SERVICE_PASSWORD";

    /**
     * 默认的延迟时间为1分钟
     */
    private static final long DEFAULT_DELAY_TIMIE = 60 * 1000;

    /**
     * 如果调用方传入的job运行id是这个值，表示这个job正在运行，忽略后续的逻辑直接返回成功
     */
    private static final long JOB_STILL_RUNNING_NUM_MARK = Long.MIN_VALUE;

    /**
     * 默认一次查询的数量
     */
    private static final long DEFAULT_QUERY_COUNT_ONCE = 100;

    /**
     * 未知错误，统一规为600
     */
    private static final int SC_UNKNOWN_ERROR = 600;

    /**
     * 认证错误，传入的id经过计算和传入的ID_SIGNAL不一样
     */
    private static final int SC_UNAUTHORIZED = 601;

    /**
     * 传入的config_id格式错误，id可能为空，可能不是整数，或超过了Long能表示的范围`
     */
    private static final int SC_CONFIG_ID_FORMAT_ERROR = 602;

    /**
     * id指定的job不是一个定时job
     */
    private static final int SC_NOT_TIMING_JOB = 603;

    /**
     * 前一次运行的job还没执行完
     */
    private static final int SC_JOB_IS_RUNNING = 604;

    /**
     * 检查源端表字段是否修改出错，这个错误不影响后续执行
     * 如果在json配置文件中设置了 adaptiveFieldModify:true，每次启动的时候就会检查字段是否更改
     * 如果设置了adaptiveFieldModify:false 就不会检查，也就不可能触发605这个错误
     */
    private static final int SC_JOB_CHECK_MODIFY_FAILURE = 605;

    /**
     * 传入的动态参数解析错误
     * 动态参数包括DATAX_CURRENT_DATE,DATAX_LAST_EXECUTE_TIME等
     */
    private static final int SC_DYNAMIC_PARAM_PARSE_ERROR = 606;

    /**
     * 传入的运行id,execute_id格式错误，id可能为空，可能不是整数，或超过了Long能表示的范围`
     */
    private static final int SC_EXECUTE_ID_FORMAT_ERROR = 607;

    /**
     * 停止job失败
     */
    private static final int SC_STOP_JOB_FAILURE = 608;

    /**
     * 找不到datax的ip无法停止
     */
    private static final int SC_STOP_JOB_FAILURE_NOTFOUND_DATAX_IP = 609;

    /**
     * 找不到datax的ip无法停止
     */
    private static final int SC_FORCESTOP_JOB_FAILURE_NOTFOUND_DATAX_IP = 610;

    /**
     * 强制停止datax超时
     */
    private static final int SC_FORCESTOP_JOB_FAILURE_TIMEOUT = 611;

    /**
     * 强制停止datax失败，等待结果时被中断
     */
    private static final int SC_FORCESTOP_JOB_FAILURE_BY_INTERRUPTED = 612;

    /**
     * 强制停止datax失败
     */
    private static final int SC_FORCESTOP_JOB_FAILURE = 613;

    /**
     * 查询单个job的运行结果，此job运行失败
     */
    private static final int SC_STATE_JOB_RESULT_FAILURE = 614;

    /**
     * 如果job关联的数据源正在切机房中,禁止job启动
     */
    private static final int SC_SWITCH_LAB_ING = 615;



    private static final int JOB_DUP_TIMES_TO_SENDMAIL = ConfigReadUtil.getInt("datax.rest.dup.times",10);    //ManagerConfig.current().getJobDupTimes();


    private static AtomicInteger jobDupTimes = new AtomicInteger(1);


    @Autowired
    JobService jobService;

    @Autowired
    @Qualifier("dynamic")
    JobControlService jobControlService;

    @Autowired
    private TaskExceptionMonitor taskExceptionMonitor;

    @Autowired
    TaskDelayTimeService taskDelayTimeService;

    @Autowired
    TaskConfigService taskService;

    @Autowired
    @Qualifier("taskPositionServiceZkImpl")
    private TaskPositionService taskPositionService;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private MailService mailService;

    @Autowired
    DoubleCenterDataxService doubleCenterDataxService;

    @RequestMapping(value = "/start")
    @ResponseBody
    public Object doStart(@RequestParam("CONFIG_ID") String id, @RequestParam("JOB_ID_SIGNAL") String md5,
                          @RequestParam(value = "PARAMETER", required = false) String jsonStr,
                          @RequestParam(value = "JVM_MAX", required = false) String jvmMax,
                          @RequestParam(value = "JVM_MIN", required = false) String jvmMin,
                          HttpServletRequest request, HttpServletResponse response) {
        try {
            id = parseId(id);
            if (!authWithJobConfigId(id, md5)) {
                response.setStatus(SC_UNAUTHORIZED);
                return assembleMsgJson("auth failure,please check username amd password, or job config id not exist");
            }
            if (!jobConfigIdCheck(id)) {
                response.setStatus(SC_CONFIG_ID_FORMAT_ERROR);
                return assembleMsgJson("job config id illegal");
            }
            long job_id = Long.parseLong(id);
            JobConfigInfo info = jobService.getJobConfigById(job_id);
            if (!isTimingJob(info)) {
                sendMailByNotTimingJob(info.getJob_name());
                response.setStatus(SC_NOT_TIMING_JOB);
                return assembleMsgJson("not timing job!");
            }
            String job_name = info.getJob_name();
            if (DataxUtil.isJobRunning(job_name)) {
                //如果当前这个job还没执行完，就给调用方返回一个 Long.MIN_VALUE，做一个特殊标志
                //待调用方下次再调用 doStart()
                logger.info("[JobServiceController]current job not end " + job_name);
                if( (jobDupTimes.get() % JOB_DUP_TIMES_TO_SENDMAIL) == 0 ) {
                    sendMailByJobDupRun(job_name);
                    jobDupTimes.incrementAndGet();
                    jobDupTimes.compareAndSet(jobDupTimes.get(),1);
                }
                response.setStatus(SC_JOB_IS_RUNNING);
                return assembleMsgJson("dup run job");
            }

/*            if( SyncModifyUtil.compareMediaTypeForTarget(info, MediaSourceType.HDFS) ) {
                //if( !SyncModifyUtil.checkModifyColumnWithoutOpen(info) ) {
                    //response.setStatus(SC_JOB_CHECK_MODIFY_FAILURE);
                    //response.sendError();
                    //return assembleMsgJson("check modify column failure");
                    SyncModifyUtil.checkModifyColumnWithoutOpen(info);
                //}
            }*/
            SyncUtil.delESWriterIndex(info);

            //如果job关联的数据源正在切机房中，禁止job启动
            if(doubleCenterDataxService.isSwitchLabIng(info.getJob_src_media_source_id()) || doubleCenterDataxService.isSwitchLabIng(info.getJob_target_media_source_id())){
                String msg = "当前job(" + job_name + ")关联的数据源正在切机房中，job暂时不能启动，请稍后再试！";
                logger.info(msg);
                response.setStatus(SC_SWITCH_LAB_ING);
                return assembleMsgJson(msg);
            }

            String job_content = info.getJob_content();
            Map<String,String> jsonToMap = jsonStringToMap(jsonStr);
            if(request.getParameter("date") != null) {
                jsonToMap.put(DataxJobConfigConstant.HTTP_PARAMETER_DATE, request.getParameter("date") );
            }
            if(request.getParameter("time") != null) {
                jsonToMap.put(DataxJobConfigConstant.HTTP_PARAMETER_TIME, request.getParameter("time"));
            }
            Map<String, String> map = DataxUtil.replaceDynamicParameter(info, jsonToMap);
            logger.info("[JobServiceController]dynamic parameter -> " + map.toString());
            DataxCommand command = new DataxCommand();
            command.setJobId(new Long(id));
            command.setJobName(job_name);
            command.setType(DataxCommand.Type.Start);
            if (map != null && map.size() > 0) {
                command.setDynamicParam(true);
                command.setMapParam(map);

            }
            if (StringUtils.isNotBlank(info.getTiming_parameter())) {
                TimingParameter p = parseTimingParameter(info.getTiming_parameter());
                command.setJvmArgs(p.getJvmMemory());
            }

            //如果本地没启动datax服务，并且没有填写worker机器ip的话，将Command对象转换成json之前
            //就会报错了，这里方便后续调试加一个datax机器地址
            //info.setTiming_target_worker("1.2.3.4");
            logger.info("[JobServiceController]pre start -> " + info.toString());
            if(StringUtils.isNotBlank(jvmMax) && StringUtils.isNotBlank(jvmMin)) {
                StringBuilder jvmArgs = new StringBuilder();
                jvmArgs.append("-Xms").append(jvmMin).append(" ").append("-Xmx").append(jvmMax);
                command.setJvmArgs(jvmArgs.toString());
            }

            String msg = jobControlService.start(command, info.getTiming_target_worker());
            JobRestResponse jrr = new JobRestResponse();
            jrr.setMsg(JobRestResponse.SUCCESS);
            jrr.setExecutId(appendEnvToID(msg));
            String result = JSONObject.toJSONString(jrr);
            logger.info("[JobServiceController]result -> " + result);
            return JSONObject.parse(result);
        } catch(DynamicParamException e1) {
            logger.error(e1.getMessage(), e1);
            response.setStatus(SC_DYNAMIC_PARAM_PARSE_ERROR);
            return assembleMsgJson(e1.getMessage());
        } catch (Exception e2) {
            logger.error(e2.getMessage(), e2);
            response.setStatus(SC_UNKNOWN_ERROR);
            return assembleMsgJson(e2.getMessage());
        }
    }


    @RequestMapping(value = "/stop")
    @ResponseBody
    public Object doStop(@RequestParam("EXECUTE_ID") String id, @RequestParam("JOB_ID_SIGNAL") String md5, HttpServletResponse response) {
        try {
            id = parseId(id);
            if (!authWithJobExecutionId(id, md5)) {
                response.setStatus(SC_UNAUTHORIZED);
                return assembleMsgJson("auth failure,please check username and password, or job execution id not exist");
            }
            if (!specialNumberHandler(id) && !jobExecutionIdCheck(id)) {
                response.setStatus(SC_EXECUTE_ID_FORMAT_ERROR);
                return assembleMsgJson("job execution id illegal");
            }
            if (specialNumberHandler(id)) {
                JobRestResponse jrr = new JobRestResponse();
                jrr.setMsg(JobRestResponse.SUCCESS);
                return JSONObject.toJSONString(jrr);
            }
            JobExecutionInfo executionInfo = jobService.getJobExecutionById(Long.parseLong(id));
            JobConfigInfo info = jobService.getJobConfigById(executionInfo.getJob_id());
            if (!isTimingJob(info)) {
                response.setStatus(SC_NOT_TIMING_JOB);
                return assembleMsgJson("not timing job!");
            }
            String jobName = info.getJob_name();
            RunningData data = DataxUtil.getRunningData(jobName);
            //如果根据job_name从 /datax/admin/jobs/running 上取不到数据，可能任务已经结束了，这时候返回正确的200状态码就可以了
            if (StringUtils.isBlank(data.getIp())) {
                logger.error("cannot found ip, current job maybe stop " + jobName);
                response.setStatus(SC_STOP_JOB_FAILURE_NOTFOUND_DATAX_IP);
                return assembleMsgJson("cannot found ip, current job maybe stop");
            }

            DataxCommand command = new DataxCommand();
            command.setJobId(executionInfo.getJob_id());
            command.setJobName(jobName);
            command.setType(DataxCommand.Type.Stop);
            String json = JSONObject.toJSONString(command);
            //发送一个HTTP请求到 DataX服务器
            String address = DataxUtil.stopURL(data.getIp());
            String result = URLConnectionUtil.retryPOST(address, json);
            if (result != null && result.contains("success")) {
                return assembleMsgJson(JobRestResponse.SUCCESS);
            } else {
                response.setStatus(SC_STOP_JOB_FAILURE);
                return assembleMsgJson("stop failure");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.setStatus(SC_UNKNOWN_ERROR);
            return assembleMsgJson(e.getMessage());
        }
    }


    /**
     * 强制停止一个job
     *
     * @param id
     * @param md5
     * @param response
     * @return
     */
    @RequestMapping(value = "/forceStop")
    @ResponseBody
    public Object forceStop(@RequestParam("EXECUTE_ID") String id, @RequestParam("JOB_ID_SIGNAL") String md5, HttpServletResponse response) {
        try {
            id = parseId(id);
            if (!authWithJobExecutionId(id, md5)) {
                response.setStatus(SC_UNAUTHORIZED);
                return assembleMsgJson("auth failure,please check username and password, or job execution id not exist");
            }
            if (!specialNumberHandler(id) && !jobExecutionIdCheck(id)) {
                response.setStatus(SC_EXECUTE_ID_FORMAT_ERROR);
                return assembleMsgJson("job execution id illegal");
            }
            if (specialNumberHandler(id)) {
                JobRestResponse jrr = new JobRestResponse();
                jrr.setMsg(JobRestResponse.SUCCESS);
                return JSONObject.toJSONString(jrr);
            }
            JobExecutionInfo executionInfo = jobService.getJobExecutionById(Long.parseLong(id));
            JobConfigInfo info = jobService.getJobConfigById(executionInfo.getJob_id());
            if (!isTimingJob(info)) {
                response.setStatus(SC_NOT_TIMING_JOB);
                return assembleMsgJson("not timing job!");
            }
            String jobName = info.getJob_name();
            RunningData data = DataxUtil.getRunningData(jobName);
            if (StringUtils.isBlank(data.getIp())) {
                logger.warn("cannot found ip, current job maybe stop " + jobName);
                response.setStatus(SC_FORCESTOP_JOB_FAILURE_NOTFOUND_DATAX_IP);
                return assembleMsgJson("cannot found ip, current job maybe stop");
            }

            DataxCommand command = new DataxCommand();
            command.setJobId(executionInfo.getJob_id());
            command.setJobName(jobName);
            command.setType(DataxCommand.Type.Stop);
            command.setForceStop(true);
            String json = JSONObject.toJSONString(command);
            //发送一个HTTP请求到 DataX服务器
            String address = DataxUtil.forceStopURL(data.getIp());
            String result = URLConnectionUtil.retryPOST(address, json);
            int count = 0;
            boolean isError = false;
            while (true) {
                if (count > 10) {
                    response.setStatus(SC_FORCESTOP_JOB_FAILURE_TIMEOUT);
                    return assembleMsgJson("force stop failure,count>10");
                }
                if (DataxUtil.isJobRunning(info.getJob_name())) {
                    try {
                        Thread.sleep(1000);
                        count++;
                    } catch (InterruptedException e) {
                        isError = true;
                        response.setStatus(SC_FORCESTOP_JOB_FAILURE_BY_INTERRUPTED);
                        return assembleMsgJson("force stop failure,by interrupted");
                    }
                } else {
                    jobService.updateJobExecutionStateForRunningJob(JobExecutionState.KILLED, "job in zk is stop " + info.getJob_name(), Long.parseLong(id));
                    break;
                }
            }
            if (result != null && result.contains("success") && !isError) {
                return assembleMsgJson(JobRestResponse.SUCCESS);
            } else {
                response.setStatus(SC_FORCESTOP_JOB_FAILURE);
                return assembleMsgJson("force stop failure");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.setStatus(SC_UNKNOWN_ERROR);
            return assembleMsgJson(e.getMessage());
        }
    }


    /**
     * 根据 job配置的id，返回所有管理的job运行状态，极端情况下一个job_config_id可能会启动多次，也就是有很多个job运行历史，这里没有考虑分页</br>
     * 暂时是一股脑的全部返回
     * 加入分页功能
     *
     * @param id
     * @param md5
     * @param response
     * @return
     */
    @RequestMapping(value = "/allState")
    @ResponseBody
    public Object allState(@RequestParam("CONFIG_ID") String id, @RequestParam("JOB_ID_SIGNAL") String md5, HttpServletResponse response,
                           @RequestParam(value = "START_PAGE", required = false) String startPageStr, @RequestParam(value = "COUNT", required = false) String countStr) {
        try {

            id = parseId(id);
            if (!authWithJobConfigId(id, md5)) {
                response.setStatus(SC_UNAUTHORIZED);
                return assembleMsgJson("auth failure,please check username and password, or job config id not exist");
            }
            if (!jobConfigIdCheck(id)) {
                response.setStatus(SC_CONFIG_ID_FORMAT_ERROR);
                return assembleMsgJson("job config id illegal");
            }
            JobConfigInfo info = jobService.getJobConfigById(Long.parseLong(id));
            if (!isTimingJob(info)) {
                response.setStatus(SC_NOT_TIMING_JOB);
                return assembleMsgJson("not timing job!");
            }
            long startPage = 0;
            long count = DEFAULT_QUERY_COUNT_ONCE;
            if (!StringUtils.isBlank(startPageStr) && !StringUtils.isBlank(countStr)) {
                startPage = Long.parseLong(startPageStr);
                count = Long.parseLong(countStr);
            }
            List<JobExecutionInfo> infos = jobControlService.history(Long.parseLong(id), startPage, count);
            JobRestResponse jrr = new JobRestResponse();
            jrr.setMsg(JobRestResponse.SUCCESS);
            jrr.setHistory(infos);
            for (JobExecutionInfo jei : infos) {
                jei.setException(null);
                jei.setOriginal_configuration(null);
                jei.setTask_communication_info(null);
            }
            String result = JSONObject.toJSONString(jrr);
            return JSONObject.parse(result);
        } catch (Exception e) {
            logger.error("job service doState failure.", e);
            response.setStatus(SC_UNKNOWN_ERROR);
            return assembleMsgJson(e.getMessage());
        }
    }


    /**
     * 根据 job执行的id查询单个job的执行状态，同时获取ZK上的job状态，如果两者都是 running 状态，则认为这个job状态是正常的并返回</br>
     * 否则如果zk上的状态是 running 而db中获取的不是，或者db中的状态是 running 而zk上获取的不是，则认为这个job当前的状态是异常，</br>
     * 将数据库中的 job execution表中的装改改为失败，并返回错误消息给调用方
     *
     * @param id
     * @param md5
     * @param response
     * @return
     */
    @RequestMapping(value = "/state")
    @ResponseBody
    public Object state(@RequestParam("EXECUTE_ID") String id, @RequestParam("JOB_ID_SIGNAL") String md5, HttpServletResponse response) {
        try {
            id = parseId(id);
            if (!authWithJobExecutionId(id, md5)) {
                response.setStatus(SC_UNAUTHORIZED);
                return assembleMsgJson("auth failure,please check username and password, or execution id not exist");
            }
            if (!specialNumberHandler(id) && !jobExecutionIdCheck(id)) {
                response.setStatus(SC_EXECUTE_ID_FORMAT_ERROR);
                return assembleMsgJson("job execution id illegal");
            }
            if (specialNumberHandler(id)) {
                JobRestResponse jrr = new JobRestResponse();
                jrr.setMsg(JobRestResponse.SUCCESS);
                return JSONObject.toJSONString(jrr);
            }
            JobExecutionInfo info = jobControlService.state(Long.parseLong(id));
            JobConfigInfo configInfo = jobService.getJobConfigById(info.getJob_id());
            logger.info("[JobServiceController]pre state " + info.toString());
            if (!isTimingJob(configInfo)) {
                response.setStatus(SC_NOT_TIMING_JOB);
                return assembleMsgJson("not timing job!");
            }

            //一切返回的状态以DB为准，如果DB中的状态是RUNNING，再检查zookeeper，若zookeeper是stop则表示这个进程挂了，返回失败
            //并更新数据库状态将此运行记录设为failure
            if (JobExecutionState.RUNNING.equals(info.getState())) {
                //如果zk的的状态为非运行
                if (!DataxUtil.isJobRunning(configInfo.getJob_name())) {
                    //数据库的状态是RUNNING，zk上的节点不存在了，不一定是任务不正常，可能是正常的退出
                    //轮询10次做检查数据库状态，如果还是RUNNING，则认为这个任务结束的状态不正常，设置为failure
                    if( !checkJobStateWhenZNodeNotExist(Long.parseLong(id)) ) {
                        String msg = "this job =" + info.toString() + "  process has been hung up";
                        logger.warn("[JobServiceController]" + msg);
                        jobService.modifyJobExecutionState(JobExecutionState.FAILED, msg, info.getId());
                        response.setStatus(HttpServletResponse.SC_OK);
                        return assembleMsgJson(msg);
                    }
                }
            }
            //如果job运行失败，也返回失败状态
            if (JobExecutionState.FAILED.equals(info.getState())) {
                String msg = "this job execute failure " + info.toString();
                logger.warn("[JobServiceController] " + msg);
                response.setStatus(SC_STATE_JOB_RESULT_FAILURE);
                return assembleMsgJson(msg);
            }
            JobRestResponse jrr = new JobRestResponse();
            info.setException(null);
            info.setOriginal_configuration(null);
            info.setTask_communication_info(null);
            jrr.setState(info);
            jrr.setMsg(JobRestResponse.SUCCESS);
            String result = JSONObject.toJSONString(jrr);
            logger.info("[JobServiceController]result -> " + result.toString());
            return JSONObject.parse(result);
        } catch (Exception e) {
            logger.error("job service doState failure.", e);
            response.setStatus(SC_UNKNOWN_ERROR);
            return assembleMsgJson(e.getMessage());
        }
    }


    @RequestMapping(value = "/taskMonitor")
    @ResponseBody
    public Object monitorTask(@RequestParam(value = "FORMAT", required = false) String format, HttpServletResponse response) {
        try {
            Map<Long, String> newExcMap = taskExceptionMonitor.getExceptionMap();

            List<TaskInfo> tinfos = taskService.getList();
            List<TaskInfo> taskInfos = new ArrayList<>();
            for (TaskInfo ti : tinfos) {
                if (ti.getTaskType() == TaskType.MYSQL) {
                    List<PluginWriterParameter> parameters = ti.getTaskWriterParameterObjs();
                    for (PluginWriterParameter pwp : parameters) {
                        if (pwp instanceof HdfsWriterParameter) {
                            taskInfos.add(ti);
                        }
                    }
                }
            }

            List<TaskDelayTimeInfo> delayTimeList = taskDelayTimeService.getList();
            List<TaskMonitorInfo> monitors = taskInfos.stream().map(i -> {
                TaskMonitorInfo view = new TaskMonitorInfo();
                view.setId(i.getId() + "");
                view.setTaskId(i.getId() + "");
                view.setTaskName(i.getTaskName());
                if (newExcMap != null && StringUtils.isNotBlank(newExcMap.get(i.getId()))) {
                    view.setIsErr("true");
                }
                long mediaSourceID = i.getTaskReaderParameterObj().getMediaSourceId();
                MediaSourceInfo msi = mediaService.getMediaSourceById(mediaSourceID);
                view.setSchema(msi.getParameterObj().getNamespace());
                return view;
            }).collect(Collectors.toList());

            //为View设置Position相关属性
            monitors.stream().forEach(i -> {
                MysqlReaderPosition position = (MysqlReaderPosition) taskPositionService.getPosition(i.getId().toString());
                if (position != null) {
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(position.getTimestamp()));
                    i.setCurrentBinlogTimeStamp(time + " (" + position.getTimestamp() + ")");

                } else {
                    i.setCurrentBinlogTimeStamp("NULL");
                }
            });

            //设置延迟时间
            monitors.stream().forEach(s -> delayTimeList.forEach(t -> {
                        if (t.getTaskId() == Long.parseLong(s.getTaskId())) {
                            if (t.getDelayTime() != null && t.getDelayTime() > DEFAULT_DELAY_TIMIE) {
                                s.setDelayTime(t.getDelayTime() == null ? "0" : t.getDelayTime() + "");
                            }
                        }
                    })
            );
            List<TaskMonitorInfo> taskMonitors = new ArrayList<>();
            for (TaskMonitorInfo tmi : monitors) {
                tmi.setId(null);
                tmi.setTaskId(null);
                if (tmi.getIsErr().equals("true") || !tmi.getDelayTime().equals("0")) {
                    taskMonitors.add(tmi);
                }
            }
            JobRestResponse jrr = new JobRestResponse();
            jrr.setMsg(JobRestResponse.SUCCESS);
            jrr.setTask(taskMonitors);
            String result = JSONObject.toJSONString(jrr);
            if (format != null && Boolean.parseBoolean(format)) {
                result = formatJson(result);
            }
            return JSONObject.parse(result);
        } catch (Exception e) {
            logger.error("task monitor execute failure.", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return assembleMsgJson(e.getMessage());
        }
    }


    private boolean checkJobStateWhenZNodeNotExist(long id) {
        boolean isSuccess = false;
        for(int i=0;i<10;i++) {
            JobExecutionInfo info = jobControlService.state(id);
            if( info!=null && JobExecutionState.SUCCEEDED.equals(info.getState())) {
                isSuccess = true;
                break;
            }
            try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(),e);
            }
        }
        return isSuccess;
    }

    private Map<String, String> jsonStringToMap(String json) {
        if (StringUtils.isBlank(json)) {
            return new HashMap<>();
        }
        return JSONObject.parseObject(json, Map.class);
    }


    private TimingParameter parseTimingParameter(String paremeter) {
        TimingParameter p = JSONObject.parseObject(paremeter, TimingParameter.class);
        return p;
    }

    private Object assembleMsgJson(String msg) {
        JobRestResponse response = new JobRestResponse();
        response.setMsg( encodeMsg(msg) );
        response.setExecutId(UUID.randomUUID().toString());
        String result = JSONObject.toJSONString(response);
        return JSONObject.parse(result);
    }


    private static String encodeMsg(String msg) {
        try {
            return URLEncoder.encode(msg,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return e.getMessage();
        }
    }

    private boolean isTimingJob(JobConfigInfo info) {
        return info.isTiming_yn();
    }


    private String parseId(String id) {
        if (StringUtils.isBlank(id)) {
            return id;
        }
        String env = getEnv();
        if (id.endsWith(env)) {
            id = id.substring(0, id.length() - env.length());
        }
        return id;
    }

    private String appendEnvToID(String id) {
        return id + getEnv();
    }


    private boolean authWithJobConfigId(String id_str, String signal) {
        return true;
/*
        if (StringUtils.isBlank(signal)) {
            return false;
        }
        JobService service = DataLinkFactory.getObject(JobService.class);
        JobConfigInfo info = service.getJobConfigById(Long.parseLong(id_str));
        if (info == null) {
            return false;
        }
        long job_id = info.getId();
        long src_media_id = info.getJob_src_media_source_id();
        long target_media_id = info.getJob_target_media_source_id();
        String job_media_name = info.getJob_media_name();
        String content = job_id + src_media_id + target_media_id + job_media_name;
        String md5 = getMd5(content);
        if (md5 != null && md5.equals(signal)) {
            return true;
        }
        return false;
*/
    }


    private boolean jobConfigIdCheck(String configId) {
        return jobExecutionIdCheck(configId);
    }

    private boolean jobExecutionIdCheck(String executeID) {
        if (StringUtils.isBlank(executeID)) {
            return false;
        }
        try {
            long id = Long.parseLong(executeID);
            if (id > 0) {
                return true;
            }
        } catch (Exception e) {
            //ignore
        }
        return false;
    }

    /**
     * 如果调用方传入的是一个特殊的数字，如Long.MIN_VALUE，或者其他约定的数字，则认为是合法的
     *
     * @param number
     * @return
     */
    private boolean specialNumberHandler(String number) {
        try {
            if (JOB_STILL_RUNNING_NUM_MARK == Long.parseLong(number)) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean authWithJobExecutionId(String id_str, String signal) {
        return true;
/*
        if (StringUtils.isBlank(signal)) {
            return false;
        }
        JobService service = DataLinkFactory.getObject(JobService.class);
        JobExecutionInfo executionInfo = service.getJobExecutionById(Long.parseLong(id_str));
        if (executionInfo == null) {
            return false;
        }
        JobConfigInfo info = service.getJobConfigById(executionInfo.getJob_id());
        if (info == null) {
            return false;
        }
        long job_id = info.getId();
        long src_media_id = info.getJob_src_media_source_id();
        long target_media_id = info.getJob_target_media_source_id();
        String job_media_name = info.getJob_media_name();
        String content = job_id + src_media_id + target_media_id + job_media_name;
        String md5 = getMd5(content);
        if (md5 != null && md5.equals(signal)) {
            return true;
        }
        return false;
*/
    }


    public String getEnv() {
        String currentEnv = ManagerConfig.current().getCurrentEnv();
        if (StringUtils.isBlank(currentEnv)) {
            return "_";
        } else {
            return "_" + currentEnv;
        }
    }

    private boolean auth(String id_str, HttpServletRequest request) {
        return true;
//        String username = request.getHeader(HTTP_HEADER_USERNAME);
//        String password = request.getHeader(HTTP_HEADER_PASSWORD);
//        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
//            return false;
//        }
//        String decodePassword = new String( Base64.getDecoder().decode(password) );
//        return UserLoginController.ucarPassportCheck(username, decodePassword);
    }


    private long parseStringToLong(String id) {
        long result = -1;
        try {
            result = Long.parseLong(id);
        } catch (Exception e) {
            //ignore
        }
        return result;
    }

    /**
     * 用于获取一个String的md5值
     *
     * @param str
     * @return
     */
    public String getMd5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(str.getBytes());
            StringBuilder sb = new StringBuilder(40);
            for (byte x : bs) {
                if ((x & 0xff) >> 4 == 0) {
                    sb.append("0").append(Integer.toHexString(x & 0xff));
                } else {
                    sb.append(Integer.toHexString(x & 0xff));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }


    private String formatJson(String json) {
        int i = 0;
        int len = 0;
        String tab = "    ";
        String targetJson = "";
        int indentLevel = 0;
        boolean inString = false;
        char currentChar = ' ';
        for (i = 0, len = json.length(); i < len; i += 1) {
            currentChar = json.charAt(i);
            switch (currentChar) {
                case '{':
                case '[':
                    if (!inString) {
                        targetJson += currentChar + "\n" + repeat(tab, indentLevel + 1);
                        indentLevel += 1;
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case '}':
                case ']':
                    if (!inString) {
                        indentLevel -= 1;
                        targetJson += "\n" + repeat(tab, indentLevel) + currentChar;
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case ',':
                    if (!inString) {
                        targetJson += ",\n" + repeat(tab, indentLevel);
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case ':':
                    if (!inString) {
                        targetJson += ": ";
                    } else {
                        targetJson += currentChar;
                    }
                    break;
                case ' ':
                case '\n':
                case '\t':
                    if (inString) {
                        targetJson += currentChar;
                    }
                    break;
                case '"':
                    if (i > 0 && json.charAt(i - 1) != '\\') {
                        inString = !inString;
                    }
                    targetJson += currentChar;
                    break;
                default:
                    targetJson += currentChar;
                    break;
            }
        }
        return targetJson;
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }


    public class MD5Encode {
        private String md5;

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }
    }


    private void sendMailByJobDupRun(String jobName) {
        try {
            List<String> recipient = getUserEamil();
            StringBuilder content = new StringBuilder();
            content.append("hi:").append("<br>").append("<br>");
            String env = ManagerConfig.current().getCurrentEnv();
            content.append("&nbsp &nbsp &nbsp").append("当前环境 : ").append(env).append("<br/>");
            content.append("重复运行的job name").append("<br/>");
            content.append(jobName);
            content.append("<br/>");
            MailInfo info = new MailInfo();
            String emailTitle = "Job Dup RUN";
            if (env != null) {
                emailTitle = emailTitle + "_" + env;
            }
            info.setSubject(emailTitle);
            info.setMailContent(content.toString());
            info.setRecipient(recipient);
            mailService.sendMail(info);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * 当 cdse调用的是非定时的job，就会报错，同时发送一个邮件提醒
     * @param jobName
     */
    private void sendMailByNotTimingJob(String jobName) {
        try {
            List<String> recipient = getUserEamil();
            StringBuilder content = new StringBuilder();
            content.append("hi:").append("<br>").append("<br>");
            String env = ManagerConfig.current().getCurrentEnv();
            content.append("&nbsp &nbsp &nbsp").append("当前环境 : ").append(env).append("<br/>");
            content.append("不是定时job!").append("<br/>");
            content.append(jobName);
            content.append("<br/>");
            MailInfo info = new MailInfo();
            String emailTitle = "有一个非定时的job被运行了！";
            if (env != null) {
                emailTitle = emailTitle + "_" + env;
            }
            info.setSubject(emailTitle);
            info.setMailContent(content.toString());
            info.setRecipient(recipient);
            mailService.sendMail(info);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    private List<String> getUserEamil() {
        UserService userService = DataLinkFactory.getObject(UserService.class);
        List<UserInfo> users = userService.getUserInfoByReceiveMail();
        List<String> recipient = new ArrayList<>();
        if (users != null && users.size() > 0) {
            for (UserInfo u : users) {
                if (StringUtils.isNotBlank(u.getUcarEmail())) {
                    String email = u.getUcarEmail() + "@ucarinc.com";
                    recipient.add(email);
                } else {
                    //邮箱都为空就不
                }
            }
        }
        return recipient;
    }


}
