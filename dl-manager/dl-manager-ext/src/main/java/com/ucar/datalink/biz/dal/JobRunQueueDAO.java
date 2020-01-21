package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.job.JobRunQueueInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by user on 2017/12/26.
 */
public interface JobRunQueueDAO {

    public void addJobRunQueueInfo(JobRunQueueInfo info);

    public void deleteJobRunQueueInfo(long id);

    public void updateJobRunQueueInfo(JobRunQueueInfo info);

    //public void updateJobRunQueueSuccessAndFailureState(long id, int successCount, int failureCount);

    public void updateJobRunQueueState(String state,long id);

    public void updateJobRunQueueTopTime(long id);

    public void updateJobRunQueueConfig(JobRunQueueInfo info);

    public JobRunQueueInfo getJobRunQueueInfo(long id);

    public JobRunQueueInfo getJobRunQueueInfoById(long id);

    public List<JobRunQueueInfo> getAllJobRunQueueInfo();

    public List<JobRunQueueInfo> getAllJobRunQueueInfoByState(@Param("state")String state) ;
}
