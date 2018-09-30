package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;

import java.util.List;

/**
 * Created by djj on 2018/7/5.
 */
public interface SysPropertiesDAO {

    Integer insert(SysPropertiesInfo sysPropertiesInfo);

    Integer update(SysPropertiesInfo sysPropertiesInfo);

    Integer delete(Long id);

    SysPropertiesInfo getSysPropertiesById(Long id);

    List<SysPropertiesInfo> findSysPropertieList();
}
