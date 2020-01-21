package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.dove.DoveMediaSrcParameter;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaSource.DoveMediaSourceView;
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
@RequestMapping(value = "/dove")
public class DoveMediaSourceController {

    private static final Logger logger = LoggerFactory.getLogger(DoveMediaSourceController.class);

    @Autowired
    MediaSourceService mediaSourceService;

    @RequestMapping(value = "/doveList")
    public ModelAndView doveList() {
        ModelAndView mav = new ModelAndView("doveMediaSource/list");
        return mav;
    }

    @RequestMapping(value = "/initDove")
    @ResponseBody
    public Page<DoveMediaSourceView> initDove(@RequestBody Map<String, String> map) {
        Page<DoveMediaSourceView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());

        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.DOVE);
        List<MediaSourceInfo> doveMediaSourceList = mediaSourceService.getListByType(setMediaSource);
        List<DoveMediaSourceView> list = doveMediaSourceList.stream().map(i -> {
            DoveMediaSourceView view = new DoveMediaSourceView();
            view.setId(i.getId());
            view.setName(i.getName());
            view.setDesc(i.getDesc());
            view.setCreateTime(i.getCreateTime());
            view.getDoveMediaSrcParameter().setMediaSourceType(i.getType());
            view.getDoveMediaSrcParameter().setTopic(((DoveMediaSrcParameter) i.getParameterObj()).getTopic());
            view.getDoveMediaSrcParameter().setZkMediaSourceId(((DoveMediaSrcParameter) i.getParameterObj()).getZkMediaSourceId());
            view.getDoveMediaSrcParameter().setClusterPrefix(((DoveMediaSrcParameter) i.getParameterObj()).getClusterPrefix());
            MediaSourceInfo zk = mediaSourceService.getById(view.getDoveMediaSrcParameter().getZkMediaSourceId());
            if (zk != null) {
                view.setZkMediaSourceName(zk.getName());
            }
            return view;
        }).collect(Collectors.toList());

        PageInfo<MediaSourceInfo> pageInfo = new PageInfo<MediaSourceInfo>(doveMediaSourceList);
        page.setDraw(page.getDraw());
        page.setAaData(list);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("doveMediaSource/add");
        mav.addObject("zkMediaSourceList", initZkList());
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("DoveMediaSourceView") DoveMediaSourceView doveMediaSourceView) {
        try {
            MediaSourceInfo mediaSourceInfo = buildDoveMediaSourceInfo(doveMediaSourceView);
            Boolean isSuccess = mediaSourceService.insert(mediaSourceInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                        , "002012003", AuditLogOperType.insert.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add Dove Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("doveMediaSource/edit");
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        if (StringUtils.isNotBlank(id)) {
            mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
        }
        DoveMediaSrcParameter doveMediaSrcParameter = mediaSourceInfo.getParameterObj();
        mav.addObject("zkMediaSourceList", initZkList());
        mav.addObject("doveMediaSourceView", mediaSourceInfo);
        mav.addObject("doveMediaSrcParameter", doveMediaSrcParameter);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("DoveMediaSourceView") DoveMediaSourceView doveMediaSourceView) {
        try {
            if (doveMediaSourceView.getId() == null) {
                throw new RuntimeException("doveMediaSourceId is empty");
            }
            MediaSourceInfo mediaSourceInfo = buildDoveMediaSourceInfo(doveMediaSourceView);
            mediaSourceInfo.setId(doveMediaSourceView.getId());
            Boolean isSuccess = mediaSourceService.update(mediaSourceInfo);
            toReloadDB(doveMediaSourceView.getId().toString());
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                        , "002012005", AuditLogOperType.update.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("Update Dove Media Source Error.", e);
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
                        , "002012006", AuditLogOperType.delete.getValue()));
                return "success";
            }
        } catch (ValidationException e) {
            logger.error("Delete Dove Media Source Error.", e);
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
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadDove/" + mediaSourceId;
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

    private MediaSourceInfo buildDoveMediaSourceInfo(DoveMediaSourceView doveMediaSourceView) {
        MediaSourceInfo doveMediaSourceInfo = new MediaSourceInfo();
        doveMediaSourceInfo.setName(doveMediaSourceView.getName());
        doveMediaSourceInfo.setDesc(doveMediaSourceView.getDesc());
        doveMediaSourceView.getDoveMediaSrcParameter().setMediaSourceType(MediaSourceType.DOVE);
        doveMediaSourceInfo.setType(doveMediaSourceView.getDoveMediaSrcParameter().getMediaSourceType());
        doveMediaSourceInfo.setParameter(doveMediaSourceView.getDoveMediaSrcParameter().toJsonString());
        return doveMediaSourceInfo;
    }

    private List<MediaSourceInfo> initZkList(){
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.ZOOKEEPER);
        return mediaSourceService.getListByType(setMediaSource);
    }
}
