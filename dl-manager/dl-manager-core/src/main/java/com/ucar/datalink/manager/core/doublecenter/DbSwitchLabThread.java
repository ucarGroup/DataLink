package com.ucar.datalink.manager.core.doublecenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.DoubleCenterDataxService;
import com.ucar.datalink.biz.service.DoubleCenterService;
import com.ucar.datalink.biz.service.GroupService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.utils.HttpUtils;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.doublecenter.DbCallBackCodeEnum;
import com.ucar.datalink.domain.doublecenter.LabSwitchStatusEnum;
import com.ucar.datalink.domain.task.TargetState;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class DbSwitchLabThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DbSwitchLabThread.class);
    private DoubleCenterService doubleCenterService;
    private GroupService groupService;
    private DoubleCenterDataxService doubleCenterDataxService;
    private SwitchLabService switchLabService;
    DLinkZkUtils zkUtils;

    private String batchId;
    private String targetCenter;
    private Date switchStartTimeDate;

    public DbSwitchLabThread(String batchId, String targetCenter, Date switchStartTimeDate){
        doubleCenterService = DataLinkFactory.getObject(DoubleCenterService.class);
        groupService = DataLinkFactory.getObject(GroupService.class);
        doubleCenterDataxService = DataLinkFactory.getObject(DoubleCenterDataxService.class);
        switchLabService = new SwitchLabService();
        zkUtils = DLinkZkUtils.get();
        this.batchId = batchId;
        this.targetCenter = targetCenter;
        this.switchStartTimeDate = switchStartTimeDate;
    }

    @Override
    public void run() {

        try{

            long currentTime = System.currentTimeMillis();
            logger.info("datalink单库切机房开始，当前时间是：{}",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));


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

            logger.info("datalink切机房进行中, 需要切机房的数据源是：{}", JSON.toJSONString(needSwitchIdList));

            /**
             * kill -9 和DB关联的datax job（定时+非定时）
             */
            doubleCenterDataxService.stopSpecifiedRunningJob(needSwitchIdList);
            logger.info("datalink切机房进行中, kill -9 datax job成功");

            /**
             * 暂停增量任务
             *      暂停目标端为DB的增量任务
             */
            doubleCenterService.stopOrStartIncrementTask(needSwitchIdList, TargetState.PAUSED);
            logger.info("datalink切机房进行中, 暂停增量任务成功");

            /**
             * 校验binlog流量
             *      是否已经完全从A同步到了B
             */
            switchLabService.checkBinlog(acrossLabTaskIdList);
            logger.info("datalink切机房进行中, 校验binlog流量成功");

            /**
             * 一键停止跨机房同步
             */
            doubleCenterService.oneKeyStopSync(acrossLabTaskIdList);
            logger.info("datalink切机房进行中, 一键停止跨机房同步成功");

            /**
             * 切数据源
             */
            doubleCenterService.changeDataSource(needSwitchIdList, targetCenter);
            logger.info("datalink切机房进行中， 数据源切换成功");

            /**
             * 睡眠10秒,因数据源有5s的缓存时间
             */
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                logger.error("等待释放中心机房缓存失败,原因:{}" ,e);
            }

            /**
             * reblance
             */
            switchLabService.reblance(needSwitchIdList);
            logger.info("datalink切机房进行中, reblance成功");


            /**
             * 更改datax定时任务的ip和端口
             */
            doubleCenterDataxService.switchDataSourceForSpecifiedTimingJob(needSwitchIdList);
            logger.info("datalink切机房进行中， 更改datax定时任务ip成功");

            /**
             * 一键反向同步
             */
            doubleCenterService.oneKeyReverseSync(acrossLabTaskIdList, switchStartTimeDate);
            logger.info("datalink切机房进行中, 一键反向同步成功");

            /**
             * 启动增量任务
             *      启动目标端为DB的增量任务
             */
            doubleCenterService.stopOrStartIncrementTask(needSwitchIdList, TargetState.STARTED);
            logger.info("datalink切机房进行中, 启动增量任务成功");

            //更新数据库中状态
            doubleCenterService.updateDb(batchId,LabSwitchStatusEnum.切换完成.getCode(),100);
            logger.info("datalink切机房进行中，更新数据库的状态为切换完成");

            /**
             * 删除机房切换中节点
             */
            zkUtils.zkClient().delete(DLinkZkPathDef.labSwitchProcessing);
            logger.info("datalink切机房进行中，删除机房切换中节点成功");

            doubleCenterService.updateVersionToZk(batchId,LabSwitchStatusEnum.切换完成.getCode());
            logger.info("datalink切机房进行中，更新机房切换状态为切换完成");

            logger.info("datalink单库切机房结束且成功，当前时间：{},共花费了：{}秒。",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),(System.currentTimeMillis() - currentTime)/1000);
            logger.info("换行");//加一个空行

            //最后通知dbms切机房结果
            switchLabService.notifyDbmsDbSwitchResult(batchId, DbCallBackCodeEnum.DATALINK_OPS_SUCCESS);

        }catch (Exception ex){

            logger.info("datalink单库切机房失败，失败原因：{}", ex);

            //更新数据库中状态
            doubleCenterService.updateDb(batchId,LabSwitchStatusEnum.切换失败.getCode(),null);

            //在zk上记录失败
            doubleCenterService.updateVersionToZk(batchId, LabSwitchStatusEnum.切换失败.getCode());

            //通知dbms切机房结果
            switchLabService.notifyDbmsDbSwitchResult(batchId, DbCallBackCodeEnum.DATALINK_OPS_FAILURE);

        }
    }


}
