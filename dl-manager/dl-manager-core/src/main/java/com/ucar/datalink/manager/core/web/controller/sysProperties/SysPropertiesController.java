package com.ucar.datalink.manager.core.web.controller.sysProperties;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.SysPropertiesService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;
import com.ucar.datalink.domain.user.UserInfo;
import com.ucar.datalink.domain.user.UserRoleInfo;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by djj on 2018/7/5.
 */
@Controller
@RequestMapping(value = "/sysProperties/")
public class SysPropertiesController {

    @Autowired
    SysPropertiesService sysPropertiesService;

    @RequestMapping(value = "/propertieList")
    public String userList() {
        return "sysProperties/list";
    }

    @RequestMapping(value = "/intPropertiesList")
    @ResponseBody
    public Page<SysPropertiesInfo> propertiesList(@RequestBody Map<String, String> map) {

        Page<SysPropertiesInfo> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        List<SysPropertiesInfo> sysPropertieList = sysPropertiesService.findSysPropertieList();

        PageInfo<SysPropertiesInfo> pageInfo = new PageInfo<SysPropertiesInfo>(sysPropertieList);
        page.setDraw(page.getDraw());
        page.setAaData(sysPropertieList);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        return page;
    }

    @RequestMapping(value = "/toAdd")
    public String toAdd() {
        return "sysProperties/add";
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(@ModelAttribute("userInfo") SysPropertiesInfo sysPropertiesInfo) {
        Boolean isSuccess = sysPropertiesService.insert(sysPropertiesInfo);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(sysPropertiesInfo, "007005003", AuditLogOperType.insert.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }
    private static AuditLogInfo getAuditLogInfo(SysPropertiesInfo info, String menuCode, String operType){
        AuditLogInfo logInfo=new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName(info.getPropertiesKey());
        logInfo.setOperType(operType);
        logInfo.setOperKey(info.getId());
        logInfo.setOperRecord(info.toString());
        return logInfo;
    }
    @RequestMapping(value = "/toEdit")
    public String toEdit(Long id,Model model) {
        SysPropertiesInfo sysPropertiesInfo;
        sysPropertiesInfo = sysPropertiesService.getSysPropertiesById(Long.valueOf(id));
        model.addAttribute("sysPropertiesInfo",sysPropertiesInfo);
        return "sysProperties/edit";
    }

    @RequestMapping(value = "/doEdit")
    @ResponseBody
    public String doEdit(@ModelAttribute("sysPropertiesInfo") SysPropertiesInfo sysPropertiesInfo) {
        Boolean isSuccess = sysPropertiesService.update(sysPropertiesInfo);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(sysPropertiesInfo, "007005005", AuditLogOperType.update.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(Long id) {
        if (id == null) {
            return "fail";
        }
        SysPropertiesInfo info = sysPropertiesService.getSysPropertiesById(id);
        Boolean isSuccess = sysPropertiesService.delete(id);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(info, "007005006", AuditLogOperType.delete.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }

}
