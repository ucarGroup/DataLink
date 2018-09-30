package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.authority.RoleAuthorityInfo;

import java.util.List;

/**
 * Created by sqq on 2017/5/4.
 */
public interface AuthorityService {

    List<RoleAuthorityInfo> getListByRoleId(Long roleId);

    Boolean insert(RoleAuthorityInfo roleAuthorityInfo);

    Boolean delete(RoleAuthorityInfo roleAuthorityInfo);

    Boolean checkExist(RoleAuthorityInfo roleAuthorityInfo);
}
