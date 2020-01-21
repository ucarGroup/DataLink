package com.ucar.datalink.manager.core.doublecenter;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.service.impl.TaskPositionServiceZkImpl;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.HttpUtils;
import com.ucar.datalink.domain.doublecenter.DbCallBackCodeEnum;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.task.TaskSyncStatus;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.manager.core.coordinator.ClusterState;
import com.ucar.datalink.manager.core.coordinator.GroupMetadataManager;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.server.ServerContainer;
import com.zuche.confcenter.bean.vo.ClientDataSource;
import com.zuche.confcenter.client.api.ConfCenterApi;
import com.zuche.confcenter.client.manager.DefaultConfigCenterManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class SwitchLabService {

    private static final Logger logger = LoggerFactory.getLogger(SwitchLabService.class);
    private TaskConfigService taskConfigService;
    private MediaService mediaService;
    private WorkerService workerService;
    private TaskSyncStatusService taskSyncStatusService;
    private MediaSourceService mediaSourceService;
    private TaskPositionService taskPositionService;

    private static final String binlogCheckWhiteList = "datalink.binlogCheckWhiteList";
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    ConfCenterApi confCenterApi;

    public SwitchLabService(){
        taskConfigService = DataLinkFactory.getObject(TaskConfigService.class);
        mediaService = DataLinkFactory.getObject(MediaService.class);
        workerService = DataLinkFactory.getObject(WorkerService.class);
        taskSyncStatusService = DataLinkFactory.getObject(TaskSyncStatusService.class);
        mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        taskPositionService = DataLinkFactory.getObject(TaskPositionServiceZkImpl.class);

        //初始化conf_center
        DefaultConfigCenterManager.getInstance("conf_center");
        DefaultConfigCenterManager defaultConfigCenterManager = DefaultConfigCenterManager.getInstance();
        confCenterApi =  defaultConfigCenterManager.getConfCenterApi();
    }

    /**
     * 验证跨机房同步是否还有流量，校验耗时5分钟
     */
    public void checkBinlog(List<Long> acrossLabTaskIdList) {

        logger.info("校验binlog流量开始");

        if(CollectionUtils.isEmpty(acrossLabTaskIdList)){
            logger.info("校验binlog流量结束");
            return;
        }

        //先休眠1分钟，等待同步任务把数据同步完成
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }

        CountDownLatch countDownLatch = new CountDownLatch(240);//倒计时4分钟

        //查询夸机房任务
        List<TaskInfo> taskInfoList = taskConfigService.findTaskInfoByBatchId(acrossLabTaskIdList);

        //binlog校验不通过列表
        List<PositionGather> noPassList = new ArrayList<PositionGather>();

        //10秒试一次
        while(true){

            //如果4分钟内还有流量，就报错，人为介入
            if(countDownLatch.getCount() <= 0){

                //白名单
                ClientDataSource clientDataSource = confCenterApi.getDataSourceByKey(binlogCheckWhiteList);
                String binlogCheckWhiteListValue = "";
                if(clientDataSource != null){
                    binlogCheckWhiteListValue = clientDataSource.getSourceValue();
                }
                List<Long> whiteList = new ArrayList<Long>();
                if(StringUtils.isNotBlank(binlogCheckWhiteListValue)){
                    String taskIds = binlogCheckWhiteListValue;
                    String[] taskIdArr = taskIds.split(",");
                    for (String taskId : taskIdArr){
                        whiteList.add(Long.parseLong(taskId));
                    }
                }

                //过滤
                Collection leftList = Collections2.filter(noPassList, new Predicate<PositionGather>() {
                    @Override
                    public boolean apply(PositionGather input) {
                        for(Long taskId : whiteList){
                            if(taskId.equals(input.getTaskId())){
                                //false是排除；true是留下
                                return false;
                            }
                        }
                        return true;
                    }
                });

                if(CollectionUtils.isNotEmpty(leftList)){
                    logger.error("最后一次校验，校验binlog流量还是不通过，需要人为介入,不通过的任务有：,{}",JSON.toJSONString(leftList));
                    throw new DatalinkException("最后一次校验，校验binlog流量还是不通过，需要人为介入");
                }else{
                    logger.error("最后一次校验，校验binlog流量通过");
                    return;
                }
            }

            //清空binlog校验不通过列表
            noPassList.clear();

            Map<TaskInfo,PositionGather> map = new HashMap<TaskInfo,PositionGather>();
            //取一次
            getPosition(taskInfoList,map);

            //10秒校验一次
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
            //减10
            for (int i = 0; i < 10; i++){
                countDownLatch.countDown();
            }

            //10秒后，再取一次
            getPosition(taskInfoList,map);

            Map<String,PositionGather> positionMap = new HashMap<String,PositionGather>();
            Iterator<Map.Entry<TaskInfo,PositionGather>> positionIterator = map.entrySet().iterator();
            while(positionIterator.hasNext()){
                Map.Entry<TaskInfo,PositionGather> entry = positionIterator.next();
                positionMap.put(entry.getKey().getTaskName(),entry.getValue());
            }
            //logger.info("位点结果是：{}", JSON.toJSONString(positionMap));

            /**
             * 校验规则
             *      1 数据库层面的canal位点是否变化，如果前后两次不一样证明还有流量
             *      2 任务同步状态校验。如果状态是同步中，则认为有流量；如果状态不是同步中，但此状态维持的不到1分钟，也认为有流量
             */
            Iterator<Map.Entry<TaskInfo,PositionGather>> iterator = map.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<TaskInfo,PositionGather> entry = iterator.next();
                TaskInfo taskInfo = entry.getKey();
                PositionGather positionGather = entry.getValue();
                //任务同步状态
                TaskSyncStatus taskSyncStatus = taskSyncStatusService.getSyncStatus(String.valueOf(taskInfo.getId()));
                if(taskSyncStatus == null){
                    continue;
                }

                //校验
                Long time = System.currentTimeMillis() - taskSyncStatus.getUpdateTime();
                Boolean flag = (time / 1000) < 60;
                if((!StringUtils.equals(positionGather.getLatestEffectSyncLogFileNameBefore(),positionGather.getLatestEffectSyncLogFileNameAfter()))
                        || (!positionGather.getLatestEffectSyncLogFileOffsetBefore().equals(positionGather.getLatestEffectSyncLogFileOffsetAfter()))
                        || taskSyncStatus.getState().equals(TaskSyncStatus.State.Busy)
                        || ((!taskSyncStatus.getState().equals(TaskSyncStatus.State.Busy)) && flag)){
                    noPassList.add(positionGather);
                }
            }

            //没有流量
            if(CollectionUtils.isEmpty(noPassList)){
                logger.info("校验binlog流量通过");
                break;
            }else{
                List<Long> noPassListId = noPassList.stream().map(PositionGather :: getTaskId).collect(Collectors.toList());
                Collections.sort(noPassListId);

                Collections.sort(noPassList, new Comparator<PositionGather>() {
                    @Override
                    public int compare(PositionGather o1, PositionGather o2) {
                        if(o1.getTaskId() > o2.getTaskId()){
                            return 1;
                        }else if(o1.getTaskId() < o2.getTaskId()){
                            return -1;
                        }else {
                            return 0;
                        }
                    }
                });
                logger.info("校验binlog流量不通过,不通过的任务id有{},不通过的任务详情是{}", JSON.toJSONString(noPassListId),JSON.toJSONString(noPassList));
            }

        }

        logger.info("校验binlog流量结束");

    }

    /**
     * canal位点
     *
     * @param taskInfoList
     * @param map
     */
    public void getPosition(List<TaskInfo> taskInfoList, Map<TaskInfo,PositionGather> map){
        List<Future<?>> futures = new ArrayList<>();
        for (TaskInfo taskInfo : taskInfoList){

            //ucar_datalink忽略校验binlog
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(taskInfo.getReaderMediaSourceId());
            if(mediaSourceInfo.getType().equals(MediaSourceType.MYSQL) && StringUtils.equals(mediaSourceInfo.getParameterObj().getNamespace(),"ucar_datalink")){
                continue;
            }

            futures.add(pool.submit(new PositionFeture(taskInfo,map)));
        }
        for(Future f: futures) {
            try {
                f.get();
            } catch (Exception e) {
                logger.error("获取canal位点失败,原因：" + e.getMessage(),e);
                throw new RuntimeException("获取canal位点失败",e);
            }
        }
    }

    private class PositionFeture implements Callable {
        final TaskInfo taskInfo;
        final Map<TaskInfo,PositionGather> map;

        PositionFeture(TaskInfo taskInfo,Map<TaskInfo,PositionGather> map) {
            this.taskInfo = taskInfo;
            this.map = map;
        }

        @Override
        public Object call() throws Exception {

            //任务当前位点
            MysqlReaderPosition taskPosition = (MysqlReaderPosition)taskPositionService.getPosition(String.valueOf(taskInfo.getId()));
            PositionGather positionGather = map.get(taskInfo);
            if(positionGather == null){
                positionGather = new PositionGather();
                positionGather.setTaskId(taskInfo.getId());
                positionGather.setTaskName(taskInfo.getTaskName());
                if(taskPosition == null){
                    positionGather.setLatestEffectSyncLogFileNameBefore("");
                    positionGather.setLatestEffectSyncLogFileOffsetBefore(-1L);
                }else{
                    positionGather.setLatestEffectSyncLogFileNameBefore(taskPosition.getLatestEffectSyncLogFileName() == null ? "" : taskPosition.getLatestEffectSyncLogFileName());
                    positionGather.setLatestEffectSyncLogFileOffsetBefore(taskPosition.getLatestEffectSyncLogFileOffset() == null ? -1 : taskPosition.getLatestEffectSyncLogFileOffset());
                }
                map.put(taskInfo, positionGather);
            }else {
                if(taskPosition == null){
                    positionGather.setLatestEffectSyncLogFileNameAfter("");
                    positionGather.setLatestEffectSyncLogFileOffsetAfter(-1L);
                }else{
                    positionGather.setLatestEffectSyncLogFileNameAfter(taskPosition.getLatestEffectSyncLogFileName() == null ? "" : taskPosition.getLatestEffectSyncLogFileName());
                    positionGather.setLatestEffectSyncLogFileOffsetAfter(taskPosition.getLatestEffectSyncLogFileOffset() == null ? -1 : taskPosition.getLatestEffectSyncLogFileOffset());
                }
            }

            return true;
        }
    }

    public void reblance(List<Long> mediaSourceIdList) {

        GroupMetadataManager groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
        ClusterState clusterState = groupManager.getClusterState();

        //查询数据源关联的任务
        List<Long> taskIdList = mediaService.findTaskIdListByMediaSourceList(mediaSourceIdList);
        //收集需要reblance的groupId
        Set<Long> groupSet = new HashSet<Long>();
        for (Long taskId : taskIdList){
            TaskInfo taskInfo = taskConfigService.getTask(taskId);
            groupSet.add(taskInfo.getGroupId());
        }
        //组装需要reblance的group
        Map<Long,Integer> needReblanceMap = new HashMap<Long,Integer>();
        for (Long groupId : groupSet){
            ClusterState.GroupData groupData = clusterState.getGroupData(groupId);
            needReblanceMap.put(groupId,groupData != null ? groupData.getGenerationId() : 0);
        }

        logger.info("datalink切机房，需要reblance的分组有：{}", JSON.toJSONString(needReblanceMap.keySet()));

        //reblance
        for (Long groupId : needReblanceMap.keySet()){

            //是否有worker
            List<WorkerInfo> list = workerService.getListForQuery(groupId);
            if(CollectionUtils.isEmpty(list)){
                //无worker do nothing
                continue;
            }

            //调用GroupCoordinator方法进行reblance
            ServerContainer.getInstance().getGroupCoordinator().forceRebalance(String.valueOf(groupId));
        }

        //根据generationId来判断是否reblance完成
        while (true){

            Boolean flag = true;

            groupManager = ServerContainer.getInstance().getGroupCoordinator().getGroupManager();
            clusterState = groupManager.getClusterState();
            Iterator<Map.Entry<Long,Integer>> iterator = needReblanceMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<Long,Integer> entry = iterator.next();
                Long groupId = entry.getKey();
                Integer generationId = entry.getValue();
                ClusterState.GroupData groupData = clusterState.getGroupData(groupId);
                if(groupData.getGenerationId().intValue() <= generationId.intValue()){
                    flag = false;
                    break;
                }
            }

            if(flag){
                break;
            }else {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    logger.error("doReblance时，线程被打断，异常信息：{}",e);
                }
            }
        }
    }


    /**
     * 通知dbms切机房结果
     */
    public void notifyDbmsDbSwitchResult(String batchId, DbCallBackCodeEnum dbCallBackCodeEnum){
        /**
         * 回调dbms，会有安全校验
         */
        int time = 0;
        boolean flag = true;

        /**
         * 通知dbms
         */
        Long currentTime = System.currentTimeMillis();
        Map<String,Object> temp = new HashMap<String,Object>();
        temp.put("batchId",batchId);
        temp.put("statusCode", dbCallBackCodeEnum.getCode());
        temp.put("status",dbCallBackCodeEnum.getName());
        temp.put("currentTime",currentTime);

        /**
         * dbms有双重校验
         *      1 dbms有Authorization校验，这个校验是基础framework统一的，无法去掉
         *      2 和dbms、sddl约定的校验DBSWITCH_TOKEN
         *
         */
        //请求头
        Map<String,String> headerMap = new HashMap<String,String>();
        headerMap.put("Authorization","bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6ImRhdGFsaW5rYm90IiwidXNlcl9pZCI6MTU2MSwiZW1haWwiOiIiLCJleHAiOjIzODkyMjgyODJ9.bCbKWeHqYRi4RT2hnykoUf7NqoXk71AB37mcOpiRQw8");
        headerMap.put(Constants.DBSWITCH_TOKEN, DigestUtils.md5Hex(currentTime + ManagerConfig.current().getNotifyDbmsDbSwitchSecurekey()));

        do{
            try{
                logger.info("datalink通知dbms结果，请求的参数是：{}",JSON.toJSONString(temp));
                String url = ManagerConfig.current().getNotifyDbmsDbSwitchResultUrl();
                String json = HttpUtils.doPost(url, JSON.toJSONString(temp),headerMap);
                logger.info("datalink通知dbms结果，接口返回的内容是：{}",json);
                Map resultMap = JSON.parseObject(json, Map.class);
                Integer status = (Integer) resultMap.get("status");
                if(status.equals(1000)){
                    logger.info("datalink通知dbms单库机房结果成功，通知的内容是：成功。datalink单库切机房流程结束！");
                    flag = false;
                    return;
                }
            }catch (Exception e){
                logger.info("datalink通知dbms结果，接口报错，错误信息是：{}",e);
            }

            time ++;
            if(time > 3){
                logger.info("datalink通知dbms结果，已经重试了3次不成功，需要人为介入");
                break;
            }
            logger.info("datalink通知dbms结果，进行第{}次重试",time);
        }while(time <= 3 && flag);
    }



}
