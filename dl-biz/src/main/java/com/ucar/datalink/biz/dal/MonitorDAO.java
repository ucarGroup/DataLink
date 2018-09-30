package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.monitor.MonitorInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by csf on 17/4/28.
 */
public interface MonitorDAO {

    List<MonitorInfo> getList();

    List<MonitorInfo> getListForQueryPage(@Param("monitorCat") Integer monitorCat, @Param("monitorType") Integer monitorType, @Param("resourceId") Long resourceId, @Param("isEffective") Integer isEffective);

    List<MonitorInfo> getListByResourceAndCat(@Param("resourceId") Long resourceId, @Param("monitorCat") Integer monitorCat);

    Integer insert(MonitorInfo monitorInfo);

    Integer update(MonitorInfo monitorInfo);

    Integer delete(Long id);

    Integer deleteByResourceIdAndCat(@Param("resourceId") Long resourceId, @Param("monitorCat") Integer monitorCat);

    MonitorInfo getById(Long id);

    Integer updateIsAlarm(int status);
}
