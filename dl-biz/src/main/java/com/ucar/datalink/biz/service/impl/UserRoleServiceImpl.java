package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.UserRoleDAO;
import com.ucar.datalink.biz.service.UserRoleService;
import com.ucar.datalink.domain.user.UserRoleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRoleServiceImpl implements UserRoleService{

    @Autowired
    UserRoleDAO userRoleDAO;

    public Boolean insert(UserRoleInfo info){
        Integer num = userRoleDAO.insert(info);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean checkExist(UserRoleInfo info) {
        Integer num = userRoleDAO.checkExist(info);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public List<UserRoleInfo> findListByUserId(Long userId) {
        return userRoleDAO.findListByUserId(userId);
    }

    public Boolean delete(UserRoleInfo info){
        Integer num = userRoleDAO.delete(info);
        if (num > 0) {
            return true;
        }
        return false;
    }


}
