package com.ucar.datalink.biz.service.impl;

import com.google.common.collect.Lists;
import com.ucar.datalink.biz.dal.SyncApplyDAO;
import com.ucar.datalink.biz.service.SyncApplyService;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.domain.sync.SyncApplyStatus;
import com.ucar.datalink.domain.sync.SyncApproveInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by sqq on 2017/9/20.
 */
@Service
public class SyncApplyServiceImpl implements SyncApplyService {

    @Autowired
    SyncApplyDAO syncApplyDAO;

    @Override
    @Transactional
    public void insert(SyncApplyInfo applyInfo, List<SyncApproveInfo> approveInfoList) {
        syncApplyDAO.insert(applyInfo);
        long applyId = applyInfo.getId();
        for (SyncApproveInfo approveInfo : approveInfoList) {
            approveInfo.setApplyId(applyId);
            syncApplyDAO.insertApproveInfo(approveInfo);
        }
    }

    @Override
    public void update(SyncApplyInfo applyInfo) {
        syncApplyDAO.update(applyInfo);
    }

    @Override
    public SyncApplyInfo getSyncApplyInfoById(Long id) {
        return syncApplyDAO.getSyncApplyInfoById(id);
    }

    @Override
    public List<SyncApproveInfo> getSyncApproveInfoByApplyId(Long applyId) {
        return syncApplyDAO.getSyncApproveInfoByApplyId(applyId);
    }


    @Override
    public List<SyncApplyInfo> syncApplyListForQueryPage(SyncApplyStatus applyStatus, Long applyUserId, String applyType, Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("applyStatus", applyStatus);
        map.put("applyType", applyType);
        map.put("applyUserId", applyUserId);
        map.put("userId", userId);
        List<SyncApplyInfo> result = syncApplyDAO.syncApplyListForQueryPage(map);
        return result == null ? Lists.newArrayList() : result;
    }

    @Override
    public MediaSourceInfo getMediaSourceById(Long id) {
        return syncApplyDAO.getMediaSourceById(id);
    }

    @Override
    @Transactional
    public void doApproveOrReject(SyncApplyInfo applyInfo, SyncApproveInfo approveInfo, Boolean autoApprove) {
        syncApplyDAO.updateApproveInfo(approveInfo);
        Boolean isApproved = isApproved(approveInfo);
        Boolean isRejected = isRejected(approveInfo);
        Boolean hasNone = hasNone(approveInfo);
        if (autoApprove) {
            syncApplyDAO.updateApplyStatus(applyInfo);
        } else if (isRejected) {
            applyInfo.setNeedNotify(true);
            syncApplyDAO.updateApplyStatus(applyInfo);
        } else if (hasNone) {
            applyInfo.setApplyStatus(SyncApplyStatus.SUBMITTED);
            syncApplyDAO.updateApplyStatus(applyInfo);
        } else if (isApproved) {
            syncApplyDAO.updateApplyStatus(applyInfo);
        }
    }

    @Override
    public Boolean isApproved(SyncApproveInfo approveInfo) {
        List<SyncApproveInfo> approveInfos = syncApplyDAO.getSyncApproveInfoByApplyId(approveInfo.getApplyId());
        List<SyncApproveInfo> approvedList = syncApplyDAO.getAllApproved(approveInfo);
        if (approvedList.size() != approveInfos.size()) {
            return false;
        }
        int count = 0;
        for (SyncApproveInfo approve : approveInfos) {
            Long approveUserId = approve.getApproveUserId();
            for (SyncApproveInfo info : approvedList) {
                if (Objects.equals(approveUserId, info.getApproveUserId())) {
                    count++;
                    break;
                }
            }
        }
        return count == approveInfos.size();
    }

    @Override
    public Boolean isRejected(SyncApproveInfo approveInfo) {
        List<SyncApproveInfo> rejectedList = syncApplyDAO.getAllRejected(approveInfo);
        return rejectedList != null && rejectedList.size() > 0;
    }

    @Override
    public Boolean hasNone(SyncApproveInfo approveInfo) {
        List<SyncApproveInfo> noneList = syncApplyDAO.getAllNone(approveInfo);
        return noneList != null && noneList.size() > 0;
    }

    @Override
    public Boolean canApprove(Long userId, Long applyId) {
        SyncApplyInfo applyInfo = syncApplyDAO.getSyncApplyInfoById(applyId);
        List<SyncApproveInfo> approveInfos = syncApplyDAO.getSyncApproveInfoByApplyId(applyId);
        if (applyInfo.getApplyStatus() == SyncApplyStatus.SUBMITTED || applyInfo.getApplyStatus() == SyncApplyStatus.REJECTED) {
            for (SyncApproveInfo approve : approveInfos) {
                if (userId.equals(approve.getApproveUserId())) {//如果这个人已经审批过了，也可以再审批
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<SyncApproveInfo> getAutoApproveList() {
        return syncApplyDAO.getAutoApproveList();
    }

    @Override
    public List<SyncApplyInfo> getNotifyApplyList() {
        return syncApplyDAO.getNotifyApplyList();
    }

    @Override
    public List<SyncApplyInfo> getApprovedApplyList() {
        return syncApplyDAO.getApprovedApplyList();
    }

    @Override
    public List<SyncApplyInfo> getFullApprovedApplyList() {
        return syncApplyDAO.getFullApprovedApplyList();
    }

    @Override
    public List<SyncApplyInfo> getFullExecutingOrFailedApplyList() {
        return syncApplyDAO.getFullExecutingOrFailedApplyList();
    }

    @Override
    public List<SyncApplyInfo> getIncrementApprovedApplyList() {
        return syncApplyDAO.getIncrementApprovedApplyList();
    }

    @Override
    public List<SyncApplyInfo> getIncrementFinishOrFailedList() {
        return syncApplyDAO.getIncrementFinishOrFailedList();
    }

    @Override
    public List<SyncApplyInfo> getFullAndIncrementApprovedApplyList() {
        return syncApplyDAO.getFullAndIncrementApprovedApplyList();
    }

    @Override
    public List<SyncApplyInfo> getFAIFullExecutingOrFailedApplyList() {
        return syncApplyDAO.getFAIFullExecutingOrFailedApplyList();
    }

    @Override
    public List<SyncApplyInfo> getFAIIncrementFinishOrFailedList() {
        return syncApplyDAO.getFAIIncrementFinishOrFailedList();
    }

    @Override
    public List<SyncApplyInfo> getTimeOutUnfinishedList() {
        return syncApplyDAO.getTimeOutUnfinishedList();
    }

    @Override
    public List<SyncApproveInfo> getRejectedApproveByApplyId(Long applyId) {
        return syncApplyDAO.getRejectedApproveByApplyId(applyId);
    }
}
