
package com.ucar.datalink.manager.core.web.controller.login;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.biz.service.LoginService;
import com.ucar.datalink.biz.service.RoleService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.domain.user.RoleType;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.controller.util.ExtendPropertiesUtils;
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
import java.io.IOException;

/**
 * Created by sqq on 2017/4/17.
 */
@Controller
@RequestMapping("/userReq")
@LoginIgnore
public class UserLoginController {

    private static final String USER_LOGIN_PATH_ADMIN = "use_login_path_admin";
    private final static Logger LOGGER = LoggerFactory.getLogger(UserLoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    LoginService loginService;


    private void redirect(HttpServletResponse response,String url) {            //重定向逻辑
        try {
            response.setStatus(302);
            response.sendRedirect(url);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }



    @RequestMapping(value = "/login")
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {
        Object op = request.getAttribute("op");

        if("admin".equalsIgnoreCase(String.valueOf(op))){
            return new ModelAndView("login/login");
        }

        StringBuilder techplatAndReturnDatalinkURL = new StringBuilder();
        techplatAndReturnDatalinkURL.append(ExtendPropertiesUtils.getTechplatURL());
        techplatAndReturnDatalinkURL.append("?rtnUrl=");
        techplatAndReturnDatalinkURL.append(ExtendPropertiesUtils.getDatalink());

        String url = techplatAndReturnDatalinkURL.toString();

        LOGGER.info(String.format("to techplat url[%s]",url));
        redirect(response, url);
        return null;
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
            user.setUserType(UserInfo.UserType.UCARINC.getValue());
            UserInfo userInfo = userService.getByUserInfo(user);
            if (userInfo == null) {
                return "toRegister";
            } else {
                if (loginService.checkUcarPassWord(userInfo, password)) {
                    saveSession(loginEmail, UserInfo.UserType.UCARINC, request);
                    setAdminPath(request);
                    retString = "success";
                } else if (loginService.checkLuckyPassWord(userInfo, password)) {
                    saveSession(loginEmail, UserInfo.UserType.UCARINC, request);
                    setAdminPath(request);
                    retString = "success";
                } else {
                    retString = "登陆认证失败";
                }
            }
        }
        return retString;
    }

    /**
     * 只要通过认证的都是admin登录进来的
     * @param request
     */
    private void setAdminPath(HttpServletRequest request){
        request.getSession().setAttribute(USER_LOGIN_PATH_ADMIN,"true");
    }


//    @RequestMapping(value = "/autoLogin")
//    public ModelAndView autoLogin(HttpServletRequest request) {
//        UserInfo user = new UserInfo();
//        String loginEmail = request.getParameter("userName");
//        user.setUcarEmail(loginEmail);
//        UserInfo userInfo = userService.getByUserInfo(user);
//        if (userInfo == null) {
//            return new ModelAndView("register");
//        } else {
//            saveSession(loginEmail, request);
//            return new ModelAndView("index");
//        }
//    }

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
            userInfo.setUserType(UserInfo.UserType.UCARINC.getValue());
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
            Object loginUserPath = request.getSession().getAttribute(USER_LOGIN_PATH_ADMIN);
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
            if(loginUserPath != null && Boolean.parseBoolean(loginUserPath.toString())){
                return "admin_success";
            }

            result = "success";
        } catch (Exception e) {
            return result = "error";
        }
        return result;
    }

    private void saveSession(String loginEmail, UserInfo.UserType userType, HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUcarEmail(loginEmail);
        userInfo.setUserType(userType.getValue());
        userInfo = userService.getByUserInfo(userInfo);
        HttpSession session = request.getSession();
        session.setAttribute("user", userInfo);
    }

}
