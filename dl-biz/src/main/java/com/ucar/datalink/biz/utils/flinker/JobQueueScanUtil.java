package com.ucar.datalink.biz.utils.flinker;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.JobControlService;
import com.ucar.datalink.biz.service.JobQueueService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MailService;
import com.ucar.datalink.biz.service.impl.JobServiceDynamicArgs;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.job.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yang.wang09 on 2019-04-02 20:11.
 */
public class JobQueueScanUtil {
    private static final Logger logger = LoggerFactory.getLogger(JobQueueScanUtil.class);

    private static ExecutorService es = null;

    private static JobService jobService;

    private static JobQueueService jobQueueService;

    private static JobControlService jobControlService;

    private static MailService mailService;

    private static final Set<String> JOB_FINAL_STATE = new HashSet<>();

    private static final String FAILED_STAT = "FAILED";

    private static final long SCAN_THREAD_SLEEP_TIME = 60 * 1000;

    private static final long CHECK_JOB_STATE_SLEEP_TIME = 10 * 1000;

    public static final String JOB_QUEUE_STATE_EXECUTE = "EXECUTE";

    public static final String JOB_QUEUE_STAT_FINISH = "FINISH";

    private static volatile boolean isRunning = false;

    private static String currentProcessJobName;

    static {
        es = Executors.newFixedThreadPool(1);
        jobService = DataLinkFactory.getObject(JobService.class);
        jobQueueService = DataLinkFactory.getObject(JobQueueService.class);
        jobControlService = DataLinkFactory.getObject(JobServiceDynamicArgs.class);
        mailService = DataLinkFactory.getObject(MailService.class);

        JOB_FINAL_STATE.add(JobExecutionState.ABANDONED);
        JOB_FINAL_STATE.add(JobExecutionState.FAILED);
        JOB_FINAL_STATE.add(JobExecutionState.KILLED);
        JOB_FINAL_STATE.add(JobExecutionState.SUCCEEDED);

        new Thread(new ScanThread(),"job_queue_scan").start();
    }


    public static final void startScan() {
        isRunning = true;
    }

