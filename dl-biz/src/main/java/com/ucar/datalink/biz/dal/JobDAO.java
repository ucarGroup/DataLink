package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobExecutionInfo;
import com.ucar.datalink.domain.job.JobExecutionMonitor;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by user on 2017/7/13.
 */
public interface JobDAO {

    //   job config 相关操作
    /**
     * 根据id查询一个指定的job
     * @param id
     * @return
     */
    JobConfigInfo getJobConfigById(long id);


    /**
     * 根据id获取job的名称
     * @param id
     * @return
     */
    String getJobConfigNameById(long id);


    /**
     * 根据name查询job的id
     * @param job_name
     * @return
     */
    Long getJobConfigIDByName(String job_name);

    /**
     * 根据传入的media source id查询关联的数据源，id可能是源端也可能是目标端的
     * @param mid
     * @return
     */
    List<JobConfigInfo> getJobConfigByMediaSourceID(long mid);

    /**
     * 插入一个JobConfigInfo对象
     * @param info
     */
    long addJobConfig(JobConfigInfo info);

    /**
     * 逻辑删除一个job任务
     * @param id
     */
    void deleteJobConfigById(long id);

    /**
     * 目前一共有多少job(为被删除的job)
     * @return
     */
    int getJobConfigCount();


    /**
     * 修改job内容
     * @param info
     */
    void updageJobConfig(JobConfigInfo info);

    /**
     * 修改job关联的src_media_id和target_media_id
     * @param info
     */
    void updateRelationMediaSource(JobConfigInfo info);

    /**
     * 更新一个job的内容
     * @param info
     */
    void updateJobConfigContent(JobConfigInfo info);

    /**
     * 分页查询
     * @param start 查询的起始位置
     * @param count 查询的数量
     * @return
     */
    List<JobConfigInfo> selectBtachJobConfig(int start,int count);


    /**
     * 根据源端和目标端类型进行分页查询
     * @param srcType
     * @param destType
     * @return
     */
    List<JobConfigInfo> selectBatchJobConfigByDBType(@Param("srcType")String srcType, @Param("destType")String destType, @Param("srcName")String srcName,
    @Param("destName")String destName, @Param("mediaName")String mediaName,@Param("jobName")String jobName,
    @Param("applyID")long applyID, @Param("applyUserId")long applyUserId, @Param("isTiming")int isTiming,
    @Param("configId") long configId,@Param("srcTableName")String srcTableName, @Param("targetTableName")String targetTableName);



    /**
     * 检查定时器是否配置重复了
     * @param srcId
     * @param destId
     * @param mediaName
     * @return
     */
    int timingDupCheck(long srcId, long destId,String mediaName);


    JobConfigInfo latestJobConfigRecord();

    JobConfigInfo lastJobConfigByName(String jobName);

    List<JobConfigInfo> allTimingJob();

    List<JobConfigInfo> allTinmingJobWithoutMonitor();

    List<JobConfigInfo> allTinmingJobInMonitor();


    List<JobExecutionInfo> allRunningJob();

    // job execution 相关操作
    /**
     * 根据id获取一个Job执行信息
     * @param id
     * @return
     */
    JobExecutionInfo getJobExecutionById(long id);

    /**
     * 插入一个job执行信息
     * @param info
     */
    void addJobExecution(JobExecutionInfo info);

    /**
     * 更新一个job执行信息
     * @param info
     */
    void updateJobExecution(JobExecutionInfo info);


    /**
     * 更新任务状态
     * @param state
     * @param id
     */
    void updateJobExecutionState(String state,String errMsg,long id);

    /**
     * 更新运行job的状态
     * @param state
     * @param errMsg
     * @param id
     */
    void updateJobExecutionStateForRunningJob(String state,String errMsg,long id);

    /**
     * 目前一共有多少job执行信息
     * @return
     */
    int getJobExecutionCount();

    /**
     * 分页查询job执行信息
     * @param start 查询的起始位置
     * @param count 查询的数量
     * @return
     */
    List<JobExecutionInfo> selectBtachJobExecutioin(int start, int count);

    /**
     * 根据任务的状态分页查询
     * @return
     */
    List<JobExecutionInfo> selectBatchJobExecutionByStateType(@Param("stateType")String stateType,@Param("job_id")long job_id,
@Param("srcType")String srcType, @Param("srcName")String srcName,@Param("destType")String destType,@Param("destName")String destName,
@Param("mediaName")String mediaName, @Param("startTime")String startTime, @Param("endTime")String endTime);

    /**
     * 这个函数是 selectBatchJobExecutionByStateType 的优化后的版本
     * 原先的实现方式是用in，现在改为inner join
     * @param stateType job运行状态
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
    List<JobExecutionInfo> selectBatchJobExecutionByOptimized(@Param("stateType")String stateType,
    @Param("job_id")long job_id, @Param("srcType")String srcType, @Param("srcName")String srcName,
    @Param("destType")String destType,@Param("destName")String destName, @Param("mediaName")String mediaName,
    @Param("startTime")String startTime, @Param("endTime")String endTime);

    /**
     * 获取最近创建的一个job历史信息
     * @param id
     * @param start_time
     * @param end_time
     * @return
     */
    JobExecutionInfo getJustCreatedJobExecution(long id, Timestamp start_time, Timestamp end_time);

    /**
     * 根据job_config的id获取最新创建的job运行历史信息(只获取最近一条)
     * @param id
     * @return
     */
    JobExecutionInfo lastExecuteJobExecutionInfo(long id);

    /**
     * 取最近一条记录优化版
     * @param id
     * @return
     */
    JobExecutionInfo lastExecuteJobExecutionInfoByOptimized(long id);

    /**
     * 根据job config的id获取最近创建的一条执行成功的job运行历史信息
     * @param id
     * @return
     */
    JobExecutionInfo lastSuccessExecuteJobExecutionInfo(long id);

    /**
     * 根据job id查询出所有运行的job
     * @param id
     * @return
     */
    List<JobExecutionInfo> getJobExecutionByJobId(long id);

    /**
     * 带分页的查询
     * @param id
     * @param startPage
     * @param count
     * @return
     */
    List<JobExecutionInfo> getJobExecutionByJobIdForPaging(long id,long startPage,long count);

    /**
     * 根据起始和结束时间，查找这段时间范围内的所有运行失败job
     * @param startTime
     * @param endTime
     * @return
     */
    List<JobExecutionInfo> getAllFailureJobByTimeFrame(Timestamp startTime, Timestamp endTime);

    /**
     * 根据同步申请id获取job列表
     * @param applyId
     * @return
     */
    List<JobConfigInfo> getJobConfigListByApplyId(long applyId);

    /**
     * 根据job config id查询最近的一个执行记录
     * @param jobConfigId
     * @return
     */
    JobExecutionInfo getJustJobExecutionByConfigId(long jobConfigId);


    List<JobExecutionMonitor> getAllFailureByMonitorCat(Timestamp startTime);

    List<JobConfigInfo> getJobsBySrcIdAndTargetIdAndTable(@Param("srcMediaSourceIdList") List<Long> srcMediaSourceIdList, @Param("targetId")Long targetId,@Param("targetTableName") String targetTableName);
}
