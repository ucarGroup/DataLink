package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.RoleDAO;
import com.ucar.datalink.biz.service.AuthorityService;
import com.ucar.datalink.biz.service.RoleService;
import com.ucar.datalink.biz.service.UserRoleService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.authority.RoleAuthorityInfo;
import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.domain.user.UserRoleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by sqq on 2017/4/27.
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    RoleDAO roleDAO;

    @Autowired
    AuthorityService authorityService;

    @Autowired
    UserService userService;

    @Autowired
    UserRoleService userRoleService;

    @Override
    public List<RoleInfo> getList() {
        return roleDAO.getList();
    }

    @Override
    public Boolean insert(RoleInfo roleInfo) {
        checkExist(roleInfo);
        Integer num = roleDAO.insert(roleInfo);
        return num > 0;
    }

    @Override
    public Boolean update(RoleInfo roleInfo) {
        checkExist(roleInfo);
        Integer num = roleDAO.update(roleInfo);
        return num > 0;
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {

        //删除用户角色关系
        UserRoleInfo info = new UserRoleInfo();
        info.setRoleId(id);
        userRoleService.delete(info);
        //删除角色菜单关系
        RoleAuthorityInfo roleAuthorityInfo = new RoleAuthorityInfo();
        roleAuthorityInfo.setRoleId(id);
        authorityService.delete(roleAuthorityInfo);
        //删除角色
        Integer num = roleDAO.delete(id);
        return num > 0;
    }

    @Override
    public RoleInfo getById(Long id) {
        return roleDAO.getById(id);
    }

    @Override
    public RoleInfo getByType(RoleType roleType) {
        return roleDAO.getByType(roleType);
    }

    @Override
    public List<RoleInfo> getRolesByMenuId(Long menuId) {
        return roleDAO.getRolesByMenuId(menuId);
    }

    @Override
    public Boolean hasRole(Long menuId, UserInfo userInfo) {
        List<RoleInfo> roleLists = getRolesByMenuId(menuId);
        for (RoleInfo roleInfo : userInfo.getRoleInfoList()){
            for (RoleInfo info : roleLists) {
                if (Objects.equals(info.getCode(), roleInfo.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void doRoleAuthority(Long roleId, List<Long> menuList) {
        RoleAuthorityInfo authorityInfo = new RoleAuthorityInfo();
        for (Long menuId : menuList) {
            authorityInfo.setRoleId(roleId);
            authorityInfo.setMenuId(menuId);
            Boolean isExist = authorityService.checkExist(authorityInfo);
            if (!isExist) {
                authorityService.insert(authorityInfo);
            }
        }
        List<RoleAuthorityInfo> dbList = authorityService.getListByRoleId(roleId);
        List<Long> dbMenuList = new ArrayList<Long>();
        for (RoleAuthorityInfo roleAuthorityInfo : dbList) {
            dbMenuList.add(roleAuthorityInfo.getMenuId());
        }
        dbMenuList.removeAll(menuList);
        for (Long menuId : dbMenuList) {
            authorityInfo.setMenuId(menuId);
            authorityInfo.setRoleId(roleId);
            authorityService.delete(authorityInfo);
        }
    }

    public void checkExist(RoleInfo roleInfo) {
        List<RoleInfo> roleList = getList();
        if (roleList != null) {
            roleList.forEach(role -> {
                if (Objects.equals(role.getCode(), roleInfo.getCode()) && !Objects.equals(role.getId(), roleInfo.getId())) {
                    throw new ValidationException("该角色类型已经存在.");
                }
            });
        }
    }

}
