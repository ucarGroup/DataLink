package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.job.JobRunQueueInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by user on 2017/12/26.
 */
public interface JobRunQueueDAO {

    void addJobRunQueueInfo(JobRunQueueInfo info);

    void deleteJobRunQueueInfo(long id);

    void updateJobRunQueueInfo(JobRunQueueInfo info);

    void updateJobRunQueueState(String state,long id);

    void updateJobRunQueueTopTime(long id);

    void updateJobRunQueueConfig(JobRunQueueInfo info);

    JobRunQueueInfo getJobRunQueueInfo(long id);

    JobRunQueueInfo getJobRunQueueInfoById(long id);

    List<JobRunQueueInfo> getAllJobRunQueueInfo();

    List<JobRunQueueInfo> getAllJobRunQueueInfoByState(@Param("state")String state) ;
}
