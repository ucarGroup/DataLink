package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.domain.user.RoleType;

import java.util.List;

/**
 * Created by sqq on 2017/4/27.
 */
public interface RoleDAO {

    List<RoleInfo> getList();

    Integer insert(RoleInfo roleInfo);

    Integer update(RoleInfo roleInfo);

    Integer delete(Long id);

    RoleInfo getById(Long id);

    RoleInfo getByType(RoleType roleType);

    List<RoleInfo> getRolesByMenuId(Long menuId);
}
