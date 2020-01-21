package com.ucar.datalink.manager.core.doublecenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.IPUtils;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.common.zookeeper.ManagerMetaData;
import com.ucar.datalink.domain.doublecenter.LabSwitchInfo;
import com.ucar.datalink.domain.doublecenter.LabSwitchStatusEnum;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.domain.task.TaskInfo;
import com.zuche.confcenter.bean.vo.ClientDataSource;
import com.zuche.confcenter.bean.vo.DataSourceChange;
import com.zuche.confcenter.bean.vo.DataSourceTransport;
import com.zuche.confcenter.bean.vo.SendDataSourceTransport;
import com.zuche.confcenter.client.api.ConfCenterApi;
import com.zuche.confcenter.client.core.callback.DataChangeListener;
import com.zuche.confcenter.client.manager.DefaultConfigCenterManager;
import com.zuche.confcenter.util.TransferTypeEnum;
import org.I0Itec.zkclient.DataUpdater;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * datalink双中心切机房入口，主要包含
 *      1 datalink增量任务切换
 *      2 datax全量任务切换
 *      3 切机房大概耗时6分钟：3.1 校验binglog最多耗时5分钟 3.2 切机房耗时1分钟
 */
public class DoubleCenterServer {

    //存储切机房异常信息
    public static Map<String,String> map = new HashMap<String,String>();

    private static final Logger logger = LoggerFactory.getLogger(DoubleCenterServer.class);
    private static final String datalinkDcsSwitch = "datalink.dcs.switch";
    private static final String datalinkDcsProgress = "datalink.dcs.progress";
    private static final String zookeeperDcsSwitch = "zookeeper.dcs.switch";
    private static final String wholeDcsSwitch = "dcsm.whole.switch";

    private DoubleCenterService doubleCenterService;
    private TaskConfigService taskConfigService;
    private MediaService mediaService;
    private DoubleCenterDataxService doubleCenterDataxService;
    private MediaSourceService mediaSourceService;

    private SwitchLabService switchLabService;

    ConfCenterApi confCenterApi;
    DLinkZkUtils zkUtils;
    boolean isThrowBinLogError;
    String labSwitchKey;

    public DoubleCenterServer(DLinkZkUtils zkUtils,String isThrowBinLogError,String labSwitchKey){

        this.zkUtils = zkUtils;
        this.isThrowBinLogError = Boolean.valueOf(isThrowBinLogError);
        this.labSwitchKey = labSwitchKey;

        //初始化conf_center
        DefaultConfigCenterManager.getInstance("conf_center");
        DefaultConfigCenterManager defaultConfigCenterManager = DefaultConfigCenterManager.getInstance();
        confCenterApi =  defaultConfigCenterManager.getConfCenterApi();

        //如果已经初始化，直接返回
        boolean exists  = zkUtils.zkClient().exists(DLinkZkPathDef.switchLabVersion);
        if(!exists){
            // 序列化
            byte[] bytes = null;
            try {
                zkUtils.zkClient().createPersistent(DLinkZkPathDef.switchLabVersion, bytes, true);
            } catch (ZkNodeExistsException e) {
                logger.info("机房切换节点已经存在，DoNothing");
                //do nothing
            } catch (Exception e) {
                throw new DatalinkException("在zk上创建数据源节点出错", e);
            }
        }

        //增加监听器
        defaultConfigCenterManager.addDataChangeListenerList(new DynamicDataSourceListener());
    }

    private Integer switchProgress = 0;
    private Integer status = LabSwitchStatusEnum.开始切换.getCode();
    private Date startTime = null;
    private Date endTime = null;
    private Date switchStartTime = null;
    private LabSwitchView labSwitchView = new LabSwitchView();
    private ClientDataSource clientDataSource = null;

    class DynamicDataSourceListener implements DataChangeListener {

