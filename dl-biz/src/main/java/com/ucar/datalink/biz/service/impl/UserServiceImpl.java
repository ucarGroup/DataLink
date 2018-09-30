package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.UserDAO;
import com.ucar.datalink.biz.service.UserRoleService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.domain.user.UserRoleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sqq on 2017/4/19.
 */
@Service
public class UserServiceImpl implements UserService {

    public static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final Map<Long,UserInfo> userMap = new ConcurrentHashMap<>();

    @Autowired
    UserDAO userDAO;
    @Autowired
    UserRoleService userRoleService;

    @Override
    public List<UserInfo> getList() {
        return userDAO.getList();
    }

    @Override
    @Transactional
    public Boolean insert(UserInfo userInfo) {
        //保存用户
        Integer num = userDAO.insert(userInfo);
        //保存用户角色关系
        String[] roleIds = userInfo.getRoleIdStr().split(",");
        for (String roleId : roleIds) {
            UserRoleInfo userRoleInfo = new UserRoleInfo();
            userRoleInfo.setUserId(userInfo.getId());
            userRoleInfo.setRoleId(Long.parseLong(roleId));
            userRoleInfo.setCreateTime(new Date());
            userRoleInfo.setModifyTime(new Date());
            userRoleService.insert(userRoleInfo);
        }
        if (num > 0) {
            clearUserLocalCache();
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Boolean update(UserInfo userInfo) {
        //更新
        Integer num = userDAO.update(userInfo);

        //保存新的角色关系
        String[] roleIds = userInfo.getRoleIdStr().split(",");
        UserRoleInfo userRoleInfo = new UserRoleInfo();
        userRoleInfo.setUserId(userInfo.getId());
        List<Long> stayList = new ArrayList<Long>();
        for(String roleId : roleIds){
            userRoleInfo.setRoleId(Long.parseLong(roleId));
            Boolean isExist = userRoleService.checkExist(userRoleInfo);
            if(!isExist){
                userRoleService.insert(userRoleInfo);
            }
            stayList.add(Long.parseLong(roleId));
        }

        //删除去掉的角色关系
        List<UserRoleInfo> list = userRoleService.findListByUserId(userInfo.getId());
        List<Long> roleList = new ArrayList<Long>();
        for(UserRoleInfo info : list){
            roleList.add(info.getRoleId());
        }
        roleList.removeAll(stayList);
        UserRoleInfo deleteInfo = new UserRoleInfo();
        deleteInfo.setUserId(userInfo.getId());
        for(Long roleId : roleList){
            deleteInfo.setRoleId(roleId);
            userRoleService.delete(deleteInfo);
        }

        if (num > 0) {
            clearUserLocalCache();
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {

        //删除用户角色关系
        UserRoleInfo info = new UserRoleInfo();
        info.setUserId(id);
        userRoleService.delete(info);
        //删除用户
        Integer num = userDAO.delete(id);

        if (num > 0) {
            clearUserLocalCache();
            return true;
        }

        return false;
    }

    @Override
    public UserInfo getById(Long id) {
        return userDAO.getById(id);
    }

    @Override
    public UserInfo getByUserInfo(UserInfo userInfo) {
        return userDAO.getByUserInfo(userInfo);
    }

    @Override
    public synchronized Map<Long, UserInfo> getUserWidthLocalCache() {
        if(userMap.size() == 0){
            List<UserInfo> userList = getList();
            if(userList != null && userList.size() > 0){
                for(UserInfo userInfo : userList){
                    userMap.put(userInfo.getId(),userInfo);
                }
            }
        }
        return userMap;
    }

    @Override
    public void clearUserLocalCache() {
        userMap.clear();
    }

    @Override
    public List<UserInfo> getUserInfoByRoleType(RoleType roleType) {
        return userDAO.getUserInfoByRoleType(roleType);
    }

    @Override
    public List<UserInfo> getUserInfoByRoleTypeAndIsAlarm(RoleType roleType) {
        return userDAO.getUserInfoByRoleTypeAndIsAlarm(roleType);
    }

    /**
     * //是否超级管理员
     *
     * @return
     */
    public Boolean isSuper(UserInfo user) {
        for (RoleInfo roleInfo : user.getRoleInfoList()){
            if (Objects.equals(RoleType.SUPER.toString(), roleInfo.getCode())) {
                return true;
            }
        }
        return false;
    }

}
