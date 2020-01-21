package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.user.UserInfo;

/**
 * Created by lubiao on 2018/3/1.
 */
public interface LoginService {

    boolean checkUcarPassWord(UserInfo userInfo, String password);

    boolean checkLuckyPassWord(UserInfo userInfo, String password);
}
