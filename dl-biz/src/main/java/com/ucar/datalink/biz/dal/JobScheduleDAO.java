package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobScheduleInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by yang.wang09 on 2018-07-19 20:21.
 */
public interface JobScheduleDAO {

    JobScheduleInfo getById(Long id);

    List<JobScheduleInfo> getByConfigId(Long id);

    List<JobScheduleInfo> getList();

    long insert(JobScheduleInfo info);

    void update(JobScheduleInfo info);

    void updateByJobConfig(JobScheduleInfo info);

    void delete(Long id);

    void deleteByJobConfigId(Long id);

    List<JobScheduleInfo> getJobScheduleByConfigId(Long id);

    void updateState(JobScheduleInfo info);

    List<JobScheduleInfo> selectBatchJobSchedule(@Param("srcType")String srcType, @Param("destType")String destType, @Param("srcName")String srcName,
    @Param("destName")String destName, @Param("mediaName")String mediaName,@Param("jobName")String jobName,@Param("isTiming")int isTiming,
    @Param("scheduleState")int scheduleState);

    JobConfigInfo latestJobScheduleRecord();

}
