package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.domain.sync.SyncApplyStatus;
import com.ucar.datalink.domain.sync.SyncApproveInfo;

import java.util.List;

/**
 * Created by sqq on 2017/9/20.
 */
public interface SyncApplyService {

    void insert(SyncApplyInfo applyInfo, List<SyncApproveInfo> approveInfoList);

    void update(SyncApplyInfo applyInfo);

    SyncApplyInfo getSyncApplyInfoById(Long id);

    List<SyncApproveInfo> getSyncApproveInfoByApplyId(Long applyId);

    List<SyncApplyInfo> syncApplyListForQueryPage(SyncApplyStatus applyStatus, Long applyUserId, String applyType, Long userId);

    MediaSourceInfo getMediaSourceById(Long id);

    void doApproveOrReject(SyncApplyInfo applyInfo, SyncApproveInfo approveInfo, Boolean autoApprove);

    Boolean isApproved(SyncApproveInfo approveInfo);

    Boolean isRejected(SyncApproveInfo approveInfo);

    Boolean hasNone(SyncApproveInfo approveInfo);

    Boolean canApprove(Long userId, Long ApplyId);

    List<SyncApproveInfo> getAutoApproveList();

    List<SyncApplyInfo> getNotifyApplyList();

    List<SyncApplyInfo> getApprovedApplyList();

    List<SyncApplyInfo> getFullApprovedApplyList();

    List<SyncApplyInfo> getFullExecutingOrFailedApplyList();

    List<SyncApplyInfo> getIncrementApprovedApplyList();

    List<SyncApplyInfo> getIncrementFinishOrFailedList();

    List<SyncApplyInfo> getFullAndIncrementApprovedApplyList();

    List<SyncApplyInfo> getFAIFullExecutingOrFailedApplyList();

    List<SyncApplyInfo> getFAIIncrementFinishOrFailedList();

    List<SyncApplyInfo> getTimeOutUnfinishedList();

    List<SyncApproveInfo> getRejectedApproveByApplyId(Long applyId);


}
