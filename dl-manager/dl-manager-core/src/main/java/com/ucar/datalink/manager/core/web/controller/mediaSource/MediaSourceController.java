package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.SysPropertiesService;
import com.ucar.datalink.biz.utils.DataSourceFactory;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.common.utils.DbConfigEncryption;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.dto.mediaSource.MediaSourceView;
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
    private MediaSourceService mediaSourceService;
    @Autowired
    SysPropertiesService sysPropertiesService;

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
        if (StringUtils.isBlank(mediaSourceName)) {
            mediaSourceName = null;
        }
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        switch (mediaSourceType) {
            case "-1":
                setMediaSource.add(MediaSourceType.MYSQL);
                setMediaSource.add(MediaSourceType.SQLSERVER);
                setMediaSource.add(MediaSourceType.POSTGRESQL);
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
        }

        Page<MediaSourceView> page = new Page<>(map);
        PageHelper.startPage(page.getPageNum(), page.getLength());
        List<MediaSourceInfo> mediaSourceList = mediaSourceService.getListForQueryPage(setMediaSource, mediaSourceName);

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
        Map<String,String> map = sysPropertiesService.map();
        Iterator<Map.Entry<String,String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,String> entry = iterator.next();
            if(entry.getKey().endsWith("_psw")){
                model.addAttribute(entry.getKey(),DbConfigEncryption.decrypt(entry.getValue()));
            }
        }
        return "mediaSource/add";
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("mediaSourceView") MediaSourceView mediaSourceView) {
        try {
            mediaSourceService.checkDbConnection(mediaSourceView.getRdbMediaSrcParameter());
            RdbMediaSrcParameter.WriteConfig writeConfig = mediaSourceView.getRdbMediaSrcParameter().getWriteConfig();
            RdbMediaSrcParameter.ReadConfig readConfig = mediaSourceView.getRdbMediaSrcParameter().getReadConfig();
            writeConfig.setEncryptPassword(writeConfig.getPassword());
            readConfig.setEncryptPassword(readConfig.getPassword());
            Boolean isSuccess = mediaSourceService.insert(buildMediaSourceInfo(mediaSourceView));
            if (isSuccess) {
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
            Boolean isSuccess = mediaSourceService.delete(Long.valueOf(id));
            if (isSuccess) {
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
    public String doEdit(@ModelAttribute("mediaSourceView") RdbMediaSrcParameter mediaSourceView, @ModelAttribute("basicDataSourceConfig") BasicDataSourceConfig basicDataSourceConfig, Long mediaSourceId) {
        try {
            mediaSourceView.setDataSourceConfig(basicDataSourceConfig);
            if (mediaSourceId == null) {
                throw new RuntimeException("mediaSourceId is empty");
            }
            mediaSourceService.checkDbConnection(mediaSourceView);
            Boolean isSuccess = mediaSourceService.update(buildRdbMediaSrcParameter(mediaSourceView, mediaSourceId));
            toReloadDB(mediaSourceId.toString());
            if (isSuccess) {
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

    private MediaSourceInfo buildMediaSourceInfo(MediaSourceView mediaSourceView) {
        MediaSourceInfo mediaSource = new MediaSourceInfo();
        mediaSource.setName(mediaSourceView.getRdbMediaSrcParameter().getName());
        mediaSource.setDesc(mediaSourceView.getRdbMediaSrcParameter().getDesc());
        mediaSource.setType(mediaSourceView.getRdbMediaSrcParameter().getMediaSourceType());
        if (MediaSourceType.MYSQL.name().equals(mediaSourceView.getRdbMediaSrcParameter().getMediaSourceType().name())) {
            mediaSourceView.getRdbMediaSrcParameter().setDriver("com.mysql.jdbc.Driver");
        } else if( MediaSourceType.SQLSERVER.name().equals(mediaSourceView.getRdbMediaSrcParameter().getMediaSourceType().name()) ) {
            mediaSourceView.getRdbMediaSrcParameter().setDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } else if(MediaSourceType.POSTGRESQL.name().equals(mediaSourceView.getRdbMediaSrcParameter().getMediaSourceType().name()) ) {
            mediaSourceView.getRdbMediaSrcParameter().setDriver("org.postgresql.Driver");
        } else {
            throw new RuntimeException("unknown type");
        }
        mediaSourceView.getRdbMediaSrcParameter().setDataSourceConfig(mediaSourceView.getBasicDataSourceConfig());
        mediaSource.setParameter(mediaSourceView.getRdbMediaSrcParameter().toJsonString());
        return mediaSource;
    }

    private MediaSourceInfo buildRdbMediaSrcParameter(RdbMediaSrcParameter mediaSourceView, Long mediaSourceId) {
        MediaSourceInfo mediaSource = new MediaSourceInfo();
        mediaSource.setId(mediaSourceId);
        mediaSource.setName(mediaSourceView.getName());
        mediaSource.setDesc(mediaSourceView.getDesc());
        mediaSource.setType(mediaSourceView.getMediaSourceType());
        RdbMediaSrcParameter.ReadConfig readConfig = mediaSourceView.getReadConfig();
        RdbMediaSrcParameter.WriteConfig writeConfig = mediaSourceView.getWriteConfig();
        writeConfig.setEncryptPassword(writeConfig.getPassword());
        readConfig.setEncryptPassword(readConfig.getPassword());
        if (MediaSourceType.MYSQL.name().equals(mediaSourceView.getMediaSourceType().name())) {
            mediaSourceView.setDriver("com.mysql.jdbc.Driver");
        } else if( MediaSourceType.SQLSERVER.name().equals(mediaSourceView.getMediaSourceType().name()) ) {
            mediaSourceView.setDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } else if( MediaSourceType.POSTGRESQL.name().equals(mediaSourceView.getMediaSourceType().name())){
            mediaSourceView.setDriver("org.postgresql.Driver");
        } else {
            throw new RuntimeException("unkonwn driver");
        }
        mediaSourceView.setDataSourceConfig(mediaSourceView.getDataSourceConfig());
        mediaSource.setParameter(mediaSourceView.toJsonString());
        return mediaSource;
    }

    @ResponseBody
    @RequestMapping(value = "/getMediaSourceById")
    @LoginIgnore
    public MediaSourceInfo getMediaSourceById(Long mediaSourceId) {
        return mediaSourceService.getById(mediaSourceId);
    }
}
