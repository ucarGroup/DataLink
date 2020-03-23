package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.JobQueueDAO;
import com.ucar.datalink.biz.service.JobQueueService;
import com.ucar.datalink.domain.job.JobQueue;
import com.ucar.datalink.domain.job.JobQueueInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yang.wang09 on 2019-04-02 15:44.
 */

@Service
public class JobQueueServiceImpl implements JobQueueService {

    @Autowired
    JobQueueDAO jobQueueDAO;

    @Override
    public List<JobQueueInfo> allJobQueueInfo() {
        return jobQueueDAO.allJobQueueInfo();
    }

    @Override
    public List<JobQueueInfo> allExecuteJobQueueInfo() {
        return jobQueueDAO.allExecuteJobQueueInfo();
    }

    @Override
    public JobQueueInfo getJobQueueInfoById(Long id) {
        return jobQueueDAO.getJobQueueInfoById(id);
    }

    @Override
    public JobQueueInfo getJobQueueInfoByName(String name) {
        return jobQueueDAO.getJobQueueInfoByName(name);
    }

    @Override
    public void createJobQueueInfo(JobQueueInfo info) {
        jobQueueDAO.createJobQueueInfo(info);
    }

    @Override
    public void modifyJobQueueInfo(JobQueueInfo info) {
        jobQueueDAO.modifyJobQueueInfo(info);
    }

    @Override
    public void deleteJobQueueInfo(Long id) {
        jobQueueDAO.deleteJobQueueInfo(id);
    }


    // job queue ...

    @Override
    public List<JobQueue> allJobQueue() {
        return jobQueueDAO.allJobQueue();
    }

    @Override
    public List<JobQueue> allJobQueueForQueueId(long queueId) {
        return jobQueueDAO.allJobQueueForQueueId(queueId);
    }

    @Override
    public void createJobQueue(JobQueue queue) {
        jobQueueDAO.createJobQueue(queue);
    }

    @Override
    public void modifyJobQueue(JobQueue queue) {
        jobQueueDAO.modifyJobQueue(queue);
    }

    @Override
    public void deleteJobQueue(Long id) {
        jobQueueDAO.deleteJobQueue(id);
    }

    @Override
    public void abandoneRemainJob(Long id) {
        jobQueueDAO.abandoneRemainJob(id);
    }

    @Override
    public JobQueue unexecuteJob() {
        return jobQueueDAO.unexecuteJob();
    }

    @Override
    public List<JobQueue> allQueueStatByQueueId(long queueId) {
        return jobQueueDAO.allQueueStatByQueueId(queueId);
    }

    @Override
    public JobQueue getJobQueueById(long id) {
        return jobQueueDAO.getJobQueueById(id);
    }


    @Override
    public JobQueue getJobQueueByJobJobNameAndQueueId(String jobName, long queueId) {
        return jobQueueDAO.getJobQueueByJobJobNameAndQueueId(jobName,queueId);
    }

    @Override
    public JobQueue getJobQueueByName(String jobName) {
        return jobQueueDAO.getJobQueueByName(jobName);
    }

}
