package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.job.DataxCommand;
import com.ucar.datalink.domain.job.JobExecutionInfo;

import java.util.List;

/**
 * Created by user on 2017/9/21.
 */
public interface JobControlService {

    public String start(DataxCommand command, Object additional);

    public List<JobExecutionInfo> history(long execute_id);

    public List<JobExecutionInfo> history(long job_id, long startPage, long count);

    public JobExecutionInfo state(long id);

}
