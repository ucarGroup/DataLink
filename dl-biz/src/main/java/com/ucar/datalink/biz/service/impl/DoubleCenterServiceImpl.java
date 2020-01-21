package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.dal.*;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.HttpUtils;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.Position;
import com.ucar.datalink.domain.doublecenter.LabEnum;
import com.ucar.datalink.domain.doublecenter.LabSwitchInfo;
import com.ucar.datalink.domain.lab.LabInfo;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.task.*;
import com.ucar.datalink.domain.worker.WorkerInfo;
import org.I0Itec.zkclient.DataUpdater;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DoubleCenterServiceImpl implements DoubleCenterService {

    private static final Logger logger = LoggerFactory.getLogger(DoubleCenterServiceImpl.class);

    @Value("${biz.doublecenter.whereami}")
    private String whereami_url;

    @Autowired
    TaskDAO taskDAO;
    @Autowired
    LabSwitchDAO labSwitchDAO;
    @Autowired
    LabDAO labDAO;
    @Autowired
    MediaSourceRelationDAO mediaSourceRelationDAO;
    @Autowired
    MediaDAO mediaDAO;
    @Autowired
    LabService labService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    @Qualifier("taskPositionServiceZkImpl")
    TaskPositionService taskPositionService;

    @Autowired
    private TaskConfigService taskService;
    @Autowired
    WorkerService workerService;

    @Autowired
    MediaService mediaService;

    /**
     * DbDialectFactory、DataSourceFactory会大量获取当前中心机房，则需加缓存；因切机房需要，则缓存时间不能太长
     */
    private final LoadingCache<String, String > centerLabCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build(
            new CacheLoader<String, String>() {
                @Override
                public String load(String virtualMsId) throws Exception {
                    String path = DLinkZkPathDef.centerLab;
                    DLinkZkUtils zkUtils = DLinkZkUtils.get();
                    byte[] data = zkUtils.zkClient().readData(path, true);

                    Map<String,String> jsonMap = JSONObject.parseObject(data,Map.class);

                    /**
                     * 获取规则：
                     *      先看DB和中心机房有没有建立关系，如果有直接返回；
                     *      如果没有，最后取整体中心机房。
                     */
                    String centerLab = jsonMap.get(virtualMsId);
                    if(StringUtils.isBlank(centerLab)){
                        centerLab = jsonMap.get(Constants.WHOLE_SYSTEM);
                    }

                    return centerLab;
                }
            }
    );

    /**
     * 重要方法，被使用地方多
     *
     * 获取当前机房
     * @return
     */
    @Override
    public String getCenterLab(Object virtualMsId){
        if(!(virtualMsId instanceof String) && !(virtualMsId instanceof Long)){
            throw new DatalinkException("非法的参数类型，只支持String、Long");
        }
        return centerLabCache.getUnchecked(String.valueOf(virtualMsId));
    }

    @Override
    public String getLabByIp(String ip){
        String lab = HttpUtils.doGet(whereami_url + ip);
        return lab;
    }

    /**
     * 暂停或开启目标端为DB的增量任务
     *
     * @param mediaSourceIdList
     */
    @Override
    @Transactional
    public void stopOrStartIncrementTask(List<Long> mediaSourceIdList,TargetState targetState) {

        //查询DB关联的增量任务
        List<Long> taskIdList = mediaDAO.findTaskIdListByMediaSourceList(mediaSourceIdList);
        int count = taskIdList.size();
        int pageSize = 1000;//步长
        int totalPage = (count + pageSize - 1) / pageSize;
        for (int i = 0; i < totalPage; i ++){
            int start = i * pageSize;
            int end = (i + 1) * pageSize;
            if(end > count){
                end = count;
            }
            List<Long> tempList = taskIdList.subList(start,end);
            //停止
            taskDAO.batchUpdateTaskStatus(tempList, targetState);
        }
    }

    /**
     * 目标机房
     *
     * @param targetLab
     */
    @Override
    public void changeDataSource(List<Long> needSwitchIdList, String targetLab) {

        DLinkZkUtils zkUtils = DLinkZkUtils.get();
        String path = DLinkZkPathDef.centerLab;

        Map<String,String> map;
        //如果是整体切
        if(needSwitchIdList.size() == 1 && StringUtils.equals(String.valueOf(needSwitchIdList.get(0)),Constants.WHOLE_SYSTEM)){
            map = new HashMap();
            map.put(Constants.WHOLE_SYSTEM,targetLab);
        }
        //DB切
        else{
            byte[] data = zkUtils.zkClient().readData(path, true);
            map = JSONObject.parseObject(data,Map.class);
            for(Long id : needSwitchIdList){
                map.put(String.valueOf(id),targetLab);
            }

            //如果数据源的当前机房和中心机房一样,去掉它
            String currentCenterLab = map.get(Constants.WHOLE_SYSTEM);
            Iterator<Map.Entry<String,String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String,String> entry = iterator.next();
                String key = entry.getKey();
                String value = entry.getValue();
                if(StringUtils.equals(key, Constants.WHOLE_SYSTEM)){
                    continue;
                }
                if(StringUtils.equals(currentCenterLab,value)){
                    iterator.remove();
                }
            }
        }

        // 序列化
        byte[] bytes = JSON.toJSONBytes(map);
        zkUtils.zkClient().updateDataSerialized(path, new DataUpdater<byte[]>() {
            @Override
            public byte[] update(byte[] currentData) {
                return bytes;
            }
        });

    }

    /**
     * 一键停止跨机房同步
     */
    @Transactional
    @Override
    public void oneKeyStopSync(List<Long> acrossLabTaskIdList) {

        if(CollectionUtils.isEmpty(acrossLabTaskIdList)){
            return;
        }

        //查询夸机房任务
        List<TaskInfo> taskInfoList = taskDAO.findTaskInfoByBatchId(acrossLabTaskIdList);
        List<Long> taskIdList = taskInfoList.stream().map(info -> info.getId()).collect(Collectors.toList());
        int count = taskIdList.size();
        int pageSize = 1000;//步长
        int totalPage = (count + pageSize - 1) / pageSize;
        for (int i = 0; i < totalPage; i ++){
            int start = i * pageSize;
            int end = (i + 1) * pageSize;
            if(end > count){
                end = count;
            }
            List<Long> tempList = taskIdList.subList(start,end);
            //停止
            taskDAO.batchUpdateTaskStatus(tempList, TargetState.PAUSED);
        }
    }



    /**
     * 一键反向同步
     */
    @Override
    @Transactional
    public void oneKeyReverseSync(List<Long> acrossLabTaskIdList, Date switchStartTimeDate){

        if(CollectionUtils.isEmpty(acrossLabTaskIdList)){
            return;
        }

        //查询夸机房任务
        List<TaskInfo> taskInfoList = taskDAO.findTaskInfoByBatchId(acrossLabTaskIdList);

        //修改taskName、 task MediaSourceId、MediaId
        acrossTaskTransform(taskInfoList);

        //清空监控缓存
        monitorService.clearCache();

        //int i = 1/0;

        //修改task位点,在发起切机房时间的时间上减1个小时作为位点开始时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(switchStartTimeDate);
        calendar.add(Calendar.HOUR,-1);
        Date tempDate = calendar.getTime();
        logger.info("一键反向同步,重置位点时间为: " + new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(tempDate));
        updateTaskPosition(taskInfoList, tempDate.getTime());

        //重启worker
        reStartWorker(taskInfoList);

        //启任务
        for(Long taskId : acrossLabTaskIdList){
            taskService.resumeTask(taskId);
        }

    }

    /**
     * 更新task位点
     *
     * @param taskList
     */
    @Override
    public void updateTaskPosition(List<TaskInfo> taskList,Long newTimeStamps) {
        for (TaskInfo taskInfo : taskList){
            Position position = taskPositionService.getPosition(String.valueOf(taskInfo.getId()));
            MysqlReaderPosition mysqlReaderPosition = (MysqlReaderPosition)position;
            if(position == null){
                logger.warn("跨机房任务id{}、name{}在zk上面没有位点,则忽略", taskInfo.getId(), taskInfo.getTaskName());
            }else{
                mysqlReaderPosition.setTimestamp(newTimeStamps);
                taskPositionService.updatePosition(String.valueOf(taskInfo.getId()),mysqlReaderPosition);
            }
        }
    }

    /**
     * 重启worker
     *
     * @param taskList
     */
    @Override
    public void reStartWorker(List<TaskInfo> taskList) {
        if(CollectionUtils.isEmpty(taskList)){
            return;
        }
        Long groupId = taskList.get(0).getGroupId();
        List<WorkerInfo> list = workerService.getListForQuery(groupId);

        List<Future<?>> futures = new ArrayList<>();
        for(WorkerInfo workerInfo : list){
            Long workerId = workerInfo.getId();
            String url = "http://" + workerInfo.getWorkerAddress() + ":" + workerInfo.getRestPort() + "/worker/restartWorker/" + workerId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity request = new HttpEntity(null, headers);
            new RestTemplate(Lists.newArrayList(new FastJsonHttpMessageConverter())).postForObject(url, request, Map.class);
        }

    }

    @Override
    public Boolean insertLabSwitchInfo(LabSwitchInfo labSwitchInfo) {
        Integer num = labSwitchDAO.insert(labSwitchInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean updateLabSwitchInfo(LabSwitchInfo labSwitchInfo) {
        Integer num = labSwitchDAO.update(labSwitchInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public LabSwitchInfo getLabSwitchByVersion(String version) {
        LabSwitchInfo labSwitchInfo = labSwitchDAO.getLabSwitchByVersion(version);
        return labSwitchInfo;
    }



    @Autowired
    MediaSourceDAO mediaSourceDAO;

    /**
     * 一键改造老任务
     *
     */
    @Override
    @Transactional
    public List<Long> taskTransform(Boolean isUpdate) {

        List<Long> list = new ArrayList<Long>();
        //改造任务：任务的reader数据源、任务的name
        //查询单机房的mysql、HBase任务
        List<TaskInfo> taskList = taskDAO.findMysqlAndHBaseTasks();
        for(TaskInfo taskInfo : taskList){

            logger.info("当前任务是：name:{},id:{}",taskInfo.getTaskName(),taskInfo.getId());

            Boolean flag = false;
            Long mediaSourceId = taskInfo.getTaskReaderParameterObj().getMediaSourceId();
            Boolean isEs = false;

            MediaSourceInfo taskMediaSourceInfo = mediaSourceDAO.getById(mediaSourceId);
            if(!taskMediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
                MediaSourceRelationInfo mediaSourceRelationInfo = mediaSourceRelationDAO.getOneByRealMsId(mediaSourceId);
                if(mediaSourceRelationInfo != null){
                    if(isUpdate){
                        //更改reader数据源
                        taskInfo.getTaskReaderParameterObj().setMediaSourceId(mediaSourceRelationInfo.getVirtualMsId());
                        taskInfo.setTaskReaderParameter(taskInfo.getTaskReaderParameterObj().toJsonString());
                        taskInfo.setReaderMediaSourceId(mediaSourceRelationInfo.getVirtualMsId());
                        taskDAO.update(taskInfo);
                    }
                    flag = true;
                }
            }

            //改造mapping
            List<MediaMappingInfo> mappingInfoList = new ArrayList<>();
            if (taskInfo.getTaskType() == TaskType.MYSQL) {
                mappingInfoList = mediaDAO.findMediaMappingsByTaskId(taskInfo.getId());
            } else if (taskInfo.getTaskType() == TaskType.HBASE && taskInfo.isLeaderTask()) {
                mappingInfoList = mediaDAO.findMediaMappingsByTaskId(taskInfo.getId());
            }
            for (MediaMappingInfo info : mappingInfoList){

                //改造source media
                MediaSourceInfo mediaSourceInfo = info.getSourceMedia().getMediaSource();
                if(!mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
                    MediaSourceRelationInfo mediaSourceRelation = mediaSourceRelationDAO.getOneByRealMsId(mediaSourceInfo.getId());
                    if(mediaSourceRelation != null){
                        if(isUpdate){
                            //更改源端meidaSourceId
                            MediaInfo mediaInfo = new MediaInfo();
                            mediaInfo.setId(info.getSourceMediaId());
                            mediaInfo.setMediaSourceId(mediaSourceRelation.getVirtualMsId());
                            mediaDAO.updateMedia(mediaInfo);
                        }
                        flag = true;
                    }
                }

                //改造target media
                MediaSourceInfo targetMediaSourceInfo = mediaSourceDAO.getById(info.getTargetMediaSourceId());
                if(!targetMediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
                    MediaSourceRelationInfo mediaSourceRelationInfo = mediaSourceRelationDAO.getOneByRealMsId(info.getTargetMediaSourceId());
                    if(mediaSourceRelationInfo != null){
                        if(isUpdate) {
                            //更改目标端meidaSourceId
                            info.setTargetMediaSourceId(mediaSourceRelationInfo.getVirtualMsId());
                            mediaDAO.updateTargetMediaSource(info);
                        }

                        //如果是es，特殊处理
                        if(targetMediaSourceInfo.getType().equals(MediaSourceType.ELASTICSEARCH)){
                            isEs = true;
                        }

                        flag = true;
                    }
                }

            }

            //如果是es 更新主task为leader task，且新增follow task
            if(isUpdate && isEs){

                //任务名称
                String taskName = taskInfo.getTaskName();

                //leader
                //中心机房作为leader的lab id
                LabInfo labInfo = labDAO.getLabByName(getCenterLab(Constants.WHOLE_SYSTEM));
                taskInfo.setLabId(labInfo.getId());
                taskInfo.setIsLeaderTask(true);
                taskInfo.setTaskName(taskName + "_0");
                taskDAO.updateTask(taskInfo);

                //follow
                //取非中心机房
                List<LabInfo> labList = labDAO.findLabList();
                LabInfo noCenterLab = null;
                for(LabInfo info : labList){
                    if(info.getId().longValue() != labInfo.getId().longValue()){
                        noCenterLab = info;
                        break;
                    }
                }
                TaskInfo taskInfoFollow = new TaskInfo();
                BeanUtils.copyProperties(taskInfo, taskInfoFollow);
                taskInfoFollow.setTaskName(taskName + "_1");
                taskInfoFollow.setLeaderTaskId(taskInfo.getId());
                taskInfoFollow.setLabId(noCenterLab.getId());
                taskInfoFollow.setIsLeaderTask(false);
                taskDAO.insert(taskInfoFollow);

                //添加新建的任务
                list.add(taskInfoFollow.getId());
            }

            if(flag){
                list.add(taskInfo.getId());
            }
        }

        return list;
    }


    /**
     * 1 校验是否存在多条如下场景的映射，如果存在，需要先删除跨机房映射后再进行任务改造
     *  a_A库 (.*) 到 c库
     *  a_A库 (.*) 到 a_B库
     *
     *  2 检查sddl分库是否都配置了虚拟数据源
     */
    @Override
    public void checkTaskTransform(){

        //查询单机房的mysql、HBase任务
        List<TaskInfo> taskList = taskDAO.findMysqlAndHBaseTasks();

        for(TaskInfo taskInfo : taskList) {

            logger.info("当前任务是：name:{},id:{}",taskInfo.getTaskName(),taskInfo.getId());

            Long readerMediaSourceId = taskInfo.getTaskReaderParameterObj().getMediaSourceId();
            MediaSourceInfo taskMediaSourceInfo = mediaSourceDAO.getById(readerMediaSourceId);
            if (!taskMediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)) {
                MediaSourceRelationInfo mediaSourceRelationInfo = mediaSourceRelationDAO.getOneByRealMsId(readerMediaSourceId);
                if (mediaSourceRelationInfo != null) {

                    //校验是否存在多条如下场景的映射，如果存在，需要先删除跨机房映射后再进行任务改造
                    MediaMappingInfo mappingInfo = new MediaMappingInfo();
                    MediaInfo sourceMedia = new MediaInfo();
                    sourceMedia.setMediaSourceId(readerMediaSourceId);
                    sourceMedia.setName("(.*)");
                    mappingInfo.setSourceMedia(sourceMedia);
                    List<MediaMappingInfo> mediaMappingInfoList = mediaDAO.findMappingListByCondition(mappingInfo);
                    if(CollectionUtils.isNotEmpty(mediaMappingInfoList) && mediaMappingInfoList.size() >= 2){

                        List<MediaSourceRelationInfo> relationInfoListTemp = mediaSourceRelationDAO.findListByVirtualId(mediaSourceRelationInfo.getVirtualMsId());
                        Long targetMediaSourceId = null;
                        for (MediaSourceRelationInfo info : relationInfoListTemp){
                            if(!info.getRealMsId().equals(readerMediaSourceId)){
                                targetMediaSourceId = info.getRealMsId();
                                break;
                            }
                        }
                        for(MediaMappingInfo info : mediaMappingInfoList){
                            if(info.getTargetMediaSourceId().equals(targetMediaSourceId)){
                                throw new DatalinkException("请先删除跨机房同步的映射，然后再执行任务改造，源端数据源name是: " + taskMediaSourceInfo.getName());
                            }
                        }
                    }

                    //检查sddl分库是否都配置了虚拟数据源
                    checkSddlIsWithVirtual(taskMediaSourceInfo);

                }
            }

            //校验mapping
            List<MediaMappingInfo> mappingInfoList = mediaDAO.findMediaMappingsByTaskId(taskInfo.getId());
            for (MediaMappingInfo info : mappingInfoList){

                //校验source media
                MediaSourceInfo mediaSourceInfo = info.getSourceMedia().getMediaSource();
                if(!mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
                    MediaSourceRelationInfo mediaSourceRelation = mediaSourceRelationDAO.getOneByRealMsId(mediaSourceInfo.getId());
                    if(mediaSourceRelation != null){

                        //检查sddl分库是否都配置了虚拟数据源
                        checkSddlIsWithVirtual(mediaSourceInfo);
                    }
                }

                //校验target media
                MediaSourceInfo targetMediaSourceInfo = mediaSourceDAO.getById(info.getTargetMediaSourceId());
                if(!targetMediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
                    MediaSourceRelationInfo mediaSourceRelationInfo = mediaSourceRelationDAO.getOneByRealMsId(info.getTargetMediaSourceId());
                    if(mediaSourceRelationInfo != null){

                        //检查sddl分库是否都配置了虚拟数据源
                        checkSddlIsWithVirtual(targetMediaSourceInfo);
                    }
                }

            }

        }
    }

    /**
     * 检查sddl分库是否都配置了虚拟数据源，包含sddl自身校验
     *
     * @param mediaSourceInfo
     */
    public void checkSddlIsWithVirtual(MediaSourceInfo mediaSourceInfo){

        Long mediaSourceId = mediaSourceInfo.getId();
        Long sddlMediaSourceId = null;

        if(mediaSourceInfo.getType() == MediaSourceType.SDDL){
            sddlMediaSourceId = mediaSourceInfo.getId();
        }
        //查找
        else {

            //必须先判断sddl，因为有些库是分布式数据库的子库
            List<MediaSourceInfo> sddlMediaSources = mediaSourceDAO.getListByType(Sets.newHashSet(MediaSourceType.SDDL));
            for (MediaSourceInfo msInfo : sddlMediaSources) {
                SddlMediaSrcParameter sddlParam = msInfo.getParameterObj();

                List<Long> primaryDbsId = sddlParam.getPrimaryDbsId();
                for (Long dbId : primaryDbsId) {
                    if (Objects.equals(mediaSourceId, dbId)) {
                        sddlMediaSourceId = msInfo.getId();
                        break;
                    }
                }

                if (sddlMediaSourceId == null) {
                    List<Long> secondaryDbsId = sddlParam.getSecondaryDbsId();
                    if (secondaryDbsId != null) {
                        for (Long dbId : secondaryDbsId) {
                            if (Objects.equals(mediaSourceId, dbId)) {
                                sddlMediaSourceId = msInfo.getId();
                                break;
                            }
                        }
                    }
                }

                if (sddlMediaSourceId != null) {
                    break;
                }
            }
        }

        if(sddlMediaSourceId == null){
            return;
        }

        MediaSourceInfo sddlMediaSourceInfo = mediaSourceDAO.getById(sddlMediaSourceId);
        //校验分库是不是都配置了虚拟数据源
        MediaSourceRelationInfo relationInfo = mediaSourceRelationDAO.getOneByRealMsId(sddlMediaSourceId);
        if(relationInfo == null){
            throw new DatalinkException("sddl虚拟数据源配置不完整,不能改造任务,没有配置虚拟数据源的name是: " + sddlMediaSourceInfo.getName());
        }
        SddlMediaSrcParameter sddlParam = sddlMediaSourceInfo.getParameterObj();
        List<Long> primaryDbsId = sddlParam.getPrimaryDbsId();
        List<Long> secondaryDbsId = sddlParam.getSecondaryDbsId();
        for(Long id : primaryDbsId){
            MediaSourceRelationInfo temp = mediaSourceRelationDAO.getOneByRealMsId(id);
            if(temp == null){
                throw new DatalinkException("sddl虚拟数据源配置不完整,不能改造任务,没有配置虚拟数据源的name是: " + mediaSourceDAO.getById(id).getName());
            }
        }
        for(Long id : secondaryDbsId){
            MediaSourceRelationInfo temp = mediaSourceRelationDAO.getOneByRealMsId(id);
            if(temp == null){
                throw new DatalinkException("sddl虚拟数据源配置不完整,不能改造任务,没有配置虚拟数据源的name是: " + mediaSourceDAO.getById(id).getName());
            }
        }

    }


    /**
     * 一键改造反向任务
     *
     */
    @Override
    @Transactional
    public void acrossTaskTransform(List<TaskInfo> taskInfoList) {


        for (TaskInfo taskInfo : taskInfoList) {

            //如果跨机房任务的同步方向已经是正确的，那么就没必要反转了
            TaskInfo tempTask = taskService.getTask(taskInfo.getId());
            Long labId = tempTask.getTaskParameterObj().getSourceLabId();
            LabInfo labInfo = labService.getLabById(labId);

            MediaSourceRelationInfo mediaSourceRelationInfo = mediaSourceRelationDAO.getOneByRealMsId(tempTask.getReaderMediaSourceId());
            MediaSourceInfo mediaSourceInfo = mediaSourceDAO.getById(mediaSourceRelationInfo.getVirtualMsId());
            String centerLab;
            if(StringUtils.equals(mediaSourceInfo.getName(),Constants.UCAR_DATALINK)){
                centerLab = getCenterLab(Constants.DB_DATALINK);
            }else{
                centerLab = getCenterLab(mediaSourceRelationInfo.getVirtualMsId());
            }
            if(StringUtils.equals(centerLab,labInfo.getLabName())){
                continue;
            }

            //查询mapping
            List<MediaMappingInfo> list = mediaDAO.findMediaMappingsByTaskId(taskInfo.getId());
            if(CollectionUtils.isEmpty(list) || list.size() != 1){
                throw new DatalinkException("配置的映射信息不对,请检查后再试");
            }
            MediaMappingInfo mediaMappingInfo = list.get(0);

            //数据源
            Long mediaId = mediaMappingInfo.getSourceMediaId();
            Long sourceId = taskInfo.getReaderMediaSourceId();
            Long targetId = mediaMappingInfo.getTargetMediaSourceId();

            TaskParameter taskParameter = taskInfo.getTaskParameterObj();
            //sourceLabId
            Long sourceLabId = taskParameter.getSourceLabId();
            //targetLabId
            Long targetLabId = taskParameter.getTargetLabId();

            //新 taskName
            String taskName = taskInfo.getTaskName();
            String[] arr = taskName.split("_2_");
            taskName = arr[1] + "_2_" + arr[0];

            //更改reader
            taskInfo.getTaskReaderParameterObj().setMediaSourceId(targetId);
            taskInfo.setTaskReaderParameter(taskInfo.getTaskReaderParameterObj().toJsonString());
            taskInfo.setReaderMediaSourceId(targetId);
            taskInfo.setTaskName(taskName);
            taskParameter.setSourceLabId(targetLabId);
            taskParameter.setTargetLabId(sourceLabId);
            taskInfo.setTaskParameter(taskParameter.toJsonString());
            taskDAO.update(taskInfo);

            //更改mediaId
            MediaInfo mediaInfo = mediaDAO.findMediaById(mediaId);
            mediaInfo.setMediaSourceId(targetId);
            mediaDAO.updateMedia(mediaInfo);

            //更改mapping
            mediaMappingInfo.setTargetMediaSourceId(sourceId);
            mediaDAO.updateTargetMediaSource(mediaMappingInfo);
        }
    }

    @Override
    public List<LabSwitchInfo> findAll(){
        return labSwitchDAO.findAll();
    }


    @Override
    public MediaSourceInfo checkVirtualChangeReal(Long virtualMediaSourceId){
        //查找真实数据源A
        List<MediaSourceInfo> mediaSourceInfoList = mediaSourceDAO.findRealListByVirtualMsId(virtualMediaSourceId);
        if(CollectionUtils.isEmpty(mediaSourceInfoList) || mediaSourceInfoList.size() != 2){
            throw new DatalinkException("真实数据源的条数不对");
        }
        MediaSourceInfo mediaSourceInfoA = null;
        for (MediaSourceInfo mediaSourceInfo : mediaSourceInfoList){
            LabInfo labInfo = labDAO.getLabById(mediaSourceInfo.getLabId());
            if(StringUtils.equals(labInfo.getLabName(),LabEnum.logicA.getCode())){
                mediaSourceInfoA = mediaSourceInfo;
                break;
            }
        }
        if(mediaSourceInfoA == null){
            throw new DatalinkException("没有找到真实数据源A");
        }
        return mediaSourceInfoA;
    }

    /**
     * 查找SDDL数据源关联的任务
     *
     * @param mediaSourceIdList
     * @return
     */
    @Override
    public List<TaskInfo> findAssociatedTaskList(List<Long> mediaSourceIdList){

        //查询数据源关联的任务
        List<TaskInfo> resultList = new ArrayList<TaskInfo>();
        List<Long> taskIdList = mediaDAO.findTaskIdListByMediaSourceList(mediaSourceIdList);
        for (Long id : taskIdList){
            TaskInfo taskInfo = taskDAO.findById(id);
            //跨机房的任务不参与改造
            if(StringUtils.equals(taskInfo.getTaskSyncMode(), TaskSyncModeEnum.acrossLabSync.getCode())){
                continue;
            }
            resultList.add(taskInfo);
        }
        return resultList;
    }

    /**
     * 虚拟变实际
     *
     * @param virtualMediaSourceId
     * @return
     */
    @Override
    @Transactional
    public void virtualChangeReal(List<TaskInfo> taskList,Long virtualMediaSourceId,MediaSourceInfo mediaSourceInfoA){

        Map<Long,TaskInfo> map = new HashMap<Long,TaskInfo>();

        //检查、查找真实数据源A
        Long mediaSourceIdA = mediaSourceInfoA.getId();

        for(TaskInfo taskInfo : taskList) {

            Long mediaSourceId = taskInfo.getTaskReaderParameterObj().getMediaSourceId();
            if (virtualMediaSourceId.equals(mediaSourceId)) {
                MediaSourceInfo taskMediaSourceInfo = mediaSourceDAO.getById(mediaSourceId);
                if (taskMediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)) {
                    taskInfo.getTaskReaderParameterObj().setMediaSourceId(mediaSourceIdA);
                    taskInfo.setTaskReaderParameter(taskInfo.getTaskReaderParameterObj().toJsonString());
                    taskInfo.setReaderMediaSourceId(mediaSourceIdA);
                    taskDAO.update(taskInfo);
                }
            }

            List<MediaMappingInfo> mappingInfoList = mediaDAO.findMediaMappingsByTaskId(taskInfo.getId());
            for (MediaMappingInfo info : mappingInfoList) {

                //改造source media
                MediaInfo mediaInfo = info.getSourceMedia();
                MediaSourceInfo mediaSourceInfo = mediaInfo.getMediaSource();
                if (virtualMediaSourceId.equals(mediaSourceInfo.getId())) {
                    if (mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)) {
                        MediaInfo infoTemp = new MediaInfo();
                        infoTemp.setId(mediaInfo.getId());
                        infoTemp.setMediaSourceId(mediaSourceIdA);
                        mediaDAO.updateMedia(infoTemp);
                    }
                }

                //改造target media
                MediaSourceInfo targetMediaSourceInfo = mediaSourceDAO.getById(info.getTargetMediaSourceId());
                if(virtualMediaSourceId.equals(targetMediaSourceInfo.getId())){
                    if(targetMediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)){
                        info.setTargetMediaSourceId(mediaSourceIdA);
                        mediaDAO.updateTargetMediaSource(info);
                    }
                }

            }
        }
    }

    /**
     * 更新db
     */
    @Override
    public void updateDb(String version,Integer status,Integer process){
        LabSwitchInfo temp = getLabSwitchByVersion(version);
        temp.setStatus(status);
        temp.setSwitchProgress(process);
        temp.setEndTime(new Date());
        updateLabSwitchInfo(temp);
    }


    /**
     * 保存本次切机房结果到zk
     */
    @Override
    public void updateVersionToZk(String version, Integer status){

        Map<String,Integer> versionMap = null;
        byte[] datas = DLinkZkUtils.get().zkClient().readData(DLinkZkPathDef.switchLabVersion, true);
        if(datas == null){
            versionMap = new HashMap<String,Integer>();
        }else{
            versionMap = JSONObject.parseObject(datas,Map.class);
        }
        versionMap.put(version,status);

        byte[] bytes = JSON.toJSONBytes(versionMap);
        DLinkZkUtils.get().zkClient().updateDataSerialized(DLinkZkPathDef.switchLabVersion, new DataUpdater<byte[]>() {
            @Override
            public byte[] update(byte[] currentData) {
                return bytes;
            }
        });

    }



}
