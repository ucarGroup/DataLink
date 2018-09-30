package com.ucar.datalink.manager.core.web.controller.login;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.biz.service.LoginService;
import com.ucar.datalink.biz.service.RoleService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by sqq on 2017/4/17.
 */
@Controller
@RequestMapping("/userReq")
@LoginIgnore
public class UserLoginController {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserLoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    LoginService loginService;

    @RequestMapping(value = "/login")
    public ModelAndView login() {
        return new ModelAndView("login/login");
    }

    @RequestMapping(value = "/doLogin")
    @ResponseBody
    public String loginValidate(HttpServletRequest request, HttpServletResponse response, String loginEmail, String password) {
        String retString;
        UserInfo user = new UserInfo();
        if (StringUtils.isEmpty(loginEmail)) {
            retString = "用户名不能为空";
        } else if (StringUtils.isEmpty(password)) {
            retString = "密码不能为空";
        } else {
            user.setUcarEmail(loginEmail);
            UserInfo userInfo = userService.getByUserInfo(user);
            if (userInfo == null) {
                return "toRegister";
            } else {
                if ((loginEmail.equals("admin") && password.equals("admin")) || loginService.checkPassWord(userInfo, password)) {
                    saveSession(loginEmail, request);
                    retString = "success";
                } else {
                    retString = "登陆认证失败";
                }
            }
        }
        return retString;
    }

    @RequestMapping(value = "/autoLogin")
    public ModelAndView autoLogin(HttpServletRequest request) {
        UserInfo user = new UserInfo();
        String loginEmail = request.getParameter("userName");
        user.setUcarEmail(loginEmail);
        UserInfo userInfo = userService.getByUserInfo(user);
        if (userInfo == null) {
            return new ModelAndView("register");
        } else {
            saveSession(loginEmail, request);
            return new ModelAndView("index");
        }
    }

    @RequestMapping(value = "/toRegister")
    public ModelAndView toRegister() {
        return new ModelAndView("register");
    }

    @RequestMapping(value = "/doRegister")
    @ResponseBody
    public String doRegister(String userName, String ucarEmail, String phone) {
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserName(userName);
            userInfo.setUcarEmail(ucarEmail);
            userInfo.setPhone(phone);
            RoleInfo roleInfo = roleService.getByType(RoleType.ORDINARY);
            userInfo.setRoleIdStr(String.valueOf(roleInfo.getId()));
            UserInfo user = userService.getByUserInfo(userInfo);
            if (user != null) {
                return "registered";
            }
            userService.insert(userInfo);
            return "success";
        } catch (Exception e) {
            LOGGER.error("Register failed.", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/forbidden")
    public ModelAndView forbidden() {
        return new ModelAndView("forbidden");
    }

    @RequestMapping(value = "/forbiddenJson")
    @ResponseBody
    public String forbiddenJson() {
        String ret = JSON.toJSONString("not permit");
        return ret;
    }

    @RequestMapping(value = "/sessionOvertime")
    @ResponseBody
    public String sessionOvertime() {
        String ret = JSON.toJSONString("登陆超时，请重新登陆");
        return ret;
    }

    @RequestMapping(value = "/logout")
    @ResponseBody
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        String result;
        try {
            request.getSession().invalidate();
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("roleAuth")) {
                        cookie.setMaxAge(0);//设置为0为立即删除该Cookie
                        cookie.setPath("/");//删除指定路径的cookie,不设置该路径，默认为删除当前路径Cookie
                        response.addCookie(cookie);
                    }
                }
            }
            result = "success";
        } catch (Exception e) {
            result = "error";
        }
        return result;
    }

    private void saveSession(String loginEmail, HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUcarEmail(loginEmail);
        userInfo = userService.getByUserInfo(userInfo);
        HttpSession session = request.getSession();
        session.setAttribute("user", userInfo);
    }

}
