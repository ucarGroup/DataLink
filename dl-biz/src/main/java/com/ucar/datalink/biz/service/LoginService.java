package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.user.UserInfo;

/**
 * Created by lubiao on 2018/3/1.
 */
public interface LoginService {

    boolean checkPassWord(UserInfo userInfo, String password);
}
