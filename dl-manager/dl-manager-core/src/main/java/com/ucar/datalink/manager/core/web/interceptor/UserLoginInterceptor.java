package com.ucar.datalink.manager.core.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.ucar.datalink.biz.service.AuthorityService;
import com.ucar.datalink.biz.service.MenuService;
import com.ucar.datalink.biz.service.RoleService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.domain.authority.RoleAuthorityInfo;
import com.ucar.datalink.domain.menu.MenuInfo;
import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.manager.core.web.annotation.AuthIgnore;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by sqq on 2017/4/18.
 */
public class UserLoginInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginInterceptor.class);

    @Autowired
    MenuService menuService;

    @Autowired
    RoleService roleService;

    @Autowired
    AuthorityService authorityService;

    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        if (request.getRequestURI().startsWith("/assets")) {
            return true;
        }

        //带@LoginIgnore注解的方法或类可以直接访问，不需要登陆
        if (o instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) o;
            Annotation methodAnnotation = method.getMethod().getAnnotation(LoginIgnore.class);
            Annotation BeanAnnotation = method.getBeanType().getAnnotation(LoginIgnore.class);
            if (methodAnnotation != null || BeanAnnotation != null) {
                return true;
            }
        }

        Object userObj = request.getSession().getAttribute("user");

        if (userObj instanceof java.util.HashMap) {
            LOGGER.error(userObj.toString());
            return false;
        }
        UserInfo user = (UserInfo) userObj;

        //架构平台入口自动登录
        String userName = null;
        Cookie[] frameCookies = request.getCookies();
        if (frameCookies != null && frameCookies.length > 0) {
            for (Cookie cookie : frameCookies) {
                if (cookie.getName().equals("ldap-user")) {
                    String sessionUser = cookie.getValue();
                    JSONObject jsonObject = JSON.parseObject(sessionUser);
                    userName = jsonObject.get("userName").toString();
                }
            }
        }
        if (user == null  && StringUtils.isNotBlank(userName)) {
            response.sendRedirect(request.getContextPath() + "/userReq/autoLogin?userName=" + userName);
            return false;
        }

        if (user != null) {
            String contextPath = request.getContextPath();
            String uri = request.getRequestURI();
            String permUrl = uri.substring(contextPath.length(), uri.length());
            Boolean isSuper = userService.isSuper(user);

            boolean authExist = false;
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("roleAuth")) {
                    authExist = true;
                    break;
                }
            }
            if (!authExist) {
                //将角色的所有权限编码存入cookie中
                List<String> roleCodeList;
                if (isSuper) {//SUPER拥有所有权限
                    List<MenuInfo> menuList = menuService.getList();
                    roleCodeList = menuList.stream().map(MenuInfo::getCode).collect(Collectors.toList());

                } else {
                    List<RoleAuthorityInfo> list = new ArrayList<RoleAuthorityInfo>();
                    for(RoleInfo info : user.getRoleInfoList()){
                        List<RoleAuthorityInfo> roleAuthList = authorityService.getListByRoleId(info.getId());
                        list.addAll(roleAuthList);
                    }
                    List<RoleAuthorityInfo> distinctList = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(bean -> bean.getCode()))), ArrayList::new));
                    roleCodeList = distinctList.stream().map(RoleAuthorityInfo::getCode).collect(Collectors.toList());
                }
                String roleCodeStr = Joiner.on(",").skipNulls().join(roleCodeList);
                Cookie cookie = new Cookie("roleAuth", roleCodeStr);
                cookie.setPath("/");//cookie有效路径
                response.addCookie(cookie);//添加到浏览器中
            }

            //带@AuthIgnore注解的方法或类可以直接访问，不需要权限验证
            if (o instanceof HandlerMethod) {
                HandlerMethod method = (HandlerMethod) o;
                Annotation methodAnnotation = method.getMethod().getAnnotation(AuthIgnore.class);
                Annotation BeanAnnotation = method.getBeanType().getAnnotation(AuthIgnore.class);
                if (methodAnnotation != null || BeanAnnotation != null) {
                    return true;
                }
            }
            //权限验证
            if (isSuper) {
                return true;
            } else {
                MenuInfo menu = menuService.getMenuByUrl(permUrl);
                if (menu != null) {
                    Boolean hasRole = roleService.hasRole(menu.getId(), user);
                    if (hasRole) {
                        return true;
                    } else if (isAjax(request)) {
                        response.sendRedirect(request.getContextPath() + "/userReq/forbiddenJson");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/userReq/forbidden");
                    }
                    return false;
                }
                return false;
            }

        } else if (isAjax(request)) {
            response.sendRedirect(request.getContextPath() + "/userReq/login");
            return false;
        } else {
            response.sendRedirect(request.getContextPath() + "/userReq/login");
            return false;
        }

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) throws Exception {
    }

    /**
     * 判断ajax请求
     *
     * @param request
     * @return
     */
    boolean isAjax(HttpServletRequest request) {
        return (request.getHeader("X-Requested-With") != null && "XMLHttpRequest".equals(request.getHeader("X-Requested-With").toString()));
    }
}
