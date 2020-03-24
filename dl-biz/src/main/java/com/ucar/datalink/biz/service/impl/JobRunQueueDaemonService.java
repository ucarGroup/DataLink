package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobUtil;
import com.ucar.datalink.domain.job.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by user on 2018/3/22.
 * <p>
 * `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键，自增类型',
 * `job_id_list` varchar(2000) NOT NULL COMMENT 'job id列表用逗号分隔',
 * `current_process_id` varchar(100) DEFAULT NULL COMMENT '当前正在运行的id列表，[job_id]-[job_execution_id],... 这种格式',
 * `queue_state` varchar(20) DEFAULT NULL COMMENT '整个队列的运行状态，未执行，有错误，执行中，执行完',
 * `job_count` INT(11) COMMENT '整个队列的job数量',
 * `executed_success_job_count` INT(11) COMMENT '已经执行完的job数量',
 * `executed_failure_job_count` INT(11) COMMENT '已执行失败的job数量',
 * `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 * `modify_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '修改时间',
 */
//@Service("runQueue")
public class JobRunQueueDaemonService extends JobDaemonService {

    private static final Logger logger = LoggerFactory.getLogger(JobRunQueueDaemonService.class);

    /**
     * 同时可以执行 3 个job
     */
    private static final int CONCURRENT_RUN_JOB_NUM = 3;

    /**
     * 每10分钟执行一次扫描任务
     */
    private static final long DELAY_TIME_BY_SECOND = 10 * 60 * 1000;


    @Autowired
    @Qualifier("dynamic")
    private JobControlService jobControlService;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRunQueueService service;

    @Autowired
    private MailService mailService;


    /**
     * 定时任务
     */
    private Timer timer = new Timer();

    /**
     * 可执行的队列job数量
     */
    private int availablePorcessCount = CONCURRENT_RUN_JOB_NUM;

    /**
     * 如果进程挂了或者手动重启了，则需要重新计算availablePorcessCount数量
     */
    private volatile boolean isFirst = true;


    private static volatile boolean isOpen = false;

    private int currentProcessJobQueueNum = 0;

    private static AtomicBoolean isIntial = new AtomicBoolean(false);


    @Override
    public void initialized() {
        if(isIntial.get()) {
            return;
        }
        isIntial.compareAndSet(false,true);
        ScanJobRunQueueTask task = new ScanJobRunQueueTask();
        timer.schedule(task, 0, 10 * 1000);
    }

    @Override
    public void destroyed() {
        timer.cancel();
    }



    public class Destory implements Runnable {
        public void run() {
            destroyed();
        }
    }


    public static void startJobRunQueue() {
        isOpen = true;
        logger.info("startJobRunQueue   is start -> " + isOpen);
    }

    public static void stopJobRunQueue() {
        isOpen = false;
        logger.info("stopJobRunQueue  is stop -> " + isOpen);
    }



    public void jobRunQueueScan() {
        try {
            if (!isOpen) {
                return;
            }
            logger.info("scan job run queue task!!!  ");
            if (isFirst) {
                caclAvailablePorcessCount();
            }
            //处理running的任务
            processRunningJob();

            //处理stop的队列，这些队列中可能还有一些任务正在运行中，遍历这些运行中的任务然后修改状态
            processStopJob();

            //处理未执行的队列
            processReadyJob();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private class ScanJobRunQueueTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (!isOpen) {
                    return;
                }
                logger.info("scan job run queue task!!!  ");
                if (isFirst) {
                    caclAvailablePorcessCount();
                }
                //处理running的任务
                processRunningJob();

                //处理stop的队列，这些队列中可能还有一些任务正在运行中，遍历这些运行中的任务然后修改状态
                processStopJob();

                //处理未执行的队列
                processReadyJob();

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }

    }

    /**
     * 根据 job config id启动job，如果这个job已经被启动了，则返回最近的execution 记录
     *
     * @param jobConfigId
     * @return job execution的id
     */
    private long scheduleJob(long jobConfigId) {
        try {
            //调用service接口，创建一个job，并返回 executeion id
            //JobService jobService = DataLinkFactory.getObject(JobService.class);
            //JobControlService jobControlService = DataLinkFactory.getObject(JobServiceDynamicArgs.class);
            JobConfigInfo info = jobService.getJobConfigById(jobConfigId);
            logger.info("[JobRunQueueListener]scheduleJob JobConfigInfo -> " + info.toString());
            String job_name = info.getJob_name();
            if (FlinkerJobUtil.isJobRunning(job_name)) {
                //根据id查询 job_execution表，返回最近的一条记录
                JobExecutionInfo jei = jobService.getJustJobExecutionByConfigId(jobConfigId);
                return jei.getId();
            }
            String job_content = info.getJob_content();
            Map<String, String> map = FlinkerJobUtil.replaceDynamicParameter(info, new HashMap<String, String>());
            JobCommand command = new JobCommand();
            command.setJobId(new Long(jobConfigId));
            command.setJobName(job_name);
            command.setType(JobCommand.Type.Start);
            if (map != null && map.size() > 0) {
                command.setDynamicParam(true);
                command.setMapParam(map);
            }
            if (StringUtils.isNotBlank(info.getTiming_parameter())) {
                TimingParameter p = JSONObject.parseObject(info.getTiming_parameter(), TimingParameter.class);
                command.setJvmArgs(p.getJvmMemory());
            }
            logger.info("[JobRunQueueListener]scheduleJob JobCommand -> " + command.toString());
            String msg = jobControlService.start(command, info.getTiming_target_worker());
            return Long.parseLong(msg);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 如果进程重启则需要重新计算 availablePorcessCount值
     * 遍历所有的RUNNING队列，然后累计所有队列的current_process_id，
     * 再用availablePorcessCount值减去这个值就是当前可用的数量
     */
    private void caclAvailablePorcessCount() {
        logger.info("[JobRunQueueListener]caclAvailablePorcessCount start");
        //JobRunQueueService service = DataLinkFactory.getObject(JobRunQueueService.class);
        List<JobRunQueueInfo> list = service.getAllJobRunQueueInfoByState(JobRunQueueState.PROCESSING);
        if (list != null && list.size() > 0) {
            for (JobRunQueueInfo jri : list) {
                String processList = jri.getCurrentPorcessId();
                if (StringUtils.isBlank(processList)) {
                    continue;
                }
                int processListCount = processList.split(",").length;
                availablePorcessCount -= processListCount;
            }
        }
        if (availablePorcessCount < 0) {
            availablePorcessCount = 0;
        }
        isFirst = false;
        logger.info("[JobRunQueueListener]caclAvailablePorcessCount end");
    }


    /**
     * 处理Job队列中的运行列表
     *
     * @param info
     */
    private void executeProcessJobList(JobRunQueueInfo info) {
        //logger.info("[JobRunQueueListener]executeProcessJobList -> "+ info.toString());
        //JobService jobService = DataLinkFactory.getObject(JobService.class);
        //JobRunQueueService queueService = DataLinkFactory.getObject(JobRunQueueService.class);
        String processJobList = info.getCurrentPorcessId();
        String jobIdList = info.getJobIdList();
        //获得可用的job id列表，job_id_list - peocess_id_list
        List<String> avaliableJobIdList = avaliableProcessJob(jobIdList, processJobList);
        logger.info("avaliable job id list -> " + avaliableJobIdList);

        List<JobPair> successList = idsStringToList(info.getSuccessList());
        List<JobPair> failureList = idsStringToList(info.getFailureList());
        availablePorcessCount += successList.size();
        availablePorcessCount += failureList.size();
        if (availablePorcessCount > CONCURRENT_RUN_JOB_NUM) {
            availablePorcessCount = CONCURRENT_RUN_JOB_NUM;
        }
//      if(availablePorcessCount <= 0) {
//          return;
//      }

        try {
            List<JobPair> list = new CopyOnWriteArrayList<>();
            if (StringUtils.isBlank(processJobList)) {
                for (String avaliableConfigId : avaliableJobIdList) {
                    long executionnId = scheduleJob(Long.parseLong(avaliableConfigId));
                    if (-1 == executionnId) {
                        logger.warn("[if breanch]schedule job failure, job_config_id -> " + avaliableConfigId);
                        continue;
                    }
                    list.add(new JobPair(Long.parseLong(avaliableConfigId), executionnId));
                }
            } else {
                String[] jobList = processJobList.split(",");
                if (jobList == null || jobList.length == 0) {
                    return;
                }
                int jobProcessCount = jobList.length;
                for (String s : jobList) {
                    list.add(new JobPair(Long.parseLong(s.split("-")[0]), Long.parseLong(s.split("-")[1])));
                }
                int successCount = 0;
                int failureCount = 0;
                for (JobPair i : list) {
                    JobExecutionInfo jei = jobService.getJobExecutionById(i.jobExecutionId);
                    //如果 JobExecutionInfo == null 说明这个任务没有运行，从运行队列中删除
                    if (null == jei) {
                        jobProcessCount++;
                        list.remove(i);
                        avaliableJobIdList.add(i.jobConfigId + "");
                    }
                    //如果job execution的状态是成功状态则 successCount +1
                    else if (JobExecutionState.SUCCEEDED.equals(jei.getState())) {
                        successCount++;
                        successList.add(i);
                        list.remove(i);
                    } else if (JobExecutionState.ABANDONED.equals(jei.getState()) || JobExecutionState.FAILED.equals(jei.getState()) || JobExecutionState.KILLED.equals(jei.getState())) {
                        failureCount++;
                        failureList.add(i);
                        list.remove(i);
                    } else {
                        //其他状态，如RUNNING，忽略即可
                        //logger.warn("unkownk state -> "+jei.toString());
                    }
                }
                //计算job执行列表中的剩余job数量
                int jobProcessListRemainderCount = jobProcessCount - successCount - failureCount;
                if (jobProcessListRemainderCount > CONCURRENT_RUN_JOB_NUM) {
                    jobProcessListRemainderCount = CONCURRENT_RUN_JOB_NUM;
                }

                logger.info("[JobRunQueueListener]executeProcessJobList job success size->" + successList.size() + "\tlist->" + successList);
                logger.info("[JobRunQueueListener]executeProcessJobList job failure size->" + failureList.size() + "\tlist->" + failureList);
                logger.info("[JobRunQueueListener]executeProcessJobList jobProcessListRemainderCount->" + jobProcessListRemainderCount);
                //如果剩余数量 jobProcessListRemainderCount > 0 则继续执行几个job
                //获取队列中剩余没有执行的job config id列表，启动这些job，记录到 pair list中
                if (jobProcessListRemainderCount > 0) {
                    for (String configId : avaliableJobIdList) {
                        long executionnId = scheduleJob(Long.parseLong(configId));
                        if (-1 == executionnId) {
                            logger.warn("[else branch]schedule job failure, job_config_id -> " + configId);
                            continue;
                        }
                        list.add(new JobPair(Long.parseLong(configId), executionnId));
                    }
                }
            }

            //如果剩余数量 jobProcessListRemainderCount == 0 则更新数据库记录
            //设置successCount，failureCount，processJobList 最后返回
            JobRunQueueInfo jrqi = new JobRunQueueInfo();
            jrqi.setId(info.getId());
            jrqi.setCurrentPorcessId(parseJobPairList(list));
            jrqi.setSuccessList(idListToString(successList));
            jrqi.setFailureList(idListToString(failureList));
            //检查整个队列是否执行完了，并根据 jobFailureCount 设置整个结果是成功或失败
            if ((successList.size() + failureList.size()) >= info.getJobCount()) {
                if (failureList.size() == 0) {
                    jrqi.setQueueState(JobRunQueueState.SUCCEEDED);
                } else {
                    jrqi.setQueueState(JobRunQueueState.FAILED);
                }
                sendMailByAsynchronous(jrqi);
            } else {
                jrqi.setQueueState(JobRunQueueState.PROCESSING);
            }
            logger.info("[JobRunQueueListener]executeProcessJobList JobRunQueueInfo ->" + jrqi.toString());
            service.modifyJobRunQueueInfo(jrqi);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        //logger.info("[JobRunQueueListener]executeProcessJobList end");
    }


    /**
     * 检查每个Job运行队列的执行id情况，[job_id]-[job_execution_id]
     * 对比 job_count，已经完成的数量，正在running的列表，就知道这个队列完成情况了
     * 如果完成的job有失败的 failure_count > 0，那么这个队列剩余的没执行的就不再运行了，将运行列表中的任务执行
     * 完就结束，将running列表的都执行完之后 修改任务状态为 DONE_FAILURE
     * 如果本次检查有runnning的任务中的一个执行完了，检查状态，如果是成功了，就更新成功数量，失败了就更新
     * 失败数量，如果全部执行完后，整个队列中的任务也执行完了，则根据是否有失败情况标记为 DONE 或者 SUCCESS
     * 如果列表中的一个任务运行完了，则将全局的 con_current_job_count 数量减 1
     */
    public void processRunningJob() {
        //logger.info("[JobRunQueueListener]processRunningJob start");
        //JobRunQueueService service = DataLinkFactory.getObject(JobRunQueueService.class);
        List<JobRunQueueInfo> list = new ArrayList<>();
        list = service.getAllJobRunQueueInfoByState(JobRunQueueState.PROCESSING);
        currentProcessJobQueueNum = list.size();
        for (JobRunQueueInfo i : list) {
            executeProcessJobList(i);
        }
        //logger.info("[JobRunQueueListener]processRunningJob end");
    }


    /**
     * 处理所有处于STOP状态的队列
     */
    public void processStopJob() {
        //logger.info("[JobRunQueueListener]processStopJob start");
        //JobRunQueueService service = DataLinkFactory.getObject(JobRunQueueService.class);
        //JobService jobService = DataLinkFactory.getObject(JobService.class);
        List<JobRunQueueInfo> list = service.getAllJobRunQueueInfoByState(JobRunQueueState.STOP);
        for (JobRunQueueInfo i : list) {
            String processJobList = i.getCurrentPorcessId();
            if (StringUtils.isBlank(processJobList)) {
                continue;
            }

            List<JobPair> successList = idsStringToList(i.getSuccessList());
            List<JobPair> failureList = idsStringToList(i.getFailureList());
            try {
                List<JobPair> processList = new CopyOnWriteArrayList<>();
                String[] jobList = processJobList.split(",");
                if (jobList == null || jobList.length == 0) {
                    return;
                }
                int successCount = 0;
                int failureCount = 0;
                for (JobPair jp : processList) {
                    JobExecutionInfo jei = jobService.getJobExecutionById(jp.jobExecutionId);
                    if (null == jei) {
                        processList.remove(jp);
                    }
                    //如果job execution的状态是成功状态则 successCount +1
                    if (JobExecutionState.SUCCEEDED.equals(jei.getState())) {
                        successCount++;
                        successList.add(jp);
                        processList.remove(jp);
                    }
                    if (JobExecutionState.ABANDONED.equals(jei.getState()) || JobExecutionState.FAILED.equals(jei.getState()) || JobExecutionState.KILLED.equals(jei.getState())) {
                        failureCount++;
                        failureList.add(jp);
                        processList.remove(jp);
                    }
                }
                //更新 processJobList
                String processListStr = parseJobPairList(processList);
                JobRunQueueInfo newInfo = new JobRunQueueInfo();
                newInfo.setId(i.getId());
                newInfo.setCurrentPorcessId(processListStr);
                newInfo.setSuccessList(idListToString(successList));
                newInfo.setFailureList(idListToString(failureList));
                newInfo.setQueueState(JobRunQueueState.STOP);
                service.modifyJobRunQueueInfo(newInfo);
                logger.info("[JobRunQueueListener]processStopJob JobRunQueueInfo ->" + newInfo.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //logger.info("[JobRunQueueListener]processStopJob end ");
        }
    }


    /**
     * 处理所有处于 UNEXECUTE 状态的队列
     */
    public void processReadyJob() {
        //logger.info("[JobRunQueueListener]processUnexecuteJob start");
        if (currentProcessJobQueueNum == 0) {
            availablePorcessCount = CONCURRENT_RUN_JOB_NUM;
        }
        if (availablePorcessCount <= 0) {
            return;
        }
        try {
            //JobRunQueueService service = DataLinkFactory.getObject(JobRunQueueService.class);
            List<JobRunQueueInfo> list = service.getAllJobRunQueueInfoByState(JobRunQueueState.READY);
            for (JobRunQueueInfo info : list) {
                String[] idList = info.getJobIdList().split(",");
                List<JobPair> jobPairList = new ArrayList<>();
                for (String s : idList) {
                    //s就是job config的id，启动这个任务
                    long jobConfigId = Long.parseLong(s);
                    long executionId = scheduleJob(jobConfigId);
                    if (-1 == executionId) {
                        continue;
                    }
                    JobPair jp = new JobPair(jobConfigId, executionId);
                    jobPairList.add(jp);
                    availablePorcessCount--;
                    if (availablePorcessCount == 0) {
                        break;
                    }
                }
                if (jobPairList.size() > 0) {
                    String processIdList = parseJobPairList(jobPairList);
                    info.setCurrentPorcessId(processIdList);
                    info.setQueueState(JobRunQueueState.PROCESSING);
                    service.modifyJobRunQueueInfo(info);
                    logger.info("[JobRunQueueListener]processUnexecuteJob JobRunQueueInfo ->" + info.toString());
                }
                if (availablePorcessCount == 0) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        //logger.info("[JobRunQueueListener]processUnexecuteJob end");
    }

    /**
     * 根据id列表，返回id的数量
     *
     * @param idList
     * @return
     */
    private int getListIdCount(String idList) {
        if (StringUtils.isBlank(idList)) {
            return 0;
        }
        return idList.split(",").length;
    }


    private JobPair toJobPair(String str) {
        return new JobPair(Long.parseLong(str.split("-")[0]), Long.parseLong(str.split("-")[1]));
    }

    /**
     * 将id的string列表转为list列表
     *
     * @param idsString
     * @return
     */
    private List<JobPair> idsStringToList(String idsString) {
        List<JobPair> list = new ArrayList<>();
        if (StringUtils.isBlank(idsString)) {
            return list;
        }
        String[] arr = idsString.split(",");
        for (String s : arr) {
            JobPair jp = new JobPair(Long.parseLong(s.split("-")[0]), Long.parseLong(s.split("-")[1]));
            list.add(jp);
        }
        return list;
    }

    private String idListToString(List<JobPair> idList) {
        if (idList == null || idList.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (JobPair s : idList) {
            sb.append(s.toString()).append(",");
        }
        if (sb.length() > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 将 job pair列表变成 "10-123,11-124,17-125,99-126" 这样的字符串
     *
     * @param jobPairList
     * @return
     */
    private String parseJobPairList(List<JobPair> jobPairList) {
        if (jobPairList == null || jobPairList.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (JobPair jp : jobPairList) {
            sb.append(jp.toString()).append(",");
        }
        if (sb.length() > 0) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 根据job_id列表，以及正在处理的job_id列表，计算出剩余可以运行的job_id
     * 如job_ld列表为 1,2,3,4,5,6
     * 正在处理的列表为1-101,2-102,3-107,4-109
     * 那么剩余可用的job_id就是5,6
     *
     * @param jobIdList
     * @param processJobList
     * @return
     */
    private List<String> avaliableProcessJob(String jobIdList, String processJobList) {
        Set<String> jobIdSet = new CopyOnWriteArraySet<>();
        Set<String> processSet = new HashSet<>();
        String[] idList = jobIdList.split(",");
        for (String s : idList) {
            jobIdSet.add(s);
        }

        String[] pList = processJobList.split(",");
        for (String s : pList) {
            processSet.add(s.split("-")[0]);
        }

        for (String s : processSet) {
            if (jobIdSet.contains(s)) {
                jobIdSet.remove(s);
            }
        }
        //最终剩下的就是不包含 process job list，也就是不在处理的job id 列表
        List<String> list = new ArrayList<>(jobIdSet);
        return list;
    }


    private void sendMailByAsynchronous(JobRunQueueInfo info) {
        new Thread(new Runnable() {
            public void run() {
                sendMail(info.getId());
            }
        }, "job-run-queue_send-mail").start();
    }

    private void sendMail(long queue_id) {
        logger.info("job队列执行完毕，队列ID："+ queue_id);
    }

    /**
     * job_id和job_execution_id对
     */
    public static class JobPair {
        long jobConfigId;
        long jobExecutionId;

        public JobPair(long jobConfigId, long jobExecutionId) {
            this.jobConfigId = jobConfigId;
            this.jobExecutionId = jobExecutionId;
        }

        @Override
        public String toString() {
            return jobConfigId + "-" + jobExecutionId;
        }
    }

}
