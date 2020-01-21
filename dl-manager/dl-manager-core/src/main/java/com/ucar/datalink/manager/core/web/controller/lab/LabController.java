package com.ucar.datalink.manager.core.web.controller.lab;

import com.ucar.datalink.biz.service.DoubleCenterService;
import com.ucar.datalink.biz.service.LabService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.lab.LabInfo;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by djj on 2018/8/15.
 */
@Controller
@RequestMapping(value = "/lab")
public class LabController {

    @Autowired
    LabService labService;
    @Autowired
    DoubleCenterService doubleCenterService;

    @RequestMapping(value = "/labList")
    public String userList() {
        return "lab/list";
    }

    @RequestMapping(value = "/intLab")
    @ResponseBody
    public Page<LabInfo> propertiesList(Model model) {
        List<LabInfo> labList = labService.findLabList();
        labList.stream().map(labInfo -> {
            if(StringUtils.isBlank(labInfo.getIpRule())){
                labInfo.setIpRule("");
            }
            if(StringUtils.equals(doubleCenterService.getCenterLab(Constants.WHOLE_SYSTEM),labInfo.getLabName())){
                labInfo.setIsCenterLab(true);
            }else {
                labInfo.setIsCenterLab(false);
            }
            return labInfo;
        }).collect(Collectors.toList());

        return new Page<LabInfo>(labList);
    }

    @RequestMapping(value = "/toAdd")
    public String toAdd() {
        return "lab/add";
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(@ModelAttribute("labInfo") LabInfo LabInfo) {
        Boolean isSuccess = labService.insert(LabInfo);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(LabInfo, "001003003", AuditLogOperType.insert.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }
    private static AuditLogInfo getAuditLogInfo(LabInfo LabInfo, String menuCode, String operType){
        AuditLogInfo logInfo=new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName(LabInfo.getLabName());
        logInfo.setOperType(operType);
        logInfo.setOperKey(LabInfo.getId());
        logInfo.setOperRecord(LabInfo.toString());
        return logInfo;
    }
    @RequestMapping(value = "/toEdit")
    public String toEdit(Long id,Model model) {
        LabInfo labInfo = labService.getLabById(Long.valueOf(id));
        model.addAttribute("labInfo",labInfo);
        return "lab/edit";
    }

    @RequestMapping(value = "/doEdit")
    @ResponseBody
    public String doEdit(@ModelAttribute("labInfo")  LabInfo labInfo) {
        Boolean isSuccess = labService.update(labInfo);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(labInfo, "001003005", AuditLogOperType.update.getValue()));
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
        LabInfo labInfo = labService.getLabById(id);
        Boolean isSuccess = labService.delete(id);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(getAuditLogInfo(labInfo, "001003006", AuditLogOperType.delete.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }

}
