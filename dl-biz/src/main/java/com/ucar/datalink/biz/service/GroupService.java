package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.statis.StatisDetail;

import java.util.List;

/**
 * Created by lubiao on 2017/1/20.
 */
public interface GroupService {

    List<GroupInfo> getAllGroups();

    Boolean insert(GroupInfo groupInfo);

    Boolean update(GroupInfo groupInfo);

    Boolean delete(Long id);

    GroupInfo getById(Long id);

    Integer groupCount();

    List<StatisDetail> getCountByName();
}
