package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.job.JobQueue;
import com.ucar.datalink.domain.job.JobQueueInfo;

import java.util.List;

/**
 * Created by yang.wang09 on 2019-04-02 15:43.
 */
public interface JobQueueDAO {

    List<JobQueueInfo> allJobQueueInfo();

    List<JobQueueInfo> allExecuteJobQueueInfo();

    JobQueueInfo getJobQueueInfoById(Long id);

    JobQueueInfo getJobQueueInfoByName(String name);

    void createJobQueueInfo(JobQueueInfo info);

    void modifyJobQueueInfo(JobQueueInfo info);

    void deleteJobQueueInfo(Long id);

    List<JobQueue> allJobQueue();

    List<JobQueue> allJobQueueForQueueId(long queueId);

    void createJobQueue(JobQueue queue);

    void modifyJobQueue(JobQueue queue);

    void deleteJobQueue(Long id);

    void abandoneRemainJob(Long id);

    JobQueue unexecuteJob();

    List<JobQueue> allQueueStatByQueueId(long queueId);

    JobQueue getJobQueueById(long id);

    JobQueue getJobQueueByJobJobNameAndQueueId(String jobName, long queueId);

    JobQueue getJobQueueByName(String jobName);
}
