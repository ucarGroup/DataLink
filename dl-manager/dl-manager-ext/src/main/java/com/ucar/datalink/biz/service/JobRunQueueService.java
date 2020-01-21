package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.job.JobRunQueueInfo;

import java.util.List;

/**
 * Created by user on 2017/12/26.
 */
public interface JobRunQueueService {

    public void createJobRunQueueInfo(JobRunQueueInfo info);

    public void removeJobRunQueueInfo(long id);

    public void modifyJobRunQueueInfo(JobRunQueueInfo info);

    public void modifyJobRunQueueState(String state,long id);

    public void modifyJobRunueueTopTime(long id);

    public void modifyJobRunQueueConfig(JobRunQueueInfo info);

    public JobRunQueueInfo getJobRunQueueInfoById(long id);

    public List<JobRunQueueInfo> getAllJobRunQueueInfo();

    public List<JobRunQueueInfo> getAllJobRunQueueInfoByState(String state) ;

}
