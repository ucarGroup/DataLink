package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobScheduleInfo;

import java.util.List;

/**
 * Created by yang.wang09 on 2018-07-19 20:23.
 */
public interface JobScheduleService {

    JobScheduleInfo getById(Long id);

    List<JobScheduleInfo> getByConfigId(Long id);

    List<JobScheduleInfo> getList();

    List<JobScheduleInfo> selectBatchJobSchedule(String srcType,String destType,String srcName,
                                                 String destName, String mediaName,String jobName,int isTiming,
                                                 int scheduleState);


    long create(JobScheduleInfo info);

    void modify(JobScheduleInfo info);

    void modifyByJobConfig(JobScheduleInfo info);

    void remove(Long id);

    void removeByJobConfigId(Long id);

    List<JobScheduleInfo> getJobScheduleByConfigId(Long id);

    void modifyState(JobScheduleInfo info);

    public JobConfigInfo latestJobScheduleRecord();
}
