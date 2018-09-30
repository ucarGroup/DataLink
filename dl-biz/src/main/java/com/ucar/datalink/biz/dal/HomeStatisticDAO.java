package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.statis.HomeStatistic;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2018/4/17.
 */
public interface HomeStatisticDAO {

    List<HomeStatistic> taskSizeStatistic(@Param("groupId")Long groupId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<HomeStatistic> taskRecordStatistic(@Param("groupId")Long groupId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<HomeStatistic> taskDelayStatistic(@Param("groupId")Long groupId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<HomeStatistic> workerJvmUsedStatistic(@Param("groupId")Long groupId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<HomeStatistic> workerYoungGCCountStatistic(@Param("groupId")Long groupId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<HomeStatistic> workerNetTrafficStatistic(@Param("groupId")Long groupId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}
