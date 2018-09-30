package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by djj on 2018/7/5.
 */
public interface SysPropertiesService {

    Boolean insert(SysPropertiesInfo sysPropertiesInfo);

    Boolean update(SysPropertiesInfo sysPropertiesInfo);

    SysPropertiesInfo getSysPropertiesById(Long id);

    List<SysPropertiesInfo> findSysPropertieList();

    Boolean delete(Long id);

    Map<String,String> map();
}
