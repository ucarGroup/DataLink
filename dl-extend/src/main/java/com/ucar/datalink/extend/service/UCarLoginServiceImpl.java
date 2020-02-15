package com.ucar.datalink.extend.service;

import com.ucar.datalink.biz.service.LoginService;
import com.ucar.datalink.domain.user.UserInfo;
import org.springframework.stereotype.Service;


/**
 * Created by lubiao on 2018/3/1.
 */
@Service
public class UCarLoginServiceImpl implements LoginService {

    @Override
    public boolean checkPassWord(UserInfo userInfo, String password) {
        //用户登陆邮箱密码校验
        return false;
    }
}
