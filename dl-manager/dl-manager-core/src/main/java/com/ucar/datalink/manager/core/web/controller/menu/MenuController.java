package com.ucar.datalink.manager.core.web.controller.menu;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.MenuService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.menu.MenuInfo;
import com.ucar.datalink.domain.menu.MenuType;
import com.ucar.datalink.manager.core.web.dto.menu.MenuView;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sqq on 2017/4/25.
 */
@Controller
@RequestMapping(value = "/menu/")
public class MenuController {

    @Autowired
    MenuService menuService;

    @RequestMapping(value = "/menuList")
    public ModelAndView menuList() {
        ModelAndView mav = new ModelAndView("menu/list");
        return mav;
    }

    @RequestMapping(value = "/initMenu")
    @ResponseBody
    public Page<MenuView> initMenu(@RequestBody Map<String, String> map) {
        Page<MenuView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

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
            return menuView;
        }).collect(Collectors.toList());

        PageInfo<MenuInfo> pageInfo = new PageInfo<MenuInfo>(menuInfos);
        page.setDraw(page.getDraw());
        page.setAaData(menuViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("menu/add");
        List<MenuInfo> menuList = menuService.getList();
        mav.addObject("menuList", menuList);
        mav.addObject("menuTypeList", MenuType.getAllMenuTypes());
        return mav;
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(@ModelAttribute("menuInfo") MenuInfo menuInfo) {
        Boolean isSuccess = menuService.insert(menuInfo);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(menuInfo, "007002003", AuditLogOperType.insert.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }

    private static AuditLogInfo getAuditLogInfo(MenuInfo info, String menuCode, String operType){
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
        MenuInfo menuInfo = new MenuInfo();
        ModelAndView mav = new ModelAndView("menu/edit");
        List<MenuInfo> menuList = menuService.getList();
        if (StringUtils.isNotBlank(id)) {
            menuInfo = menuService.getById(Long.valueOf(id));
        }
        mav.addObject("menuInfo", menuInfo);
        mav.addObject("menuList", menuList);
        mav.addObject("menuTypeList", MenuType.getAllMenuTypes());
        return mav;
    }

    @RequestMapping(value = "/doEdit")
    @ResponseBody
    public String doEdit(@ModelAttribute("menuInfo") MenuInfo menuInfo) {
        Boolean isSuccess = menuService.update(menuInfo);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(menuInfo, "007002005", AuditLogOperType.update.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }

    @RequestMapping(value = "/doDelete")
    @ResponseBody
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        Long idLong = Long.valueOf(id);
        MenuInfo info = menuService.getById(idLong);
        Boolean isSuccess = menuService.delete(idLong);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(info, "007002006", AuditLogOperType.delete.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }

}
