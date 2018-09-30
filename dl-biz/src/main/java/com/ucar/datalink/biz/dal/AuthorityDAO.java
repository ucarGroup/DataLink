package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.authority.RoleAuthorityInfo;

import java.util.List;

/**
 * Created by sqq on 2017/5/4.
 */
public interface AuthorityDAO {

    List<RoleAuthorityInfo> getListByRoleId(Long roleId);

    Integer insert(RoleAuthorityInfo roleAuthorityInfo);

    Integer delete(RoleAuthorityInfo roleAuthorityInfo);

    Integer checkExist(RoleAuthorityInfo roleAuthorityInfo);
}
