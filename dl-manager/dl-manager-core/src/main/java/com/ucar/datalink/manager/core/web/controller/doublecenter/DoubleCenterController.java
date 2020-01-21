package com.ucar.datalink.manager.core.web.controller.doublecenter;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.utils.HttpUtils;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.doublecenter.*;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceRelationInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.manager.core.doublecenter.DatalinkSwitchLabThread;
import com.ucar.datalink.manager.core.doublecenter.DbSwitchLabThread;
import com.ucar.datalink.manager.core.doublecenter.DoubleCenterServer;
import com.ucar.datalink.manager.core.doublecenter.SwitchLabService;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.utils.NetworkUtil;
import com.ucar.datalink.manager.core.web.annotation.LoginIgnore;
import com.ucar.datalink.manager.core.web.dto.doublecenter.DbInfo;
import com.ucar.datalink.manager.core.web.dto.doublecenter.DbSwitchLabModel;
import com.ucar.datalink.manager.core.web.util.Page;
import org.I0Itec.zkclient.DataUpdater;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Created by daijunjian on 2018/10/12.
 */
@Controller
@RequestMapping(value = "/doublecenter/")
public class DoubleCenterController {

    private static final Logger logger = LoggerFactory.getLogger(DoubleCenterController.class);

    @Autowired
    DoubleCenterService doubleCenterService;
    @Autowired
    MediaSourceService mediaSourceService;
    @Autowired
    MediaSourceRelationService mediaSourceRelationService;

    @Autowired
    MediaService mediaService;
    @Autowired
    TaskConfigService taskConfigService;

    @Autowired
    SysPropertiesService sysPropertiesService;

    @LoginIgnore
    @RequestMapping(value = "/labSwitchList")
    public String labSwitchList() {
        return "labSwitch/list";
    }

    @LoginIgnore
    @RequestMapping(value = "/intLabSwitch")
    @ResponseBody
    public Page<LabSwitchInfo> intLabSwitch(Model model) {
        List<LabSwitchInfo> list = doubleCenterService.findAll();
        list.stream().map(labSwitchInfo -> {
            labSwitchInfo.setStatusName(LabSwitchStatusEnum.getEnumByCode(labSwitchInfo.getStatus()).getName());
            labSwitchInfo.setSwitchProgressName(labSwitchInfo.getSwitchProgress() + "%");
            //页面展示切机房异常
            if(StringUtils.isNotBlank(DoubleCenterServer.map.get(labSwitchInfo.getVersion()))){
                labSwitchInfo.setLabSwitchException(DoubleCenterServer.map.get(labSwitchInfo.getVersion()));
            }else{
                labSwitchInfo.setLabSwitchException("");
            }
            return labSwitchInfo;
        }).collect(Collectors.toList());
        return new Page<LabSwitchInfo>(list);
    }

    @RequestMapping(value = "/showException")
    @ResponseBody
    public String showException(String version) {
        if (StringUtils.isNotBlank(version)) {
            String exception = DoubleCenterServer.map.get(version);
            return exception;
        }
        return "";
    }


