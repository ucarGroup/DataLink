package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.job.JobCommand;
import com.ucar.datalink.domain.job.JobExecutionInfo;

import java.util.List;

/**
 * Created by user on 2017/9/21.
 */
public interface JobControlService {

     String start(JobCommand command, Object additional);

     List<JobExecutionInfo> history(long execute_id);

     List<JobExecutionInfo> history(long job_id, long startPage, long count);

     JobExecutionInfo state(long id);

}
