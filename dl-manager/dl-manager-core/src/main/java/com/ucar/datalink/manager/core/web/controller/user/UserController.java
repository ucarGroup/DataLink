package com.ucar.datalink.manager.core.web.controller.user;

import com.ucar.datalink.biz.service.RoleService;
import com.ucar.datalink.biz.service.UserRoleService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.domain.user.UserRoleInfo;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sqq on 2017/4/19.
 */
@Controller
@RequestMapping(value = "/user/")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;
    @Autowired
    UserRoleService userRoleService;

    @RequestMapping(value = "/userList")
    public ModelAndView userList() {
        ModelAndView mav = new ModelAndView("user/list");
        return mav;
    }

    @RequestMapping(value = "/initUser")
    @ResponseBody
    public Page<UserInfo> initUser() {
        List<UserInfo> listUser = userService.getList();
        return new Page<UserInfo>(listUser);
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("user/add");
        mav.addObject("roleList", roleService.getList());
        return mav;
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(@ModelAttribute("userInfo") UserInfo userInfo) {
        Boolean isSuccess = userService.insert(userInfo);
        if (isSuccess) {
            return "success";
        } else {
            return "fail";
        }
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        UserInfo userInfo = new UserInfo();
        ModelAndView mav = new ModelAndView("user/edit");
        if (StringUtils.isNotBlank(id)) {
            userInfo = userService.getById(Long.valueOf(id));

            //取出用户的角色
            if(userInfo != null){
                List<UserRoleInfo> userRoleInfoList = userRoleService.findListByUserId(userInfo.getId());
                List<String> roleIdList = new ArrayList<String>();
                for(UserRoleInfo info : userRoleInfoList){
                    roleIdList.add(String.valueOf(info.getRoleId()));
                }
               String roleIdStr = String.join(",",roleIdList);
                userInfo.setRoleIdStr(roleIdStr);
            }
        }
        mav.addObject("userInfo", userInfo);
        mav.addObject("roleList", roleService.getList());
        return mav;
    }

    @RequestMapping(value = "/doEdit")
    @ResponseBody
    public String doEdit(@ModelAttribute("userInfo") UserInfo userInfo) {
        Boolean isSuccess = userService.update(userInfo);
        if (isSuccess) {
            return "success";
        } else {
            return "fail";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        Boolean isSuccess = userService.delete(Long.valueOf(id));
        if (isSuccess) {
            return "success";
        } else {
            return "fail";
        }
    }

}
