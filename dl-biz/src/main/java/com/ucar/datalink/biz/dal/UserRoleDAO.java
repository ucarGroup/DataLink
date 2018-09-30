package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.user.UserRoleInfo;

import java.util.List;

public interface UserRoleDAO {

    Integer insert(UserRoleInfo info);

    Integer checkExist(UserRoleInfo info);

    List<UserRoleInfo> findListByUserId(Long userId);

    Integer delete(UserRoleInfo info);

}
