package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.hdfs.HDFSMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.ucar.datalink.manager.core.web.dto.mediaSource.ZkMediaSourceView;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sqq on 2017/5/16.
 */
@Controller
@RequestMapping(value = "/zk/")
public class ZkMediaSourceController {

    public static final Logger logger = LoggerFactory.getLogger(ZkMediaSourceController.class);

    @Autowired
    MediaSourceService mediaSourceService;

    @RequestMapping(value = "/zkList")
    public ModelAndView zkList() {
        ModelAndView mav = new ModelAndView("zkMediaSource/list");
        return mav;
    }

    @RequestMapping(value = "/initZk")
    @ResponseBody
    public Page<ZkMediaSourceView> initZk() {
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.ZOOKEEPER);
        List<MediaSourceInfo> zkMediaSourceList = mediaSourceService.getListByType(setMediaSource);
        List<ZkMediaSourceView> taskView = zkMediaSourceList.stream().map(i -> {
            ZkMediaSourceView view = new ZkMediaSourceView();
            view.setId(i.getId());
            view.setName(i.getName());
            view.setDesc(i.getDesc());
            view.setCreateTime(i.getCreateTime());
            view.getZkMediaSrcParameter().setMediaSourceType(i.getType());
            view.getZkMediaSrcParameter().setServers(((ZkMediaSrcParameter) i.getParameterObj()).getServers());
            view.getZkMediaSrcParameter().setSessionTimeout(((ZkMediaSrcParameter) i.getParameterObj()).getSessionTimeout());
            view.getZkMediaSrcParameter().setConnectionTimeout(((ZkMediaSrcParameter) i.getParameterObj()).getConnectionTimeout());
            return view;
        }).collect(Collectors.toList());
        return new Page<ZkMediaSourceView>(taskView);
    }

    @RequestMapping(value = "/toAdd")
    public ModelAndView toAdd() {
        ModelAndView mav = new ModelAndView("zkMediaSource/add");
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("ZkMediaSourceView") ZkMediaSourceView zkMediaSourceView) {
        try {
            checkZkServers(zkMediaSourceView);
            Boolean isSuccess = mediaSourceService.insert(buildZkMediaSourceInfo(zkMediaSourceView));
            if (isSuccess) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("Add Zk Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @RequestMapping(value = "/toEdit")
    public ModelAndView toEdit(HttpServletRequest request) {
        String id = request.getParameter("id");
        ModelAndView mav = new ModelAndView("zkMediaSource/edit");
        MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
        if (StringUtils.isNotBlank(id)) {
            mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
        }
        ZkMediaSrcParameter zkMediaSrcParameter = mediaSourceInfo.getParameterObj();
        mav.addObject("zkMediaSourceView", mediaSourceInfo);
        mav.addObject("zkMediaSrcParameter", zkMediaSrcParameter);
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("ZkMediaSourceView") ZkMediaSourceView zkMediaSourceView) {
        try {
            if (zkMediaSourceView.getId() == null) {
                throw new RuntimeException("zkMediaSourceId is empty");
            }
            checkZkServers(zkMediaSourceView);
            MediaSourceInfo mediaSourceInfo = buildZkMediaSourceInfo(zkMediaSourceView);
            mediaSourceInfo.setId(zkMediaSourceView.getId());
            Boolean isSuccess = mediaSourceService.update(mediaSourceInfo);
            toReloadDB(zkMediaSourceView.getId().toString());
            if (isSuccess) {
                return "success";
            }
        } catch (Exception e) {
            logger.error("Update Zk Media Source Error.", e);
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
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.HDFS);
        List<MediaSourceInfo> hdfsMediaSourceList = mediaSourceService.getListByType(setMediaSource);
        for (MediaSourceInfo mediaSourceInfo : hdfsMediaSourceList) {
            Long zkId = ((HDFSMediaSrcParameter) mediaSourceInfo.getParameterObj()).getZkMediaSourceId();
            if (Long.valueOf(id).equals(zkId)) {
                return "请先删除该Zk集群下的HDFS节点";
            }
        }
        try {
            Boolean isSuccess = mediaSourceService.delete(Long.valueOf(id));
            if (isSuccess) {
                return "success";
            }
        } catch (ValidationException e) {
            logger.error("Delete Zk Media Source Error.", e);
            return e.getMessage();
        }
        return "fail";
    }

    @ResponseBody
    @RequestMapping(value = "/checkZk")
    public String checkZk(HttpServletRequest request) {
        String id = request.getParameter("id");
        try {
            MediaSourceInfo mediaSourceInfo = new MediaSourceInfo();
            if (StringUtils.isNotBlank(id)) {
                mediaSourceInfo = mediaSourceService.getById(Long.valueOf(id));
            }
            JSONObject jsonObject = JSON.parseObject(mediaSourceInfo.getParameter());
            jsonObject.remove("@type");
            ZkMediaSrcParameter zkMediaSrcParameter = JSON.parseObject(jsonObject.toString(), ZkMediaSrcParameter.class);
            ZkMediaSourceView zkMediaSourceView = new ZkMediaSourceView();
            zkMediaSourceView.setZkMediaSrcParameter(zkMediaSrcParameter);
            checkZkServers(zkMediaSourceView);
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

    private MediaSourceInfo buildZkMediaSourceInfo(ZkMediaSourceView zkMediaSourceView) {
        MediaSourceInfo zkMediaSourceInfo = new MediaSourceInfo();
        zkMediaSourceInfo.setName(zkMediaSourceView.getName());
        zkMediaSourceInfo.setDesc(zkMediaSourceView.getDesc());
        zkMediaSourceInfo.setType(zkMediaSourceView.getZkMediaSrcParameter().getMediaSourceType());
        zkMediaSourceInfo.setParameter(zkMediaSourceView.getZkMediaSrcParameter().toJsonString());
        return zkMediaSourceInfo;
    }

    private void checkZkServers(ZkMediaSourceView zkMediaSourceView) throws IOException {
        String servers = zkMediaSourceView.getZkMediaSrcParameter().getServers();
        String[] serversArray = servers.split(",");
        List<String> serversList = new ArrayList<String>();
        for (String str : serversArray) {
            serversList.add(str);
        }
        for (String server : serversList) {
            String[] array = server.split(":");
            if (array.length > 1) {
                String host = array[0];
                int port = Integer.valueOf(array[1]);
                try {
                    String result = send4LetterWord(host, port, "ruok", 5000);
                    if (result.equals("imok\n")) {
                        continue;
                    }
                } catch (IOException e) {
                    throw new IOException("Zk服务器验证失败.", e);
                }
            }else {
                throw new ValidationException("ip地址格式不正确.");
            }
        }
    }

    public static String send4LetterWord(String host, int port, String cmd, int timeout) throws IOException {
        Socket sock = new Socket();
        InetSocketAddress hostAddress = host != null ? new InetSocketAddress(host, port) :
                new InetSocketAddress(InetAddress.getByName(null), port);
        BufferedReader reader = null;
        try {
            sock.setSoTimeout(timeout);
            sock.connect(hostAddress, timeout);
            OutputStream outStream = sock.getOutputStream();
            outStream.write(cmd.getBytes());
            outStream.flush();
            // this replicates NC - close the output stream before reading
            sock.shutdownOutput();

            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        } catch (SocketTimeoutException e) {
            throw new IOException("Exception while executing four letter word: " + cmd, e);
        } finally {
            sock.close();
            if (reader != null) {
                reader.close();
            }
        }
    }
}
