package com.ucar.datalink.manager.core.web.controller.login;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.manager.core.utils.DesUtil;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.controller.util.ExtendPropertiesUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author xy.li
 * @date 2019/06/21
 */


@Controller
@LoginIgnore
public class TechplatLoginController {

    private final static Logger LOGGER = LoggerFactory.getLogger(TechplatLoginController.class);

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/techplat/user/login")
    public void login(HttpServletRequest request, HttpServletResponse response, String username, String password, String op, String rtnUrl, String desName) {
        try {
            LOGGER.info("统一平台登录进来！");
            JSONObject jo = DesUtil.decrypt(desName);
            LOGGER.info(String.format("统一平台登录,desName[%s]",String.valueOf(jo)));
            if(jo == null){
                LOGGER.info(String.format("数据异常，重定向到统一登录平台",String.valueOf(jo)));
                redirect(response,  ExtendPropertiesUtils.getTechplatURL());
                return;
            }
            String user = jo.getString("user");
            String loginType = jo.getString("loginType");
            if(user == null || StringUtils.isBlank(user) || loginType == null || UserInfo.UserType.contain(loginType.toUpperCase())){
                LOGGER.info(String.format("数据异常，重定向到统一登录平台",String.valueOf(jo)));
                redirect(response,  ExtendPropertiesUtils.getTechplatURL());
                return;
            }

            String ucarincEmail = jo.getString("ucarincEmail");  //获取从登录平台过来的用户信息  解密后 正常为  用户名@邮箱地址  例如：wen.zhang02@ucarinc.com
            String luckyEmail = jo.getString("luckyEmail");
            UserInfo userInfo = new UserInfo();
            String email;

            if(UserInfo.UserType.valueOf(loginType.toUpperCase()) == UserInfo.UserType.UCARINC){
                email = ucarincEmail;
            }else if(UserInfo.UserType.valueOf(loginType.toUpperCase()) == UserInfo.UserType.LUCKY){
                email = luckyEmail;
            }else{
                LOGGER.info(String.format("userType数据异常，重定向到统一登录平台",String.valueOf(jo)));
                redirect(response,  ExtendPropertiesUtils.getTechplatURL());
                return;
            }

            userInfo.setUcarEmail(email);
            userInfo.setUserType(UserInfo.UserType.valueOf(loginType.toUpperCase()).getValue());
            userInfo = this.userService.getByUserInfo(userInfo);
            //不存在就添加到表中
            if(userInfo == null){
                userInfo = new UserInfo();
                userInfo.setUcarEmail(email);
                userInfo.setUserName(email);
                userInfo.setUserType(UserInfo.UserType.valueOf(loginType.toUpperCase()).getValue());
                userInfo.setRoleIdStr("2");//普通用户
                userInfo.setPhone("13900000000");//兼容表结构
                userInfo.setIsAlarm(false);
                userInfo.setIsReceiveDataxMail(false);
                LOGGER.info(String.format("insert userInfo[%s]",userInfo.toString()));
                this.userService.insert(userInfo);
            }
            saveSession(userInfo,request);
            redirect(response, ExtendPropertiesUtils.getDatalink());
        } catch (Exception e) {
            LOGGER.error("联合登陆平台登陆失败", e);
            redirect(response, ExtendPropertiesUtils.getDatalink() + "/admin");
        }


    }


    @RequestMapping(value = "/admin")
    public void login(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setAttribute("op","admin");
            request.getRequestDispatcher("/userReq/login").forward(request,response);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }


    private void saveSession( UserInfo userInfo, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("user", userInfo);
    }


    private void redirect(HttpServletResponse response,String url) {            //重定向逻辑
        try {
            LOGGER.info(String.format("重定向到[%s]",url));
            response.setStatus(302);
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}
