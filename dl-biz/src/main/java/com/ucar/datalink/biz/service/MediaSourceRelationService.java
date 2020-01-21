package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.media.MediaSourceRelationInfo;

import java.util.List;

/**
 * Created by yang.wang09 on 2018-10-31 17:14.
 */
public interface MediaSourceRelationService {

    List<MediaSourceRelationInfo> findList();

    Integer insert(MediaSourceRelationInfo mediaSourceInfo);

    Integer update(MediaSourceRelationInfo mediaSourceInfo);

    Integer delete(Long id);

    Integer delete(MediaSourceRelationInfo info);

    MediaSourceRelationInfo getOneById(Long id);

    Integer checkExist(MediaSourceRelationInfo info);

    List<MediaSourceRelationInfo> findListByVirtualId(Long id);

    MediaSourceRelationInfo getOneByRealMsId(Long realMsId);

    Integer checkExitOneByVritualMsId(Long virtualId);
}
