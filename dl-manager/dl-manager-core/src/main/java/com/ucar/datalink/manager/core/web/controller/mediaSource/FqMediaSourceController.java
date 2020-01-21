package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.fq.FqMediaSrcParameter;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaSource.FqMediaSourceView;
import com.ucar.datalink.manager.core.web.util.AuditLogInfoUtil;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by sqq on 2017/5/16.
 */
@Controller
@RequestMapping(value = "/fq/")
public class FqMediaSourceController {

    private static final Logger logger = LoggerFactory.getLogger(FqMediaSourceController.class);

    @Autowired
    MediaSourceService mediaSourceService;

    @RequestMapping(value = "/fqList")
    public ModelAndView fqList() {
        ModelAndView mav = new ModelAndView("fqMediaSource/list");
        return mav;
    }

    @RequestMapping(value = "/initFq")
    @ResponseBody
    public Page<FqMediaSourceView> initFq(@RequestBody Map<String, String> map) {
        Page<FqMediaSourceView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.FLEXIBLEQ);
        List<MediaSourceInfo> fqMediaSourceList = mediaSourceService.getListByType(setMediaSource);
        List<FqMediaSourceView> taskView = fqMediaSourceList.stream().map(i -> {
            FqMediaSourceView view = new FqMediaSourceView();
            view.setId(i.getId());
            view.setName(i.getName());
            view.setDesc(i.getDesc());
            view.setCreateTime(i.getCreateTime());
            view.getFqMediaSrcParameter().setMediaSourceType(i.getType());
            view.getFqMediaSrcParameter().setTopic(((FqMediaSrcParameter) i.getParameterObj()).getTopic());
            view.getFqMediaSrcParameter().setZkMediaSourceId(((FqMediaSrcParameter) i.getParameterObj()).getZkMediaSourceId());
            view.getFqMediaSrcParameter().setClusterPrefix(((FqMediaSrcParameter) i.getParameterObj()).getClusterPrefix());
            MediaSourceInfo zk = mediaSourceService.getById(view.getFqMediaSrcParameter().getZkMediaSourceId());
            if (zk != null) {
                view.setZkMediaSourceName(zk.getName());
            }
            return view;
        }).collect(Collectors.toList());

        PageInfo<MediaSourceInfo> pageInfo = new PageInfo<MediaSourceInfo>(fqMediaSourceList);
        page.setDraw(page.getDraw());
        page.setAaData(taskView);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("fqMediaSource/add");
        mav.addObject("zkMediaSourceList", initZkList());
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("FqMediaSourceView") FqMediaSourceView fqMediaSourceView) {
        try {
            MediaSourceInfo mediaSourceInfo = buildFqMediaSourceInfo(fqMediaSourceView);
            Boolean isSuccess = mediaSourceService.insert(mediaSourceInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                        , "002005003", AuditLogOperType.insert.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add Fq Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("fqMediaSource/edit");
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        if (StringUtils.isNotBlank(id)) {
            mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
        }
        FqMediaSrcParameter fqMediaSrcParameter = mediaSourceInfo.getParameterObj();
        mav.addObject("zkMediaSourceList", initZkList());
        mav.addObject("fqMediaSourceView", mediaSourceInfo);
        mav.addObject("fqMediaSrcParameter", fqMediaSrcParameter);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("FqMediaSourceView") FqMediaSourceView fqMediaSourceView) {
        try {
            if (fqMediaSourceView.getId() == null) {
                throw new RuntimeException("fqMediaSourceId is empty");
            }
            MediaSourceInfo mediaSourceInfo = buildFqMediaSourceInfo(fqMediaSourceView);
            mediaSourceInfo.setId(fqMediaSourceView.getId());
            Boolean isSuccess = mediaSourceService.update(mediaSourceInfo);
            toReloadDB(fqMediaSourceView.getId().toString());
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                        , "002005005", AuditLogOperType.update.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("Update Fq Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)) {
            return "fail";
        }
        try {
            Long idLong = Long.valueOf(id);
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(idLong);
            Boolean isSuccess = mediaSourceService.delete(idLong);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                        , "002005006", AuditLogOperType.delete.getValue()));
                return "success";
            }
        } catch (ValidationException e) {
            logger.error("Delete Fq Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/toReloadDB")
    public String toReloadDB(String mediaSourceId) {
        try {
            if (StringUtils.isBlank(mediaSourceId)) {
                throw new RuntimeException("mediaSourceId is empty");
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
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadFQ/" + mediaSourceId;
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

    private MediaSourceInfo buildFqMediaSourceInfo(FqMediaSourceView fqMediaSourceView) {
        MediaSourceInfo fqMediaSourceInfo = new MediaSourceInfo();
        fqMediaSourceInfo.setName(fqMediaSourceView.getName());
        fqMediaSourceInfo.setDesc(fqMediaSourceView.getDesc());
        fqMediaSourceInfo.setType(fqMediaSourceView.getFqMediaSrcParameter().getMediaSourceType());
        fqMediaSourceInfo.setParameter(fqMediaSourceView.getFqMediaSrcParameter().toJsonString());
        return fqMediaSourceInfo;
    }

    private List<MediaSourceInfo> initZkList(){
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.ZOOKEEPER);
        return mediaSourceService.getListByType(setMediaSource);
    }
}
