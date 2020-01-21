package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.biz.service.SysPropertiesService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

public class UserServiceTest {

    @Test
    public void test(){
        new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
        UserService userService = DataLinkFactory.getObject(UserService.class);
        List<UserInfo> list =  userService.getUserInfoByRoleTypeAndIsAlarm(RoleType.SUPER);
        System.out.println(JSON.toJSON(list));
    }
}
