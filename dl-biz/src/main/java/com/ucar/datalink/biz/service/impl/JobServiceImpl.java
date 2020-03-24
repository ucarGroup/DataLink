package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.JobDAO;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.flinker.FlinkerJobUtil;
import com.ucar.datalink.biz.utils.flinker.job.JobConfigBuilder;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.JobExecutionMonitor;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;


/**
 * Created by user on 2017/7/13.
 */
@Service
public class JobServiceImpl implements JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    @Autowired
    JobDAO jobDAO;

    @Autowired
    MediaSourceService mediaSourceService;

    // job config相关操作
    @Override
    public JobConfigInfo getJobConfigById(long id) {
        JobConfigInfo info = jobDAO.getJobConfigById(id);
        return info;
    }

    @Override
    public String getJobConfigNameById(long id) {
        return jobDAO.getJobConfigNameById(id);
    }

    @Override
    public long getJobConfigIDByName(String job_name) {
        Long value = jobDAO.getJobConfigIDByName(job_name);
        if(value == null) {
            return -1;
        }
        return value.longValue();
    }

    @Override
    public List<JobConfigInfo> getJobConfigByMediaSourceID(long mid) {
        return jobDAO.getJobConfigByMediaSourceID(mid);
    }

    @Override
    public void createJobConfig(JobConfigInfo info) {
        jobDAO.addJobConfig(info);
    }

    @Override
    public void deleteJobConfigById(long id) {
        jobDAO.deleteJobConfigById(id);
    }

    @Override
    public int getJobConfigCount() {
        return jobDAO.getJobConfigCount();
    }

    @Override
    public void modifyJobConfig(JobConfigInfo info) {
        jobDAO.updageJobConfig(info);
    }

    @Override
    public void modifyRelationMediaSource(JobConfigInfo info) {
        jobDAO.updateRelationMediaSource(info);
    }

    @Override
    public void modifyJobConfigContent(JobConfigInfo info) {
        jobDAO.updateJobConfigContent(info);
    }

    @Override
    public void roloadJobsByMediaSourceId(Long mediaSourceId) {
        List<JobConfigInfo> jobConfigInfoList = jobDAO.getJobConfigByMediaSourceID(mediaSourceId);
        for (JobConfigInfo info : jobConfigInfoList) {
            logger.info("reload job_config_info->"+info.toString());
            long srcID = info.getJob_src_media_source_id();
            long destID = info.getJob_target_media_source_id();
            MediaSourceInfo srcInfo = mediaSourceService.getById(srcID);
            MediaSourceInfo destInfo = mediaSourceService.getById(destID);
            if(info.isTiming_yn()) {
                String json = JobConfigBuilder.reload(info, srcInfo, destInfo);
                json = FlinkerJobUtil.formatJson(json);
                info.setJob_content(json);
                jobDAO.updateJobConfigContent(info);
            }
        }
    }

    @Override
    public List<JobConfigInfo> queryJobConfigsByPaging(int start, int count) {
        return jobDAO.selectBtachJobConfig(start,count);
    }

    @Override
    public List<JobConfigInfo> queryJobConfigDBTypeByPaging(String srcType,String destType,String srcName,String destName, String mediaName,String jobName,long applyID,long applyUserId,String isTiming,long configId,String srcTableName,String targetTableName) {
        if("all".equalsIgnoreCase(isTiming)) {
            return jobDAO.selectBatchJobConfigByDBType(srcType,destType,srcName,destName,mediaName,jobName,applyID,applyUserId,0,configId,srcTableName,targetTableName);
        }
        else if("true".equalsIgnoreCase(isTiming)) {
            return jobDAO.selectBatchJobConfigByDBType(srcType,destType,srcName,destName,mediaName,jobName,applyID,applyUserId,1,configId,srcTableName,targetTableName);
        }
        else {
            return jobDAO.selectBatchJobConfigByDBType(srcType,destType,srcName,destName,mediaName,jobName,applyID,applyUserId,-1,configId,srcTableName,targetTableName);
        }

    }

    @Override
    public int timingDupCheck(long srcId, long destId,String mediaName) {
        return jobDAO.timingDupCheck(srcId,destId,mediaName);
    }

    @Override
    public JobConfigInfo latestJobConfigRecord() {
        return jobDAO.latestJobConfigRecord();
    }

    @Override
    public JobConfigInfo lastJobConfigByName(String jobName) {
        return jobDAO.lastJobConfigByName(jobName);
    }

    @Override
    public List<JobConfigInfo> allTimingJob() {
        return jobDAO.allTimingJob();
    }

    @Override
    public List<JobConfigInfo> allTinmingJobWithoutMonitor() {
        return jobDAO.allTinmingJobWithoutMonitor();
    }

    @Override
    public List<JobConfigInfo> allTinmingJobInMonitor() {
        return jobDAO.allTinmingJobInMonitor();
    }

    @Override
    public List<JobExecutionInfo> allRunningJob() {
        return jobDAO.allRunningJob();
    }


    // job 执行信息相关操作
    @Override
    public JobExecutionInfo getJobExecutionById(long id) {
        return jobDAO.getJobExecutionById(id);
    }

    @Override
    public void createJobExecution(JobExecutionInfo info) {
        jobDAO.addJobExecution(info);
    }

    @Override
    public void modifyJobExecution(JobExecutionInfo info) {
        jobDAO.updateJobExecution(info);
    }


    @Override
    public void updateJobExecutionStateForRunningJob(String state,String errMsg,long id) {
        jobDAO.updateJobExecutionStateForRunningJob(state,errMsg,id);
    }

    @Override
    public void modifyJobExecutionState(String state,String errMsg,long id) {
        jobDAO.updateJobExecutionState(state,errMsg,id);
    }

    @Override
    public int getJobExecutionCount() {
        return jobDAO.getJobExecutionCount();
    }

    @Override
    public List<JobExecutionInfo> queryJobExecutionByPaging(int start, int count) {
        return jobDAO.selectBtachJobExecutioin(start, count);
    }

    @Override
    public List<JobExecutionInfo> queryJobExecutionStateByPaging(String state, long job_id, String srcType, String srcName,
    String destType,String destName,String mediaName,String startTime,String endTime) {
        return jobDAO.selectBatchJobExecutionByStateType(state, job_id,srcType, srcName, destType,destName,mediaName,startTime,endTime);
    }

    @Override
    public List<JobExecutionInfo> queryJobExecutionStateByOptimized(String state, long job_id, String srcType, String srcName,
                                                                 String destType,String destName,String mediaName,String startTime,String endTime) {
        return jobDAO.selectBatchJobExecutionByOptimized(state, job_id,srcType, srcName, destType,destName,mediaName,startTime,endTime);
    }



    @Override
    public JobExecutionInfo getJustCreatedJobExecution(long id, Timestamp start_time, Timestamp end_time) {
        return jobDAO.getJustCreatedJobExecution(id,start_time,end_time);
    }

    @Override
    public JobExecutionInfo lastExecuteJobExecutionInfo(long id) {
        return jobDAO.lastExecuteJobExecutionInfo(id);
    }

    @Override
    public JobExecutionInfo lastSuccessExecuteJobExecutionInfo(long id) {
        return jobDAO.lastSuccessExecuteJobExecutionInfo(id);
    }

    @Override
    public JobExecutionInfo lastExecuteJobExecutionInfoByOptimized(long id) {
        return jobDAO.lastExecuteJobExecutionInfoByOptimized(id);
    }

    @Override
    public List<JobExecutionInfo> getJobExecutionByJobId(long id) {
        List<JobExecutionInfo> list = jobDAO.getJobExecutionByJobId(id);
        for(JobExecutionInfo info : list) {
            info.setOriginal_configuration(null);
        }
        return list;
    }

    @Override
    public List<JobExecutionInfo> getJobExecutionByJobId(long id,long startPage,long count) {
        return jobDAO.getJobExecutionByJobIdForPaging(id,startPage,count);
    }


    @Override
    public List<JobExecutionInfo> getAllFailureJob(Timestamp startTime, Timestamp endTime) {
        return jobDAO.getAllFailureJobByTimeFrame(startTime,endTime);
    }

    @Override
    public List<JobConfigInfo> getJobConfigListByApplyId(long applyId) {
        return jobDAO.getJobConfigListByApplyId(applyId);
    }

    @Override
    public JobExecutionInfo getJustJobExecutionByConfigId(long jobConfigId) {
        return jobDAO.getJustJobExecutionByConfigId(jobConfigId);
    }

    @Override
    public List<JobExecutionMonitor> getAllFailureByMonitorCat(Timestamp startTime) {
        return jobDAO.getAllFailureByMonitorCat(startTime);
    }

    @Override
    public List<JobConfigInfo> getJobsBySrcIdAndTargetIdAndTable(List<Long> srcMediaSourceIdList, Long targetId, String targetTableName) {
        return jobDAO.getJobsBySrcIdAndTargetIdAndTable(srcMediaSourceIdList,targetId,targetTableName);
    }
}
