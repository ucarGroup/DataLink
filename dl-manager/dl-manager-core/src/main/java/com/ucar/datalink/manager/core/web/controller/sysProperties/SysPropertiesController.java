package com.ucar.datalink.manager.core.web.controller.sysProperties;

import com.ucar.datalink.biz.service.SysPropertiesService;
import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;
import com.ucar.datalink.manager.core.web.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by djj on 2018/7/5.
 */
@Controller
@RequestMapping(value = "/sysProperties/")
public class SysPropertiesController {

    @Autowired
    SysPropertiesService sysPropertiesService;

    @RequestMapping(value = "/propertieList")
    public String propertieList() {
        return "sysProperties/list";
    }

    @RequestMapping(value = "/intPropertiesList")
    @ResponseBody
    public Page<SysPropertiesInfo> propertiesList(Model model) {
        List<SysPropertiesInfo> sysPropertieList = sysPropertiesService.findSysPropertieList();
        return new Page<SysPropertiesInfo>(sysPropertieList);
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
            return "success";
        } else {
            return "fail";
        }
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
        Boolean isSuccess = sysPropertiesService.delete(id);
        if (isSuccess) {
            return "success";
        } else {
            return "fail";
        }
    }

}
