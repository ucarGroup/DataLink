package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.SysPropertiesService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.biz.utils.DataSourceFactory;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.common.utils.DbConfigEncryption;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.dto.mediaSource.MediaSourceView;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by csf on 2017/4/12.
 */
@Controller
@RequestMapping(value = "/mediaSource/")
public class MediaSourceController {

    private static final Logger logger = LoggerFactory.getLogger(MediaSourceController.class);
    @Autowired
    SysPropertiesService sysPropertiesService;
    @Autowired
    TaskConfigService taskConfigService;
    @Autowired
    private MediaSourceService mediaSourceService;

    @RequestMapping(value = "/mediaSourceList")
    public ModelAndView mediaSourceList() {
        ModelAndView mav = new ModelAndView("mediaSource/list");
        return mav;
    }

    @RequestMapping(value = "/initMediaSource")
    @ResponseBody
    public Page<MediaSourceView> initMediaSource(@RequestBody Map<String, String> map) {
        String mediaSourceType = map.get("mediaSourceType");
        String mediaSourceName = map.get("name");
        String mediaSourceIp = map.get("ip");
        if (StringUtils.isBlank(mediaSourceName)) {
            mediaSourceName = null;
        }
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        switch (mediaSourceType) {
            case "-1":
                setMediaSource.add(MediaSourceType.MYSQL);
                setMediaSource.add(MediaSourceType.SQLSERVER);
                setMediaSource.add(MediaSourceType.POSTGRESQL);
                setMediaSource.add(MediaSourceType.ORACLE);
                break;
            case "MYSQL":
                setMediaSource.add(MediaSourceType.MYSQL);
                break;
            case "SQLSERVER":
                setMediaSource.add(MediaSourceType.SQLSERVER);
                break;
            case "POSTGRESQL":
                setMediaSource.add(MediaSourceType.POSTGRESQL);
                break;
            case "ORACLE":
                setMediaSource.add(MediaSourceType.ORACLE);
                break;
            case "HANA":
                setMediaSource.add(MediaSourceType.HANA);
                break;
        }

        Page<MediaSourceView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<MediaSourceInfo> mediaSourceList = mediaSourceService.getListForQueryPage(setMediaSource, mediaSourceName, mediaSourceIp);

        //构造view
        List<MediaSourceView> mediaSourceViews = mediaSourceList.stream().map(i -> {
            MediaSourceView view = new MediaSourceView();
            view.setId(i.getId());
            view.getRdbMediaSrcParameter().setName(i.getName());
            view.getRdbMediaSrcParameter().setMediaSourceType(i.getType());
            view.setCreateTime(i.getCreateTime());

            RdbMediaSrcParameter.WriteConfig writeConfig = new RdbMediaSrcParameter.WriteConfig();
            writeConfig.setWriteHost(((RdbMediaSrcParameter) i.getParameterObj()).getWriteConfig().getWriteHost());
            writeConfig.setUsername(((RdbMediaSrcParameter) i.getParameterObj()).getWriteConfig().getUsername());
            view.getRdbMediaSrcParameter().setWriteConfig(writeConfig);

            RdbMediaSrcParameter.ReadConfig readConfig = new RdbMediaSrcParameter.ReadConfig();
            readConfig.setHosts(((RdbMediaSrcParameter) i.getParameterObj()).getReadConfig().getHosts());
            readConfig.setUsername(((RdbMediaSrcParameter) i.getParameterObj()).getReadConfig().getUsername());
            view.getRdbMediaSrcParameter().setReadConfig(readConfig);

            return view;
        }).collect(Collectors.toList());
        PageInfo<MediaSourceInfo> pageInfo = new PageInfo<>(mediaSourceList);
        page.setDraw(page.getDraw());
        page.setAaData(mediaSourceViews);
        page.setRecordsTotal((int) pageInfo.getTotal());
        page.setRecordsFiltered(page.getRecordsTotal());
        return page;
    }

