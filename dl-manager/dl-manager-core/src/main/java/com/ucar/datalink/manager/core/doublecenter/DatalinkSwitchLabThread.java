package com.ucar.datalink.manager.core.doublecenter;

import com.ucar.datalink.biz.service.DoubleCenterService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.service.TaskConfigService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.doublecenter.LabSwitchInfo;
import com.ucar.datalink.domain.doublecenter.LabSwitchStatusEnum;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.virtual.VirtualMediaSrcParameter;
import com.ucar.datalink.domain.task.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DatalinkSwitchLabThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DbSwitchLabThread.class);
    private DoubleCenterService doubleCenterService;
    DLinkZkUtils zkUtils;

    private String fromCenter;
    private String targetCenter;
    private String batchId;
    private Date switchStartTimeDate;
    private String json;

    public DatalinkSwitchLabThread(String batchId, String fromCenter, String targetCenter , Date switchStartTimeDate,String json){
        doubleCenterService = DataLinkFactory.getObject(DoubleCenterService.class);
        zkUtils = DLinkZkUtils.get();
        this.batchId = batchId;
        this.fromCenter = fromCenter;
        this.targetCenter = targetCenter;
        this.switchStartTimeDate = switchStartTimeDate;
        this.json = json;
    }

    @Override
    public void run() {

        try {

            long currentTime = System.currentTimeMillis();
            logger.info("datalink自身切机房开始，当前时间是：{}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            /**
             * 切数据源
             *      datalink自身切换时，虚拟出一个mediaSourceId为-2，-2就代表datalink
             */
            List<Long> needSwitchIdList = new ArrayList<Long>();
            needSwitchIdList.add(Long.valueOf(Constants.DB_DATALINK));
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

            //查找dataLink的跨机房任务
            MediaSourceInfo mediaSourceInfo = DataLinkFactory.getObject(MediaSourceService.class).getOneByName(Constants.UCAR_DATALINK);
            List<Long> realIdList = ((VirtualMediaSrcParameter)mediaSourceInfo.getParameterObj()).getRealDbsId();
            List<TaskInfo> taskInfoList = DataLinkFactory.getObject(TaskConfigService.class).findAcrossLabTaskListByMsList(realIdList);
            List<Long> acrossLabTaskIdList = taskInfoList.stream().map(info -> info.getId()).collect(Collectors.toList());

            /**
             * 一键停止跨机房同步
             */
            doubleCenterService.oneKeyStopSync(acrossLabTaskIdList);
            logger.info("datalink切机房进行中, 一键停止跨机房同步成功");

            /**
             * 睡眠5秒,给停止预留时间
             */
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                logger.error("等待释放中心机房缓存失败,原因:{}" ,e);
            }

            /**
             * 一键反向同步
             */
            doubleCenterService.oneKeyReverseSync(acrossLabTaskIdList, switchStartTimeDate);
            logger.info("datalink切机房进行中, 一键反向同步成功");

            //更新数据库中状态
            updateDb();
            logger.info("datalink切机房进行中，更新数据库的状态为切换完成");

            doubleCenterService.updateVersionToZk(batchId,LabSwitchStatusEnum.切换完成.getCode());
            logger.info("datalink切机房进行中，更新zk切换状态为切换完成");

            logger.info("datalink自身切机房结束且成功，当前时间：{},共花费了：{}秒。",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),(System.currentTimeMillis() - currentTime)/1000);
            logger.info("换行");//加一个空行

        }catch (Exception ex){

            //在zk上记录失败
            doubleCenterService.updateVersionToZk(batchId, LabSwitchStatusEnum.切换失败.getCode());

            logger.info("datalink自身切机房失败，失败原因：{}", ex);
        }
    }

    /**
     * 切机房申请入库
     */
    public void updateDb(){
        /**
         * 入库
         */
        LabSwitchInfo temp = doubleCenterService.getLabSwitchByVersion(batchId);
        if(temp == null){
            LabSwitchInfo labSwitchInfo = new LabSwitchInfo();
            labSwitchInfo.setVersion(batchId);
            labSwitchInfo.setBusinessLine("ucar");
            labSwitchInfo.setStatus(LabSwitchStatusEnum.切换完成.getCode());
            labSwitchInfo.setSwitchProgress(100);
            labSwitchInfo.setFromCenter(fromCenter);
            labSwitchInfo.setTargetCenter(targetCenter);
            Date d = new Date();
            labSwitchInfo.setStartTime(d);
            labSwitchInfo.setEndTime(d);
            labSwitchInfo.setVirtualMsIds(Constants.DB_DATALINK);
            labSwitchInfo.setSwitchStartTime(switchStartTimeDate);
            labSwitchInfo.setOriginalContent(json);
            doubleCenterService.insertLabSwitchInfo(labSwitchInfo);
        }
        //第一次切换失败后，重试
        else{
            temp.setStatus(LabSwitchStatusEnum.切换完成.getCode());
            temp.setEndTime(new Date());
            doubleCenterService.updateLabSwitchInfo(temp);
        }
    }

}
