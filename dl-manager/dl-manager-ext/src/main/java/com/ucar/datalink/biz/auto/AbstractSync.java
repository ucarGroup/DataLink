package com.ucar.datalink.biz.auto;

import com.ucar.datalink.biz.auto.envfilter.IEnvStrategy;
import com.ucar.datalink.biz.auto.envfilter.StrategyFactory;
import com.ucar.datalink.biz.dal.SyncApplyDAO;
import com.ucar.datalink.biz.service.JobService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.domain.sync.*;
import com.ucar.datalink.util.SyncUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by yang.wang09 on 2018-04-24 10:43.
 */
public abstract class AbstractSync implements SyncExecutor {

    private static Logger logger = LoggerFactory.getLogger(AbstractSync.class);

    @Autowired
    private MediaSourceService mediaSourceService;

    @Autowired
    private JobService jobService;

    @Autowired
    private SyncApplyDAO syncApplyDAO;

    private IEnvStrategy strategy = StrategyFactory.getStrategy();



    public AbstractSync() {

    }

    /**
     * 是否包含全量任务
     */
    public boolean hasFull;

    /**
     * 是否包含增量任务，如果 hasFull=true，hasIncrement=true
     * 表示 全量+增量
     */
    public boolean hasIncrement;

    /**
     * 是否先执行全量
     */
    public boolean isFullFirst;



    public boolean canAutoProcess(SyncApplyInfo info) {
        boolean canAuto = true;
        if( isFullFirst ) {
            canAuto = strategy.preInitFull(info);
        }
        else {
            canAuto = strategy.preInitIncrement(info);
        }
        return canAuto;
    }


    @Override
    public void initFull(SyncApplyInfo info) {

    }

    @Override
    public void processFull(SyncApplyInfo info) {

    }

    @Override
    public void initIncrement(SyncApplyInfo info) {

    }

    @Override
    public void processIncrement(SyncApplyInfo info) {

    }


    // ---  预处理操作 full
    public boolean preInitFull(SyncApplyInfo info) {
        //通过策略模式，引入不同的环境策略，
        //根据策略类工厂，根据不同的 环境，创建不同的策略类
        //再返回不同的结果
        return strategy.preInitFull(info);
    }

    public boolean postInitFull(SyncApplyInfo info) {
        return strategy.postInitFull(info);
    }

    public boolean preProcessFull(SyncApplyInfo info) {
        return strategy.preProcessFull(info);
    }

    public boolean postProcessFull(SyncApplyInfo info) {
        return true;
    }


    // -- 预处理操作 increment
    public boolean preInitIncrement(SyncApplyInfo info) {
        //引入策略模式，
        return true;
    }

    public boolean postInitIncrement(SyncApplyInfo info) {
        return true;
    }

    public boolean preProcessIncrement(SyncApplyInfo info) {
        return true;
    }

    public boolean postProcessIncrement(SyncApplyInfo info) {
        return true;
    }



    public boolean hasNext() {
        return hasFull && hasIncrement;
    }


    /**
     * 检查全量，增量是否执行完成，如果全量执行完了，会发送一个建表信息到CDSE
     * @param applyInfo
     * @return
     */
    @Override
    public boolean checkFinish(SyncApplyInfo applyInfo) {
        boolean isFinish = false;
        if(hasFull && !hasIncrement) {
            //如果只有全量，如果全量执行完了，发送建表信息到CDSE
            isFinish = checkFullFinish(applyInfo);
            if( isFinish ) {
                //sendCreateJobInfoToCDSE(applyInfo);
            }
        }
        else if(!hasFull && hasIncrement) {
            //如果只有增量
            isFinish = checkIncrementFinish(applyInfo);
        }
        else if(hasFull && hasIncrement) {
            //如果全量增量都有
            if( isFullFirst ) {
                isFinish = checkFullFinish(applyInfo);
                if( isFinish ) {
                    //sendCreateJobInfoToCDSE(applyInfo);
                    isFinish = checkIncrementFinish(applyInfo);
                }
            } else {
                isFinish = checkIncrementFinish(applyInfo);
                if( isFinish ) {
                    isFinish = checkFullFinish(applyInfo);
                    if( isFinish ) {
                        //sendCreateJobInfoToCDSE(applyInfo);
                    }
                }
            }
        }
        return isFinish;
    }