        @Override
        public void call(DataSourceTransport dataSourceTransport) {

            logger.info("打印配置中心数据:{}。",JSON.toJSONString(dataSourceTransport));

            switchProgress = 0;
            status = LabSwitchStatusEnum.开始切换.getCode();
            startTime = null;
            endTime = null;
            labSwitchView = new LabSwitchView();
            clientDataSource = null;
            //置空异常信息
            map.clear();

            clientDataSource = dataSourceTransport.getClientDataSource();
            String sourceName = clientDataSource.getSourceName();
            String sourceValue = clientDataSource.getSourceValue();

            try {

                startTime = new Date();

                /**
                 * 创建机房切换中临时节点
                 *      1、整体切机房开始时，zk服务也同时开始切机房，所以这个时候连接zk可能失败，则创建临时节点的时间放到了zk 服务切机房成功后
                 *      2、监听到zk 服务切机房成功后,同时校验版本号是否一致、整体切机房是不是开始状态，正常流程能校验通过的，则创建临时节点
                 */
                if(StringUtils.equals(sourceName,zookeeperDcsSwitch)){
                    Map map1 = JSON.parseObject(sourceValue,Map.class);
                    Integer status1 = (Integer)map1.get("status");
                    String version1 = (String) map1.get("version");
                    if(status1.equals(LabSwitchStatusEnum.切换完成.getCode())){

                        logger.info("datalink切机房准备工作: zk服务切机房成功，datalink接下来会创建机房切换中临时节点");

                        ClientDataSource clientDataSourceTemp = confCenterApi.getDataSourceByKey(wholeDcsSwitch);
                        String sourceValueTemp = clientDataSourceTemp.getSourceValue();
                        Map mapTemp = JSON.parseObject(sourceValueTemp,Map.class);
                        String version2 = (String) mapTemp.get("version");
                        Integer status2 = (Integer)mapTemp.get("status");
                        if(StringUtils.equals(version1, version2) && status2.equals(LabSwitchStatusEnum.开始切换.getCode())){

                            Long startTime = (Long) mapTemp.get("startTime");
                            switchStartTime = new Date(startTime);

                            /**
                             * 创建机房切换中节点,该节点存在就不能启动数据源关联的job（定时+非定时）
                             */
                            String processingPath = DLinkZkPathDef.labSwitchProcessing;
                            boolean exists  = zkUtils.zkClient().exists(processingPath);
                            if(!exists){

                                /**
                                 * 准备需要切机房的数据源
                                 */
                                //所有的虚拟数据源
                                Set<MediaSourceType> setMediaSource = new HashSet<>();
                                setMediaSource.add(MediaSourceType.VIRTUAL);
                                List<MediaSourceInfo> allVirtualMediaSourceList = mediaSourceService.getListByType(setMediaSource);


                                //需要切机房的数据源，去掉自己的当前机房就是目标机房的数据源
                                List<MediaSourceInfo> needSwitchList = allVirtualMediaSourceList.stream().filter(info -> !StringUtils.equals(doubleCenterService.getCenterLab(info.getId()),labSwitchView.getTargetCenter())).collect(Collectors.toList());
                                List<Long> needSwitchIdList = needSwitchList.stream().map(info -> info.getId()).collect(Collectors.toList());
                                //需要切机房的数据源，只要mysql类型
                                List<MediaSourceInfo> needSwitchMysqlList = needSwitchList.stream().filter(info2 -> info2.getSimulateMsType().equals(MediaSourceType.MYSQL)).collect(Collectors.toList());
                                //需要切机房的数据源，只要mysql类型且是真实数据源
                                List<MediaSourceInfo> needSwitchMysqlRealList = new ArrayList<MediaSourceInfo>();
                                for(MediaSourceInfo virtual : needSwitchMysqlList){
                                    MediaSourceInfo realMediaSource = mediaService.getRealDataSource(virtual);
                                    needSwitchMysqlRealList.add(realMediaSource);
                                }
                                List<Long> needSwitchMysqlRealIdList = needSwitchMysqlRealList.stream().map(info -> info.getId()).collect(Collectors.toList());
                                //mysql task跨机房列表
                                List<TaskInfo> taskInfoList = taskConfigService.findAcrossLabTaskListByMsList(needSwitchMysqlRealIdList);
                                List<Long> taskIdList = taskInfoList.stream().map(info -> info.getId()).collect(Collectors.toList());

                                //组装数据
                                String needSwitchIdStr = Joiner.on(",").join(needSwitchIdList);
                                String taskIdStr = Joiner.on(",").join(taskIdList);
                                Map<String,String> map = new HashMap<String,String>();
                                map.put("virtualMsIdList",needSwitchIdStr);
                                map.put("acrossLabTaskList",taskIdStr);
                                //存储到zk上
                                byte[] byteData = JSON.toJSONBytes(map);
                                try {
                                    zkUtils.zkClient().createPersistent(processingPath, byteData, true);
                                } catch (ZkNodeExistsException e) {
                                    logger.info("机房切换中节点已经存在，DoNothing");
                                    //do nothing
                                } catch (Exception e) {
                                    throw new DatalinkException("在zk上创建机房切换中节点出错", e);
                                }
                            }
                            logger.info("datalink切机房准备工作: 创建机房切换中临时节点成功");
                        }
                    }
                    return;
                }

                //只监听切机房的数据源
                if(!StringUtils.equals(sourceName,datalinkDcsSwitch)){
                    return;
                }

                logger.info("切机房入口，datalink收到配置中心的数据是:{}。",sourceValue);

                //切之前校验
                Boolean chechResult = preCheck(sourceValue);
                if(!chechResult){
                    return;
                }

                long currentTime = System.currentTimeMillis();
                logger.info("datalink整体切机房开始，当前时间是：{}",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                /**
                 * 取出要切机房的数据源id、跨机房任务id
                 */
                byte[] bytes = DLinkZkUtils.get().zkClient().readData(DLinkZkPathDef.labSwitchProcessing, true);
                Map map = JSONObject.parseObject(bytes,Map.class);
                String virtualMsIdStr = (String) map.get("virtualMsIdList");
                String acrossLabTaskStr = (String)map.get("acrossLabTaskList");
                //转换数据
                List<Long> needSwitchIdList = new ArrayList<Long>();
                List<Long> acrossLabTaskIdList = new ArrayList<Long>();
                if(StringUtils.isNotBlank(virtualMsIdStr)){
                    String[] arr = virtualMsIdStr.split(",");
                    for(String item : arr){
                        needSwitchIdList.add(Long.valueOf(item));
                    }
                }
                if(StringUtils.isNotBlank(acrossLabTaskStr)){
                    String[] arr = acrossLabTaskStr.split(",");
                    for(String item : arr){
                        acrossLabTaskIdList.add(Long.valueOf(item));
                    }
                }

                if(CollectionUtils.isEmpty(needSwitchIdList)){
                    //直接通知dbms切机房成功
                    logger.info("datalink切机房完成, 数据源都已经在目标机房，无需切换");
                    return;
                }
                logger.info("datalink切机房进行中, 需要切机房的数据源是：{}", JSON.toJSONString(needSwitchIdList));

                /**
                 * 保存本次状态到zk
                 */
                updateVersionToZk();
                logger.info("datalink切机房进行中, 更新Version和切换结果为开始到zk成功");
                switchProgress = 5;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * kill -9 datax job（定时+非定时）
                 */
                doubleCenterDataxService.stopSpecifiedRunningJob(needSwitchIdList);
                logger.info("datalink切机房进行中, kill -9 datax job成功");
                switchProgress = 15;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * 暂停增量任务
                 *      暂停目标端为DB的增量任务
                 */
                doubleCenterService.stopOrStartIncrementTask(needSwitchIdList, TargetState.PAUSED);
                logger.info("datalink切机房进行中, 暂停增量任务成功");
                switchProgress = 25;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * 校验binlog流量
                 *      是否已经完全从A同步到了B
                 */
                switchLabService.checkBinlog(acrossLabTaskIdList);
                logger.info("datalink切机房进行中, 校验binlog流量成功");
                switchProgress = 35;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * 一键停止跨机房同步
                 */
                doubleCenterService.oneKeyStopSync(acrossLabTaskIdList);
                logger.info("datalink切机房进行中, 一键停止跨机房同步成功");
                switchProgress = 45;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * 切数据源
                 *      整体切机房时，虚拟出一个mediaSourceId为-1，-1就代表整体切
                 */
                List<Long> tempIdList = new ArrayList<Long>();
                tempIdList.add(Long.valueOf(Constants.WHOLE_SYSTEM));
                doubleCenterService.changeDataSource(tempIdList, labSwitchView.getTargetCenter());
                logger.info("datalink切机房进行中， 数据源切换成功");
                switchProgress = 55;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * 睡眠10秒,因数据源有5s的缓存时间
                 */
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    logger.error("等待释放中心机房缓存失败,原因:{}" ,e);
                }
                switchProgress = 65;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * reblance
                 */
                switchLabService.reblance(needSwitchIdList);
                logger.info("datalink切机房进行中, reblance成功");
                switchProgress = 75;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * 更改datax定时任务的ip和端口
                 */
                doubleCenterDataxService.switchDataSourceForSpecifiedTimingJob(needSwitchIdList);
                logger.info("datalink切机房进行中， 更改datax定时任务ip成功");
                switchProgress = 85;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * 启动增量任务
                 *      启动目标端为DB的增量任务
                 */
                doubleCenterService.stopOrStartIncrementTask(needSwitchIdList, TargetState.STARTED);
                logger.info("datalink切机房进行中, 启动增量任务成功");
                switchProgress = 90;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * 删除机房切换中节点
                 */
                zkUtils.zkClient().delete(DLinkZkPathDef.labSwitchProcessing);
                logger.info("datalink切机房进行中，删除机房切换中节点成功");
                switchProgress = 95;
                endTime = new Date();
                updateDbAndGaea();

                /**
                 * 保存本次状态到zk
                 */
                switchProgress = 100;
                endTime = new Date();
                status = LabSwitchStatusEnum.切换完成.getCode();
                updateDbAndGaea();
                updateVersionToZk();
                logger.info("datalink切机房进行中, 更新机房切换状态为切换完成");

                logger.info("datalink整体切机房结束且成功,当前时间：{},共花费了：{}秒。",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),(System.currentTimeMillis() - currentTime)/1000);
                logger.info("换行");

            }catch (Exception e){

                //存储切机房异常信息
                map.put(labSwitchView.getVersion(), ExceptionUtils.getFullStackTrace(e));

                logger.info("datalink切机房失败，原因：{}。 ",e);
                endTime = new Date();
                status = LabSwitchStatusEnum.切换失败.getCode();
                updateDbAndGaea();

                /**
                 * 保存本次状态到zk
                 */
                updateVersionToZk();
                logger.info("datalink切机房进行中, 更新Version和切换结果为失败到zk成功");

            }

        }
    }

    //切之前校验
    public Boolean preCheck(String sourceValue){

        //切机房开关
        boolean flag = false;
        ClientDataSource labSwitchClientDataSource = confCenterApi.getDataSourceByKey(labSwitchKey);
        String labSwitchValue = "";
        if(labSwitchClientDataSource != null){
            labSwitchValue = labSwitchClientDataSource.getSourceValue();
        }
        if(StringUtils.isBlank(labSwitchValue) || StringUtils.equals(labSwitchValue,"true")){
            flag = true;
        }
        if(!flag){
            logger.info("切机房开关是关闭状态");
            return false;
        }

        //转换sourceValue为LabSwitchInfo
        labSwitchView = JSON.parseObject(sourceValue,LabSwitchView.class);

        //0：未开始 2:切换完成 3:切换失败 不处理
        if(labSwitchView.getStatus().intValue() != LabSwitchStatusEnum.开始切换.getCode().intValue()){
            logger.info("datalink收到配置中心的数据状态不是开始切换，则不需要切机房。");
            return false;
        }

        //logicA或者logicB校验
        if(((!StringUtils.equals(labSwitchView.getFromCenter(),"logicA")) && (!StringUtils.equals(labSwitchView.getFromCenter(),"logicB"))) ||
                ((!StringUtils.equals(labSwitchView.getTargetCenter(),"logicA")) && (!StringUtils.equals(labSwitchView.getTargetCenter(),"logicB")))){
            logger.info("datalink收到配置中心的机房code既不是logicA,也不是logicB,则不能切机房。");
            return false;
        }

        //version校验
        if(StringUtils.isBlank(labSwitchView.getVersion())){
            logger.info("datalink收到配置中心的数据字段version为空，则不能切机房。");
            return false;
        }

        //Active Manager才能切机房
        //获取Active Manager
        byte[] bytes = DLinkZkUtils.get().zkClient().readData(DLinkZkPathDef.ManagerActiveNode, true);
        ManagerMetaData managerMetaData = null;
        if (bytes != null) {
            managerMetaData = JSON.parseObject(bytes, ManagerMetaData.class);
        }
        if(managerMetaData == null){
            logger.info("目前没有Active Manager，不能执行切机房操作，请稍后再试。");
            return false;
        }
        //是Active Manager
        if(StringUtils.equals(IPUtils.getHostIp(),managerMetaData.getAddress())){
            logger.info("当前机器是Active Manager，它会执行切机房操作。");
        }
        //不是Active Manager
        else{
            logger.info("当前机器不是Active Manager，它不能执行切机房操作。");
            return false;
        }

        //此处可能需要校验只有目标机房的Active Manager才能切机房

        byte[] datas = DLinkZkUtils.get().zkClient().readData(DLinkZkPathDef.switchLabVersion, true);
        if(datas != null){
            Map<String,Integer> versionMap = JSONObject.parseObject(datas,Map.class);
            Integer status = versionMap.get(labSwitchView.getVersion());

            //幂等校验
            if(status != null){
                if(status.equals(LabSwitchStatusEnum.切换完成.getCode())){
                    logger.info("version是{}，该version之前切换过机房，则不需要重新切机房。",labSwitchView.getVersion());
                    return false;
                }else if(status.equals(LabSwitchStatusEnum.开始切换.getCode())){
                    logger.info("version是{}，该version正在切机房，则不需要重新发起。",labSwitchView.getVersion());
                    return false;
                }
            }
            //同一时刻只能发起一个机房切换校验
            else{
                Iterator<Map.Entry<String,Integer>> iteratorTemp = versionMap.entrySet().iterator();
                while (iteratorTemp.hasNext()){
                    Map.Entry<String,Integer> entry = iteratorTemp.next();
                    Integer statusTemp = entry.getValue();
                    if(statusTemp.equals(LabSwitchStatusEnum.开始切换.getCode())){
                        logger.info("同一时刻只能发起一个机房切换");
                        return false;
                    }
                }
            }
        }

        return true;

    }

    /**
     * 保存本次切机房结果到zk
     */
    public void updateVersionToZk(){

        String version = labSwitchView.getVersion();
        Integer status = labSwitchView.getStatus();
        Map<String,Integer> versionMap = null;
        byte[] datas = zkUtils.zkClient().readData(DLinkZkPathDef.switchLabVersion, true);
        if(datas == null){
            versionMap = new HashMap<String,Integer>();
        }else{
            versionMap = JSONObject.parseObject(datas,Map.class);
        }
        versionMap.put(version,status);

        byte[] bytes = JSON.toJSONBytes(versionMap);
        zkUtils.zkClient().updateDataSerialized(DLinkZkPathDef.switchLabVersion, new DataUpdater<byte[]>() {
            @Override
            public byte[] update(byte[] currentData) {
                return bytes;
            }
        });

    }


    /**
     * 更新db和配置中心
     */
    private void updateDbAndGaea(){

        //保存本次切机房请求,同时更新进度
        labSwitchView.setSwitchProgress(switchProgress);
        labSwitchView.setStatus(status);
        labSwitchView.setStartTime(startTime.getTime());
        labSwitchView.setEndTime(endTime.getTime());

        //数据
        LabSwitchInfo info = new LabSwitchInfo();
        BeanUtils.copyProperties(labSwitchView,info);
        info.setStartTime(startTime);
        info.setEndTime(endTime);
        info.setSwitchStartTime(switchStartTime);

        //保存或者更新
        LabSwitchInfo temp = doubleCenterService.getLabSwitchByVersion(info.getVersion());
        if(temp == null){
            doubleCenterService.insertLabSwitchInfo(info);
        }else{
            info.setId(temp.getId());
            doubleCenterService.updateLabSwitchInfo(info);
        }

        //更新配置中心进度
        DataSourceChange change = new DataSourceChange();
        change.setSendDataSourceTransports(convert(clientDataSource,labSwitchView));
        try {
            confCenterApi.updateServerDataSource(change);
        }catch (Exception e){
            throw new DatalinkException(e.getMessage(),e);
        }
        logger.info("datalink切机房进行中， 更新db和配置中心成功");

    }

    /**
     * 组装回写的数据
     *
     * @param clientDataSource
     * @param labSwitchView
     * @return
     */
    public List<SendDataSourceTransport> convert(ClientDataSource clientDataSource,LabSwitchView labSwitchView){
        List<SendDataSourceTransport> transportList = new ArrayList<SendDataSourceTransport>();

        ClientDataSource source = new ClientDataSource();
        source.setSourceName(datalinkDcsProgress);
        source.setSourceValue(JSON.toJSONString(labSwitchView));
        source.setBusinessLine(clientDataSource.getBusinessLine());
        //0私有，1公共
        source.setSourceType((byte) 1);
        SendDataSourceTransport transport = new SendDataSourceTransport();
        transport.setTransferTypeEnum(TransferTypeEnum.TRANSFER_UPDATE);
        transport.setClientDataSource(source);
        transport.setDataSourceDesc(datalinkDcsProgress);
        transportList.add(transport);
        return transportList;
    }

    public void startup(){
        doubleCenterService = DataLinkFactory.getObject(DoubleCenterService.class);
        taskConfigService = DataLinkFactory.getObject(TaskConfigService.class);
        mediaService = DataLinkFactory.getObject(MediaService.class);
        doubleCenterDataxService = DataLinkFactory.getObject(DoubleCenterDataxService.class);
        mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        switchLabService = new SwitchLabService();
    }

    private static final String MYSQL_URL = "jdbc:mysql://{0}:{1}/{2}";
    private long getDbPosition(MediaSourceInfo mediaSourceInfo) throws Exception{
        RdbMediaSrcParameter parameter = mediaSourceInfo.getParameterObj();
        String ip = parameter.getWriteConfig().getWriteHost();
        int port = parameter.getPort();
        String schema = parameter.getNamespace();
        String url = MessageFormat.format(MYSQL_URL, ip, port + "", schema);

        //canal有show master status权限
        String username = parameter.getReadConfig().getUsername();
        String password =  parameter.getReadConfig().getDecryptPassword();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();
            rs = stmt.executeQuery("show master status");
            while(rs.next()){
                long position = rs.getLong("Position");
                return position;
            }
            return 0L;
        } finally {
            close(conn, stmt, rs);
        }
    }

    private static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }

    private static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }

    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }

    private static void close(Connection conn, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }


}
