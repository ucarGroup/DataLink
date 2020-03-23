package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.JobExecutionMonitor;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by user on 2017/7/13.
 */
public interface JobService {

    //  job config 相关操作
    /**
     * 根据id查询一个指定的JobConfigInfo对象
     * @param id
     * @return
     */
    public JobConfigInfo getJobConfigById(long id);

    /**
     * 根据id获取job的名称
     * @param id
     * @return
     */
    public String getJobConfigNameById(long id);

    /**
     * 根据任务名称查询job的id
     * @param job_name
     * @return
     */
    public long getJobConfigIDByName(String job_name);

    /**
     * 根据传入的media source id查询关联的数据源，id可能是源端也可能是目标端的
     * @param mid
     * @return
     */
    public List<JobConfigInfo> getJobConfigByMediaSourceID(long mid);

    /**
     * 创建一个JobConfigInfo对象
     * @param info
     */
    public void createJobConfig(JobConfigInfo info);

    /**
     * 逻辑删除一个JobConfig，将isDelete标志位设置为true
     * @param id
     */
    public void deleteJobConfigById(long id);

    /**
     * 返回job的数量(未被删除的job)
     * @return
     */
    public int getJobConfigCount();

    /**
     * 修改一个job的内容
     * @param info
     */
    public void modifyJobConfig(JobConfigInfo info);

    /**
     * 修改job关联的src_media_id和target_media_id
     * @param info
     */
    public void modifyRelationMediaSource(JobConfigInfo info);

    /**
     * 修改一个job的任务内容，也就是job_content这个字段，其他不动
     * @param info
     */
    public void modifyJobConfigContent(JobConfigInfo info);

    /**
     * 根据数据源reload源端/目标端为该数据源的定时任务
     * @param mediaSourceId
     */
    void roloadJobsByMediaSourceId(Long mediaSourceId);

    /**
     * 分页查询Job配置
     * @param start 查询的起始位置
     * @param count 一次查询的数量
     * @return
     */
    public List<JobConfigInfo> queryJobConfigsByPaging(int start, int count);


    /**
     * 按条件分页查询
     * @param srcType
     * @param destType
     * @param srcName
     * @param destName
     * @param mediaName
     * @param jobName
     * @return
     */
    public List<JobConfigInfo> queryJobConfigDBTypeByPaging(String srcType,String destType,String srcName,String destName,
        String mediaName,String jobName,long applyID,long applyUserId,String isTiming,long configId, String srcTableName, String targetTableName);


    /**
     * 检查定时器是否配置重复了
     * @param srcId
     * @param destId
     * @param mediaName
     * @return
     */
    public int timingDupCheck(long srcId, long destId,String mediaName);


    public JobConfigInfo latestJobConfigRecord();

    public JobConfigInfo lastJobConfigByName(String jobName);

    public List<JobConfigInfo> allTimingJob();

    public List<JobConfigInfo> allTinmingJobWithoutMonitor();

    public List<JobConfigInfo> allTinmingJobInMonitor();

    public List<JobExecutionInfo> allRunningJob();





    //job 执行信息相关操作
    /**
     * 根据id获取一个job执行信息
     * @param id
     * @return
     */
    public JobExecutionInfo getJobExecutionById(long id);

    /**
     * 创建一个job执行信息
     * @param info
     */
    public void createJobExecution(JobExecutionInfo info);


    /**
     * 更新job执行信息
     * @param info
     */
    public void modifyJobExecution(JobExecutionInfo info);


    /**
     * 更新运行时job的状态
     * @param state
     * @param errMsg
     * @param id
     */
    public void updateJobExecutionStateForRunningJob(String state,String errMsg,long id);

    /**
     * 更新任务的状态
     * @param state
     * @param id
     */
    public void modifyJobExecutionState(String state,String errMsg,long id);

    /**
     * 返回job执行信息的数量
     * @return
     */
    public int getJobExecutionCount();

    /**
     * 分页查询job执行信息
     * @param start 查询的起始位置
     * @param count 查询的数量
     * @return
     */
    public List<JobExecutionInfo> queryJobExecutionByPaging(int start, int count);

    /**
     * 根据任务状态分页查询
     * @param state
     * @return
     */
    public List<JobExecutionInfo> queryJobExecutionStateByPaging(String state, long job_id, String srcType, String srcName,
    String destType,String destName,String mediaName,String startTime,String endTime);

    /**
     * 这个函数是 queryJobExecutionStateByPaging 的优化后的版本
     * 原先的实现方式是用in，现在改为inner join
     * @param state job运行状态
     * @param job_id 关联的jobConfig表的id
     * @param srcType 源端类型
     * @param srcName 源端名称
     * @param destType 目标端类型
     * @param destName 目标端名称
     * @param mediaName 表名称
     * @param startTime 任务的起始时间
     * @param endTime   任务的结束时间
     * @return JobExecutionInfo 集合
     */
    public List<JobExecutionInfo> queryJobExecutionStateByOptimized(String state, long job_id, String srcType, String srcName,
    String destType,String destName,String mediaName,String startTime,String endTime);


    /**
     * 获取刚刚创建的job execution
     * @param id
     * @param start_time
     * @param end_time
     * @return
     */
    public JobExecutionInfo getJustCreatedJobExecution(long id, Timestamp start_time, Timestamp end_time);


    /**
     * 根据job config的id获取最近创建的一条job运行历史信息
     * @param id
     * @return
     */
    public JobExecutionInfo lastExecuteJobExecutionInfo(long id);

    /**
     * 根据job config的id获取最近创建的一条执行成功的job运行历史信息
     * @param id
     * @return
     */
    public JobExecutionInfo lastSuccessExecuteJobExecutionInfo(long id);

    /**
     * 取最近一条记录优化版
     * @param id
     * @return
     */
    public JobExecutionInfo lastExecuteJobExecutionInfoByOptimized(long id);

    /**
     * 根据job id查询出所有运行的job历史信息
     * @param id
     * @return
     */
    public List<JobExecutionInfo> getJobExecutionByJobId(long id);

    /**
     * 根据job id查询关联的所有记录，并分页
     * @param id
     * @param startPage
     * @param count
     * @return
     */
    public List<JobExecutionInfo> getJobExecutionByJobId(long id,long startPage,long count);

    /**
     * 根据起始和结束时间，获取所有运行失败的job
     * @param startTime
     * @param endTime
     * @return
     */
    public List<JobExecutionInfo> getAllFailureJob(Timestamp startTime, Timestamp endTime);

    /**
     * 根据同步申请id获取job列表
     * @param applyId
     * @return
     */
    public List<JobConfigInfo> getJobConfigListByApplyId(long applyId);


    /**
     * 根据job config id查询最近的一个执行记录
     * @param jobConfigId
     * @return
     */
    public JobExecutionInfo getJustJobExecutionByConfigId(long jobConfigId);


    public List<JobExecutionMonitor> getAllFailureByMonitorCat(Timestamp startTime);

    List<JobConfigInfo> getJobsBySrcIdAndTargetIdAndTable(List<Long> srcMediaSourceIdList, Long targetId, String targetTableName);
}
;