    private boolean checkFullFinish(SyncApplyInfo applyInfo) {
        boolean isFinish = false;
        SyncUtil.FullFinishCheck check = SyncUtil.checkFullResult(applyInfo);
        if (check.totalJob > 0 && check.totalJob == check.succeedJobIds.size()) {
            applyInfo.setApplyStatus(SyncApplyStatus.SUCCEEDED);
            applyInfo.setReplyRemark("全部job自动执行成功！applyId = " + applyInfo.getId() + ".jobIds：" + check.succeedJobIds.toString());
            syncApplyDAO.updateApplyStatus(applyInfo);
            //sendEmail(applyInfo);
            logger.info("All jobs auto execution succeed. applyId = " + applyInfo.getId() + ".jobIds:" + check.succeedJobIds.toString());
            isFinish = true;
        } else if (check.failedJobIds.size()>0 && check.totalJob==(check.succeedJobIds.size()+check.failedJobIds.size()) ) {
            applyInfo.setApplyStatus(SyncApplyStatus.FAILED);
            applyInfo.setReplyRemark("同步申请自动执行失败！applyId = " + applyInfo.getId() +
                    "<br>" + "<br>" + "失败的jobIds：" + check.failedJobIds.toString() + "<br>" + "<br>" + "成功的jobIds：" + check.succeedJobIds.toString());
            applyInfo.setNeedNotify(true);
            syncApplyDAO.updateApplyStatus(applyInfo);
            //sendEmail(applyInfo);
            isFinish = true;
        } else {
            isFinish = false;
        }
        return isFinish;
    }

    private boolean checkIncrementFinish(SyncApplyInfo applyInfo) {
        return true;
    }


    private boolean sendCreateJobInfoToCDSE(SyncApplyInfo applyInfo) {
        boolean isSuccess = true;
//        long id = applyInfo.getId();
//        List<JobConfigInfo> list = jobService.getJobConfigListByApplyId(id);
//        if(list==null || list.size() ==0) {
//            return true;
//        }
//        long targetMediaSourceId = list.get(0).getJob_target_media_source_id();
//        MediaSourceInfo mediaSourceInfo = mediaSourceService.getById(targetMediaSourceId);
//        if(mediaSourceInfo.getType() != MediaSourceType.HDFS) {
//            return true;
//        }
//        for(JobConfigInfo jci : list) {
//            try {
//                SyncUtil.sendCreateJobInfoToCDSE(applyInfo,jci);
//            } catch (Exception e) {
//                logger.error(e.getMessage(),e);
//                isSuccess = false;
//            }
//        }
        return isSuccess;
    }




    public boolean isSuccess(SyncApplyInfo info) {
        boolean isFinish = false;
        if(hasFull && !hasIncrement) {
            //如果只有全量
            SyncUtil.FullFinishCheck check = SyncUtil.checkFullResult(info);
            if (check.totalJob > 0 && check.totalJob == check.succeedJobIds.size()) {
                info.setNeedNotify(true);
                syncApplyDAO.updateApplyStatus(info);
                isFinish = true;
            }
        }
        else if(!hasFull && hasIncrement) {
            //如果只有增量
            isFinish = true;
        }
        else if(hasFull && hasIncrement) {
            //如果全量增量都有
            if( isFullFirst ) {
                SyncUtil.FullFinishCheck check = SyncUtil.checkFullResult(info);
                if (check.totalJob > 0 && check.totalJob == check.succeedJobIds.size()) {
                    isFinish = true;
                }
                isFinish = true;
            } else {
                isFinish = true;
                SyncUtil.FullFinishCheck check = SyncUtil.checkFullResult(info);
                if (check.totalJob > 0 && check.totalJob == check.succeedJobIds.size()) {
                    isFinish = true;
                }
            }
        }
        return isFinish;
    }



    public void syncFull(SyncApplyInfo info) {
        if( preInitFull(info) ) {
            initFull(info);
            postInitFull(info);
            if( preProcessFull(info) ) {
                processFull(info);
                postProcessFull(info);
            }
        }
    }

    public void syncIncrement(SyncApplyInfo info) {
        if( preInitIncrement(info) ) {
            initIncrement(info);
            postInitIncrement(info);

            if( preProcessIncrement(info) ) {
                processIncrement(info);
                postProcessIncrement(info);
            }
        }
    }


    @Override
    public void first(SyncApplyInfo info) {
        //如果只配置了全量，或者只配置了增量
        if( (hasFull && !hasIncrement) || (hasIncrement && !hasFull) ) {
            if( hasFull ) {
                syncFull(info);
            } else {
                syncIncrement(info);
            }
        }
        else {
            //又有增量，又有全量
            if( isFullFirst ) {
                syncFull(info);
            } else {
                syncIncrement(info);
            }
        }
    }


    //能执行的 second()的，肯定是 全量+增量的
    @Override
    public void second(SyncApplyInfo info) {
        if(!hasNext()) {
            return;
        }
        if( isFullFirst ) {
            syncIncrement(info);
        } else {
            syncFull(info);
        }
    }


    public void isFullFinish() {

    }

    public void isIncrementFinish() {

    }





//    public abstract boolean isFullFirst(SyncApplyInfo info);


    public void increment() {

    }

    public void incrementWithTimeTask() {

    }





    public boolean isHasFull() {
        return hasFull;
    }

    public void setHasFull(boolean hasFull) {
        this.hasFull = hasFull;
    }

    public boolean isHasIncrement() {
        return hasIncrement;
    }

    public void setHasIncrement(boolean hasIncrement) {
        this.hasIncrement = hasIncrement;
    }

    public boolean isFullFirst() {
        return isFullFirst;
    }

    public void setFullFirst(boolean isFullFirst) {
        this.isFullFirst = isFullFirst;
    }
}
