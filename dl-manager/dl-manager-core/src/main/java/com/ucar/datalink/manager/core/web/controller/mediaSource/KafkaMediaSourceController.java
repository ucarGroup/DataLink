package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.kafka.KafkaMediaSrcParameter;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaSource.KafkaMediaSourceView;
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

@Controller
@RequestMapping(value = "/kafka/")
public class KafkaMediaSourceController {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMediaSourceController.class);

    @Autowired
    MediaSourceService mediaSourceService;

    @RequestMapping(value = "/kafkaList")
    public ModelAndView kafkaList() {
        ModelAndView mav = new ModelAndView("kafkaMediaSource/list");
        return mav;
    }

    @RequestMapping(value = "/initKafka")
    @ResponseBody
    public Page<KafkaMediaSourceView> initKafka() {
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.KAFKA);
        List<MediaSourceInfo> kafkaMediaSourceList = mediaSourceService.getListByType(setMediaSource);
        List<KafkaMediaSourceView> taskView = kafkaMediaSourceList.stream().map(i -> {
            KafkaMediaSourceView view = new KafkaMediaSourceView();
            view.setId(i.getId());
            view.setName(i.getName());
            view.setDesc(i.getDesc());
            view.setCreateTime(i.getCreateTime());
            view.getKafkaMediaSrcParameter().setMediaSourceType(i.getType());
            view.getKafkaMediaSrcParameter().setTopic(((KafkaMediaSrcParameter) i.getParameterObj()).getTopic());
            return view;
        }).collect(Collectors.toList());
        return new Page<KafkaMediaSourceView>(taskView);
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("kafkaMediaSource/add");
        mav.addObject("zkMediaSourceList", initZkList());
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("KafkaMediaSourceView") KafkaMediaSourceView kafkaMediaSourceView) {
        try {
            MediaSourceInfo mediaSourceInfo = buildKafkaMediaSourceInfo(kafkaMediaSourceView);
            Boolean isSuccess = mediaSourceService.insert(mediaSourceInfo);
            if (isSuccess) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add Kafka Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("kafkaMediaSource/edit");
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        if (StringUtils.isNotBlank(id)) {
            mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
        }
        KafkaMediaSrcParameter kafkaMediaSrcParameter = mediaSourceInfo.getParameterObj();
        mav.addObject("kafkaMediaSourceView", mediaSourceInfo);
        mav.addObject("kafkaMediaSrcParameter", kafkaMediaSrcParameter);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("KafkaMediaSourceView") KafkaMediaSourceView kafkaMediaSourceView) {
        try {
            if (kafkaMediaSourceView.getId() == null) {
                throw new RuntimeException("kafkaMediaSourceId is empty");
            }
            MediaSourceInfo mediaSourceInfo = buildKafkaMediaSourceInfo(kafkaMediaSourceView);
            mediaSourceInfo.setId(kafkaMediaSourceView.getId());
            Boolean isSuccess = mediaSourceService.update(mediaSourceInfo);
            toReloadDB(kafkaMediaSourceView.getId().toString());
            if (isSuccess) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("Update Kafka Media Source Error.", e);
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
            Boolean isSuccess = mediaSourceService.delete(idLong);
            if (isSuccess) {
                return "success";
            }
        } catch (ValidationException e) {
            logger.error("Delete Kafka Media Source Error.", e);
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
                String url = "http://" + mem.getWorkerState().url() + "/flush/reloadKafka/" + mediaSourceId;
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

    private MediaSourceInfo buildKafkaMediaSourceInfo(KafkaMediaSourceView kafkaMediaSourceView) {
        MediaSourceInfo kafkaMediaSourceInfo = new MediaSourceInfo();
        kafkaMediaSourceInfo.setName(kafkaMediaSourceView.getName());
        kafkaMediaSourceInfo.setDesc(kafkaMediaSourceView.getDesc());
        kafkaMediaSourceInfo.setType(kafkaMediaSourceView.getKafkaMediaSrcParameter().getMediaSourceType());
        kafkaMediaSourceInfo.setParameter(kafkaMediaSourceView.getKafkaMediaSrcParameter().toJsonString());
        return kafkaMediaSourceInfo;
    }

    private List<MediaSourceInfo> initZkList(){
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.ZOOKEEPER);
        return mediaSourceService.getListByType(setMediaSource);
    }
}
