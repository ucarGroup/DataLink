package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.google.common.base.Splitter;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.es.EsMediaSrcParameter;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaSource.EsMediaSourceView;
import com.ucar.datalink.manager.core.web.util.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
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
 * Created by lubiao on 2017/6/16.
 */
@Controller
@RequestMapping(value = "/es/")
public class EsMediaSourceController {

    private static final Logger logger = LoggerFactory.getLogger(EsMediaSourceController.class);

    @Autowired
    MediaSourceService mediaSourceService;

    @RequestMapping(value = "/esList")
    public ModelAndView esList() {
        ModelAndView mav = new ModelAndView("esMediaSource/list");
        return mav;
    }

    @RequestMapping(value = "/initEs")
    @ResponseBody
    public Page<EsMediaSourceView> initEs() {
        Set<MediaSourceType> setMediaSource = new HashSet<>();
        setMediaSource.add(MediaSourceType.ELASTICSEARCH);
        List<MediaSourceInfo> esMediaSourceList = mediaSourceService.getListByType(setMediaSource);
        List<EsMediaSourceView> esView = esMediaSourceList.stream().map(i -> {
            EsMediaSourceView view = new EsMediaSourceView();
            view.setId(i.getId());
            view.setName(i.getName());
            view.setDesc(i.getDesc());
            view.setCreateTime(i.getCreateTime());
            view.setEsMediaSrcParameter(i.getParameterObj());
            return view;
        }).collect(Collectors.toList());
        return new Page<>(esView);
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("esMediaSource/add");
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("esMediaSourceView") EsMediaSourceView esMediaSourceView) {
        try {
            checkEsConfig(esMediaSourceView.getEsMediaSrcParameter());
            Boolean isSuccess = mediaSourceService.insert(buildEsMediaSourceInfo(esMediaSourceView));
            if (isSuccess) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add Es Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("esMediaSource/edit");
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        if (StringUtils.isNotBlank(id)) {
            mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
        }

        EsMediaSourceView view = new EsMediaSourceView();
        view.setId(mediaSourceInfo.getId());
        view.setName(mediaSourceInfo.getName());
        view.setDesc(mediaSourceInfo.getDesc());
        view.setCreateTime(mediaSourceInfo.getCreateTime());
        view.setEsMediaSrcParameter(mediaSourceInfo.getParameterObj());
        mav.addObject("esMediaSourceView", view);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("esMediaSourceView") EsMediaSourceView esMediaSourceView) {
        try {
            if (esMediaSourceView.getId() == null) {
                throw new RuntimeException("esMediaSourceId is empty");
            }
            checkEsConfig(esMediaSourceView.getEsMediaSrcParameter());
            MediaSourceInfo mediaSourceInfo = buildEsMediaSourceInfo(esMediaSourceView);
            mediaSourceInfo.setId(esMediaSourceView.getId());
            Boolean isSuccess = mediaSourceService.update(mediaSourceInfo);
            toReloadES(esMediaSourceView.getId().toString());
            if (isSuccess) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("Update Es Media Source Error.", e);
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
            logger.error("Delete Es Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/checkEs")
    public String checkEs(HttpServletRequest request) {
        String id = request.getParameter("id");
        try {
            MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
            if (StringUtils.isNotBlank(id)) {
                mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
            }
            checkEsConfig(mediaSourceInfo.getParameterObj());
            return "success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/toReloadES")
    public String toReloadES(String mediaSourceId) {
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
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadEs/" + mediaSourceId;
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

    private MediaSourceInfo buildEsMediaSourceInfo(EsMediaSourceView esMediaSourceView) {
        MediaSourceInfo esMediaSourceInfo = new MediaSourceInfo();
        esMediaSourceInfo.setName(esMediaSourceView.getName());
        esMediaSourceInfo.setDesc(esMediaSourceView.getDesc());
        esMediaSourceInfo.setType(MediaSourceType.ELASTICSEARCH);
        esMediaSourceView.getEsMediaSrcParameter().setMediaSourceType(MediaSourceType.ELASTICSEARCH);
        esMediaSourceInfo.setParameter(esMediaSourceView.getEsMediaSrcParameter().toJsonString());
        return esMediaSourceInfo;
    }

    private void checkEsConfig(EsMediaSrcParameter esMediaSrcParameter) {
        for (String host : Splitter.on(",").trimResults().omitEmptyStrings().split(esMediaSrcParameter.getClusterHosts())) {
            String url = "http://" + host + ":" + esMediaSrcParameter.getHttpPort() + "/";
            verify(url, esMediaSrcParameter.getUserName(), esMediaSrcParameter.getPassword());
        }
    }

    private void verify(String url, String user, String pass) {
        try {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, pass);
            provider.setCredentials(AuthScope.ANY, credentials);
            HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
            HttpResponse response = client.execute(new HttpGet(url));
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new ValidationException("verify elasticsearch node [" + url + "] failure, response status: " + response);
            }

            logger.info("Verify Es MediaSource successfully.");
        } catch (Exception e) {
            throw new ValidationException("verify elasticsearch node [" + url + "] failure, cause of: " + e);
        }
    }
}
