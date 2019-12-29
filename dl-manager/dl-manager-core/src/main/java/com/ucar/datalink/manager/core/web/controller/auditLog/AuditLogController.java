package com.ucar.datalink.manager.core.web.controller.auditLog;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Strings;
import com.ucar.datalink.biz.service.AuditLogService;
import com.ucar.datalink.biz.service.MenuService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.menu.MenuInfo;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.manager.core.web.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyifan
 */
@Controller
@RequestMapping(value = "/auditLog/")
public class AuditLogController {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Autowired
    AuditLogService auditLogService;

    @Autowired
    UserService userService;

    @Autowired
    MenuService menuService;

    @RequestMapping(value = "/auditLogList")
    public ModelAndView auditLogList() {
        ModelAndView mav = new ModelAndView("auditLog/list");
        AuditLogOperType[] auditLogOperTypes = AuditLogOperType.values();
        List<UserInfo> userInfoList = userService.getList();
        List<MenuInfo> menuInfoList = menuService.getList();
        mav.addObject("auditLogOperTypeList", auditLogOperTypes);
        mav.addObject("userInfoList", userInfoList);
        mav.addObject("menuInfoList", menuInfoList);
        return mav;
    }

    @RequestMapping(value = "/initAuditLog")
    @ResponseBody
    public Page<AuditLogInfo> initAuditLog(@RequestBody Map<String, String> map) {
        Page<AuditLogInfo> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        String operType = map.get("operType");
        String userId = map.get("userId");
        Long userIdLong=null;
        if(!Strings.isNullOrEmpty(userId)){
            userIdLong = Long.valueOf(userId);
        }
        String menuCode = map.get("menuCode");
        String operKey = map.get("operKey");
        Long operKeyLong=null;
        if (!Strings.isNullOrEmpty(operKey)){
            operKeyLong = Long.valueOf(operKey);
        }
        String operName = map.get("operName");

        AuditLogInfo param=new AuditLogInfo();
        param.setOperType(operType);
        param.setUserId(userIdLong);
        param.setMenuCode(menuCode);
        param.setOperKey(operKeyLong);
        param.setOperName(operName);
        List<AuditLogInfo> listAuditLog = auditLogService.getListByParam(param);
        for (AuditLogInfo info : listAuditLog) {
            info.setOperTimeStr(simpleDateFormat.format(info.getOperTime()));
            info.setOperType(AuditLogOperType.getDescFromValue(info.getOperType()));
        }

        PageInfo<AuditLogInfo> pageInfo = new PageInfo<>(listAuditLog);
        page.setDraw(page.getDraw());
        page.setAaData(listAuditLog);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }




}
