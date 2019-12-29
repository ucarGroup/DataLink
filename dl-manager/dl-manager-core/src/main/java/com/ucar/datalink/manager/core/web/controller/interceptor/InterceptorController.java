package com.ucar.datalink.manager.core.web.controller.interceptor;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.InterceptorService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.auditLog.AuditLogInfo;
import com.ucar.datalink.domain.interceptor.InterceptorInfo;
import com.ucar.datalink.domain.interceptor.InterceptorType;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.interceptor.InterceptorView;
import com.ucar.datalink.manager.core.web.util.Page;
import com.ucar.datalink.manager.core.web.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
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
    public Page<InterceptorView> initInterceptor(@RequestBody Map<String, String> map) {
        Page<InterceptorView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

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

        PageInfo<InterceptorInfo> pageInfo = new PageInfo<>(interceptorLists);
        page.setDraw(page.getDraw());
        page.setAaData(interceptorViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
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
            AuditLogUtils.saveAuditLog(getAuditLogInfo(interceptorInfo, "004030300", AuditLogOperType.insert.getValue()));
            return "success";
        }
        return "fail";
    }
    private static AuditLogInfo getAuditLogInfo(InterceptorInfo interceptorInfo, String menuCode, String operType){
        AuditLogInfo logInfo=new AuditLogInfo();
        logInfo.setUserId(UserUtil.getUserIdFromRequest());
        logInfo.setMenuCode(menuCode);
        logInfo.setOperName(interceptorInfo.getName());
        logInfo.setOperType(operType);
        logInfo.setOperKey(interceptorInfo.getId());
        logInfo.setOperRecord(interceptorInfo.toString());
        return logInfo;
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
            AuditLogUtils.saveAuditLog(getAuditLogInfo(interceptorInfo, "004030500", AuditLogOperType.update.getValue()));
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
            Long idLong = Long.valueOf(id);
            InterceptorInfo interceptorInfo = interceptorService.getInterceptorById(idLong);
            Boolean isSuccess = interceptorService.delete(idLong);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(getAuditLogInfo(interceptorInfo, "004030600", AuditLogOperType.delete.getValue()));
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
