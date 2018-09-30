package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.statis.StatisDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by csf on 2017/3/16.
 */
public interface MediaSourceService {

    List<MediaSourceInfo> getList();

    List<MediaSourceInfo> getListByType(Set<MediaSourceType> mediaSourceType);

    List<MediaSourceInfo> getListForQueryPage(@Param("mediaSourceType")Set<MediaSourceType> mediaSourceType, @Param("mediaSourceName")String mediaSourceName);

    Boolean insert(MediaSourceInfo mediaSourceInfo);

    Boolean update(MediaSourceInfo mediaSourceInfo);

    Boolean delete(Long id);

    MediaSourceInfo getById(Long id);

    List<String> getRdbTableName(MediaSourceInfo info);

    List<String> getRdbColumnName(MediaSourceInfo info,String tableName);

    void checkDbConnection(RdbMediaSrcParameter rdbMediaSrcParameter) throws Exception;

    Integer msCount();

    List<StatisDetail> getCountByType();

    List<String> getHbaseTableName(MediaSourceInfo hbaseMediaSourceInfo);
}
