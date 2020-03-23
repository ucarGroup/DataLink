package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.job.JobRunQueueInfo;

import java.util.List;

/**
 * Created by user on 2017/12/26.
 */
public interface JobRunQueueService {

     void createJobRunQueueInfo(JobRunQueueInfo info);

     void removeJobRunQueueInfo(long id);

     void modifyJobRunQueueInfo(JobRunQueueInfo info);

     void modifyJobRunQueueState(String state,long id);

     void modifyJobRunueueTopTime(long id);

     void modifyJobRunQueueConfig(JobRunQueueInfo info);

     JobRunQueueInfo getJobRunQueueInfoById(long id);

     List<JobRunQueueInfo> getAllJobRunQueueInfo();

     List<JobRunQueueInfo> getAllJobRunQueueInfoByState(String state) ;

}
