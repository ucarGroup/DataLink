package com.ucar.datalink.manager.core.web.controller.user;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.RoleService;
import com.ucar.datalink.biz.service.UserRoleService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.domain.user.UserRoleInfo;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static AuditLogInfo getAuditLogInfo(UserInfo info, String menuCode, String operType) {
        AuditLogInfo logInfo = new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName(info.getUserName());
        logInfo.setOperType(operType);
        logInfo.setOperKey(info.getId());
        logInfo.setOperRecord(info.toString());
        return logInfo;
    }

    @RequestMapping(value = "/userList")
    public ModelAndView userList() {
        ModelAndView mav = new ModelAndView("user/list");
        return mav;
    }

    @RequestMapping(value = "/initUser")
    @ResponseBody
    public Page<UserInfo> initUser(@RequestBody Map<String, String> map) {
        Page<UserInfo> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        List<UserInfo> listUser = userService.getList();

        PageInfo<UserInfo> pageInfo = new PageInfo<>(listUser);
        page.setDraw(page.getDraw());
        page.setAaData(listUser);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
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
            AuditLogUtils.saveAuditLog(getAuditLogInfo(userInfo, "007001003", AuditLogOperType.insert.getValue()));
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
            if (userInfo != null) {
                List<UserRoleInfo> userRoleInfoList = userRoleService.findListByUserId(userInfo.getId());
                List<String> roleIdList = new ArrayList<String>();
                for (UserRoleInfo info : userRoleInfoList) {
                    roleIdList.add(String.valueOf(info.getRoleId()));
                }
                String roleIdStr = String.join(",", roleIdList);
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
            AuditLogUtils.saveAuditLog(getAuditLogInfo(userInfo, "007001005", AuditLogOperType.update.getValue()));
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
        Long idLong = Long.valueOf(id);
        UserInfo info = userService.getById(idLong);
        Boolean isSuccess = userService.delete(idLong);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(info, "007001006", AuditLogOperType.delete.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }

}
