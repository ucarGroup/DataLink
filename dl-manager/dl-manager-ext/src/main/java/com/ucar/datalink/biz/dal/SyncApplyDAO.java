package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.sync.SyncApplyInfo;
import com.ucar.datalink.domain.sync.SyncApproveInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by sqq on 2017/9/20.
 */
public interface SyncApplyDAO {

    long insert(SyncApplyInfo applyInfo);

    long insertApproveInfo(SyncApproveInfo approveInfo);

    Integer update(SyncApplyInfo applyInfo);

    void updateApproveInfo(SyncApproveInfo approveInfo);

    SyncApplyInfo getSyncApplyInfoById(Long id);

    List<SyncApproveInfo> getSyncApproveInfoByApplyId(Long applyId);

    SyncApproveInfo getSyncApproveInfoByApplyIdAndApproveUserId(SyncApproveInfo approveInfo);

    List<SyncApplyInfo> syncApplyListForQueryPage(Map<String,Object> mapPram);

    MediaSourceInfo getMediaSourceById(Long id);

    void updateApplyStatus(SyncApplyInfo applyInfo);

    List<SyncApproveInfo> getAllApproved(SyncApproveInfo approveInfo);

    List<SyncApproveInfo> getAllRejected(SyncApproveInfo approveInfo);

    List<SyncApproveInfo> getAllNone(SyncApproveInfo approveInfo);

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
