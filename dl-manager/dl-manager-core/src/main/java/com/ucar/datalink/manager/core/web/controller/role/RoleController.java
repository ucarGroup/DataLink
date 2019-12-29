package com.ucar.datalink.manager.core.web.controller.role;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.AuthorityService;
import com.ucar.datalink.biz.service.MenuService;
import com.ucar.datalink.biz.service.RoleService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.authority.RoleAuthorityInfo;
import com.ucar.datalink.domain.menu.MenuInfo;
import com.ucar.datalink.domain.user.RoleInfo;
import com.ucar.datalink.manager.core.web.dto.login.RoleView;
import com.ucar.datalink.manager.core.web.dto.menu.MenuView;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by sqq on 2017/5/4.
 */
@Controller
@RequestMapping(value = "/role/")
public class RoleController {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RoleController.class);

    @Autowired
    RoleService roleService;

    @Autowired
    AuthorityService authorityService;

    @Autowired
    MenuService menuService;

    @RequestMapping(value = "/roleList")
    public ModelAndView roleList() {
        ModelAndView mav = new ModelAndView("role/list");
        return mav;
    }

    @RequestMapping(value = "/initRole")
    @ResponseBody
    public Page<RoleView> initRole(@RequestBody Map<String, String> map) {
        Page<RoleView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        List<RoleInfo> roleInfos = roleService.getList();
        List<RoleView> roleViews = roleInfos.stream().map(i -> {
            RoleView roleView = new RoleView();
            roleView.setId(i.getId());
            roleView.setCode(i.getCode());
            roleView.setName(i.getName());
            return roleView;
        }).collect(Collectors.toList());

        PageInfo<RoleInfo> pageInfo = new PageInfo<RoleInfo>(roleInfos);
        page.setDraw(page.getDraw());
        page.setAaData(roleViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        return new ModelAndView("role/add");
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(@ModelAttribute("roleInfo") RoleInfo roleInfo) {
        try {
            Boolean isSuccess = roleService.insert(roleInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(roleInfo, "007003003", AuditLogOperType.insert.getValue()));
                return "success";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "fail";
    }
    private static AuditLogInfo getAuditLogInfo(RoleInfo info, String menuCode, String operType){
        AuditLogInfo logInfo=new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName(info.getName());
        logInfo.setOperType(operType);
        logInfo.setOperKey(info.getId());
        logInfo.setOperRecord(info.toString());
        return logInfo;
    }
    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        RoleInfo roleInfo = new RoleInfo();
        ModelAndView mav = new ModelAndView("role/edit");
        if (StringUtils.isNotBlank(id)) {
            roleInfo = roleService.getById(Long.valueOf(id));
            List<RoleAuthorityInfo> list = authorityService.getListByRoleId(Long.valueOf(id));
        }
        mav.addObject("roleInfo", roleInfo);
        return mav;
    }

    @RequestMapping(value = "/doEdit")
    @ResponseBody
    public String doEdit(@ModelAttribute("roleInfo") RoleInfo roleInfo) {
        try {
            if (roleInfo.getId() == null) {
                throw new RuntimeException("roleId is empty");
            }
            Boolean isSuccess = roleService.update(roleInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(roleInfo, "007003005", AuditLogOperType.update.getValue()));
                return "success";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/initAuthority")
    @ResponseBody
    public Page<MenuView> initAuthority(HttpServletRequest request) {
        Long roleId = Long.valueOf(request.getParameter("roleId"));
        List<RoleAuthorityInfo> authorityList = authorityService.getListByRoleId(roleId);
        List<MenuInfo> menuInfos = menuService.getList();
        List<MenuView> menuViews = menuInfos.stream().map(i -> {
            MenuView menuView = new MenuView();
            menuView.setId(i.getId());
            menuView.setCode(i.getCode());
            menuView.setName(i.getName());
            menuView.setParentCode(i.getParentCode());
            menuView.setType(i.getType());
            menuView.setUrl(i.getUrl());
            menuView.setIcon(i.getIcon());
            for (RoleAuthorityInfo authorityInfo : authorityList) {
                if (Objects.equals(i.getId(), authorityInfo.getMenuId())) {
                    menuView.setCheckFlag(1);
                    break;
                } else {
                    menuView.setCheckFlag(0);
                }
            }
            return menuView;
        }).collect(Collectors.toList());
        return new Page<MenuView>(menuViews);
    }

    @RequestMapping(value = "/doEditAuthority")
    @ResponseBody
    public String doEditAuthority(@RequestBody Map<String, String> requestParams) {
        Long roleId = Long.valueOf(requestParams.get("roleId"));
        String ids = requestParams.get("menuIds");
        JSONArray jsonArray = JSONArray.parseArray(ids);
        List<Long> menuList = new ArrayList<Long>();
        for (Object aJsonArray : jsonArray) {
            menuList.add(Long.valueOf(aJsonArray.toString()));
        }
        try {
            roleService.doRoleAuthority(roleId, menuList);
            return "success";
        } catch (Exception e) {
            LOGGER.error("分配权限失败", e);
            return "fail";
        }
    }

    @RequestMapping(value = "/doDelete")
    @ResponseBody
    public String doDelete(HttpServletRequest request) {
        try {
            String id = request.getParameter("id");
            if (StringUtils.isBlank(id)) {
                return "fail";
            }
            Long idLong = Long.valueOf(id);
            RoleInfo info = roleService.getById(idLong);
            Boolean isSuccess = roleService.delete(idLong);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(info, "007003006", AuditLogOperType.delete.getValue()));
                return "success";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "fail";
    }
}
