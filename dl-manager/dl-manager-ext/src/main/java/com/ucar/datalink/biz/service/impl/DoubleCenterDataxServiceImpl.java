package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.job.JobConfigBuilder;
import com.ucar.datalink.biz.module.RunningData;
import com.ucar.datalink.biz.service.DoubleCenterDataxService;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MediaSourceRelationService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.job.*;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceRelationInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.util.DataxUtil;
import com.ucar.datalink.util.VirtualDataSourceUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by yang.wang09 on 2018-11-01 16:46.
 */
@Service
class DoubleCenterDataxServiceImpl implements DoubleCenterDataxService {

    private static final Logger logger = LoggerFactory.getLogger(DoubleCenterDataxServiceImpl.class);

    @Autowired
    JobService jobService;

    @Autowired
    MediaSourceRelationService relationService;

    @Autowired
    MediaSourceService mediaSourceService;

    @Autowired
    MediaSourceRelationService mediaSourceRelationService;

    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);


    public DoubleCenterDataxServiceImpl() {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            if(executorService != null) {
                executorService.shutdown();
            }
        }));
    }

    /**
     * 修改所有定时任务的job，将定时任务关联的虚拟节点，换成真实的节点
     * @throws Exception
     */
    @Override
    public void switchDataSourceForAllTimingJob() {
        List<JobConfigInfo> jobConfigInfoList = jobService.allTimingJob();
        logger.info("prepare switch datax job data_source,total job -> "+jobConfigInfoList.size());
        List<Future> futures = new ArrayList<>();
        jobConfigInfoList.forEach(jobConfigInfo -> {
            Future f = executorService.submit(new JobFutureTask(jobConfigInfo));
            futures.add(f);
        });//end forEach
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                logger.info(e.getMessage(),e);
                throw new RuntimeException(e.getMessage());
            }
        });
    }

    /**
     * 双中心二期新添加的功能，根据指定的数据源，切换job的内容
     * @param willSwitchedMediaSourceIdList
     */
    @Override
    public void switchDataSourceForSpecifiedTimingJob(List<Long> willSwitchedMediaSourceIdList) {
        List<JobConfigInfo> allTimingJobList = jobService.allTimingJob();
        if(allTimingJobList==null || allTimingJobList.size()==0) {
            return;
        }
        logger.info("prepare switch datax job data_source,total timing job list -> "+allTimingJobList.size());
        Set<Long> willSwitchedMediaSourceIdSet = new HashSet<>(willSwitchedMediaSourceIdList);
        List<JobConfigInfo> willSwitchedJobConfigList = new ArrayList<>();
        List<Future> waitingJobFinishList = new ArrayList<>();

        //遍历所有的定时job，如果定时job的src_media_source_id，或者job的target_media_source_id 等于指定的数据源
        //则将这个job放到待切换的列表中
        allTimingJobList.forEach(i->{
            Long srcMediaSourceId = i.getJob_src_media_source_id();
            Long targetMediaSourceId = i.getJob_target_media_source_id();
            if(willSwitchedMediaSourceIdSet.contains(srcMediaSourceId) || willSwitchedMediaSourceIdSet.contains(targetMediaSourceId)) {
                willSwitchedJobConfigList.add(i);
            }
        });

        //切换指定的job
        willSwitchedJobConfigList.forEach(i->{
            Future f = executorService.submit(new JobFutureTask(i));
            waitingJobFinishList.add(f);
        });
        //等待所有的线程执行完，也就是等待所有的job内容切换完成
        waitingJobFinishList.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                throw new RuntimeException(e);
            }
        });
    }



    /**
     * 一键改造旧数据源
     * 获取所有定时任务的job，非定时的job很多，而且都是一次性的就不用管了
     * 依次遍历这些定时job，如果某个job关联的src_media_source是真实的，并且在MediaSourceRelationInfo中有关联，
     * 则获取MediaSourceRelationInfo对应的虚拟id，将这个虚拟id赋给此job
     * 对于job关联的target_media_source也是一样的操作
     * 更新这个job
     */
    @Override
    public void oldDataSourceChangeToVirtual() {
        List<JobConfigInfo> jobConfigInfoList = jobService.allTimingJob();
        logger.info("prepare modify all source (old-> virtual) total all timing job -> "+jobConfigInfoList.size());
        jobConfigInfoList.forEach(jobConfigInfo -> {
            logger.info("switch job_config->" + jobConfigInfo.toString());
            Long mediaSrcId = jobConfigInfo.getJob_src_media_source_id();
            Long mediaTargetId = jobConfigInfo.getJob_target_media_source_id();
            MediaSourceRelationInfo srcRelationInfo = relationService.getOneByRealMsId(mediaSrcId);
            MediaSourceRelationInfo destRelationInfo = relationService.getOneByRealMsId(mediaTargetId);
            boolean hasChangeConfig = false;
            if(srcRelationInfo != null) {
                long virtualId = srcRelationInfo.getVirtualMsId();
                jobConfigInfo.setJob_src_media_source_id(virtualId);
                hasChangeConfig = true;
            }
            if(destRelationInfo != null) {
                long virtualId = destRelationInfo.getVirtualMsId();
                jobConfigInfo.setJob_target_media_source_id(virtualId);
                hasChangeConfig = true;
            }
            if( hasChangeConfig ) {
                jobService.modifyRelationMediaSource(jobConfigInfo);
            }
        });
    }


    /**
     * 停止所有正在运行的job
     */
    @Override
    public void stopAllRunningJob() {
        List<JobExecutionInfo> allRunningJobs = jobService.allRunningJob();
        logger.info("prepare stop all running job, total running job  -> " + allRunningJobs.size());
        List<Future> futures = new ArrayList<>();
        allRunningJobs.forEach(j -> {
            Future f = executorService.submit(new StopRunninigJobTask(j));
            futures.add(f);
        });
        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * 停止指定的数据源，双中切第二期新添加的功能
     * @param willStopedMediaSourceIdList 根据传入的数据源id列表，停止关联的运行job
     *                                         强杀: kill -9
     */
    @Override
    public void stopSpecifiedRunningJob(List<Long> willStopedMediaSourceIdList) {
        if(willStopedMediaSourceIdList==null || willStopedMediaSourceIdList.size()==0) {
            return;
        }
        logger.info("stop sepcified running job!");
        //遍历所有的 数据源id，找出对应的数据源
        Set<Long> willStopMediaSourceIdSet = new HashSet<>(willStopedMediaSourceIdList);
        List<Future> waitingJobFinishList = new ArrayList<>();
        List<JobExecutionInfo> allRunningJobExecutions = jobService.allRunningJob();
        List<JobExecutionInfo> willKilledJobExecutionList = new ArrayList<>();

        //遍历所有正在运行的job，如果这个job的src_media_source_id，或者是target_media_srouce_id 等于数据源的id
        //就将这个job运行记录，放到等待停止的列表中
        allRunningJobExecutions.forEach(j->{
            Long jobId = j.getJob_id();
            JobConfigInfo jobConfigInfo = jobService.getJobConfigById(jobId);
            //正常情况获取的 jobConfigInfo不应该为null，考虑可能有一些遗留问题，所以加了判断，如果此处的jobConfigInfo为null
            //则不会被加入到 将要被停止的job运行列表中
            if(jobConfigInfo == null) {
                logger.warn("perhaps it's legacy problem,getJobConofigById("+jobId+") is null");
            } else {
                Long srcMediaId = jobConfigInfo.getJob_src_media_source_id();
                Long targetMediaId = jobConfigInfo.getJob_target_media_source_id();
                //将要被停止的job运行列表
                if(willStopMediaSourceIdSet.contains(srcMediaId) || willStopMediaSourceIdSet.contains(targetMediaId)) {
                    willKilledJobExecutionList.add(j);
                }
            }
        });

        //遍历 将要被停止的job运行列表，创建一个待停止的任务对象 StopRunningJobTask，并将这个对象放到线程池中执行
        willKilledJobExecutionList.forEach(j->{
            Future f = executorService.submit(new StopRunninigJobTask(j));
            waitingJobFinishList.add(f);
        });
        //等待线程池中的任务执行完，也就是等待所有job停止
        waitingJobFinishList.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                throw new RuntimeException(e);
            }
        });

    }


    @Override
    public List<JobConfigInfo> checkAllDataSource() {
        List<JobConfigInfo> allJobs = jobService.allTimingJob();
        logger.info("prepare check all datasource, total job  -> " + allJobs.size());
        List<Future<JobConfigInfo>> futures = new ArrayList<>();
        allJobs.forEach(j->{
            Future<JobConfigInfo> f = executorService.submit(new JobCheckTask(j));
            futures.add(f);
        });
        List<JobConfigInfo> configInfos = new ArrayList<>();
        futures.forEach(f->{
            try {
                JobConfigInfo jobInfo = f.get();
                if(jobInfo.getId()>0) {
                    configInfos.add(jobInfo);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                //throw new RuntimeException(e);
            }
        });
        return configInfos;
    }


    private class JobCheckTask implements Callable<JobConfigInfo> {
        private final JobConfigInfo info;

        public JobCheckTask(JobConfigInfo info) {
            this.info = info;
        }

        @Override
        public JobConfigInfo call() throws Exception {
            logger.info("prepare check job content->"+info.toString());
            MediaSourceInfo mediaSourceInfoSrc = mediaSourceService.getById(info.getJob_src_media_source_id());
            MediaSourceInfo mediaSourceInfoDest = mediaSourceService.getById(info.getJob_target_media_source_id());
            boolean srcBool = true;
            boolean destBool = true;
            String jobContent = info.getJob_content();
            if(StringUtils.isBlank(jobContent) || !verifyJsonFormat(jobContent)) {
                return new JobConfigInfo();
            }

            //check job content is right as if media source type is VIRTUAL
            if(mediaSourceInfoSrc!=null && mediaSourceInfoSrc.getType()==MediaSourceType.VIRTUAL) {
                List<HostNodeInfo> srcHosts = JobConfigBuilder.srcHostList(info);
                MediaSourceInfo srcInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(info.getJob_src_media_source_id());
                List<HostNodeInfo> srcMediaHosts = JobConfigBuilder.parseSrcMediaSourceInfo(srcInfo);
                srcBool = JobConfigBuilder.compareSrcHostNodeInfos(srcInfo, srcHosts, srcMediaHosts);
            }
            if(mediaSourceInfoDest!=null && mediaSourceInfoDest.getType()==MediaSourceType.VIRTUAL) {
                List<HostNodeInfo> destHosts = JobConfigBuilder.destHostList(info);
                MediaSourceInfo destInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(info.getJob_target_media_source_id());
                List<HostNodeInfo> destMediaHosts = JobConfigBuilder.parseDestMediaSourceInfo(destInfo);
                destBool = JobConfigBuilder.compareDestHostNodeInfos(destInfo,destHosts,destMediaHosts);
            }
            if(!srcBool || !destBool) {
                logger.error("check job content illegal,jobconfig->"+info.toString());
                return info;
            } else {
                return new JobConfigInfo();
            }
        }
    }




    private class JobFutureTask implements Callable<Boolean> {
        private final JobConfigInfo jobConfigInfo;

        public JobFutureTask(JobConfigInfo jobConfigInfo) {
            this.jobConfigInfo = jobConfigInfo;
        }

        @Override
        public Boolean call() throws Exception {
            if(jobConfigInfo == null) {
                return Boolean.FALSE;
            }
            logger.info("switch job_config->"+jobConfigInfo.toString());
            Long mediaSrcId = jobConfigInfo.getJob_src_media_source_id();
            Long mediaTargetId = jobConfigInfo.getJob_target_media_source_id();
            boolean existSrcRelationInfo = relationService.checkExitOneByVritualMsId(mediaSrcId)>=1 ? true:false;
            boolean existDestRelationInfo = relationService.checkExitOneByVritualMsId(mediaTargetId)>=1 ? true:false;
            MediaSourceInfo srcMediaSourceInfo = null;
            MediaSourceInfo destMediaSourceInfo = null;

             if( existSrcRelationInfo ) {
                srcMediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(mediaSrcId);
            } else {
                srcMediaSourceInfo = mediaSourceService.getById(mediaSrcId);
            }
            if( existDestRelationInfo ) {
                destMediaSourceInfo = VirtualDataSourceUtil.getRealMediaSourceInfoById(mediaTargetId);
            } else {
                destMediaSourceInfo = mediaSourceService.getById(mediaTargetId);
            }

            if(existSrcRelationInfo || existDestRelationInfo) {
                try {
                    String json = JobConfigBuilder.reload(jobConfigInfo, srcMediaSourceInfo, destMediaSourceInfo);
                    jobConfigInfo.setJob_content(json);
                    jobService.modifyJobConfigContent(jobConfigInfo);
                }catch(Exception e) {
                    logger.error("reload job failure,job src->"+jobConfigInfo.getJob_src_media_source_id()+" dest->"+jobConfigInfo.getJob_target_media_source_id());
                    logger.error("src media->"+srcMediaSourceInfo.getType()+"  "+srcMediaSourceInfo.getId());
                    logger.error("dest media->"+destMediaSourceInfo.getType()+"  "+destMediaSourceInfo.getId());
                    logger.error(e.getMessage());
                    return Boolean.FALSE;
                }
            }
            return Boolean.TRUE;
        }
    }


    private class StopRunninigJobTask implements Callable<Boolean> {
        private final JobExecutionInfo executionInfo;

        public StopRunninigJobTask(JobExecutionInfo executionInfo) {
            this.executionInfo = executionInfo;
        }

        @Override
        public Boolean call() throws Exception {
            JobConfigInfo configInfo = jobService.getJobConfigById(executionInfo.getJob_id());
            //根据运行记录id，找不到job配置信息，实际上这种情况不会出现，但考虑到一些极端情况
            //如历史遗留问题，或者是job配置被删除了，但job却还在运行
            if(configInfo == null) {
                logger.error("perhaps it's legacy problem, execution->"+executionInfo.toString()+"\t but relational jobconfig is null");
                //return Boolean.FALSE;
            }
            DataxCommand command = new DataxCommand();
            command.setJobId(executionInfo.getJob_id());
            logger.info("stop job_config->" + configInfo.toString());

            command.setJobName(configInfo.getJob_name());
            command.setType(DataxCommand.Type.Stop);
            command.setForceStop(true);
            String json = JSONObject.toJSONString(command);
            //发送一个HTTP请求到 DataX服务器
            RunningData data = DataxUtil.getRunningData(configInfo.getJob_name());
            if(data==null || StringUtils.isBlank(data.getIp())) {
                return Boolean.TRUE;
            }
            String address = DataxUtil.forceStopURL(data.getIp());
            String result = URLConnectionUtil.retryPOST(address, json);
            int count = 0;
            boolean isError = false;
            while (true) {
                if (count > 10) {
                    throw new RuntimeException("force stop failure,count>10");
                }
                if (DataxUtil.isJobRunning(configInfo.getJob_name())) {
                    try {
                        Thread.sleep(1000);
                        count++;
                    } catch (InterruptedException e) {
                        throw new RuntimeException("force stop failure,by interrupted");
                    }
                } else {
                    jobService.modifyJobExecutionState(JobExecutionState.KILLED, "job in zk is stop " + configInfo.getJob_content(), configInfo.getId());
                    break;
                }
            }
            return Boolean.TRUE;
        }
    }

    private boolean verifyJsonFormat(String json) {
        try {
            Object obj = JSONObject.parse(json);
            if(obj instanceof String) {
                return false;
            }
            return true;
        }catch(Exception e) {
            return false;
        }
    }


    /**
     * 是否切机房中
     * @param mediaSourceId
     * @return
     */
    @Override
    public Boolean isSwitchLabIng(Long mediaSourceId){

        boolean exists = DLinkZkUtils.get().zkClient().exists(DLinkZkPathDef.labSwitchProcessing);
        if(exists){
            byte[] bytes = DLinkZkUtils.get().zkClient().readData(DLinkZkPathDef.labSwitchProcessing, true);
            Map map = JSONObject.parseObject(bytes,Map.class);
            String idStr = (String) map.get("virtualMsIdList");
            String[] idArr = idStr.split(",");
            for (String id : idArr){
                if(mediaSourceId.equals(Long.valueOf(id))){
                    return true;
                }
                List<MediaSourceRelationInfo> relationInfoList = mediaSourceRelationService.findListByVirtualId(Long.valueOf(id));
                for(MediaSourceRelationInfo info : relationInfoList){
                    if(mediaSourceId.equals(Long.valueOf(info.getRealMsId()))){
                        return true;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }


}
