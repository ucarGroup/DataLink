package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceRelationInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.user.UserRoleInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 */
public interface MediaSourceRelationDAO {

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
