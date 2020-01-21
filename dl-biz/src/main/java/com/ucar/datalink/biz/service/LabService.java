package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.lab.LabInfo;

import java.util.List;

/**
 * Created by djj on 2018/7/5.
 */
public interface LabService {

    Boolean insert(LabInfo labInfo);

    Boolean update(LabInfo labInfo);

    Boolean delete(Long id);

    LabInfo getLabById(Long id);

    List<LabInfo> findLabList();

    LabInfo getLabByName(String name);

    Boolean updateCenterLab(LabInfo labInfo);

}
