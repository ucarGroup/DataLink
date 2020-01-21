package com.ucar.datalink.manager.core.service.impl;

import com.ucar.datalink.biz.dal.SyncApplyDAO;
import com.ucar.datalink.biz.service.SyncApplyService;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.domain.sync.SyncApplyStatus;
import com.ucar.datalink.domain.sync.SyncApproveInfo;
import com.ucar.datalink.domain.sync.SyncApproveStatus;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.service.AutoKeeperService;
import com.ucar.datalink.manager.core.web.util.IncrementSyncUtil;
import com.ucar.datalink.util.MailUtil;
import com.ucar.datalink.util.SyncUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by yang.wang09 on 2018-04-19 17:05.
 */
public class AutoKeeperServiceImpl implements AutoKeeperService,Job {

    @Autowired
    private SyncApplyService syncApplyService;

    @Autowired
    private SyncApplyDAO syncApplyDAO;

    private static Logger logger = LoggerFactory.getLogger(AutoKeeperServiceImpl.class);


    public AutoKeeperServiceImpl() {
    }

    @Override
    public void autoCheck() {
        logger.info("Auto Check Begin.");
        try {
            //超过一天还未执行完（SUCCEEDED/FAILED），则自动置成ABANDONED
            List<SyncApplyInfo> timeOutUnfinishedList = syncApplyService.getTimeOutUnfinishedList();
            if (timeOutUnfinishedList != null && timeOutUnfinishedList.size() > 0) {
                for (SyncApplyInfo applyInfo : timeOutUnfinishedList) {
                    applyInfo.setApplyStatus(SyncApplyStatus.ABANDONED);
                    syncApplyDAO.updateApplyStatus(applyInfo);
                }
            }

            //遍历申请任务，并执行
            List<SyncApproveInfo> autoApproveList = syncApplyService.getAutoApproveList();
            if (autoApproveList != null && autoApproveList.size() > 0) {
                for (SyncApproveInfo approveInfo : autoApproveList) {
                    SyncApplyInfo applyInfo = syncApplyService.getSyncApplyInfoById(approveInfo.getApplyId());
                    applyInfo.setApplyStatus(SyncApplyStatus.APPROVED);
                    approveInfo.setApproveStatus(SyncApproveStatus.APPROVED);
                    approveInfo.setApproveRemark("Auto Approved.");
                    syncApplyService.doApproveOrReject(applyInfo, approveInfo, true);
                    logger.info("Auto approve succeed. applyId = " + applyInfo.getId());

                    if (applyInfo.getApplyType().equals("Full")) {
                        SyncUtil.createFullJob(applyInfo);
                        applyInfo.setApplyStatus(SyncApplyStatus.FULL_EXECUTING);
                        applyInfo.setNeedNotify(true);
                        syncApplyDAO.updateApplyStatus(applyInfo);
                    } else if (applyInfo.getApplyType().equals("Increment") && !applyInfo.getIsInitialData()) {
                        //auto create media-mapping
                        syncApplyDAO.updateApplyStatus(applyInfo);
                        if (IncrementSyncUtil.processIncrement(applyInfo, null, ManagerConfig.current().getIsReuseTask(),
                                ManagerConfig.current().getZkServer(), ManagerConfig.current().getCurrentEnv())) {
                            applyInfo.setApplyStatus(SyncApplyStatus.SUCCEEDED);
                            applyInfo.setReplyRemark("同步申请增量映射配置成功！applyId = " + applyInfo.getId());
                            applyInfo.setNeedNotify(true);
                            syncApplyDAO.updateApplyStatus(applyInfo);
                        }
                    } else if (applyInfo.getApplyType().equals("Increment") && applyInfo.getIsInitialData()) {
                        //auto create job config
                        SyncUtil.createFullJob(applyInfo);
                        if (SyncUtil.fullFirst(applyInfo)) {//先执行全量
                            applyInfo.setApplyStatus(SyncApplyStatus.FULL_EXECUTING);
                            applyInfo.setNeedNotify(true);
                            syncApplyDAO.updateApplyStatus(applyInfo);
                        } else {//先执行增量，增量执行成功再通知执行全量
                            applyInfo.setApplyStatus(SyncApplyStatus.INCREMENT_EXECUTING);
                            syncApplyDAO.updateApplyStatus(applyInfo);
                            if (IncrementSyncUtil.processIncrement(applyInfo, null,ManagerConfig.current().getIsReuseTask(),
                                    ManagerConfig.current().getZkServer(), ManagerConfig.current().getCurrentEnv())) {
                                applyInfo.setApplyStatus(SyncApplyStatus.FULL_EXECUTING);
                                applyInfo.setReplyRemark("同步申请增量映射配置成功，请执行该申请ID的全量job！applyId = " + applyInfo.getId());
                                applyInfo.setNeedNotify(true);
                                syncApplyDAO.updateApplyStatus(applyInfo);
                            }
                        }
                    }
                }
            }

            //只全量:
            // step1.APPROVED--自动创建job--FULL_EXECUTING--邮件通知执行
            List<SyncApplyInfo> fullApprovedList = syncApplyService.getFullApprovedApplyList();
            if (fullApprovedList != null && fullApprovedList.size() > 0) {
                for (SyncApplyInfo applyInfo : fullApprovedList) {
                    SyncUtil.createFullJob(applyInfo);
                    applyInfo.setApplyStatus(SyncApplyStatus.FULL_EXECUTING);
                    applyInfo.setNeedNotify(true);
                    syncApplyDAO.updateApplyStatus(applyInfo);
                }
            }

            //step2.检测FULL_EXECUTING/FULL_FAILED全量申请的job执行结果，job全部成功的置为SUCCEEDED，失败的继续通知手动执行，直到job全部成功
            List<SyncApplyInfo> fullExecutingOrFailedList = syncApplyService.getFullExecutingOrFailedApplyList();
            if (fullExecutingOrFailedList != null && fullExecutingOrFailedList.size() > 0) {
                for (SyncApplyInfo applyInfo : fullExecutingOrFailedList) {
                    if (SyncUtil.checkFullFinish(applyInfo)) {
                        applyInfo.setApplyStatus(SyncApplyStatus.SUCCEEDED);
                        applyInfo.setReplyRemark("同步申请全量job执行成功！applyId = " + applyInfo.getId());
                        applyInfo.setNeedNotify(true);
                        syncApplyDAO.updateApplyStatus(applyInfo);
                    } else if (applyInfo.getApplyStatus() == SyncApplyStatus.FULL_EXECUTING) {
                        if(!MailUtil.getMailedIfPresent(applyInfo.getId(), SyncApplyStatus.FULL_EXECUTING)) {
                            applyInfo.setNeedNotify(true);
                            syncApplyDAO.updateApplyStatus(applyInfo);
                            MailUtil.getMailed(applyInfo.getId(), SyncApplyStatus.FULL_EXECUTING);
                        }
                    }
                }
            }

            //只增量
            //step1.APPROVED--INCREMENT_EXECUTING--邮件通知执行
            List<SyncApplyInfo> incrementApprovedList = syncApplyService.getIncrementApprovedApplyList();
            if (incrementApprovedList != null && incrementApprovedList.size() > 0) {
                for (SyncApplyInfo applyInfo : incrementApprovedList) {
                    applyInfo.setApplyStatus(SyncApplyStatus.INCREMENT_EXECUTING);
                    applyInfo.setNeedNotify(true);
                    syncApplyDAO.updateApplyStatus(applyInfo);
                }
            }

            //step2.INCREMENT_FINISH的增量申请置为SUCCEED，INCREMENT_FAILED的增量申请则继续通知，手动配置成功之后置为SUCCEED
            List<SyncApplyInfo> incrementFinishOrFailedList = syncApplyService.getIncrementFinishOrFailedList();
            if (incrementFinishOrFailedList != null && incrementFinishOrFailedList.size() > 0) {
                for (SyncApplyInfo applyInfo : incrementFinishOrFailedList) {
                    if (applyInfo.getApplyStatus() == SyncApplyStatus.INCREMENT_FINISH) {
                        applyInfo.setApplyStatus(SyncApplyStatus.SUCCEEDED);
                        applyInfo.setReplyRemark("同步申请增量映射配置成功！applyId = " + applyInfo.getId());
                        applyInfo.setNeedNotify(true);
                        syncApplyDAO.updateApplyStatus(applyInfo);
                    } else if (applyInfo.getApplyStatus() == SyncApplyStatus.INCREMENT_FAILED) {
                        if(!MailUtil.getMailedIfPresent(applyInfo.getId(), SyncApplyStatus.INCREMENT_FAILED)) {
                            applyInfo.setNeedNotify(true);
                            syncApplyDAO.updateApplyStatus(applyInfo);
                            MailUtil.getMailed(applyInfo.getId(), SyncApplyStatus.INCREMENT_FAILED);
                        }
                    }
                }
            }

            //全量+增量
            //step1.APPROVED--自动创建job--FULL_EXECUTING/INCREMENT_EXECUTING--邮件通知执行
            List<SyncApplyInfo> fullAndIncrementApprovedList = syncApplyService.getFullAndIncrementApprovedApplyList();
            if (fullAndIncrementApprovedList != null && fullAndIncrementApprovedList.size() > 0) {
                for (SyncApplyInfo applyInfo : fullAndIncrementApprovedList) {
                    SyncUtil.createFullJob(applyInfo);
                    if (SyncUtil.fullFirst(applyInfo)) {//先执行全量
                        applyInfo.setApplyStatus(SyncApplyStatus.FULL_EXECUTING);
                        if(!MailUtil.getMailedIfPresent(applyInfo.getId(), SyncApplyStatus.FULL_EXECUTING)) {
                            applyInfo.setNeedNotify(true);
                            MailUtil.getMailed(applyInfo.getId(), SyncApplyStatus.FULL_EXECUTING);
                        }
                        syncApplyDAO.updateApplyStatus(applyInfo);
                    } else {//先执行增量
                        applyInfo.setApplyStatus(SyncApplyStatus.INCREMENT_EXECUTING);
                        if(!MailUtil.getMailedIfPresent(applyInfo.getId(), SyncApplyStatus.INCREMENT_EXECUTING)) {
                            applyInfo.setNeedNotify(true);
                            MailUtil.getMailed(applyInfo.getId(), SyncApplyStatus.INCREMENT_EXECUTING);
                        }
                        syncApplyDAO.updateApplyStatus(applyInfo);
                    }
                }
            }

            //step2.检测FULL_EXECUTING/FULL_FAILED全量申请的job执行结果，job全部成功的置为SUCCEEDED，失败的继续通知手动执行，直到job全部成功
            List<SyncApplyInfo> FAIFullExecutingOrFailedList = syncApplyService.getFAIFullExecutingOrFailedApplyList();
            if (FAIFullExecutingOrFailedList != null && FAIFullExecutingOrFailedList.size() > 0) {
                for (SyncApplyInfo applyInfo : FAIFullExecutingOrFailedList) {
                    if (SyncUtil.checkFullFinish(applyInfo)) {//全量完成
                        if (SyncUtil.fullFirst(applyInfo)) {//先全量后增量的申请，全量完成则自动/通知执行增量
                            if (SyncUtil.isAutokeeper(applyInfo)) {
                                //auto create media-mapping
                                applyInfo.setApplyStatus(SyncApplyStatus.INCREMENT_EXECUTING);
                                syncApplyDAO.updateApplyStatus(applyInfo);
                                if (IncrementSyncUtil.processIncrement(applyInfo, null,ManagerConfig.current().getIsReuseTask(),
                                        ManagerConfig.current().getZkServer(), ManagerConfig.current().getCurrentEnv())) {
                                    applyInfo.setApplyStatus(SyncApplyStatus.SUCCEEDED);
                                    applyInfo.setReplyRemark("同步申请执行成功！applyId = " + applyInfo.getId());
                                    applyInfo.setNeedNotify(true);
                                    syncApplyDAO.updateApplyStatus(applyInfo);
                                }
                            } else {
                                applyInfo.setApplyStatus(SyncApplyStatus.INCREMENT_EXECUTING);
                                if(!MailUtil.getMailedIfPresent(applyInfo.getId(), SyncApplyStatus.INCREMENT_EXECUTING)) {
                                    applyInfo.setNeedNotify(true);
                                    MailUtil.getMailed(applyInfo.getId(), SyncApplyStatus.INCREMENT_EXECUTING);
                                }
                                syncApplyDAO.updateApplyStatus(applyInfo);
                            }
                        } else {//先增量后全量的申请，全量完成则申请执行成功
                            applyInfo.setApplyStatus(SyncApplyStatus.SUCCEEDED);
                            applyInfo.setReplyRemark("同步申请执行成功！applyId = " + applyInfo.getId());
                            applyInfo.setNeedNotify(true);
                            syncApplyDAO.updateApplyStatus(applyInfo);
                        }
                    }
                }
            }

            //step3.检测INCREMENT_FINISH的先全量后增量的申请置为SUCCEED，INCREMENT_FAILED的增量申请则继续通知
            List<SyncApplyInfo> FAIIncrementFinishOrFailedList = syncApplyService.getFAIIncrementFinishOrFailedList();
            if (FAIIncrementFinishOrFailedList != null && FAIIncrementFinishOrFailedList.size() > 0) {
                for (SyncApplyInfo applyInfo : FAIIncrementFinishOrFailedList) {
                    if (applyInfo.getApplyStatus() == SyncApplyStatus.INCREMENT_FINISH) {//增量完成
                        if (SyncUtil.fullFirst(applyInfo)) {//先全量后增量的申请，增量完成则申请执行成功
                            applyInfo.setApplyStatus(SyncApplyStatus.SUCCEEDED);
                            applyInfo.setReplyRemark("同步申请执行成功！applyId = " + applyInfo.getId());
                            applyInfo.setNeedNotify(true);
                            syncApplyDAO.updateApplyStatus(applyInfo);
                        } else {//先增量后全量的申请，增量完成则通知执行全量
                            applyInfo.setApplyStatus(SyncApplyStatus.FULL_EXECUTING);
                            if(!MailUtil.getMailedIfPresent(applyInfo.getId(), SyncApplyStatus.FULL_EXECUTING)) {
                                applyInfo.setNeedNotify(true);
                                MailUtil.getMailed(applyInfo.getId(), SyncApplyStatus.FULL_EXECUTING);
                            }
                            syncApplyDAO.updateApplyStatus(applyInfo);
                        }
                    } else if (applyInfo.getApplyStatus() == SyncApplyStatus.INCREMENT_FAILED) {
                    //增量失败则通知管理员手动配置，成功之后自动置为INCREMENT_FINISH
                        if(!MailUtil.getMailedIfPresent(applyInfo.getId(), SyncApplyStatus.INCREMENT_FAILED)) {
                            applyInfo.setNeedNotify(true);
                            syncApplyDAO.updateApplyStatus(applyInfo);
                            MailUtil.getMailed(applyInfo.getId(), SyncApplyStatus.INCREMENT_FAILED);
                        }
                    }

                }
            }

            List<SyncApplyInfo> notifyApplyList = syncApplyService.getNotifyApplyList();
            if (notifyApplyList != null && notifyApplyList.size() > 0) {
                for (SyncApplyInfo apply : notifyApplyList) {
                    MailUtil.sendEmail(apply);
                    apply.setNeedNotify(false);
                    syncApplyDAO.updateApplyStatus(apply);
                }
            }

        } catch (Exception e) {
            logger.error("Auto check failed.", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }
}
