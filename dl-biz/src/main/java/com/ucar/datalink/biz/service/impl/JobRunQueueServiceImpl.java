package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.JobRunQueueDAO;
import com.ucar.datalink.biz.service.JobRunQueueService;
import com.ucar.datalink.domain.job.JobRunQueueInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by user on 2017/12/26.
 */
@Service
public class JobRunQueueServiceImpl implements JobRunQueueService {

    @Autowired
    JobRunQueueDAO dao;

    @Override
    public void createJobRunQueueInfo(JobRunQueueInfo info) {
        dao.addJobRunQueueInfo(info);
    }

    @Override
    public void removeJobRunQueueInfo(long id) {
        dao.deleteJobRunQueueInfo(id);
    }

    @Override
    public void modifyJobRunQueueInfo(JobRunQueueInfo info) {
        dao.updateJobRunQueueInfo(info);
    }

    @Override
    public void modifyJobRunQueueState(String state,long id) {
        dao.updateJobRunQueueState(state,id);
    }

    @Override
    public void modifyJobRunueueTopTime(long id) {
        dao.updateJobRunQueueTopTime(id);
    }

    @Override
    public void modifyJobRunQueueConfig(JobRunQueueInfo info) {
        dao.updateJobRunQueueConfig(info);
    }


    @Override
    public JobRunQueueInfo getJobRunQueueInfoById(long id) {
        return dao.getJobRunQueueInfoById(id);
    }

    @Override
    public List<JobRunQueueInfo> getAllJobRunQueueInfo() {
        return dao.getAllJobRunQueueInfo();
    }

    @Override
    public List<JobRunQueueInfo> getAllJobRunQueueInfoByState(String state) {
        if(StringUtils.isBlank(state)) {
            return dao.getAllJobRunQueueInfoByState(null);
        }
        return dao.getAllJobRunQueueInfoByState(state);
    }
}