    public Map checkSwitchLab(DbSwitchLabModel model, HttpServletRequest request){

        //结果
        Map resultMap = new HashMap(2);

        //安全校验
        Long currentTime = model.getCurrentTime();
        String sign = DigestUtils.md5Hex(currentTime + ManagerConfig.current().getNotifyDbmsDbSwitchSecurekey());
        String dbSwitch_token = request.getHeader(Constants.DBSWITCH_TOKEN);
        //验证未通过
        if(!StringUtils.equals(sign,dbSwitch_token)){
            resultMap.put("flag",false);
            resultMap.put("message","非法访问");
            logger.info("非法访问,访问ip：{}, currentTimeAndkey：{}, requestSign：{}, myselfSign：{}", NetworkUtil.getIpAddress(request),currentTime + ManagerConfig.current().getNotifyDbmsDbSwitchSecurekey(),dbSwitch_token,sign);
            return resultMap;
        }

        if(StringUtils.isBlank(model.getBatchId())){
            resultMap.put("flag",false);
            resultMap.put("message","单库切机房失败，batchId不能为空");
            return resultMap;
        }
        IdLabEnum currentCenterEnum = IdLabEnum.getEnumByCode(model.getCurrentCenter());
        IdLabEnum targetCenterEnum = IdLabEnum.getEnumByCode(model.getTargetCenter());
        if(currentCenterEnum == null || targetCenterEnum == null){
            resultMap.put("flag",false);
            resultMap.put("message","单库切机房失败，传入的原机房或者目标机房值不合法");
            return resultMap;
        }
        if(currentCenterEnum.equals(targetCenterEnum)){
            resultMap.put("flag",false);
            resultMap.put("message","单库切机房失败，原机房和目标机房不能一样");
            return resultMap;
        }
        if(StringUtils.isBlank(model.getSwitchStartTime())){
            resultMap.put("flag",false);
            resultMap.put("message","单库切机房失败，switchStartTime不能为空");
            return resultMap;
        }
        //开始时间进行校验(涉及反转的位点)
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(model.getSwitchStartTime());
            if(date.getTime() > System.currentTimeMillis()){
                resultMap.put("flag",false);
                resultMap.put("message","单库切机房失败，切机房开始时间不能大于当前系统时间");
                return resultMap;
            }
            System.out.println("------------开始时间进行校验------------: " + new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(date));
            model.setSwitchStartTimeDate(date);
        } catch (ParseException e) {
            resultMap.put("flag",false);
            resultMap.put("message","单库切机房失败，切机房开始时间值不合法");
            return resultMap;
        }

