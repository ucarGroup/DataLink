package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.DoubleCenterService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.biz.utils.DataSourceFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaSource.VirtualMediaSourceView;
import com.ucar.datalink.manager.core.web.util.AuditLogInfoUtil;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/virtual")
public class VirtualMediaSourceController {

    private static final Logger logger = LoggerFactory.getLogger(FqMediaSourceController.class);

    @Autowired
    MediaSourceService mediaSourceService;
    @Autowired
    DoubleCenterService doubleCenterService;

    @RequestMapping(value = "/virtualList")
    public String list() {
        return "virtualMediaSource/list";
    }

    @RequestMapping(value = "/intVirtual")
    @ResponseBody
    public Page<VirtualMediaSourceView> initList(@RequestBody Map<String, String> map) {

        String mediaSourceName = map.get("name");
        Set<MediaSourceType> setMediaSource = new HashSet<>();
        setMediaSource.add(MediaSourceType.VIRTUAL);

        Page<VirtualMediaSourceView> page = new Page<VirtualMediaSourceView>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<MediaSourceInfo> list = mediaSourceService.getListForQueryPage(setMediaSource,mediaSourceName,null);

        List<VirtualMediaSourceView> viewList = list.stream().map(mediaSourceInfo -> {
            VirtualMediaSourceView view = new VirtualMediaSourceView();
            view.setId(mediaSourceInfo.getId());
            view.setName(mediaSourceInfo.getName());
            view.setDesc(mediaSourceInfo.getDesc());
            view.setCreateTime(mediaSourceInfo.getCreateTime());
            view.setMediaSrcParameter(mediaSourceInfo.getParameterObj());

            //查询出真实db
            List<MediaSourceInfo> realList = mediaSourceService.findRealListByVirtualMsId(mediaSourceInfo.getId());
            List<String> nameList = new ArrayList<String>();
            for (MediaSourceInfo info : realList) {
                nameList.add(String.valueOf(info.getName()));
            }
            String realDbName = String.join(",", nameList);
            view.setRealDbNames(realDbName);
            view.setSimulateMsType(mediaSourceInfo.getSimulateMsType());

            //当前机房
            view.setCurrentLab(doubleCenterService.getCenterLab(mediaSourceInfo.getId()));
            return view;
        }).collect(Collectors.toList());

        PageInfo<MediaSourceInfo> pageInfo = new PageInfo<>(list);
        page.setDraw(page.getDraw());
        page.setAaData(viewList);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());

        return page;
    }

    @RequestMapping(value = "/toAdd")
    public String toAdd(Model model) {
        model.addAttribute("mediaSourceTypeList", MediaSourceType.getMediaSourceTypesForVirtual());

        return "virtualMediaSource/add";
    }

    @ResponseBody
    @RequestMapping(value = "/getMediaSources")
    public Map<String, Object> getMediaSources(String mediaSourceType) {
        Map<String, Object> map = new HashMap<>();
        MediaSourceType msType = MediaSourceType.valueOf(mediaSourceType);
        Set<MediaSourceType> typeSet = new HashSet<>();
        typeSet.add(msType);
        List<MediaSourceInfo> mediaSourceList = mediaSourceService.getListByType(typeSet);
        map.put("mediaSourceList", mediaSourceList);
        return map;
    }

    @RequestMapping(value = "/doAdd")
    @ResponseBody
    public String doAdd(VirtualMediaSourceView view) {
        try {
            MediaSourceInfo mediaSourceInfo = buildMediaSourceInfo(view);
            Boolean isSuccess = mediaSourceService.insertVirtual(mediaSourceInfo, view.getRealDbIds());
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                        , "002009003", AuditLogOperType.insert.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add virtual Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public String toEdit(Long id, Model model) {
        MediaSourceInfo info = mediaSourceService.getById(Long.valueOf(id));

        VirtualMediaSourceView view = new VirtualMediaSourceView();
        view.setId(info.getId());
        view.setName(info.getName());
        view.setDesc(info.getDesc());
        view.setSimulateMsType(info.getSimulateMsType());

        //查询出真实db
        List<MediaSourceInfo> realList = mediaSourceService.findRealListByVirtualMsId(info.getId());
        List<String> idList = new ArrayList<String>();
        for (MediaSourceInfo temp : realList) {
            idList.add(String.valueOf(temp.getId()));
        }
        String idStr = String.join(",", idList);
        view.setRealDbIds(idStr);
        model.addAttribute("mediaSourceInfo", view);

        model.addAttribute("mediaSourceTypeList", MediaSourceType.getMediaSourceTypesForVirtual());

        return "virtualMediaSource/edit";
    }

    @RequestMapping(value = "/doEdit")
    @ResponseBody
    public String doEdit(VirtualMediaSourceView view) {
        try {
            MediaSourceInfo info = buildMediaSourceInfo(view);
            info.setId(view.getId());
            Boolean isSuccess = mediaSourceService.updateVirtual(info, view.getRealDbIds());
            mediaSourceService.clearRealMediaSourceListCache(info.getId());
            toReloadDB(info.getId().toString());
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(info
                        , "002009005", AuditLogOperType.update.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("edit virtual Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/doDelete")
    public String doDelete(Long id) {
        if (id == null) {
            return "fail";
        }
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(id);
        Boolean isSuccess = mediaSourceService.delete(id);
        if (isSuccess) {
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                    , "002009006", AuditLogOperType.delete.getValue()));
            return "success";
        } else {
            return "fail";
        }
    }

    private MediaSourceInfo buildMediaSourceInfo(VirtualMediaSourceView view) {
        MediaSourceInfo info = new MediaSourceInfo();
        info.setName(view.getName());
        info.setType(MediaSourceType.VIRTUAL);
        info.setDesc(view.getDesc());
        view.getMediaSrcParameter().setMediaSourceType(MediaSourceType.VIRTUAL);
        if (view.getSimulateMsType().isRdbms()) {
            view.getMediaSrcParameter().setNamespace(view.getName());
        }
        //真实数据源id
        String[] ids = view.getRealDbIds().split(",");
        List<Long> list = new ArrayList<Long>();
        for (String str : ids) {
            list.add(Long.parseLong(str));
        }
        view.getMediaSrcParameter().setRealDbsId(list);

        //虚拟数据源存储真实数据源的namespace
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(list.get(0));
        String namespace = mediaSourceInfo.getParameterObj().getNamespace();
        view.getMediaSrcParameter().setNamespace(namespace);

        info.setParameter(view.getMediaSrcParameter().toJsonString());
        info.setSimulateMsType(view.getSimulateMsType());
        return info;
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
            if (CollectionUtils.isEmpty(memberDatas)) {
                return "success";
            }

            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(Long.valueOf(mediaSourceId));
            MediaSourceType type = mediaSourceInfo.getSimulateMsType();
            String url = null;
            if (type.isRdbms()) {
                DataSourceFactory.invalidate(mediaSourceInfo, () -> null);
                url = "/flush/reloadRdbMediaSource/";
            } else if (MediaSourceType.HBASE.equals(type)) {
                url = "/flush/reloadHBase/";
            } else if (MediaSourceType.ELASTICSEARCH.equals(type)) {
                url = "/flush/reloadEs/";
            }

            for (ClusterState.MemberData mem : memberDatas) {
                String target = "http://" + mem.getWorkerState().url() + url + mediaSourceId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);
                new RestTemplate().postForObject(target, request, Map.class);
            }

            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
