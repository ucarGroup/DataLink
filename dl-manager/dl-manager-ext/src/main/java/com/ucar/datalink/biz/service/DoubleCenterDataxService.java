package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.job.JobConfigInfo;

import java.util.List;

/**
 * Created by yang.wang09 on 2018-11-01 16:44.
 */
public interface DoubleCenterDataxService {

    void switchDataSourceForAllTimingJob();

    /**
     * 根据传入的数据源id，切换数据源
     * @param mediaSourceList
     */
    void switchDataSourceForSpecifiedTimingJob(List<Long> mediaSourceList);

    void oldDataSourceChangeToVirtual();

    void stopAllRunningJob();

    /**
     * 根据传入的数据源id，停止正在运行的job 强杀: kill -9
     * @param mediaSourceList
     */
    void stopSpecifiedRunningJob(List<Long> mediaSourceList);

    List<JobConfigInfo> checkAllDataSource();

    /**
     * 是否切机房中
     * @param virtualMediaSourceId
     * @return
     */
    Boolean isSwitchLabIng(Long virtualMediaSourceId);

}
