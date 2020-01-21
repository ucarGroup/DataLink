package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.lab.LabInfo;

import java.util.List;

/**
 * Created by djj on 2018/8/15.
 */
public interface LabDAO {

    Integer insert(LabInfo labInfo);

    Integer update(LabInfo labInfo);

    Integer delete(Long id);

    LabInfo getLabById(Long id);

    List<LabInfo> findLabList();

    LabInfo getLabByName(String name);

    Integer updateCenterLab(LabInfo labInfo);

    LabInfo getLabByDataSource(String dataSource);

}
