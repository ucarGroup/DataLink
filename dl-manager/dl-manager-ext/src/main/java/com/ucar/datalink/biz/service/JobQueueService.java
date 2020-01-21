package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.job.JobQueue;
import com.ucar.datalink.domain.job.JobQueueInfo;

import java.util.List;

/**
 * Created by yang.wang09 on 2019-04-02 15:44.
 */
public interface JobQueueService {

    public List<JobQueueInfo> allJobQueueInfo();

    public List<JobQueueInfo> allExecuteJobQueueInfo();

    public JobQueueInfo getJobQueueInfoById(Long id);

    public JobQueueInfo getJobQueueInfoByName(String name);

    public void createJobQueueInfo(JobQueueInfo info);

    public void modifyJobQueueInfo(JobQueueInfo info);

    public void deleteJobQueueInfo(Long id);



    // job queue

    public List<JobQueue> allJobQueue();

    public List<JobQueue> allJobQueueForQueueId(long queueId);

    public void createJobQueue(JobQueue queue);

    public void modifyJobQueue(JobQueue queue);

    public void deleteJobQueue(Long id);

    public void abandoneRemainJob(Long id);

    public JobQueue unexecuteJob();

    public List<JobQueue> allQueueStatByQueueId(long queueId);

    public JobQueue getJobQueueById(long id);

    public JobQueue getJobQueueByJobJobNameAndQueueId(String jobName, long queueId);

    public JobQueue getJobQueueByName(String jobName);
}
