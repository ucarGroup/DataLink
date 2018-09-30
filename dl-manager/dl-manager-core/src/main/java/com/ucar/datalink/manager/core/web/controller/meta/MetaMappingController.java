package com.ucar.datalink.manager.core.web.controller.meta;

import com.ucar.datalink.biz.meta.MetaMapping;
import com.ucar.datalink.biz.service.MetaMappingService;
import com.ucar.datalink.domain.meta.MetaMappingInfo;
import com.ucar.datalink.manager.core.web.dto.meta.MetaMappingView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import com.ucar.datalink.common.errors.ValidationException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by user on 2017/11/3.
 */

@Controller
@RequestMapping(value = "/metaMapping/")
public class MetaMappingController {

    private static final Logger logger = LoggerFactory.getLogger(MetaMappingController.class);


    @Autowired
    MetaMappingService service;



    @RequestMapping(value = "/metaMappingList")
    public ModelAndView jobList() {
        ModelAndView mav = new ModelAndView("metaMapping/list");
        return mav;
    }


    @RequestMapping(value = "/initMapping")
    @ResponseBody
    public Page<MetaMappingView> initJobs(@RequestBody Map<String, String> map) {
        String src = map.get("srcType");
        String target = map.get("destType");
        if( StringUtils.isBlank(src) || "-1".equals(src)) {
            src = null;
        }
        if( StringUtils.isBlank(target) || "-1".equals(target) ) {
            target = null;
        }
        List<MetaMappingInfo> list = service.queryAllMetaMappingByType(src, target);
        List<MetaMappingView> metaView = list.stream().map(i -> {
            MetaMappingView view = new MetaMappingView();
            view.setId(i.getId());
            view.setSrcMediaSourceType(i.getSrcMediaSourceType());
            view.setTargetMediaSourceType(i.getTargetMediaSourceType());
            view.setSrcMappingType(i.getSrcMappingType());
            view.setTargetMappingType(i.getTargetMappingType());
            return view;
        }).collect(Collectors.toList());
        return new Page<>(metaView);
    }



    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("metaMapping/add");
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("view") MetaMappingView view) {
        try {
            MetaMappingInfo info = new MetaMappingInfo();
            info.setSrcMediaSourceType(view.getSrcMediaSourceType());
            info.setTargetMediaSourceType(view.getTargetMediaSourceType());
            info.setSrcMappingType(view.getSrcMappingType());
            info.setTargetMappingType(view.getTargetMappingType());
            service.createMetaMapping(info);
            MetaMapping.load();
            return "success";
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }


    @ResponseBody
    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("metaMapping/edit");
        MetaMappingInfo info = null;
        if (StringUtils.isNotBlank(id)) {
            info = service.queryMetaMappingById( Long.parseLong(id) );
        }
        MetaMappingView view = new MetaMappingView();
        if (info != null) {
            view.setId(info.getId());
            view.setSrcMediaSourceType(info.getSrcMediaSourceType());
            view.setTargetMediaSourceType(info.getTargetMediaSourceType());
            view.setSrcMappingType(info.getSrcMappingType());
            view.setTargetMappingType(info.getTargetMappingType());
        }
        mav.addObject("metaMappingView", view);
        return mav;
    }


    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("view") MetaMappingView view) {
        try {
            MetaMappingInfo info = new MetaMappingInfo();
            info.setId(view.getId());
            info.setSrcMediaSourceType(view.getSrcMediaSourceType());
            info.setTargetMediaSourceType(view.getTargetMediaSourceType());
            info.setSrcMappingType(view.getSrcMappingType());
            info.setTargetMappingType(view.getTargetMappingType());
            service.modifyMetaMapping(info);
            MetaMapping.load();
            return "success";
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }


    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            service.deleteMetaMappingById( Long.parseLong(id) );
            return "success";
        } catch (ValidationException e) {
            logger.error("delete job media mapping failure.", e);
            return e.getMessage();
        }
    }


    @ResponseBody
    @RequestMapping(value = "/doReload")
    public String doReload(HttpServletRequest request) {
        try {
            MetaMapping.load();
            return "success";
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
            return "failure";
        }
    }



}
