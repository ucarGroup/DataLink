package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.JobScheduleDAO;
import com.ucar.datalink.biz.service.JobScheduleService;
import com.ucar.datalink.domain.job.JobConfigInfo;
import com.ucar.datalink.domain.job.JobScheduleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yang.wang09 on 2018-07-19 20:25.
 */
@Service
public class JobScheduleServiceImpl implements JobScheduleService {

    @Autowired
    JobScheduleDAO jobScheduleDAO;


    @Override
    public JobScheduleInfo getById(Long id) {
        return jobScheduleDAO.getById(id);
    }

    @Override
    public List<JobScheduleInfo> getByConfigId(Long id) {
        return jobScheduleDAO.getByConfigId(id);
    }

    @Override
    public List<JobScheduleInfo> getList() {
        return jobScheduleDAO.getList();
    }

    @Override
    public List<JobScheduleInfo> selectBatchJobSchedule(String srcType,String destType,String srcName,
    String destName, String mediaName,String jobName,int isTiming, int scheduleState) {
        return jobScheduleDAO.selectBatchJobSchedule(srcType,destType,srcName,destName,mediaName,jobName,isTiming,scheduleState);
    }


    @Override
    public long create(JobScheduleInfo info) {
        return jobScheduleDAO.insert(info);
    }

    @Override
    public void modify(JobScheduleInfo info) {
        jobScheduleDAO.update(info);
    }

    @Override
    public void modifyByJobConfig(JobScheduleInfo info) {
        jobScheduleDAO.updateByJobConfig(info);
    }

    @Override
    public void remove(Long id) {
        jobScheduleDAO.delete(id);
    }

    @Override
    public void removeByJobConfigId(Long id) {
        jobScheduleDAO.deleteByJobConfigId(id);
    }

    @Override
    public List<JobScheduleInfo> getJobScheduleByConfigId(Long id) {
        return jobScheduleDAO.getJobScheduleByConfigId(id);
    }

    @Override
    public void modifyState(JobScheduleInfo info) {
        jobScheduleDAO.updateState(info);
    }


    @Override
    public JobConfigInfo latestJobScheduleRecord() {
        return jobScheduleDAO.latestJobScheduleRecord();
    }
}
