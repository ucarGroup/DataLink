package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.hdfs.HDFSMediaSrcParameter;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaSource.HDFSMediaSourceView;
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
 * Created by sqq on 2017/6/16.
 */
@Controller
@RequestMapping(value = "/hdfs")
public class HDFSMediaSourceController {

    private static final Logger logger = LoggerFactory.getLogger(HDFSMediaSourceController.class);

    @Autowired
    MediaSourceService mediaSourceService;

    @RequestMapping(value = "/hdfsList")
    public ModelAndView hdfsList(){
        ModelAndView mav = new ModelAndView("hdfsMediaSource/list");
        return mav;
    }

    @RequestMapping(value = "/initHDFSList")
    @ResponseBody
    public Page<HDFSMediaSourceView> initHDFSList(){
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.HDFS);
        List<MediaSourceInfo> hdfsMediaSourceList = mediaSourceService.getListByType(setMediaSource);
        List<HDFSMediaSourceView> msView = hdfsMediaSourceList.stream().map(i -> {
            HDFSMediaSourceView view = new HDFSMediaSourceView();
            view.setId(i.getId());
            view.setName(i.getName());
            view.setDesc(i.getDesc());
            view.setCreateTime(i.getCreateTime());
            view.getHdfsMediaSrcParameter().setMediaSourceType(i.getType());
            view.getHdfsMediaSrcParameter().setNameServices(((HDFSMediaSrcParameter) i.getParameterObj()).getNameServices());
            view.getHdfsMediaSrcParameter().setNameNode1(((HDFSMediaSrcParameter) i.getParameterObj()).getNameNode1());
            view.getHdfsMediaSrcParameter().setNameNode2(((HDFSMediaSrcParameter) i.getParameterObj()).getNameNode2());
            view.getHdfsMediaSrcParameter().setHadoopUser(((HDFSMediaSrcParameter) i.getParameterObj()).getHadoopUser());
            view.getHdfsMediaSrcParameter().setZkMediaSourceId(((HDFSMediaSrcParameter) i.getParameterObj()).getZkMediaSourceId());
//            view.getHdfsMediaSrcParameter().setSparkcubeAddress(((HDFSMediaSrcParameter) i.getParameterObj()).getSparkcubeAddress());
            MediaSourceInfo zk = mediaSourceService.getById(view.getHdfsMediaSrcParameter().getZkMediaSourceId());
            if (zk != null) {
                view.setZkMediaSourceName(zk.getName());
            }
            return view;
        }).collect(Collectors.toList());
        return new Page<HDFSMediaSourceView>(msView);
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("hdfsMediaSource/add");
        mav.addObject("zkMediaSourceList", initZkList());
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("hdfsMediaSourceView") HDFSMediaSourceView hdfsMediaSourceView) {
        try {
            Boolean isSuccess = mediaSourceService.insert(buildHDFSMediaSourceInfo(hdfsMediaSourceView));
            if (isSuccess) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add HDFS Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("hdfsMediaSource/edit");
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        if (StringUtils.isNotBlank(id)) {
            mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
        }
        HDFSMediaSrcParameter hdfsMediaSrcParameter = mediaSourceInfo.getParameterObj();
        mav.addObject("zkMediaSourceList", initZkList());
        mav.addObject("hdfsMediaSourceView", mediaSourceInfo);
        mav.addObject("hdfsMediaSrcParameter", hdfsMediaSrcParameter);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("hdfsMediaSourceView") HDFSMediaSourceView hdfsMediaSourceView) {
        try {
            if (hdfsMediaSourceView.getId() == null) {
                throw new RuntimeException("hdfsMediaSourceId is empty");
            }
            MediaSourceInfo mediaSourceInfo = buildHDFSMediaSourceInfo(hdfsMediaSourceView);
            mediaSourceInfo.setId(hdfsMediaSourceView.getId());
            Boolean isSuccess = mediaSourceService.update(mediaSourceInfo);
            toReloadDB(hdfsMediaSourceView.getId().toString());
            if (isSuccess) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("Update HDFS Media Source Error.", e);
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
            Boolean isSuccess = mediaSourceService.delete(Long.valueOf(id));
            if (isSuccess) {
                return "success";
            }
        } catch (ValidationException e) {
            logger.error("Delete HDFS Media Source Error.", e);
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
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadMediaSource/" + mediaSourceId;
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

    private MediaSourceInfo buildHDFSMediaSourceInfo(HDFSMediaSourceView hdfsMediaSourceView) {
        MediaSourceInfo hdfsMediaSourceInfo = new MediaSourceInfo();
        hdfsMediaSourceInfo.setName(hdfsMediaSourceView.getName());
        hdfsMediaSourceInfo.setDesc(hdfsMediaSourceView.getDesc());
        hdfsMediaSourceInfo.setType(hdfsMediaSourceView.getHdfsMediaSrcParameter().getMediaSourceType());
        hdfsMediaSourceInfo.setParameter(hdfsMediaSourceView.getHdfsMediaSrcParameter().toJsonString());
        return hdfsMediaSourceInfo;
    }

    private List<MediaSourceInfo> initZkList(){
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.ZOOKEEPER);
        return mediaSourceService.getListByType(setMediaSource);
    }
}
