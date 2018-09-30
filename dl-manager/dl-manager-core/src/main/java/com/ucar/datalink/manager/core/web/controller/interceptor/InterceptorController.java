package com.ucar.datalink.manager.core.web.controller.interceptor;

import com.ucar.datalink.biz.service.InterceptorService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.interceptor.InterceptorInfo;
import com.ucar.datalink.domain.interceptor.InterceptorType;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.interceptor.InterceptorView;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sqq on 2017/5/24.
 */
@Controller
@RequestMapping(value = "/interceptor")
public class InterceptorController {

    @Autowired
    InterceptorService interceptorService;

    @RequestMapping(value = "/interceptorList")
    public ModelAndView interceptorList() {
        ModelAndView mav = new ModelAndView("interceptor/list");
        return mav;
    }

    @RequestMapping(value = "/initInterceptor")
    @ResponseBody
    public Page<InterceptorView> initInterceptor() {
        List<InterceptorInfo> interceptorLists = interceptorService.getList();
        List<InterceptorView> interceptorViews = interceptorLists.stream().map(i -> {
            InterceptorView view = new InterceptorView();
            view.setId(i.getId());
            view.setName(i.getName());
            view.setDesc(i.getDesc());
            view.setType(i.getType());
            view.setContent(i.getContent());
            view.setCreateTime(i.getCreateTime());
            return view;
        }).collect(Collectors.toList());
        return new Page<InterceptorView>(interceptorViews);
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("interceptor/add");
        mav.addObject("interceptorTypeList", InterceptorType.getAllInterceptorTypes());
        return mav;
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(@ModelAttribute("interceptorInfo") InterceptorInfo interceptorInfo) {
        Boolean isSuccess = interceptorService.insert(interceptorInfo);
        if (isSuccess) {
            return "success";
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        InterceptorInfo interceptorInfo = new InterceptorInfo();
        ModelAndView mav = new ModelAndView("interceptor/edit");
        if (StringUtils.isNotBlank(id)) {
            interceptorInfo = interceptorService.getInterceptorById(Long.valueOf(id));
        }
        mav.addObject("interceptorInfo", interceptorInfo);
        mav.addObject("interceptorTypeList", InterceptorType.getAllInterceptorTypes());
        return mav;
    }

    @RequestMapping(value = "/doEdit")
    @ResponseBody
    public String doEdit(@ModelAttribute("interceptorInfo") InterceptorInfo interceptorInfo) {
        Long interceptorId = interceptorInfo.getId();
        if (null == interceptorId) {
            throw new RuntimeException("interceptorId is empty");
        }
        Boolean isSuccess = interceptorService.update(interceptorInfo);
        toReload(interceptorId.toString());
        if (isSuccess) {
            return "success";
        }
        return "fail";
    }

    @RequestMapping(value = "/doDelete")
    @ResponseBody
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            Boolean isSuccess = interceptorService.delete(Long.valueOf(id));
            if (isSuccess) {
                return "success";
            }
        } catch (ValidationException e) {
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/toReload")
    public String toReload(String interceptorId) {
        try {
            if (StringUtils.isBlank(interceptorId)) {
                throw new RuntimeException("interceptorId is empty");
            }
            GroupMetadataManager groupMetadataManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
            ClusterState clusterState = groupMetadataManager.getClusterState();
            if (clusterState == null) {
                return "success";
            }
            List<ClusterState.MemberData> memberDatas = clusterState.getAllMemberData();
            if (memberDatas == null || memberDatas.size() == 0) {
                return "success";
            }
            for (ClusterState.MemberData mem : memberDatas) {
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadInterceptor/" + interceptorId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);
                new RestTemplate().postForObject(url, request, Map.class);
            }
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
