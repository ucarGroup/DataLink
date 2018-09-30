package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.AuthorityDAO;
import com.ucar.datalink.biz.service.AuthorityService;
import com.ucar.datalink.domain.authority.RoleAuthorityInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sqq on 2017/5/4.
 */
@Service
public class AuthorityServiceImpl implements AuthorityService {

    @Autowired
    AuthorityDAO authorityDAO;

    @Override
    public List<RoleAuthorityInfo> getListByRoleId(Long roleId) {
        return authorityDAO.getListByRoleId(roleId);
    }

    @Override
    public Boolean insert(RoleAuthorityInfo roleAuthorityInfo) {
        Integer num = authorityDAO.insert(roleAuthorityInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean delete(RoleAuthorityInfo roleAuthorityInfo) {
        Integer num = authorityDAO.delete(roleAuthorityInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean checkExist(RoleAuthorityInfo roleAuthorityInfo) {
        Integer num = authorityDAO.checkExist(roleAuthorityInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }
}