        resultMap.put("flag",true);
        return resultMap;
    }

    /**
     * 单库切机房接口，给DBMS使用
     *
     * @param json
     * @return
     */
    @LoginIgnore
    @RequestMapping(value = "/dbSwitchLab_4_dbms",method = RequestMethod.POST)
    @ResponseBody
    public Object dbSwitchLab(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {

        logger.info("收到DBMS单库切机房请求， 收到的数据是:{} {}", System.getProperty("line.separator"),json);

        //参数数据
        DbSwitchLabModel model = JSON.parseObject(json,DbSwitchLabModel.class);

        //返回值
        Map returnMap = new HashMap();

        /**
         * 简单校验
         */
        Map resultMap = checkSwitchLab(model, request);
        Boolean flag = (Boolean)resultMap.get("flag");
        //校验不通过
        if(!flag){
            response.setStatus(500);
            returnMap.put("message",resultMap.get("message"));
            logger.info((String) resultMap.get("message"));
            return returnMap;
        }

        /*if(1==1){
            notifyDbmsDbSwitchResult(DbCallBackCodeEnum.DATALINK_OPS_SUCCESS,model.getBatchId());
        }*/

        //经常使用的字段
        String batchId = model.getBatchId();
        String targetCenter = IdLabEnum.getEnumByCode(model.getTargetCenter()).getName();

        //每个数据库对应两条记录，一个代表A机房数据库信息，一个代表B机房数据库信息，uuid来确定他们是同一个库。则用库维度（uuid）来分组
        Map<String, List<DbInfo>> groupMap = model.getDbList().stream().collect(Collectors.groupingBy(DbInfo::getUuid));
        //要切换的虚拟数据源列表
        List<MediaSourceInfo> needSwitchList = new ArrayList<MediaSourceInfo>();
        Set<String> businessLineSet = new HashSet<String>();

        Iterator<Map.Entry<String, List<DbInfo>>> iterator = groupMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = iterator.next();
            String uuid = (String) entry.getKey();
            List<DbInfo> dbInfoList = (List<DbInfo>)entry.getValue();

            if(CollectionUtils.size(dbInfoList) != 2){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",传入的数据条数不是2条";
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }

            DbInfo dbInfoLogicA = null;
            DbInfo dbInfoLogicB = null;
            for(DbInfo info : dbInfoList){
                if(info.getIdcId().equals(IdLabEnum.logicA.getCode())){
                    dbInfoLogicA = info;
                }else if(info.getIdcId().equals(IdLabEnum.logicB.getCode())){
                    dbInfoLogicB = info;
                }
            }

            if(dbInfoLogicA == null || dbInfoLogicB == null){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",传入的数据不包含A机房或者B机房信息";
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }

            if((!StringUtils.equals(dbInfoLogicA.getDbName(),dbInfoLogicB.getDbName()))
                    || ((!StringUtils.equals(dbInfoLogicA.getDbType(),dbInfoLogicB.getDbType())))
                    || ((!StringUtils.equals(dbInfoLogicA.getProductName(),dbInfoLogicB.getProductName())))){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",传入的A机房与B机房的数据不匹配";
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }

            if(StringUtils.isBlank(dbInfoLogicA.getDbName())
                    || StringUtils.isBlank(dbInfoLogicA.getDbType())
                    || StringUtils.isBlank(dbInfoLogicA.getIpAddress())
                    || StringUtils.isBlank(dbInfoLogicA.getProductName())){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",db信息存在为空的情况";
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }

            DbMediaTypeEnum dbMediaTypeEnum = DbMediaTypeEnum.getEnumByCode(dbInfoLogicA.getDbType());
            if(dbMediaTypeEnum == null){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",传入的dbType值不合法";
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }

            //ip、port
            String [] arrLogicA = dbInfoLogicA.getIpAddress().split(":");
            String logicAIp = arrLogicA[0];
            String logicAPort = arrLogicA[1];
            String [] arrLogicB = dbInfoLogicB.getIpAddress().split(":");
            String logicBIp = arrLogicB[0];
            String logicBPort = arrLogicB[1];

            //校验A机房和B机房的数据库信息且找出对应的虚拟数据源
            Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
            setMediaSource.add(MediaSourceType.valueOf(dbMediaTypeEnum.getName()));
            List<MediaSourceInfo> list = mediaSourceService.getListByType(setMediaSource);

            MediaSourceInfo logicAMediaSourceInfo = null;
            MediaSourceInfo logicBMediaSourceInfo = null;
            for(MediaSourceInfo mediaSourceInfo : list){
                RdbMediaSrcParameter rdbMediaSrcParameter = mediaSourceInfo.getParameterObj();
                if(StringUtils.equals(rdbMediaSrcParameter.getNamespace().toLowerCase(),dbInfoLogicA.getDbName())
                        && StringUtils.equals(rdbMediaSrcParameter.getWriteConfig().getWriteHost(),logicAIp)
                        && rdbMediaSrcParameter.getPort() == Integer.parseInt(logicAPort)){
                    logicAMediaSourceInfo = mediaSourceInfo;
                }
                if(StringUtils.equals(rdbMediaSrcParameter.getNamespace().toLowerCase(),dbInfoLogicA.getDbName())
                        && StringUtils.equals(rdbMediaSrcParameter.getWriteConfig().getWriteHost(),logicBIp)
                        && rdbMediaSrcParameter.getPort() == Integer.parseInt(logicBPort)){
                    logicBMediaSourceInfo = mediaSourceInfo;
                }
            }
            if(logicAMediaSourceInfo == null){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",A机房数据库不存在，库名: " + dbInfoLogicA.getDbName() + ",地址：" + logicAIp + ":" + logicAPort;
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }
            if(logicBMediaSourceInfo == null){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",B机房数据库不存在，库名: " + dbInfoLogicA.getDbName() + ",地址：" + logicAIp + ":" + logicAPort;
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }
            if(logicAMediaSourceInfo.getId().equals(logicBMediaSourceInfo.getId())){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",A、B机房数据库不能是同一个库";
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }
            MediaSourceRelationInfo mediaSourceRelationInfoA = mediaSourceRelationService.getOneByRealMsId(logicAMediaSourceInfo.getId());
            MediaSourceRelationInfo mediaSourceRelationInfoB = mediaSourceRelationService.getOneByRealMsId(logicBMediaSourceInfo.getId());
            if(mediaSourceRelationInfoA == null || mediaSourceRelationInfoB == null || (!mediaSourceRelationInfoA.getVirtualMsId().equals(mediaSourceRelationInfoB.getVirtualMsId()))){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",虚拟数据源和实际数据源不对应，请提醒datalink检查";
                returnMap.put("message",temp);
                logger.info(temp);
                return returnMap;
            }
            Long virtualMsId = mediaSourceRelationInfoA.getVirtualMsId();
            MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(virtualMsId);
            if(mediaSourceInfo == null){
                response.setStatus(500);
                String temp = "单库切机房失败，uuid是:" + uuid + ",虚拟数据源未找到，请提醒datalink检查";
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }

            //加入
            needSwitchList.add(mediaSourceInfo);

            businessLineSet.add(dbInfoLogicA.getProductName());
        }

        if(CollectionUtils.isEmpty(needSwitchList)){
            response.setStatus(500);
            String temp = "单库切机房失败，数据源未找到，请提醒datalink检查";
            returnMap.put("message",temp);
            logger.info(temp);
            return  returnMap;
        }

        byte[] datas = DLinkZkUtils.get().zkClient().readData(DLinkZkPathDef.switchLabVersion, true);
        if(datas != null){
            Map<String,Integer> versionMap = JSONObject.parseObject(datas,Map.class);
            Integer status = versionMap.get(batchId);

            //幂等校验
            if(status != null){
                if(status.equals(LabSwitchStatusEnum.开始切换.getCode())){
                    String temp = "正在切换中，不需要重新发起";
                    returnMap.put("message",temp);
                    logger.info(temp);
                    return returnMap;
                }else if(status.equals(LabSwitchStatusEnum.切换完成.getCode())){
                    String temp = "已经完成切换，不需要重新发起!";
                    returnMap.put("message",temp);
                    logger.info(temp);
                    return returnMap;
                }
            }
            //同一时刻只能发起一个机房切换校验
            else{
                Iterator<Map.Entry<String,Integer>> iteratorTemp = versionMap.entrySet().iterator();
                while (iteratorTemp.hasNext()){
                    Map.Entry<String,Integer> entry = iteratorTemp.next();
                    Integer statusTemp = entry.getValue();
                    if(statusTemp.equals(LabSwitchStatusEnum.开始切换.getCode()) || statusTemp.equals(LabSwitchStatusEnum.切换失败.getCode())){
                        response.setStatus(500);
                        returnMap.put("message","单库切机房失败，同一时刻只能发起一个机房切换");
                        logger.info("单库切机房失败，同一时刻只能发起一个机房切换");
                        return returnMap;
                    }
                }
            }
        }

        //需要切机房的数据源列表

        List<Long> needSwitchIdList = new ArrayList<Long>();

        /**
         * 创建机房切换中节点,该节点存在就不能启动数据源关联的job（定时+非定时）
         */
        String processingPath = DLinkZkPathDef.labSwitchProcessing;
        boolean exists  = DLinkZkUtils.get().zkClient().exists(processingPath);
        if(!exists){

            List<MediaSourceInfo> havaSameList = needSwitchList.stream().filter(info -> StringUtils.equals(doubleCenterService.getCenterLab(info.getId()),targetCenter)).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(havaSameList)){
                response.setStatus(500);
                String temp = "单库切机房失败，有数据源已经在目标机房，请检查为什么还要发起切换";
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }

            needSwitchIdList = needSwitchList.stream().map(info -> info.getId()).collect(Collectors.toList());
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
                DLinkZkUtils.get().zkClient().createPersistent(processingPath, byteData, true);
            } catch (ZkNodeExistsException e) {
                logger.info("机房切换中节点已经存在，DoNothing");
                //do nothing
            } catch (Exception e) {
                throw new DatalinkException("在zk上创建机房切换中节点出错", e);
            }
        }else {
            byte[] processingData  = DLinkZkUtils.get().zkClient().readData(processingPath,true);
            Map map = JSON.parseObject(processingData, Map.class);
            String virtualMsIdList = (String) map.get("virtualMsIdList");
            String[] ids = virtualMsIdList.split(",");
            List<MediaSourceInfo> needSwitchListTemp = new ArrayList<MediaSourceInfo>();
            for(String id : ids){
                MediaSourceInfo info = mediaSourceService.getById(Long.valueOf(id));
                needSwitchListTemp.add(info);
            }
            needSwitchIdList = needSwitchListTemp.stream().map(info -> info.getId()).collect(Collectors.toList());
        }

        //入库
        updateDb(model,businessLineSet,needSwitchIdList,json);

        /**
         * 触发异步切机房，且完成后通知dbms
         */
        new Thread(new DbSwitchLabThread(batchId,targetCenter,model.getSwitchStartTimeDate())).start();

        /**
         * 保存本次状态到zk
         */
        updateVersionToZk(batchId, LabSwitchStatusEnum.开始切换.getCode());

        logger.info("收到DBMS单库切机房请求成功");
        returnMap.put("message","收到DBMS单库切机房请求成功");
        return returnMap;
    }

    /**
     * 单库切机房接口，给DBMS使用
     *
     * @param json
     * @return
     */
    @LoginIgnore
    @RequestMapping(value = "/sddlSwitchLab_4_dbms",method = RequestMethod.POST)
    @ResponseBody
    public Object sddlSwitchLab(@RequestBody String json,HttpServletRequest request, HttpServletResponse response) {

        logger.info("收到DBMS sddl切机房请求， 收到的数据是: {} {}",System.getProperty("line.separator"), json);

        //返回值
        Map returnMap = new HashMap();

        SysPropertiesInfo sysPropertiesInfo = sysPropertiesService.getSysPropertiesByKey("sddlSwitchLabKey");
        if(sysPropertiesInfo == null || (!StringUtils.equals(sysPropertiesInfo.getPropertiesValue(),"true"))){
            response.setStatus(500);
            String message = "sddl数据源的切机房开关未打开";
            returnMap.put("message",message);
            logger.info(message);
            return returnMap;
        }

        //参数数据
        DbSwitchLabModel model = JSON.parseObject(json,DbSwitchLabModel.class);

        /**
         * 简单校验
         */
        Map resultMap = checkSwitchLab(model, request);
        Boolean flag = (Boolean)resultMap.get("flag");

        //校验不通过
        if(!flag){
            response.setStatus(500);
            returnMap.put("message",resultMap.get("message"));
            logger.info((String) resultMap.get("message"));
            return returnMap;
        }

        //经常使用的字段
        String batchId = model.getBatchId();
        String targetCenter = IdLabEnum.getEnumByCode(model.getTargetCenter()).getName();

        MediaSourceInfo mediaSourceInfo = mediaSourceService.getOneByName(model.getGroupName());
        if(mediaSourceInfo == null){
            response.setStatus(500);
            String temp = "单库切机房失败，SDDL数据源未找到，请提醒datalink检查";
            returnMap.put("message",temp);
            logger.info(temp);
            return  returnMap;
        }

        byte[] datas = DLinkZkUtils.get().zkClient().readData(DLinkZkPathDef.switchLabVersion, true);
        if(datas != null){
            Map<String,Integer> versionMap = JSONObject.parseObject(datas,Map.class);
            Integer status = versionMap.get(batchId);

            //幂等校验
            if(status != null){
                if(status.equals(LabSwitchStatusEnum.开始切换.getCode()) || status.equals(LabSwitchStatusEnum.切换完成.getCode())){
                    returnMap.put("message","收到DBMS单库切机房请求成功");
                    logger.info("收到DBMS单库切机房请求成功");
                    return returnMap;
                }
            }
            //同一时刻只能发起一个机房切换校验
            else{
                Iterator<Map.Entry<String,Integer>> iterator = versionMap.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String,Integer> entry = iterator.next();
                    Integer statusTemp = entry.getValue();
                    if(statusTemp.equals(LabSwitchStatusEnum.开始切换.getCode()) || statusTemp.equals(LabSwitchStatusEnum.切换失败.getCode())){
                        response.setStatus(500);
                        returnMap.put("message","同一时刻只能发起一个机房切换");
                        logger.info("同一时刻只能发起一个机房切换");
                        return returnMap;
                    }
                }
            }
        }

        //需要切机房的数据源列表
        List<MediaSourceInfo> needSwitchList = new ArrayList<MediaSourceInfo>();
        List<Long> needSwitchIdList = new ArrayList<Long>();

        //业务线
        Set<String> businessLineSet = new HashSet<String>();
        businessLineSet.add(model.getProductName());

        /**
         * 创建机房切换中节点,该节点存在就不能启动数据源关联的job（定时+非定时）
         */
        String processingPath = DLinkZkPathDef.labSwitchProcessing;
        boolean exists  = DLinkZkUtils.get().zkClient().exists(processingPath);
        //第一次切,初始化
        if(!exists){

            needSwitchList.add(mediaSourceInfo);
            /**
             * 准备需要切机房的数据源
             */
            //含有sddl虚拟数据源，需特殊处理
            List<MediaSourceInfo> tempList = new ArrayList<MediaSourceInfo>();
            for(MediaSourceInfo info : needSwitchList){
                MediaSourceInfo realMediaSource = mediaService.getRealDataSource(info);
                SddlMediaSrcParameter mediaSrcParameter = (SddlMediaSrcParameter)realMediaSource.getParameterObj();
                List<Long> primaryDbList = mediaSrcParameter.getPrimaryDbsId();
                List<Long> secondaryDbList = mediaSrcParameter.getSecondaryDbsId();
                for(Long id : primaryDbList){
                    MediaSourceRelationInfo relationInfo = mediaSourceRelationService.getOneByRealMsId(id);
                    MediaSourceInfo mediaSourceInfoTemp = mediaSourceService.getById(relationInfo.getVirtualMsId());
                    tempList.add(mediaSourceInfoTemp);
                }
                for(Long id : secondaryDbList){
                    MediaSourceRelationInfo relationInfo = mediaSourceRelationService.getOneByRealMsId(id);
                    MediaSourceInfo mediaSourceInfoTemp = mediaSourceService.getById(relationInfo.getVirtualMsId());
                    tempList.add(mediaSourceInfoTemp);
                }
            }
            //sddl下挂的真实库对应的虚拟数据源，也需要切
            needSwitchList.addAll(tempList);

            //校验
            List<MediaSourceInfo> havaSameList = needSwitchList.stream().filter(info -> StringUtils.equals(doubleCenterService.getCenterLab(info.getId()),targetCenter)).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(havaSameList)){
                response.setStatus(500);
                String temp = "有数据源已经在目标机房，请dbms检查为什么还要发起切换";
                returnMap.put("message",temp);
                logger.info(temp);
                return  returnMap;
            }

            needSwitchIdList = needSwitchList.stream().map(info -> info.getId()).collect(Collectors.toList());
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
                DLinkZkUtils.get().zkClient().createPersistent(processingPath, byteData, true);
            } catch (ZkNodeExistsException e) {
                logger.info("机房切换中节点已经存在，DoNothing");
                //do nothing
            } catch (Exception e) {
                throw new DatalinkException("在zk上创建机房切换中节点出错", e);
            }
        }
        //第一次切失败后重试
        else {
            byte[] processingData = DLinkZkUtils.get().zkClient().readData(processingPath, true);
            Map map = JSON.parseObject(processingData, Map.class);
            String virtualMsIdList = (String) map.get("virtualMsIdList");
            String[] ids = virtualMsIdList.split(",");
            for(String id : ids){
                MediaSourceInfo info = mediaSourceService.getById(Long.valueOf(id));
                needSwitchList.add(info);
            }
            needSwitchIdList = needSwitchList.stream().map(info -> info.getId()).collect(Collectors.toList());
        }

        logger.info("datalink切机房准备工作: 创建机房切换中临时节点成功");

        //入库
        updateDb(model,businessLineSet,needSwitchIdList,json);

        /**
         * 触发异步切机房，且完成后通知dbms
         */
        new Thread(new DbSwitchLabThread(batchId,targetCenter,model.getSwitchStartTimeDate())).start();

        /**
         * 保存本次状态到zk
         */
        updateVersionToZk(batchId, LabSwitchStatusEnum.开始切换.getCode());

        logger.info("收到DBMS单库切机房请求成功");
        returnMap.put("message","收到DBMS单库切机房请求成功");
        return returnMap;
    }

    /**
     * 切机房申请入库
     *
     * @param model
     * @param businessLineSet
     * @param needSwitchIdList
     * @param json
     */
    public void updateDb(DbSwitchLabModel model, Set businessLineSet, List<Long> needSwitchIdList, String json){
        /**
         * 入库
         */
        LabSwitchInfo temp = doubleCenterService.getLabSwitchByVersion(model.getBatchId());
        if(temp == null){
            LabSwitchInfo labSwitchInfo = new LabSwitchInfo();
            labSwitchInfo.setVersion(model.getBatchId());
            labSwitchInfo.setBusinessLine(Joiner.on(",").join(businessLineSet));
            labSwitchInfo.setStatus(LabSwitchStatusEnum.开始切换.getCode());
            labSwitchInfo.setSwitchProgress(0);
            labSwitchInfo.setFromCenter(IdLabEnum.getEnumByCode(model.getCurrentCenter()).getName());
            labSwitchInfo.setTargetCenter(IdLabEnum.getEnumByCode(model.getTargetCenter()).getName());
            Date d = new Date();
            labSwitchInfo.setStartTime(d);
            labSwitchInfo.setEndTime(d);
            labSwitchInfo.setVirtualMsIds(Joiner.on(",").join(needSwitchIdList));
            labSwitchInfo.setOriginalContent(json);
            labSwitchInfo.setSwitchStartTime(model.getSwitchStartTimeDate());
            doubleCenterService.insertLabSwitchInfo(labSwitchInfo);
        }
        //第一次切换失败后，重试
        else{
            temp.setStatus(LabSwitchStatusEnum.开始切换.getCode());
            doubleCenterService.updateLabSwitchInfo(temp);
        }
    }

    /**
     * 保存本次切机房结果到zk
     */
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

    /**
     * 单库切换，手动通知dbms结果(通知失败时才使用)
     *
     * @param batchId
     * @return
     */
    @RequestMapping(value = "/reNotifyDbms")
    @ResponseBody
    public Object reNotifyDbms(@RequestParam("batchId") String batchId) {
        logger.info("单库切换，手动通知dbms结果-开始，参数是{}", batchId);
        try{
            if(StringUtils.isBlank(batchId)){
                return "批次号不能为空";
            }
            LabSwitchInfo labSwitchInfo = doubleCenterService.getLabSwitchByVersion(batchId);
            if(labSwitchInfo == null){
                return "该批次不存在";
            }
            if(!labSwitchInfo.getStatus().equals(LabSwitchStatusEnum.切换完成.getCode())){
                return "最终结果不是成功，不能通知";
            }
            SwitchLabService switchLabService = new SwitchLabService();
            switchLabService.notifyDbmsDbSwitchResult(labSwitchInfo.getVersion(), DbCallBackCodeEnum.DATALINK_OPS_SUCCESS);
        } catch (Exception e) {
            logger.error("单库切换，手动通知dbms结果失败，失败原因: {}", e);
            return "操作失败:" + e.getMessage();
        }
        logger.info("单库切换，手动通知dbms结果-结束");
        Map map = new HashMap();
        map.put("result","success");
        return map;
    }

    /**
     * datalink本身切机房接口，此接口只能自己使用
     *
     * 此接口调用的唯一场景是：datalink自身数据库无法访问
     *
     * @param json
     * @return
     */
    @LoginIgnore
    @RequestMapping(value = "/datalinkSwitchLab")
    @ResponseBody
    public Object datalinkSwitchLab(@RequestBody String json,HttpServletRequest request) {

        logger.info("datalink自身数据库切机房发起请求， 收到的数据是: {} {}",System.getProperty("line.separator"), json);

        Map returnMap = new HashMap(2);

        //参数数据
        DbSwitchLabModel model = JSONObject.parseObject(json,DbSwitchLabModel.class);

        /**
         * 简单校验
         */
        Map resultMap = checkSwitchLab(model, request);
        Boolean flag = (Boolean)resultMap.get("flag");
        //校验不通过
        if(!flag){
            returnMap.put("message",resultMap.get("message"));
            logger.info((String) resultMap.get("message"));
            return returnMap;
        }

        byte[] datas = DLinkZkUtils.get().zkClient().readData(DLinkZkPathDef.switchLabVersion, true);
        if(datas != null){
            Map<String,Integer> versionMap = JSONObject.parseObject(datas,Map.class);
            Integer status = versionMap.get(model.getBatchId());

            //幂等校验
            if(status != null){
                if(status.equals(LabSwitchStatusEnum.开始切换.getCode())){
                    String temp = "正在切换中，不需要重新发起";
                    returnMap.put("message",temp);
                    logger.info(temp);
                    return returnMap;
                }else if(status.equals(LabSwitchStatusEnum.切换完成.getCode())){
                    String temp = "已经完成切换，不要重新发起!";
                    returnMap.put("message",temp);
                    logger.info(temp);
                    return returnMap;
                }
            }
        }

        //经常使用的字段
        String batchId = model.getBatchId();
        String fromCenter = IdLabEnum.getEnumByCode(model.getCurrentCenter()).getName();
        String targetCenter = IdLabEnum.getEnumByCode(model.getTargetCenter()).getName();

        //校验
        if(StringUtils.equals(doubleCenterService.getCenterLab(Constants.DB_DATALINK),targetCenter)){
            String temp = "有数据源已经在目标机房，请dbms检查为什么还要发起切换";
            returnMap.put("message",temp);
            logger.info(temp);
            return returnMap;
        }

        /**
         * 触发异步切机房，且完成后通知dbms
         */
        new Thread(new DatalinkSwitchLabThread(batchId,fromCenter,targetCenter,model.getSwitchStartTimeDate(),json)).start();

        /**
         * 保存本次状态到zk
         */
        updateVersionToZk(batchId, LabSwitchStatusEnum.开始切换.getCode());

        String temp = "datalink自身数据库切机房发起请求结束，异步线程会切机房";
        logger.info(temp);
        returnMap.put("message",temp);
        return returnMap;

    }


    /**
     * 此接口仅仅是测试用
     *      整体切机房之前，用来初始化测试数据
     *
     *
     * @param json
     * @return
     */
    @LoginIgnore
    @RequestMapping(value = "/initWholeSwitchLabData")
    @ResponseBody
    public Object initWholeSwitchLabData(@RequestBody String json) {

        logger.info("初始化数据开始");

        Map sourceMap = JSON.parseObject(json,Map.class);
        String targetCenter = (String) sourceMap.get("targetCenter");

        /**
         * 准备需要切机房的数据源
         */
        //所有的虚拟数据源
        Set<MediaSourceType> setMediaSource = new HashSet<>();
        setMediaSource.add(MediaSourceType.VIRTUAL);
        List<MediaSourceInfo> allVirtualMediaSourceList = mediaSourceService.getListByType(setMediaSource);


        //需要切机房的数据源，去掉自己的当前机房就是目标机房的数据源
        List<MediaSourceInfo> needSwitchList = allVirtualMediaSourceList.stream().filter(info -> !StringUtils.equals(doubleCenterService.getCenterLab(info.getId()),targetCenter)).collect(Collectors.toList());
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
            DLinkZkUtils.get().zkClient().createPersistent(DLinkZkPathDef.labSwitchProcessing, byteData, true);
        } catch (ZkNodeExistsException e) {
            logger.info("机房切换中节点已经存在，DoNothing");
            //do nothing
        } catch (Exception e) {
            throw new DatalinkException("在zk上创建机房切换中节点出错", e);
        }


        Map returnMap = new HashMap();
        String temp = "初始化数据结束";
        logger.info(temp);
        returnMap.put("message",temp);
        return returnMap;

    }



    /**
     * 通知dbms切机房结果
     */
    /*public void notifyDbmsDbSwitchResult(DbCallBackCodeEnum dbCallBackCodeEnum,String batchId){
        *//**
         * 回调dbms，会有安全校验
         *//*
        int time = 0;
        boolean flag = true;

        *//**
         * 通知dbms
         *//*
        Map<String,Object> temp = new HashMap<String,Object>();
        temp.put("batchId",batchId);
        temp.put("statusCode", dbCallBackCodeEnum.getCode());
        temp.put("status",dbCallBackCodeEnum.getName());
        temp.put("dbswitch_token",System.currentTimeMillis());

        do{
            try{
                logger.info("datalink通知dbms结果，请求的参数是：{}",JSON.toJSONString(temp));
                String url = ManagerConfig.current().getNotifyDbmsDbSwitchResultUrl();
                String json = HttpUtils.doPost(url, JSON.toJSONString(temp));
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
    }*/



}