    public static final void pauseScan() {
        isRunning = false;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private static class ScanThread implements Runnable {

        public ScanThread() {
            logger.info("create scan thread");
        }

        @Override
        public void run() {
            logger.info("run scan thread");
            while(true) {
                try {
                    if(isRunning) {
                        scanJobQueue();
                    }
                    Thread.sleep(SCAN_THREAD_SLEEP_TIME);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(),e);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(),ex);
                }
            }

        }
    }

    public static void scanJobQueue() {
        JobQueue queue = jobQueueService.unexecuteJob();
        if(queue == null) {
            //所有的job都执行完了，返回
            return;
        }
        logger.info(" scan job ->"+queue.toString());
        executeJob(queue);
    }


    private static class ExecuteThread implements Runnable {
        JobQueue info;

        public ExecuteThread(JobQueue info) {
            this.info = info;
        }

        @Override
        public void run() {
            executeJob(info);
        }
    }

    public static void executeJob(JobQueue queue) {
        if(queue==null) {
            return;
        }
        logger.info("execute job->"+queue.toString());
        String jobName = queue.getJobName();
        Long jobConfigId = jobService.getJobConfigIDByName(jobName);
        JobConfigInfo info = jobService.getJobConfigById(jobConfigId);

        String job_content = info.getJob_content();
        Map<String,String> jsonToMap = new HashMap<>();

        Map<String, String> map = FlinkerJobUtil.replaceDynamicParameter(info, jsonToMap);
        //logger.info("[JobServiceController]dynamic parameter -> " + map.toString());
        JobCommand command = new JobCommand();
        command.setJobId(info.getId());
        command.setJobName(info.getJob_name());
        command.setType(JobCommand.Type.Start);
        if (map != null && map.size() > 0) {
            command.setDynamicParam(true);
            command.setMapParam(map);
        }
        if (StringUtils.isNotBlank(info.getTiming_parameter())) {
            TimingParameter p = parseTimingParameter(info.getTiming_parameter());
            command.setJvmArgs(p.getJvmMemory());
        }
        String msg = jobControlService.start(command, info.getTiming_target_worker());
        logger.info("job control service reuslt -> "+msg);
        //每过10秒检查job运行状态，再更新job队列，如果整个队列都执行完了，则发送邮件
        if( checkJobState(queue,msg) ) {
            //检查队列中的其他任务是否都执行完了，如果全都执行完了，则发送邮件通知
            long queueId = queue.getQueueId();
            checkWholeQueueStat(queueId,msg);
        }
    }

    private static boolean checkJobState(JobQueue queue, String executeId) {
        while(true) {
            try {
                Thread.sleep(CHECK_JOB_STATE_SLEEP_TIME);
                if( StringUtils.isBlank(executeId) || !StringUtils.isNumeric(executeId) ) {
                    //运行id不是数字，直接返回
                    return false;
                }
                currentProcessJobName = queue.getJobName();
                Long id = Long.parseLong(executeId);
                JobExecutionInfo info = jobService.getJobExecutionById(id);
                String state = info.getState();
                if(StringUtils.isBlank(state)) {
                    return false;
                }
                JobQueue q = jobQueueService.getJobQueueById(queue.getId());
                if(q == null) {
                    //一个队列记录是空，说明这个记录被删除了，直接返回true即可
                    return true;
                }
                queue.setJobState(state);
                jobQueueService.modifyJobQueue(queue);
                if(JOB_FINAL_STATE.contains(state.toUpperCase())) {
                    //job运行完成
                    return true;
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

    /**
     * 每个job执行完后，检查整个job队列的状态，如果整个队列的任务都执行完了
     * 则发送邮件通知
     * @param queueId
     */
    private static void checkWholeQueueStat(long queueId, String executIdStr) {
        List<JobQueue> list = jobQueueService.allQueueStatByQueueId(queueId);
        if(list==null || list.size()==0) {
            //如果整个队列都被删除了，直接返回
            return;
        }
        JobQueueInfo jobQueueInfo = jobQueueService.getJobQueueInfoById(queueId);
        String failToStop = jobQueueInfo.getFailToStop();
        boolean isFailToStop = false;
        if(StringUtils.isBlank(failToStop)) {
            isFailToStop = false;
        } else {
            isFailToStop = Boolean.parseBoolean(failToStop);
        }
        JobExecutionInfo jobExecutionInfo = jobService.getJobExecutionById(Long.parseLong(executIdStr));
        //如果job队列的设置了 任务失败就不继续执行, 并且 当前执行完的这个任务状态是 失败，则丢弃剩余的任务
        if(isFailToStop && StringUtils.endsWithIgnoreCase(FAILED_STAT,jobExecutionInfo.getState())) {
            //将所有剩余的任务状态改为丢弃
            jobQueueService.abandoneRemainJob(queueId);
            list = jobQueueService.allQueueStatByQueueId(queueId);
            finshJobQueue(jobQueueInfo,queueId,list);
            return;
        }

        boolean isFinish = true;
        for(JobQueue q : list) {
            String state = q.getJobState().toUpperCase();
            if(!JOB_FINAL_STATE.contains(state)) {
                isFinish = false;
            }
        }
        if(isFinish) {
            finshJobQueue(jobQueueInfo,queueId,list);
        }
    }


    private static void finshJobQueue(JobQueueInfo jobQueueInfo, long queueId,List<JobQueue> list) {
        //全部执行完，修改queue的状态，发送邮件
        jobQueueInfo.setQueueState(JOB_QUEUE_STAT_FINISH);
        jobQueueService.modifyJobQueueInfo(jobQueueInfo);
        currentProcessJobName = "";
        sendMail(queueId, list);
    }

    private static void sendMail(long queueId, List<JobQueue> list) {
        JobQueueInfo jobQueueInfo = jobQueueService.getJobQueueInfoById(queueId);
        logger.info("job 执行完成_ \r\n" + assembleMailContent(jobQueueInfo, list));
    }

    private static String assembleMailContent(JobQueueInfo info, List<JobQueue> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("&nbsp &nbsp &nbsp").append("<br/>");
        sb.append("job队列名称：  ");
        sb.append(info.getQueueName());
        sb.append("<br/>");
        sb.append("<table border='1'>").append("<tr><td>job名称</td><td>任务状态</td></tr>");
        list.forEach(i->{
            sb.append("<tr><td>").append(i.getJobName()).append("</td>").append("<td>").append(i.getJobState()).append("</td></tr>");
        });
        return sb.toString();
    }

    public static String currentProcessJobName() {
        return currentProcessJobName;
    }

    private static TimingParameter parseTimingParameter(String paremeter) {
        TimingParameter p = JSONObject.parseObject(paremeter, TimingParameter.class);
        return p;
    }

}
