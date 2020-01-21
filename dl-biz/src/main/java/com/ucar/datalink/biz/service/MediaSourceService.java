package com.ucar.datalink.biz.service;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.statis.StatisDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by csf on 2017/3/16.
 */
public interface MediaSourceService {

    List<MediaSourceInfo> getList();

    List<MediaSourceInfo> getListByType(Set<MediaSourceType> mediaSourceType);

    List<MediaSourceInfo> getListForQueryPage(@Param("mediaSourceType")Set<MediaSourceType> mediaSourceType, @Param("mediaSourceName")String mediaSourceName,@Param("mediaSourceIp") String mediaSourceIp);

    Boolean insert(MediaSourceInfo mediaSourceInfo);

    Boolean update(MediaSourceInfo mediaSourceInfo);

    Boolean delete(Long id);

    MediaSourceInfo getById(Long id);

    List<String> getRdbTableName(MediaSourceInfo info) throws Exception;

    List<String> getRdbColumnName(MediaSourceInfo info,String tableName) throws Exception;

    void checkIpBelongLab(RdbMediaSrcParameter rdbMediaSrcParameter,Long labId) throws Exception;

    void checkDbConnection(RdbMediaSrcParameter rdbMediaSrcParameter) throws Exception;

    Integer msCount();

    List<StatisDetail> getCountByType();

    List<String> getHbaseTableName(MediaSourceInfo hbaseMediaSourceInfo);

    List<String> getMappingTableNameByTaskIdAndTargetMediaSourceId(Map<String, Object> mapParam);

    List<MediaSourceInfo> findRealListByVirtualMsId(Long id);

    void clearRealMediaSourceListCache(Long id);

    Boolean insertVirtual(MediaSourceInfo mediaSourceInfo,String realDbIds);

    Boolean updateVirtual(MediaSourceInfo mediaSourceInfo,String realDbId);

    MediaSourceInfo getOneByName(String msName);

    /**
     * 根据虚拟数据源id和所属机房获取数据源
     * @param virtualMsId
     * @param labName
     * @return
     */
    MediaSourceInfo findRealSignleByMsIdAndLab(Long virtualMsId, String labName);

    List<MediaMappingInfo> getMappingByTaskIdAndTargetMediaSourceId(Map<String, Object> mapParam);

    JSONObject getDbInfo(String dbName, String dbType, String env);

    List<MediaSourceInfo> getListByNameList(List<String> mediaSourceNameList);

    boolean insertDoubleMediaSource(List<MediaSourceInfo> mediaSourceInfoList, List<String> existsMediaSourceList);

    /**
     * 根据schema查询数据源
     * @param targetNamespace
     * @return
     */
    List<MediaSourceInfo> getMediaSourceLikeSchema(String targetNamespace);
}
