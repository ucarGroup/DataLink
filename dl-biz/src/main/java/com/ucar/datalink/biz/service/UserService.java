package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by sqq on 2017/4/19.
 */
public interface UserService {

    List<UserInfo> getList();

    Boolean insert(UserInfo userInfo);

    Boolean update(UserInfo userInfo);

    Boolean delete(Long id);

    UserInfo getById(Long id);

    UserInfo getByUserInfo(UserInfo userInfo);

    Map<Long, UserInfo> getUserWidthLocalCache();

    void clearUserLocalCache();

    List<UserInfo> getUserInfoByRoleType(RoleType roleType);

    List<UserInfo> getUserInfoByRoleTypeAndIsAlarm(RoleType roleType);

    /**
     * //是否超级管理员
     *
     * @return
     */
    public Boolean isSuper(UserInfo user);

}
