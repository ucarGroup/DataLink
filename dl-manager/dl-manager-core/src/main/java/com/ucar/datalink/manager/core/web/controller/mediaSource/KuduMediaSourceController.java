package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.ucar.datalink.biz.service.LabService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.lab.LabInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.kudu.KuduMediaSrcParameter;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaSource.KuduMediaSourceView;
import com.ucar.datalink.manager.core.web.util.AuditLogInfoUtil;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduException;
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

@Controller
@RequestMapping(value = "/kudu/")
public class KuduMediaSourceController {

    private static final Logger logger = LoggerFactory.getLogger(KuduMediaSourceController.class);

    @Autowired
    MediaSourceService mediaSourceService;
    @Autowired
    LabService labService;

    @RequestMapping(value = "/kuduList")
    public ModelAndView kuduList() {
        ModelAndView mav = new ModelAndView("kuduMediaSource/list");
        return mav;
    }

    @RequestMapping(value = "/initKudu")
    @ResponseBody
    public Page<KuduMediaSourceView> initKudu() {
        Set<MediaSourceType> setMediaSource = new HashSet<>();
        setMediaSource.add(MediaSourceType.KUDU);
        List<MediaSourceInfo> kuduMediaSourceList = mediaSourceService.getListByType(setMediaSource);
        List<KuduMediaSourceView> kuduView = kuduMediaSourceList.stream().map(i -> {
            KuduMediaSourceView view = new KuduMediaSourceView();
            view.setId(i.getId());
            view.setName(i.getName());
            view.setDesc(i.getDesc());
            view.setCreateTime(i.getCreateTime());
            view.setLabId(i.getLabId());
            view.setKuduMediaSrcParameter(i.getParameterObj());
            view.setLabName(StringUtils.isNotBlank(i.getLabName()) ? i.getLabName() : "");
            return view;
        }).collect(Collectors.toList());
        return new Page<>(kuduView);
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("kuduMediaSource/add");
        List<LabInfo> labInfoList = labService.findLabList();
        mav.addObject("labInfoList", labInfoList);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("kuduMediaSourceView") KuduMediaSourceView kuduMediaSourceView) {
        try {
            checkKuduConfig(kuduMediaSourceView.getKuduMediaSrcParameter());
            MediaSourceInfo mediaSourceInfo = buildKuduMediaSourceInfo(kuduMediaSourceView);
            Boolean isSuccess = mediaSourceService.insert(mediaSourceInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                        , "002010003", AuditLogOperType.insert.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add Kudu Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("kuduMediaSource/edit");
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        if (StringUtils.isNotBlank(id)) {
            mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
        }

        KuduMediaSourceView view = new KuduMediaSourceView();
        view.setId(mediaSourceInfo.getId());
        view.setName(mediaSourceInfo.getName());
        view.setDesc(mediaSourceInfo.getDesc());
        view.setCreateTime(mediaSourceInfo.getCreateTime());
        view.setKuduMediaSrcParameter(mediaSourceInfo.getParameterObj());

        mav.addObject("kuduMediaSourceView", view);
        List<LabInfo> labInfoList = labService.findLabList();
        mav.addObject("labInfoList", labInfoList);

        mav.addObject("labId", mediaSourceInfo.getLabId());
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("kuduMediaSourceView") KuduMediaSourceView kuduMediaSourceView) {
        try {
            if (kuduMediaSourceView.getId() == null) {
                throw new RuntimeException("kuduMediaSourceId is empty");
            }
            checkKuduConfig(kuduMediaSourceView.getKuduMediaSrcParameter());
            MediaSourceInfo mediaSourceInfo = buildKuduMediaSourceInfo(kuduMediaSourceView);
            mediaSourceInfo.setId(kuduMediaSourceView.getId());
            mediaSourceInfo.setType(MediaSourceType.KUDU);
            Boolean isSuccess = mediaSourceService.update(mediaSourceInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                        , "002010005", AuditLogOperType.update.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("Update Kudu Media Source Error.", e);
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
                        , "002010006", AuditLogOperType.delete.getValue()));
                return "success";
            }
        } catch (ValidationException e) {
            logger.error("Delete Kudu Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/checkKudu")
    public String checkKudu(HttpServletRequest request) {
        String id = request.getParameter("id");
        try {
            MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
            if (StringUtils.isNotBlank(id)) {
                mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
            }
            checkKuduConfig(mediaSourceInfo.getParameterObj());
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }


    private MediaSourceInfo buildKuduMediaSourceInfo(KuduMediaSourceView kuduMediaSourceView) {
        MediaSourceInfo kuduMediaSourceInfo = new MediaSourceInfo();
        kuduMediaSourceInfo.setName(kuduMediaSourceView.getName());
        kuduMediaSourceInfo.setDesc(kuduMediaSourceView.getDesc());
        kuduMediaSourceInfo.setType(MediaSourceType.KUDU);
        kuduMediaSourceView.getKuduMediaSrcParameter().setMediaSourceType(MediaSourceType.KUDU);
        kuduMediaSourceInfo.setParameter(kuduMediaSourceView.getKuduMediaSrcParameter().toJsonString());
        kuduMediaSourceInfo.setLabId(kuduMediaSourceView.getLabId());
        return kuduMediaSourceInfo;
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
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadKudu/" + mediaSourceId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);
                new RestTemplate().postForObject(url, request, Map.class);
            }
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(Long.valueOf(mediaSourceId));
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                    , "002010008", AuditLogOperType.other.getValue()));
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }





/*
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
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(Long.valueOf(mediaSourceId));
            DataSourceFactory.invalidate(mediaSourceInfo, () -> null);
            for (ClusterState.MemberData mem : memberDatas) {
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadKudu/" + mediaSourceId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);
                new RestTemplate().postForObject(url, request, Map.class);
            }
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }*/



    private void  checkKuduConfig(KuduMediaSrcParameter kuduMediaSrcParameter) {
        String database = kuduMediaSrcParameter.getDatabase();
        List<String> host2Ports = kuduMediaSrcParameter.getHost2Ports();
        boolean dbExists = false;
        KuduClient client = null;
        try {
            client = new KuduClient.KuduClientBuilder(host2Ports).build();
            List<String> tablesList = client.getTablesList().getTablesList();
            logger.info("kudu tables{}", ArrayUtils.toString(tablesList));
            for(String table : tablesList){
                int lastIndex = table.lastIndexOf(".");
                if(lastIndex == -1){
                    continue;
                }
                String db = table.substring(0,lastIndex);
                if(db.equals(database)){
                    dbExists = true;
                    break;
                }
            }
        } catch (KuduException e) {
            throw new RuntimeException("kudu链接错误,请检查配置");
        } finally {
            if(client != null){
                try {
                    client.close();
                } catch (KuduException e) {
                }
            }
        }

        if(!dbExists){
            throw new RuntimeException("数据库不存在,请检查!");
        }
    }


}
