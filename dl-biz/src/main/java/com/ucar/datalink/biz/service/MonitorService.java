package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.monitor.MonitorCat;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by csf on 17/4/28.
 */
public interface MonitorService {

    List<MonitorInfo> getList();

    List<MonitorInfo> getListForQueryPage(@Param("monitorCat") Integer monitorCat, @Param("monitorType") Integer monitorType, @Param("resourceId") Long resourceId, @Param("isEffective") Integer isEffective);

    Boolean insert(MonitorInfo monitorInfo);

    Boolean update(MonitorInfo monitorInfo);

    Boolean delete(Long id);

    MonitorInfo getById(Long id);

    Boolean updateIsAlarm(int status);

    void createAllMonitor(Long resourceId, MonitorCat monitorCat);

    MonitorInfo getByResourceAndType(Long resourceId, MonitorType monitorType);

    void clearCache();
}
