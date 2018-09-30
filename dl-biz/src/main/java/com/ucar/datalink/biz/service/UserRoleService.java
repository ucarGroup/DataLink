package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.user.UserRoleInfo;

import java.util.List;

public interface UserRoleService {

    Boolean insert(UserRoleInfo info);

    Boolean checkExist(UserRoleInfo info);

    List<UserRoleInfo> findListByUserId(Long userId);

    Boolean delete(UserRoleInfo info);

}