    @RequestMapping(value = "/toAdd")
    public String toAdd(Model model) {
        //这四个密码参数从数据库中读取
        Map<String, String> map = sysPropertiesService.map();
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            if (entry.getKey().endsWith("_psw")) {
                model.addAttribute(entry.getKey(), DbConfigEncryption.decrypt(entry.getValue()));
            }
        }
        return "mediaSource/add";
    }


    @RequestMapping(value = "/toGetTableStructure")
    public String toGetTableStructure() {
        return "mediaSource/tableStructure";
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("mediaSourceView") MediaSourceView mediaSourceView) {
        try {
            mediaSourceService.checkDbConnection(mediaSourceView.getRdbMediaSrcParameter());
            MediaSourceInfo mediaSourceInfo = buildMediaSourceInfo(mediaSourceView.getRdbMediaSrcParameter(),
                    mediaSourceView.getBasicDataSourceConfig());
            Boolean isSuccess = mediaSourceService.insert(mediaSourceInfo);
            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo, "002001003", AuditLogOperType.insert.getValue()));
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add Media Source Error.", e);
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
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo, "002001006", AuditLogOperType.delete.getValue()));
                return "success";
            }
        } catch (ValidationException e) {
            return e.getMessage();
        }
        return "fail";
    }


    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        ModelAndView mav = new ModelAndView("mediaSource/edit");
        if (StringUtils.isNotBlank(id)) {
            mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
        }
        JSONObject jsonObject = JSON.parseObject(mediaSourceInfo.getParameter());
        jsonObject.remove("@type");
        RdbMediaSrcParameter mediaSourceView = JSON.parseObject(jsonObject.toString(), RdbMediaSrcParameter.class);
        RdbMediaSrcParameter.ReadConfig readConfig = mediaSourceView.getReadConfig();
        RdbMediaSrcParameter.WriteConfig writeConfig = mediaSourceView.getWriteConfig();
        readConfig.setPassword(readConfig.getDecryptPassword());
        writeConfig.setPassword(writeConfig.getDecryptPassword());
        mav.addObject("mediaSourceView", mediaSourceView);
        mav.addObject("mediaSourceId", id);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("mediaSourceView") RdbMediaSrcParameter rdbMediaSrcParameter, @ModelAttribute("basicDataSourceConfig") BasicDataSourceConfig basicDataSourceConfig, Long mediaSourceId) {
        try {
            if (mediaSourceId == null) {
                throw new RuntimeException("mediaSourceId is empty");
            }

            mediaSourceService.checkDbConnection(rdbMediaSrcParameter);
            MediaSourceInfo mediaSourceInfo = buildMediaSourceInfo(rdbMediaSrcParameter, basicDataSourceConfig);
            mediaSourceInfo.setId(mediaSourceId);
            Boolean isSuccess = mediaSourceService.update(mediaSourceInfo);
            toReloadDB(mediaSourceId.toString());

            if (isSuccess) {
                AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo, "002001005", AuditLogOperType.update.getValue()));
                return "success";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/checkDbConnection")
    public String checkDbConnection(String id) {
        try {
            MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
            if (StringUtils.isNotBlank(id)) {
                mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
            }
            JSONObject jsonObject = JSON.parseObject(mediaSourceInfo.getParameter());
            jsonObject.remove("@type");
            RdbMediaSrcParameter mediaSrcParameter = JSON.parseObject(jsonObject.toString(), RdbMediaSrcParameter.class);
            //验证解密后的密码
            RdbMediaSrcParameter.WriteConfig writeConfig = mediaSrcParameter != null ? mediaSrcParameter.getWriteConfig() : null;
            RdbMediaSrcParameter.ReadConfig readConfig = mediaSrcParameter != null ? mediaSrcParameter.getReadConfig() : null;
            if (writeConfig != null) {
                writeConfig.setPassword(writeConfig.getDecryptPassword());
            }
            if (readConfig != null) {
                readConfig.setPassword(readConfig.getDecryptPassword());
            }
            mediaSourceService.checkDbConnection(mediaSrcParameter);
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
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
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(Long.valueOf(mediaSourceId));
            DataSourceFactory.invalidate(mediaSourceInfo, () -> null);
            for (ClusterState.MemberData mem : memberDatas) {
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadRdbMediaSource/" + mediaSourceId;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);
                new RestTemplate().postForObject(url, request, Map.class);
            }
            //重启reader是该数据源的Task
            List<TaskInfo> taskList = taskConfigService.getTasksByReaderMediaSourceId(Long.valueOf(mediaSourceId));
            for (TaskInfo taskInfo : taskList) {
                ClusterState.MemberData memberData = clusterState.getMemberData(taskInfo.getId());
                String url = "http://" + memberData.getWorkerState().url() + "/tasks/" + taskInfo.getId() + "/restart";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity request = new HttpEntity(null, headers);
                Map<String, String> result = new RestTemplate().postForObject(url, request, Map.class);
            }
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo, "002001008"
                    , AuditLogOperType.other.getValue()));
            return "success";
        } catch (Exception e) {
            logger.error("reload rdb-media-source error:", e);
            return e.getMessage();
        }
    }

    private MediaSourceInfo buildMediaSourceInfo(RdbMediaSrcParameter rdbMediaSrcParameter, BasicDataSourceConfig dataSourceConfig) {
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        mediaSourceInfo.setName(rdbMediaSrcParameter.getName());
        mediaSourceInfo.setDesc(rdbMediaSrcParameter.getDesc());
        mediaSourceInfo.setType(rdbMediaSrcParameter.getMediaSourceType());
        RdbMediaSrcParameter.ReadConfig readConfig = rdbMediaSrcParameter.getReadConfig();
        RdbMediaSrcParameter.WriteConfig writeConfig = rdbMediaSrcParameter.getWriteConfig();
        writeConfig.setEncryptPassword(writeConfig.getPassword());
        readConfig.setEncryptPassword(readConfig.getPassword());
        if (MediaSourceType.MYSQL.name().equals(rdbMediaSrcParameter.getMediaSourceType().name())) {
            rdbMediaSrcParameter.setDriver("com.mysql.jdbc.Driver");
        } else if (MediaSourceType.SQLSERVER.name().equals(rdbMediaSrcParameter.getMediaSourceType().name())) {
            rdbMediaSrcParameter.setDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } else if (MediaSourceType.POSTGRESQL.name().equals(rdbMediaSrcParameter.getMediaSourceType().name())) {
            rdbMediaSrcParameter.setDriver("org.postgresql.Driver");
        } else if (MediaSourceType.ORACLE.name().equals(rdbMediaSrcParameter.getMediaSourceType().name())) {
            rdbMediaSrcParameter.setDriver("oracle.jdbc.OracleDriver");
        } else if (MediaSourceType.HANA.name().equals(rdbMediaSrcParameter.getMediaSourceType().name())) {
            rdbMediaSrcParameter.setDriver("com.sap.db.jdbc.Driver");
        } else {
            throw new RuntimeException("unkonwn driver");
        }
        rdbMediaSrcParameter.setDataSourceConfig(dataSourceConfig);
        mediaSourceInfo.setParameter(rdbMediaSrcParameter.toJsonString());
        return mediaSourceInfo;
    }

    @ResponseBody
    @RequestMapping(value = "/getMediaSourceById")
    @LoginIgnore
    public MediaSourceInfo getMediaSourceById(Long mediaSourceId) {
        return mediaSourceService.getById(mediaSourceId);
    }
}
