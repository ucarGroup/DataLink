package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.statis.StatisDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by csf on 2017/3/16.
 */
public interface MediaSourceDAO {

    List<MediaSourceInfo> getList();

    List<MediaSourceInfo> getListByType(@Param(value="mediaSourceType")Set mediaSourceType);

    List<MediaSourceInfo> getListForQueryPage(@Param("mediaSourceType")Set<MediaSourceType> mediaSourceType, @Param("mediaSourceName")String mediaSourceName);

    Integer insert(MediaSourceInfo mediaSourceInfo);

    Integer update(MediaSourceInfo mediaSourceInfo);

    Integer delete(Long id);

    MediaSourceInfo getById(Long id);

    Integer msCount();

    List<StatisDetail> getCountByType();
}
