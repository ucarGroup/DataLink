package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.dal.MediaSourceDAO;
import com.ucar.datalink.biz.dal.MediaSourceRelationDAO;
import com.ucar.datalink.biz.dal.TaskDAO;
import com.ucar.datalink.biz.meta.HBaseUtil;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.biz.service.DoubleCenterService;
import com.ucar.datalink.biz.service.LabService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.common.utils.HttpUtils;
import com.ucar.datalink.domain.lab.LabInfo;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceRelationInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.dove.DoveMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.fq.FqMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.hdfs.HDFSMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.virtual.VirtualMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.TaskInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by csf on 17/4/7.
 */
@Service
public class MediaSourceServiceImpl implements MediaSourceService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MediaSourceServiceImpl.class);

    private LoadingCache<Long, List<MediaSourceInfo>> realMediaSourceListCache;

    private static final Map<String,String> envMap = new HashMap<>(4);

    private static final Map<String,String> headerMap = new HashMap<>(4);

    @Autowired
    private MediaSourceDAO mediaSourceDAO;

    @Autowired
    private TaskDAO taskDAO;

    @Autowired
    private MediaDAO mediaDAO;

    @Autowired
    MediaSourceRelationDAO mediaSourceRelationDAO;

    @Autowired
    DoubleCenterService doubleCenterService;

    @Autowired
    LabService labService;

    @Value("${biz.dbms.ips.url}")
    private String dbmsIpsUrl;

    static {
        envMap.put("dev","test1");
        envMap.put("test","test1");

        headerMap.put("Cookie","csrftoken=GylVs5vKWaW1Lc6AP20jGlUjS7hEoOsA; sessionid=9cbsh86fs3yo28u0nlbpcxjr75tvwzv1");
        headerMap.put("Authorization","Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImRhdGFsaW5rYm90IiwidXNlcl9pZCI6MTU2MSwiZW1haWwiOiIiLCJleHAiOjIzODkyMjgyODJ9.bCbKWeHqYRi4RT2hnykoUf7NqoXk71AB37mcOpiRQw8");
    }

    public MediaSourceServiceImpl() {
        realMediaSourceListCache = CacheBuilder.newBuilder().build(
                new CacheLoader<Long, List<MediaSourceInfo>>() {
                    @Override
                    public List<MediaSourceInfo> load(Long id) throws Exception {
                        List<MediaSourceInfo> realMediaSourceList = mediaSourceDAO.findRealListByVirtualMsId(id);
                        return realMediaSourceList == null ? Lists.newArrayList() : realMediaSourceList;
                    }
                }
        );
    }

    @Override
    public void clearRealMediaSourceListCache(Long id) {
        List<MediaSourceInfo> realList = realMediaSourceListCache.getUnchecked(id);
        if (realList != null) {
            realMediaSourceListCache.invalidate(id);
            LOGGER.info("realMediaSourceListCache has been cleared for follower virtualId:" + id);
        }
    }

    @Override
    public List<MediaSourceInfo> getList() {
        return mediaSourceDAO.getList();
    }

    @Override
    public List<MediaSourceInfo> getListByType(Set<MediaSourceType> mediaSourceType) {
        return mediaSourceDAO.getListByType(mediaSourceType);
    }

    @Override
    public List<MediaSourceInfo> getListForQueryPage(@Param("mediaSourceType") Set<MediaSourceType> mediaSourceType, @Param("mediaSourceName") String mediaSourceName,@Param("mediaSourceIp") String mediaSourceIp) {
        return mediaSourceDAO.getListForQueryPage(mediaSourceType, mediaSourceName,mediaSourceIp);
    }

    @Override
    public Boolean insert(MediaSourceInfo mediaSourceInfo) {
        if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.ZOOKEEPER) {
            checkZkInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.FLEXIBLEQ) {
            checkFqInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.SDDL) {
            checkSddlInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.HDFS) {
            checkHDFSInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.ELASTICSEARCH) {
            checkEsInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.HBASE) {
            checkHBaseInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.KUDU) {
            checkKuduInfo(mediaSourceInfo);
        }else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.KAFKA) {
            checkKafkaInfo(mediaSourceInfo);
        }else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.DOVE) {
            checkDoveInfo(mediaSourceInfo);
        } else {
            checkRdbmsInfo(mediaSourceInfo);
        }
        Integer num;
        try {
            num = mediaSourceDAO.insert(mediaSourceInfo);
        } catch (DuplicateKeyException e) {
            throw new ValidationException(String.format("名称为%s的数据源已经存在.", mediaSourceInfo.getName()));
        }
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean update(MediaSourceInfo mediaSourceInfo) {
        if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.ZOOKEEPER) {
            checkZkInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.FLEXIBLEQ) {
            checkFqInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.SDDL) {
            checkSddlInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.HDFS) {
            checkHDFSInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.ELASTICSEARCH) {
            checkEsInfo(mediaSourceInfo);
        } else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.HBASE) {
            checkHBaseInfo(mediaSourceInfo);
        }else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.KUDU) {
            checkKuduInfo(mediaSourceInfo);
        }else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.KAFKA) {
            checkKafkaInfo(mediaSourceInfo);
        }else if (mediaSourceInfo.getParameterObj().getMediaSourceType() == MediaSourceType.DOVE) {
            checkDoveInfo(mediaSourceInfo);
        } else {
            checkRdbmsInfo(mediaSourceInfo);
        }
        Integer num;
        try {
            num = mediaSourceDAO.update(mediaSourceInfo);
        } catch (DuplicateKeyException e) {
            throw new ValidationException(String.format("名称为%s的数据源已经存在.", mediaSourceInfo.getName()));
        }
        if (num >= 0) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        List<TaskInfo> taskInfos = taskDAO.listByCondition(null, id, null, null, null);
        if (taskInfos != null && !taskInfos.isEmpty()) {
            throw new ValidationException(
                    String.format("编号为%s的Task正在读端使用该介质源，不能执行删除操作！",
                            JSONObject.toJSONString(taskInfos.stream().map(t -> t.getId()).collect(Collectors.toList())))
            );
        }

        List<MediaMappingInfo> srcMappingInfos = mediaDAO.findMediaMappingsBySrcMediaSourceId(id);
        if (srcMappingInfos != null && !srcMappingInfos.isEmpty()) {
            throw new ValidationException(
                    String.format("编号为%s的Task正在读端使用该介质源，不能执行删除操作！",
                            JSONObject.toJSONString(srcMappingInfos.stream().map(m -> m.getTaskId()).distinct().collect(Collectors.toList())))
            );
        }

        List<MediaMappingInfo> targetMappingInfos = mediaDAO.findMediaMappingsByTargetMediaSourceId(id);
        if (targetMappingInfos != null && !targetMappingInfos.isEmpty()) {
            throw new ValidationException(
                    String.format("编号为%s的Task正在写端使用该介质源，不能执行删除操作！",
                            JSONObject.toJSONString(targetMappingInfos.stream().map(m -> m.getTaskId()).distinct().collect(Collectors.toList())))
            );
        }

        //如果是虚拟数据源要删除对应关系
        MediaSourceInfo info = mediaSourceDAO.getById(id);
        if(info.getType().equals(MediaSourceType.VIRTUAL)){
            MediaSourceRelationInfo relationInfo = new MediaSourceRelationInfo();
            relationInfo.setVirtualMsId(id);
            mediaSourceRelationDAO.delete(relationInfo);
        }

        mediaDAO.deleteMediaByMediaSourceId(id);
        Integer num = mediaSourceDAO.delete(id);
        if (num >= 0) {
            return true;
        }
        return false;
    }

    @Override
    public MediaSourceInfo getById(Long id) {
        return mediaSourceDAO.getById(id);
    }

    @Override
    public List<String> getRdbTableName(MediaSourceInfo info) throws Exception{
        List<String> result = new LinkedList<>();
        result.add("(.*)");
        result.addAll(RDBMSUtil.getTableName(info));
        return result;
    }

    @Override
    public List<String> getHbaseTableName(MediaSourceInfo hbaseMediaSourceInfo) {
        return HBaseUtil.getTables(hbaseMediaSourceInfo).stream().map(i -> i.getName()).collect(Collectors.toList());
    }

    @Override
    public List<String> getRdbColumnName(MediaSourceInfo info, String tableName) throws Exception{
        return RDBMSUtil.getColumnName(info,tableName);
    }

    @Override
    public List<String> getMappingTableNameByTaskIdAndTargetMediaSourceId(Map<String, Object> mapParam) {
        List<MediaMappingInfo> mediaMappingList = mediaDAO.findMediaMappingsByTaskIdAndTargetMediaSourceId(mapParam);
        List<String> tableList = new ArrayList<>();
        for (MediaMappingInfo media : mediaMappingList) {
            tableList.add(media.getSourceMedia().getName());
        }
        return tableList;
    }

    @Override
    public void checkIpBelongLab(RdbMediaSrcParameter rdbMediaSrcParameter,Long labId) throws Exception {
        RdbMediaSrcParameter.WriteConfig writeConfig = rdbMediaSrcParameter.getWriteConfig();
        RdbMediaSrcParameter.ReadConfig readConfig = rdbMediaSrcParameter.getReadConfig();

        //校验4个ip是否属于所属机房
        String labName = doubleCenterService.getLabByIp(writeConfig.getWriteHost());
        LabInfo labInfo = labService.getLabByName(labName);
        if(labInfo == null || (!labInfo.getId().equals(labId))){
            throw new DatalinkException("填写的ip不属于选择的机房");
        }

        List<String> hosts = readConfig.getHosts();
        if (CollectionUtils.isNotEmpty(hosts)) {
            for (String ip : hosts) {
                labName = doubleCenterService.getLabByIp(ip);
                labInfo = labService.getLabByName(labName);
                if(labInfo == null || (!labInfo.getId().equals(labId))){
                    throw new DatalinkException("填写的ip不属于选择的机房");
                }
            }
        }

        labName = doubleCenterService.getLabByIp(readConfig.getEtlHost());
        labInfo = labService.getLabByName(labName);
        if(labInfo == null || (!labInfo.getId().equals(labId))){
            throw new DatalinkException("填写的ip不属于选择的机房");
        }
    }

    @Override
    public void checkDbConnection(RdbMediaSrcParameter rdbMediaSrcParameter) throws Exception {
        RdbMediaSrcParameter.WriteConfig writeConfig = rdbMediaSrcParameter.getWriteConfig();
        RdbMediaSrcParameter.ReadConfig readConfig = rdbMediaSrcParameter.getReadConfig();
        RDBMSUtil.checkRdbConnection(
                rdbMediaSrcParameter.getMediaSourceType().name(),
                writeConfig.getWriteHost(),
                rdbMediaSrcParameter.getPort(),
                rdbMediaSrcParameter.getNamespace(),
                writeConfig.getUsername(),
                writeConfig.getPassword());
        List<String> hosts = readConfig.getHosts();
        if (hosts != null && hosts.size() > 0) {
            for (String ip : hosts) {
                RDBMSUtil.checkRdbConnection(
                        rdbMediaSrcParameter.getMediaSourceType().name(),
                        ip,
                        rdbMediaSrcParameter.getPort(),
                        rdbMediaSrcParameter.getNamespace(),
                        readConfig.getUsername(),
                        readConfig.getPassword());
            }
        }
        RDBMSUtil.checkRdbConnection(
                rdbMediaSrcParameter.getMediaSourceType().name(),
                readConfig.getEtlHost(),
                rdbMediaSrcParameter.getPort(),
                rdbMediaSrcParameter.getNamespace(),
                readConfig.getUsername(),
                readConfig.getPassword());
    }

    private void checkRdbmsInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namespace = mediaSourceInfo.getParameterObj().getNamespace();
        if (!msName.startsWith(namespace)) {
            throw new ValidationException(String.format("数据源名称必须以%s为前缀.", namespace + "_"));
        }

        MediaSrcParameter srcParameter = mediaSourceInfo.getParameterObj();
        if (srcParameter instanceof RdbMediaSrcParameter) {
            RdbMediaSrcParameter rdbMediaSrcParameter = (RdbMediaSrcParameter) srcParameter;
            RdbMediaSrcParameter.ReadConfig readConfig = rdbMediaSrcParameter.getReadConfig();
            if (readConfig.getHosts().size() < 2) {
                throw new ValidationException("读库数量必须大于等于两个.");
            }

            if (StringUtils.isBlank(readConfig.getEtlHost())) {
                throw new ValidationException("Etl-Host为必输项，请配置.");
            }
        }
    }

    private void checkSddlInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String prefix = "sddl_";
        if (!msName.startsWith(prefix)) {
            throw new ValidationException(String.format("数据源名称必须以%s为前缀.", prefix));
        }
    }

    private void checkZkInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "zk_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("zk数据源名称必须以%s为前缀.", namePrefix));
        }
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.ZOOKEEPER);
        List<MediaSourceInfo> zkList = getListByType(setMediaSource);
        if (zkList != null) {
            zkList.forEach(zk -> {
                String servers = ((ZkMediaSrcParameter) zk.getParameterObj()).getServers();
                if (servers.equals(((ZkMediaSrcParameter) mediaSourceInfo.getParameterObj()).getServers()) && !zk.getId().equals(mediaSourceInfo.getId())) {
                    throw new ValidationException(String.format("ip地址已经存在：%s.", servers));
                }
            });
        }
    }

    private void checkFqInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "fq_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("fq数据源名称必须以%s为前缀.", namePrefix));
        }
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.FLEXIBLEQ);
        List<MediaSourceInfo> fqkList = getListByType(setMediaSource);
        FqMediaSrcParameter fqInfo = mediaSourceInfo.getParameterObj();
        if (fqkList != null) {
            fqkList.forEach(fq -> {
                FqMediaSrcParameter fqParameter = fq.getParameterObj();
                if (fqInfo.equals(fqParameter) && !fq.getId().equals(mediaSourceInfo.getId())) {
                    throw new ValidationException(String.format("topic已经存在：%s.", fqParameter.toJsonString()));
                }
            });
        }
    }
    private void checkDoveInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "dove_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("dove数据源名称必须以%s为前缀.", namePrefix));
        }
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.DOVE);
        List<MediaSourceInfo> list = getListByType(setMediaSource);
        DoveMediaSrcParameter para = mediaSourceInfo.getParameterObj();
        if (list != null) {
            list.forEach(dove -> {
                DoveMediaSrcParameter doveParameter = dove.getParameterObj();
                if (para.equals(doveParameter) && !dove.getId().equals(mediaSourceInfo.getId())) {
                    throw new ValidationException(String.format("topic已经存在：%s.", doveParameter.toJsonString()));
                }
            });
        }
    }

    public void checkHDFSInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "hdfs_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("HDFS数据源名称必须以%s为前缀.", namePrefix));
        }
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.HDFS);
        List<MediaSourceInfo> hdfsList = getListByType(setMediaSource);
        HDFSMediaSrcParameter hdfsParameter = mediaSourceInfo.getParameterObj();
        if (hdfsList != null) {
            hdfsList.forEach(hdfs -> {
                HDFSMediaSrcParameter hdfsPara = hdfs.getParameterObj();
                if (hdfsPara.getNameServices().equals(hdfsParameter.getNameServices()) && !hdfs.getId().equals(mediaSourceInfo.getId())) {
                    throw new ValidationException(String.format("nameServices已经存在：%s.", hdfsPara.getNameServices()));
                }
            });
        }
    }

    public void checkEsInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "es_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("ES数据源名称必须以%s为前缀.", namePrefix));
        }
    }

    public void checkHBaseInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "hbase_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("HBase名称必须以%s为前缀.", namePrefix));
        }
    }


    public void checkKuduInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "kudu_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("Kudu名称必须以%s为前缀.", namePrefix));
        }
    }


    public void checkKafkaInfo(MediaSourceInfo mediaSourceInfo) {
        String msName = mediaSourceInfo.getName();
        String namePrefix = "kafka_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("Kafka名称必须以%s为前缀.", namePrefix));
        }
    }

    public void checkVirtualInfo(MediaSourceInfo mediaSourceInfo,String realDbIds) {
        //name校验
        /*String msName = mediaSourceInfo.getName();
        String namePrefix = "virtual_";
        if (!msName.startsWith(namePrefix)) {
            throw new ValidationException(String.format("虚拟数据源名称必须以%s为前缀.", namePrefix));
        }*/

        //Schema校验
        String[] ids = realDbIds.split(",");
        if(ids.length != 2){
            throw new ValidationException("虚拟数据源必须包含两个真实数据源.");
        }
        MediaSourceInfo info1 = mediaSourceDAO.getById(Long.valueOf(ids[0]));
        MediaSourceInfo info2 = mediaSourceDAO.getById(Long.valueOf(ids[1]));
        if(!StringUtils.equals(info1.getParameterObj().getNamespace(),info2.getParameterObj().getNamespace())){
            throw new ValidationException("两个真实数据源的Schema必须相同.");
        }
    }

    @Override
    public Integer msCount() {
        return mediaSourceDAO.msCount();
    }

    @Override
    public List<StatisDetail> getCountByType() {
        return mediaSourceDAO.getCountByType();
    }

    public List<MediaSourceInfo> findRealListByVirtualMsId(Long id){
        return realMediaSourceListCache.getUnchecked(id);
    }

    @Override
    @Transactional
    public Boolean insertVirtual(MediaSourceInfo mediaSourceInfo,String realDbIds) {

        checkVirtualInfo(mediaSourceInfo,realDbIds);
        Integer num;
        try {
            num = mediaSourceDAO.insert(mediaSourceInfo);
        } catch (DuplicateKeyException e) {
            throw new ValidationException(String.format("名称为%s的数据源已经存在.", mediaSourceInfo.getName()));
        }

        //保存关联关系
        String[] ids = realDbIds.split(",");
        for(String id : ids){
            MediaSourceRelationInfo info = new MediaSourceRelationInfo();
            info.setVirtualMsId(mediaSourceInfo.getId());
            info.setRealMsId(Long.valueOf(id));
            info.setCreateTime(new Date());
            info.setModifyTime(new Date());
            try {
                mediaSourceRelationDAO.insert(info);
            } catch (DuplicateKeyException e) {
                MediaSourceInfo mediaSourceInfoTemp = mediaSourceDAO.getById(Long.valueOf(id));
                throw new ValidationException(String.format("名称为%s的数据源已经与其他虚拟数据源存在对应关系.", mediaSourceInfoTemp.getName()));
            }
        }

        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Boolean updateVirtual(MediaSourceInfo info,String realDbIds) {

        checkVirtualInfo(info,realDbIds);
        Integer num;
        try {
            num = mediaSourceDAO.update(info);
        } catch (DuplicateKeyException e) {
            throw new ValidationException(String.format("名称为%s的数据源已经存在.", info.getName()));
        }

        //保存关联关系
        String[] ids = realDbIds.split(",");
        MediaSourceRelationInfo temp = new MediaSourceRelationInfo();
        temp.setVirtualMsId(info.getId());
        List<Long> stayList = new ArrayList<Long>();
        for(String id : ids){
            temp.setRealMsId(Long.parseLong(id));
            Integer numTemp = mediaSourceRelationDAO.checkExist(temp);
            Boolean isExist = numTemp != null && numTemp == 1;
            if(!isExist){
                try {
                    mediaSourceRelationDAO.insert(temp);
                } catch (DuplicateKeyException e) {
                    MediaSourceInfo mediaSourceInfoTemp = mediaSourceDAO.getById(Long.valueOf(id));
                    throw new ValidationException(String.format("名称为%s的数据源已经与其他虚拟数据源存在对应关系.", mediaSourceInfoTemp.getName()));
                }
            }
            stayList.add(Long.parseLong(id));
        }

        //删除去掉的关系
        List<MediaSourceRelationInfo> list = mediaSourceRelationDAO.findListByVirtualId(info.getId());
        List<Long> allList = new ArrayList<Long>();
        for(MediaSourceRelationInfo e : list){
            allList.add(e.getRealMsId());
        }
        allList.removeAll(stayList);
        MediaSourceRelationInfo deleteInfo = new MediaSourceRelationInfo();
        deleteInfo.setVirtualMsId(info.getId());
        for(Long id : allList){
            deleteInfo.setRealMsId(id);
            mediaSourceRelationDAO.delete(deleteInfo);
        }

        if (num > 0) {
            return true;
        }

        return false;
    }


    public MediaSourceInfo getOneByName(String msName){
        return mediaSourceDAO.getOneByName(msName);
    }

    @Override
    public MediaSourceInfo findRealSignleByMsIdAndLab(Long virtualMsId, String labName) {
        Map<String,Object> parameterMap = new HashMap<>(4);
        parameterMap.put("virtualMsId",virtualMsId);
        parameterMap.put("labName",labName);
        return mediaSourceDAO.findRealSignleByMsIdAndLab(parameterMap);
    }

    @Override
    public List<MediaMappingInfo> getMappingByTaskIdAndTargetMediaSourceId(Map<String, Object> mapParam) {
        List<MediaMappingInfo> mediaMappingList = mediaDAO.findMediaMappingsByTaskIdAndTargetMediaSourceId(mapParam);
        return mediaMappingList;
    }

    @Override
    public JSONObject getDbInfo(String dbName,String dbType,String env) {
        Map<String,Object> parameterMap = new HashMap<>(4);
        parameterMap.put("db_name",dbName);
        parameterMap.put("dbenv_name",envMap.get(env.toLowerCase())==null?env.toLowerCase():envMap.get(env.toLowerCase()));
        parameterMap.put("db_type",dbType);
        LOGGER.info("dbms请求参数:"+JSONObject.toJSONString(parameterMap).toString());
        String result = HttpUtils.doPost(dbmsIpsUrl,parameterMap,headerMap);
        LOGGER.info("dbms返回结果:"+result);
        if(StringUtils.isEmpty(result)) {
            throw new RuntimeException("查询数据库ip信息为空");
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        if(jsonObject.getInteger("status")!=1000) {
            throw new RuntimeException(jsonObject.getString("msg"));
        }
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        if(jsonArray == null) {
            throw new RuntimeException("没有查到ip相关信息");
        }
        //是否是多数据库
        boolean isMultiDb = jsonArray.size()>1 ? true:false;
        Map<String,Integer> productNameMap = new HashMap<>(4);
        for (int i=0;i<jsonArray.size();i++) {
            JSONObject dbJson = jsonArray.getJSONObject(i);
            dbJson.put("id",i);
            String productName = dbJson.getString("product_name");
            Integer number = productNameMap.get(productName);
            if(number == null) {
                number = 0;
            }
            parameterMap.put(productName,++number);
            JSONArray ipList = dbJson.getJSONArray("ip_list");
            boolean isA = false;
            boolean isB = false;
            for (int j=0;j<ipList.size();j++) {
                JSONObject ipJson = ipList.getJSONObject(j);
                String logicName = ipJson.getString("idc_name");
                if("logicA".equals(logicName)) {
                    isA = true;
                }else if("logicB".equals(logicName)) {
                    isB = true;
                }
                if(isA && isB) {
                    dbJson.put("isDoubleCenter",true);
                    break;
                }
                dbJson.put("isDoubleCenter",false);
            }
        }
        for (Map.Entry<String,Integer> entry : productNameMap.entrySet()) {
            if(entry.getValue()>1) {
                throw new DatalinkException("同一业务线下存在多个相同的shema");
            }
        }
        JSONObject returnJson = new JSONObject();
        returnJson.put("isMultiDb",isMultiDb);
        returnJson.put("result",jsonArray);
        return returnJson;
    }

    @Override
    public List<MediaSourceInfo> getListByNameList(List<String> mediaSourceNameList) {
        return mediaSourceDAO.getListByNameList(mediaSourceNameList);
    }

    @Override
    @Transactional
    public boolean insertDoubleMediaSource(List<MediaSourceInfo> mediaSourceInfoList, List<String> existsMediaSourceList) {
        List<Long> mediaSourceIdList = new ArrayList<>();
        String virtualMediaSourceName = mediaSourceInfoList.get(0).getName();
        String virtualMediaSourceDesc =mediaSourceInfoList.get(0).getDesc();
        String namespace = mediaSourceInfoList.get(0).getParameterObj().getNamespace();
        MediaSourceType mediaSourceType = mediaSourceInfoList.get(0).getType();
        for (MediaSourceInfo mediaSourceInfo:mediaSourceInfoList) {
            String mediaSourceName = mediaSourceInfo.getName();
            if(mediaSourceInfo.getLabName().equals("logicA")) {
                MediaSourceInfo oldMediaSourceInfo = mediaSourceDAO.getOneByName(mediaSourceName);
                if(oldMediaSourceInfo!=null&&oldMediaSourceInfo.getType()!=MediaSourceType.VIRTUAL) {
                    mediaSourceInfo = oldMediaSourceInfo;
                    existsMediaSourceList.add(mediaSourceName);
                }
                mediaSourceName = mediaSourceName+"_A";
            }else {
                mediaSourceName = mediaSourceName+"_B";
            }
            mediaSourceInfo.setName(mediaSourceName);
            RdbMediaSrcParameter rdbMediaSrcParameter = mediaSourceInfo.getParameterObj();
            rdbMediaSrcParameter.setName(mediaSourceName);
            mediaSourceInfo.setParameter(rdbMediaSrcParameter.toJsonString());
            boolean isSuccess = false;
            if(mediaSourceInfo.getId() != null){
                isSuccess = update(mediaSourceInfo);
            }else{
                isSuccess = insert(mediaSourceInfo);
            }
            if(!isSuccess) {
                throw new RuntimeException("新增数据源失败");
            }
            mediaSourceIdList.add(mediaSourceInfo.getId());
        }

        VirtualMediaSrcParameter virtualMediaSrcParameter = new VirtualMediaSrcParameter();
        virtualMediaSrcParameter.setRealDbsId(mediaSourceIdList);
        virtualMediaSrcParameter.setNamespace(namespace);
        virtualMediaSrcParameter.setMediaSourceType(MediaSourceType.VIRTUAL);
        MediaSourceInfo virtualInfo = new MediaSourceInfo();
        virtualInfo.setName(virtualMediaSourceName);
        virtualInfo.setType(MediaSourceType.VIRTUAL);
        virtualInfo.setDesc(virtualMediaSourceDesc);
        virtualInfo.setParameter(virtualMediaSrcParameter.toJsonString());
        virtualInfo.setSimulateMsType(mediaSourceType);

        Boolean isSuccess = insertVirtual(virtualInfo, StringUtils.join(mediaSourceIdList,","));
        if(!isSuccess) {
            throw new RuntimeException("新增数据源失败");
        }
        mediaSourceInfoList.add(virtualInfo);

        return true;
    }

    @Override
    public List<MediaSourceInfo> getMediaSourceLikeSchema(String targetNamespace) {
        String likeTargetNamespace = "\"namespace\":\""+targetNamespace+"\"";
        return mediaSourceDAO.getMediaSourceLikeSchema(likeTargetNamespace);
    }

}
