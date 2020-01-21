package com.ucar.datalink.manager.core.web.controller.mediaSource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.AuditLogOperType;
import com.ucar.datalink.biz.utils.AuditLogUtils;
import com.ucar.datalink.biz.utils.DataSourceFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.common.utils.DbConfigEncryption;
import com.ucar.datalink.domain.lab.LabInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.virtual.VirtualMediaSrcParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ManagerConfig;
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

    private static Map<String,String> dbUsernameMap = new HashMap<>();

    private static Map<String,String> dbTypeMap = new HashMap<>();

    @Autowired
    private MediaSourceService mediaSourceService;
    @Autowired
    SysPropertiesService sysPropertiesService;
    @Autowired
    LabService labService;
    @Autowired
    DoubleCenterService doubleCenterService;
    @Autowired
    TaskConfigService taskConfigService;
    @Autowired
    JobService jobService;

    static {
        dbUsernameMap.put("mysql_write_user","ucar_dep");
        dbUsernameMap.put("mysql_read_user","canal");
        dbUsernameMap.put("sqlserver_write_user","ucar_dep_w");
        dbUsernameMap.put("sqlserver_read_user","ucar_dep_r");

        dbTypeMap.put("MYSQL","MySQL");
        dbTypeMap.put("SQLSERVER","SQL Server");
        dbTypeMap.put("ORACLE","Oracle");
    }

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
        List<MediaSourceInfo> mediaSourceList = mediaSourceService.getListForQueryPage(setMediaSource, mediaSourceName,mediaSourceIp);

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

            view.setLabId(i.getLabId());
            view.setLabName(StringUtils.isNotBlank(i.getLabName()) ? i.getLabName() : "");
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
        //这四个密码参数改成从数据库中读取
        Map<String,String> map = sysPropertiesService.map();
        Iterator<Map.Entry<String,String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,String> entry = iterator.next();
            if(entry.getKey().endsWith("_psw")){
                model.addAttribute(entry.getKey(),DbConfigEncryption.decrypt(entry.getValue()));
            }
        }
        List<LabInfo> labInfoList = labService.findLabList();
        model.addAttribute("labInfoList", labInfoList);
        return "mediaSource/add";
    }

    @RequestMapping(value = "/toAddQuick")
    public String toAddQuick(Model model) {
        return "mediaSource/addQuick";
    }

    @RequestMapping(value = "/toGetTableStructure")
    public String toGetTableStructure() {
        return "mediaSource/tableStructure";
    }

    @ResponseBody
    @RequestMapping(value = "/doAdd")
    public String doAdd(@ModelAttribute("mediaSourceView") MediaSourceView mediaSourceView) {
        try {
            check(mediaSourceView);

            encryptPassword(mediaSourceView);

            MediaSourceInfo mediaSourceInfo = buildMediaSourceInfo(mediaSourceView);

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

    private void encryptPassword(MediaSourceView mediaSourceView) {
        RdbMediaSrcParameter.WriteConfig writeConfig = mediaSourceView.getRdbMediaSrcParameter().getWriteConfig();
        RdbMediaSrcParameter.ReadConfig readConfig = mediaSourceView.getRdbMediaSrcParameter().getReadConfig();
        writeConfig.setEncryptPassword(writeConfig.getPassword());
        readConfig.setEncryptPassword(readConfig.getPassword());
    }

    private void check(MediaSourceView mediaSourceView) throws Exception {
        //校验4个ip是否属于所属机房
        mediaSourceService.checkIpBelongLab(mediaSourceView.getRdbMediaSrcParameter(),mediaSourceView.getLabId());

        MediaSourceType mediaSourceType = mediaSourceView.getRdbMediaSrcParameter().getMediaSourceType();
//            if (mediaSourceType!=MediaSourceType.SQLSERVER && mediaSourceType!=MediaSourceType.ORACLE) {
//                mediaSourceService.checkDbConnection(mediaSourceView.getRdbMediaSrcParameter());
//            }
        if(mediaSourceType == MediaSourceType.MYSQL) {
            mediaSourceService.checkDbConnection(mediaSourceView.getRdbMediaSrcParameter());
        }
    }

    @ResponseBody
    @RequestMapping(value = "/doAddQuick")
    public String doAddQuick(String dbInfo,String mediaSourceType,String namespace,String name) {
        try {
            if(StringUtils.isEmpty(namespace)) {
                throw new RuntimeException("数据库名不能为空");
            }
            if(StringUtils.isEmpty(dbInfo)) {
                throw new RuntimeException("没有查到要配置的写ip信息");
            }
            Boolean isSuccess = false;
            List<MediaSourceInfo> mediaSourceInfoList = new ArrayList<>();
            JSONObject ipJsonObject = JSONObject.parseObject(dbInfo);
            boolean isDoubleCenter = ipJsonObject.getBoolean("isDoubleCenter");

            IpInfoConfig ipInfoConfig = parseIps(ipJsonObject);

            if(isDoubleCenter){//双中心

                List<MediaSourceView> mediaSourceViewList = getMediaSourceView(ipInfoConfig,ipJsonObject,name);

                if(mediaSourceViewList.size()<1) {
                    throw new RuntimeException("没有查到要配置的写ip信息");
                }

                for(MediaSourceView mediaSourceView : mediaSourceViewList) {
                    check(mediaSourceView);
                    encryptPassword(mediaSourceView);
                    MediaSourceInfo mediaSourceInfo = buildMediaSourceInfo(mediaSourceView);
                    mediaSourceInfoList.add(mediaSourceInfo);
                }
                List<String> existsMediaSourceList = new ArrayList<>();
                isSuccess = mediaSourceService.insertDoubleMediaSource(mediaSourceInfoList,existsMediaSourceList);
                if(existsMediaSourceList.size()>0){
                    return "old";
                }
            }else {//不是双中心

                List<MediaSourceView> mediaSourceViewList = getMediaSourceView(ipInfoConfig,ipJsonObject,name);

                if(mediaSourceViewList.size()>1) {
                    throw new RuntimeException("添加的数据源有多个，请检查");
                }

                if(mediaSourceViewList.size()<1) {
                    throw new RuntimeException("没有查到要配置的写ip信息");
                }

                MediaSourceView mediaSourceView = mediaSourceViewList.get(0);

                check(mediaSourceView);

                encryptPassword(mediaSourceView);

                MediaSourceInfo mediaSourceInfo = buildMediaSourceInfo(mediaSourceView);

                isSuccess = mediaSourceService.insert(mediaSourceInfo);

                mediaSourceInfoList.add(mediaSourceInfo);
            }
            if(isSuccess){
                for (MediaSourceInfo mediaSourceInfo : mediaSourceInfoList) {
                    AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo
                            , "002001010", AuditLogOperType.other.getValue()));
                }
            }
          return "success";
        } catch (Exception e) {
            logger.error("Add Media Source Error.", e);
            return e.getMessage();
        }
    }

    private List<MediaSourceView> getMediaSourceView(IpInfoConfig ipInfoConfig, JSONObject ipJsonObject,String name) {
        String typeName = ipJsonObject.getString("db_type").replaceAll(" ","").toLowerCase();
        //获取lab info
        Map<String,LabInfo> labInfoMap = getLabInfoMap();
        //获取用户名 ，密码
        String writerPasswrod = "";
        String readerPassword = "";
        Map<String,String> map = sysPropertiesService.map();
        Iterator<Map.Entry<String,String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,String> entry = iterator.next();
            if(entry.getKey().endsWith(typeName+"_write_psw")){
                writerPasswrod = entry.getValue();
            }else if(entry.getKey().endsWith(typeName+"_read_psw")) {
                readerPassword = entry.getValue();
            }
        }
        List<MediaSourceView> list = new ArrayList<>();
        for (Map.Entry<String,String> entry : ipInfoConfig.getPortMap().entrySet()) {
            MediaSourceView mediaSourceView = new MediaSourceView();
            RdbMediaSrcParameter rdbMediaSrcParameter = mediaSourceView.getRdbMediaSrcParameter();

            RdbMediaSrcParameter.WriteConfig writeConfig = new RdbMediaSrcParameter.WriteConfig();
            writeConfig.setWriteHost(ipInfoConfig.getWriteIpMap().get(entry.getKey()));
            writeConfig.setUsername(dbUsernameMap.get(typeName+"_write_user"));
            writeConfig.setPassword(DbConfigEncryption.decrypt(writerPasswrod));

            RdbMediaSrcParameter.ReadConfig readConfig = new RdbMediaSrcParameter.ReadConfig();
            readConfig.setHosts(ipInfoConfig.getReadIpListMap().get(entry.getKey()+entry.getValue()));
            readConfig.setUsername(dbUsernameMap.get(typeName+"_read_user"));
            readConfig.setPassword(DbConfigEncryption.decrypt(readerPassword));
            readConfig.setEtlHost(ipInfoConfig.getEtlIpMap().get(entry.getKey()+entry.getValue()));

            rdbMediaSrcParameter.setName(name);
            rdbMediaSrcParameter.setNamespace(ipJsonObject.getString("db_name"));
            rdbMediaSrcParameter.setPort(Integer.valueOf(entry.getValue()));
            rdbMediaSrcParameter.setEncoding("utf-8");
            rdbMediaSrcParameter.setIsTIDB(ipJsonObject.getBooleanValue("is_tidb"));
            rdbMediaSrcParameter.setMediaSourceType(MediaSourceType.valueOf(ipJsonObject.getString("db_type").replaceAll(" ","").toUpperCase()));
            rdbMediaSrcParameter.setDesc(ipJsonObject.getString("product_name")+" : "+ipJsonObject.get("db_name"));
            rdbMediaSrcParameter.setWriteConfig(writeConfig);
            rdbMediaSrcParameter.setReadConfig(readConfig);
            mediaSourceView.setLabId(labInfoMap.get(entry.getKey()).getId());
            mediaSourceView.setLabName(entry.getKey());
            mediaSourceView.setRdbMediaSrcParameter(rdbMediaSrcParameter);
            list.add(mediaSourceView);
        }
        return list;
    }

    private IpInfoConfig parseIps(JSONObject ipJsonObject) {
        JSONArray array = ipJsonObject.getJSONArray("ip_list");
        Map<String,String> portMap = new HashMap<>(4);
        Map<String,List<String>> readIpListMap = new HashMap<>(8);
        Map<String,String> etlIpMap = new HashMap<>(4);
        Map<String,String> writeIpMap = new HashMap<>(4);

        for (int i=0;i<array.size();i++) {
            JSONObject ipJson = array.getJSONObject(i);
            String logicName = ipJson.getString("idc_name");
            String ip = ipJson.getString("ip_addr").split(":")[0];
            String port = ipJson.getString("ip_addr").split(":")[1];
            if(ipJson.getString("role_name").equals("读写")) {
                portMap.put(logicName,port);
                writeIpMap.put(logicName,ip);
            }else {
                String key = logicName+port;
                List<String> ipList = readIpListMap.get(key);
                if(ipList == null) {
                    ipList = new ArrayList<>();
                }
                ipList.add(ip);
                readIpListMap.put(key,ipList);
                String etlPort = ipJson.getString("read_ip").split(":")[1];
                String etIp = ipJson.getString("read_ip").split(":")[0];
                etlIpMap.put(logicName+etlPort,etIp);
            }
        }

        for(Map.Entry<String,String> entry : writeIpMap.entrySet()) {
            String port = portMap.get(entry.getKey());
            String key = entry.getKey()+port;
            List<String> readList = readIpListMap.get(key);
            if("prod".equalsIgnoreCase(ManagerConfig.current().getCurrentEnv())){
                if(readList == null || readList.size() ==0){
                    throw new DatalinkException("没有查到读库ip");
                }
            }
            if(readList == null) {
                readList = new ArrayList<>();
                readList.add(entry.getValue());
                readList.add(entry.getValue());
                readIpListMap.put(key,readList);
            }else if(readList.size() == 0) {
                readList.add(entry.getValue());
                readList.add(entry.getValue());
            }else if(readList.size() == 1){
                readList.add(readList.get(0));
            }else if(readList.size()>2) {
                int size = readList.size()-2;
                for(int i=0;i<size;i++) {
                    readList.remove(readList.size()-1);
                }
            }
            String etlIp = etlIpMap.get(key);
            if(StringUtils.isEmpty(etlIp)) {
                etlIpMap.put(key,readList.get(1));
            }
        }

        IpInfoConfig ipInfoConfig = new IpInfoConfig();
        ipInfoConfig.setWriteIpMap(writeIpMap);
        ipInfoConfig.setReadIpListMap(readIpListMap);
        ipInfoConfig.setPortMap(portMap);
        ipInfoConfig.setEtlIpMap(etlIpMap);
        return ipInfoConfig;
    }

    private Map<String,LabInfo> getLabInfoMap() {
        Map<String,LabInfo> map = new HashMap<>();
        List<LabInfo> labInfoList = labService.findLabList();
        for (LabInfo labInfo:labInfoList) {
            map.put(labInfo.getLabName(),labInfo);
        }
        return map;
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

        List<LabInfo> labInfoList = labService.findLabList();
        mav.addObject("labInfoList", labInfoList);
        mav.addObject("labId", mediaSourceInfo.getLabId());
        return mav;
    }

    @ResponseBody
    @RequestMapping(value = "/doEdit")
    public String doEdit(@ModelAttribute("mediaSourceView") RdbMediaSrcParameter mediaSourceView, @ModelAttribute("basicDataSourceConfig") BasicDataSourceConfig basicDataSourceConfig, Long mediaSourceId,Long labId) {
        try {
            mediaSourceView.setDataSourceConfig(basicDataSourceConfig);
            if (mediaSourceId == null) {
                throw new RuntimeException("mediaSourceId is empty");
            }

            //校验4个ip是否属于所属机房
            mediaSourceService.checkIpBelongLab(mediaSourceView,labId);

            MediaSourceType mediaSourceType = mediaSourceView.getMediaSourceType();
            if (mediaSourceType == MediaSourceType.MYSQL) {
                mediaSourceService.checkDbConnection(mediaSourceView);
            }
            MediaSourceInfo mediaSourceInfo = buildRdbMediaSrcParameter(mediaSourceView, mediaSourceId, labId);
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
            //根据数据源reload源端/目标端为该数据源的datax定时任务
            jobService.roloadJobsByMediaSourceId(Long.valueOf(mediaSourceId));
            AuditLogUtils.saveAuditLog(AuditLogInfoUtil.getAuditLogInfoFromMediaSourceInfo(mediaSourceInfo, "002001008"
                    , AuditLogOperType.other.getValue()));
            return "success";
        } catch (Exception e) {
            logger.error("reload rdb-media-source error:", e);
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
        } else if(MediaSourceType.ORACLE.name().equals(mediaSourceView.getRdbMediaSrcParameter().getMediaSourceType().name())) {
            mediaSourceView.getRdbMediaSrcParameter().setDriver("oracle.jdbc.driver.OracleDriver");
        } else if(MediaSourceType.HANA.name().equals(mediaSourceView.getRdbMediaSrcParameter().getMediaSourceType().name())) {
            mediaSourceView.getRdbMediaSrcParameter().setDriver("com.sap.db.jdbc.Driver");
        } else {
            throw new RuntimeException("unknown type");
        }
        mediaSourceView.getRdbMediaSrcParameter().setDataSourceConfig(mediaSourceView.getBasicDataSourceConfig());
        mediaSource.setParameter(mediaSourceView.getRdbMediaSrcParameter().toJsonString());
        mediaSource.setLabId(mediaSourceView.getLabId());
        mediaSource.setLabName(mediaSourceView.getLabName());
        return mediaSource;
    }

    private MediaSourceInfo buildRdbMediaSrcParameter(RdbMediaSrcParameter mediaSourceView, Long mediaSourceId,Long labId) {
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
        } else if( MediaSourceType.POSTGRESQL.name().equals(mediaSourceView.getMediaSourceType().name())) {
            mediaSourceView.setDriver("org.postgresql.Driver");
        } else if( MediaSourceType.ORACLE.name().equals(mediaSourceView.getMediaSourceType().name()) ) {
            mediaSourceView.setDriver("oracle.jdbc.OracleDriver");
        } else if( MediaSourceType.HANA.name().equals(mediaSourceView.getMediaSourceType().name()) ) {
            mediaSourceView.setDriver("com.sap.db.jdbc.Driver");
        } else {
            throw new RuntimeException("unkonwn driver");
        }
        mediaSourceView.setDataSourceConfig(mediaSourceView.getDataSourceConfig());
        mediaSource.setParameter(mediaSourceView.toJsonString());
        mediaSource.setLabId(labId);
        return mediaSource;
    }

    @ResponseBody
    @RequestMapping(value = "/getMediaSourceById")
    @LoginIgnore
    public MediaSourceInfo getMediaSourceById(Long mediaSourceId) {
        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(mediaSourceId);
        if(!mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
            logger.info("getMediaSourceById接口返回的结果是：{}", JSON.toJSONString(mediaSourceInfo));
            return mediaSourceInfo;
        }
        //虚拟数据源下的两个真实数据源顺序要根据中心机房特殊处理下
        else{
            //中心机房
            String labName = doubleCenterService.getCenterLab(mediaSourceId);
            LabInfo centerLab = labService.getLabByName(labName);
            List<MediaSourceInfo> list = mediaSourceService.findRealListByVirtualMsId(mediaSourceInfo.getId());
            Long[] idArr = new Long[2];
            for (MediaSourceInfo info : list){
                //是中心机房
                if(info.getLabId().longValue() == centerLab.getId().longValue()){
                    idArr[0] = info.getId();
                }else{
                    idArr[1] = info.getId();
                }
            }
            List<Long> idList = new ArrayList<Long>();
            Collections.addAll(idList,idArr);
            VirtualMediaSrcParameter mediaSrcParameter = mediaSourceInfo.getParameterObj();
            mediaSrcParameter.setRealDbsId(idList);
            mediaSourceInfo.setParameter(mediaSrcParameter.toJsonString());
            logger.info("getMediaSourceById接口返回的结果是：{}", JSON.toJSONString(mediaSourceInfo));
            return mediaSourceInfo;
        }
    }

    @RequestMapping(value = "/toDatabaseList")
    public String toDatabaseList(Model model){

        return "mediaSource/databaseList";
    }

    @ResponseBody
    @RequestMapping(value = "/getDbInfo")
    public Object getDbInfo(String dbName,String dbType) {

        JSONObject jsonObject = null;
        try {
            logger.info(String.format("Receive a check request: \r\n dbName is %s , \r\n dbType is %s,",
                    dbName, dbType));

            jsonObject = mediaSourceService.getDbInfo(dbName,dbTypeMap.get(dbType), ManagerConfig.current().getCurrentEnv());

            jsonObject.put("code",200);
        }catch (Exception e) {
            jsonObject = new JSONObject();
            jsonObject.put("code",999);
        }

        return jsonObject;
    }

    static class IpInfoConfig {
        private Map<String,String> portMap;
        private Map<String,List<String>> readIpListMap;
        private Map<String,String> etlIpMap;
        private Map<String,String> writeIpMap;

        public Map<String, String> getPortMap() {
            return portMap;
        }

        public void setPortMap(Map<String, String> portMap) {
            this.portMap = portMap;
        }

        public Map<String, List<String>> getReadIpListMap() {
            return readIpListMap;
        }

        public void setReadIpListMap(Map<String, List<String>> readIpListMap) {
            this.readIpListMap = readIpListMap;
        }

        public Map<String, String> getEtlIpMap() {
            return etlIpMap;
        }

        public void setEtlIpMap(Map<String, String> etlIpMap) {
            this.etlIpMap = etlIpMap;
        }

        public Map<String, String> getWriteIpMap() {
            return writeIpMap;
        }

        public void setWriteIpMap(Map<String, String> writeIpMap) {
            this.writeIpMap = writeIpMap;
        }
    }

}
