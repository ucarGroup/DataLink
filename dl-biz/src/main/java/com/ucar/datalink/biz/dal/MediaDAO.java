package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.statis.StatisDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2017/3/16.
 */
public interface MediaDAO {

    //-----------------------------------------------------------------------------------------------
    //----------------------------------------methods for media--------------------------------------
    //-----------------------------------------------------------------------------------------------

    long mediaInsert(MediaInfo mediaInfo);

    int deleteMediaById(long id);

    int deleteMediaByMediaSourceId(Long mediaSourceId);

    MediaInfo getMediaByMediaSourceAndMediaName(Map<String, Object> mapPram);

    MediaInfo findMediaById(long id);

    //-----------------------------------------------------------------------------------------------
    //-------------------------------------methods for mediamapping----------------------------------
    //-----------------------------------------------------------------------------------------------

    int deleteMediaMappingById(long id);

    int deleteMediaMappingColumnByMappingId(long id);

    int deleteMediaMappingByTaskId(Long taskId);

    int deleteMediaMappingColumnByTaskId(Long taskId);

    long mediaMappingInsert(MediaMappingInfo mediaMappingInfo);

    void updateMediaMapping(MediaMappingInfo mediaMappingInfo);

    MediaMappingInfo findMediaMappingsById(long id);

    List<MediaMappingInfo> findMediaMappingsBySrcMediaSourceId(Long srcMediaSourceId);

    List<MediaMappingInfo> findMediaMappingsByTargetMediaSourceId(Long targetMediaSourceId);

    List<MediaMappingInfo> findMediaMappingsByTaskIdAndTargetMediaSourceId(Map<String, Object> mapParam);

    List<MediaMappingInfo> getAllMediaMappings();

    List<MediaSourceInfo> findMediaSourcesByTypes(MediaSourceType... types);

    List<MediaMappingInfo> findMediaMappingsByTaskId(Long taskId);

    List<MediaMappingInfo> findMediaMappingsByInterceptorId(Long interceptorId);

    List<MediaMappingInfo> mappingListsForQueryPage(MediaMappingInfo mediaMappingInfo);

    MediaSourceInfo findMediaSourceById(Long id);


    //-----------------------------------------------------------------------------------------------
    //-------------------------------------methods for mediacolumnmapping----------------------------
    //-----------------------------------------------------------------------------------------------

    void updateMediaColumnMapping(MediaColumnMappingInfo mediaColumnMappingInfo);

    long mediaColumnInsert(MediaColumnMappingInfo mediaColumnMappingInfo);

    List<MediaColumnMappingInfo> findMediaColumnByMappingId(long mappingId);

    Integer mappingCount();

    List<StatisDetail> getCountByType();

    List<Long> findTaskIdsByMediaSourceId(Long mediaSourceId);

    List<MediaInfo> findMediaByMediaSourceId(Long mediaSourceId);

    List<Long> findTaskIdListByMediaSourceList(@Param("mediaSourceIdList") List<Long> mediaSourceIdList);

    MediaMappingInfo getMediaMappingOfSpecial(@Param(value = "tableName") String tableName, @Param(value = "mediaSourceId") Long mediaSourceId);

    List<MediaMappingInfo> findMappingListByCondition(MediaMappingInfo mediaMappingInfo);

    List<MediaMappingInfo> getMappingsByTargetMediaNameAndNamespace(@Param(value = "targetMediaSourceId") Long targetMediaSourceId, @Param(value = "targetNamespace") String targetNamespace, @Param(value = "targetTableName") String targetTableName);

    List<MediaMappingInfo> getMappingsByMediaSourceIdAndTargetTable(@Param(value = "srcMediaSourceId") Long srcMediaSourceId, @Param(value = "targetMediaSourceId") Long targetMediaSourceId, @Param(value = "targetTableName") String targetTableName);

    MediaMappingInfo findMediaMappingByJoinIndex(MediaMappingInfo mediaMappingInfo);

}
