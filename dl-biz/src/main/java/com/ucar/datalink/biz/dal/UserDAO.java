package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;

import java.util.List;

/**
 * Created by sqq on 2017/4/19.
 */
public interface UserDAO {

    List<UserInfo> getList();

    Integer insert(UserInfo userInfo);

    Integer update(UserInfo userInfo);

    Integer delete(Long id);

    UserInfo getById(Long id);

    UserInfo getByUserInfo(UserInfo userInfo);

    List<UserInfo> getUserInfoByRoleType(RoleType roleType);

    List<UserInfo> getUserInfoByRoleTypeAndIsAlarm(RoleType roleType);
}
