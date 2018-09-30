package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;

import java.util.List;

/**
 * Created by sqq on 2017/4/27.
 */
public interface RoleService {

    List<RoleInfo> getList();

    Boolean insert(RoleInfo roleInfo);

    Boolean update(RoleInfo roleInfo);

    Boolean delete(Long id);

    RoleInfo getById(Long id);

    RoleInfo getByType(RoleType roleType);

    List<RoleInfo> getRolesByMenuId(Long menuId);

    Boolean hasRole(Long menuId, UserInfo userInfo);

    void doRoleAuthority(Long roleId, List<Long> menuList);

}
