package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.group.GroupInfo;
import com.ucar.datalink.domain.statis.StatisDetail;

import java.util.List;

/**
 * Created by lubiao on 2017/1/20.
 */
public interface GroupDAO {

    List<GroupInfo> listAllGroups();

    Integer insert(GroupInfo groupInfo);

    Integer update(GroupInfo groupInfo);

    Integer delete(Long id);

    GroupInfo getById(Long id);

    Integer groupCount();

    List<StatisDetail> getCountByName();
}
