package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.doublecenter.LabSwitchInfo;
import com.ucar.datalink.domain.lab.LabInfo;

import java.util.List;

/**
 * Created by djj on 2018/8/15.
 */
public interface LabSwitchDAO {

    Integer insert(LabSwitchInfo labSwitchInfo);

    Integer update(LabSwitchInfo labSwitchInfo);

    LabSwitchInfo getLabSwitchByVersion(String version);

    List<LabSwitchInfo> findAll();

}
