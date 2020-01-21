package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.vo.TaskMediaNameVo;
import org.apache.ddlutils.model.Table;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lubiao on 2017/3/8.
 */
public interface MediaService {

    List<Long> insert(List<MediaInfo> mediaList, List<MediaMappingInfo> mediaMappingList, List<MediaColumnMappingInfo> mediaColumnMappingList) throws Exception;

    void delete(long id);

    void update(MediaColumnMappingInfo mediaColumnMappingInfo, MediaMappingInfo mediaMappingInfo) throws Exception;

    MediaInfo findMediaById(long id);

    List<MediaColumnMappingInfo> findMediaColumnByMappingId(long mappingId);

    MediaMappingInfo findMediaMappingsById(long id);

    void clearMediaMappingCache(Long taskId);

    /**
     * 判断Column-Mapping配置是否合法
     * 当不合法时抛出ValidationException
     */
    void checkMediaColumnMappings(Table table, List<MediaColumnMappingInfo> list, ColumnMappingMode mode);

    /**
     * 获取指定id对应的MediaSourceInfo实例
     */
    MediaSourceInfo getMediaSourceById(Long id);

    /**
     * 获取指定类型的MediaSource列表
     *
     * @param types
     * @return
     */
    List<MediaSourceInfo> getMediaSourcesByTypes(MediaSourceType... types);

    /**
     * 查找单机房数据源
     *
     * @param types 数据源类型
     * @return
     */
    List<MediaSourceInfo> findMediaSourcesForSingleLab(List<MediaSourceType> types);

    /**
     * 查找跨机房数据源
     *
     * @param types 数据源类型
     * @return
     */
    List<MediaSourceInfo> findMediaSourcesForAcrossLab(Long sourceLabId, List<MediaSourceType> types);

    /**
     * 查找跨机房数据源，根据传入的MediaSourceType，返回管理的所有虚拟数据源
     *
     * @param types 数据源类型
     * @return
     */
    public List<MediaSourceInfo> findMediaSourcesForAllAcrossLab(List<MediaSourceType> types);


    /**
     * 获取某个Task下所有的Mapping配置
     *
     */
    List<MediaMappingInfo> findMediaMappingsByTask(Long taskId);

    /**
     * 获取某个Task下所有的Mapping配置
     *
     * @param justValid 是否只获取当前状态为有效的配置
     */
    List<MediaMappingInfo> getMediaMappingsByTask(Long taskId, boolean justValid);

    /**
     * 通过Record的namespace和mediaName,获取某个Task下,同步到指定类型数据源的mapping集合
     * 如果不存在配置,则返回一个空的List.
     *
     * @param justValid 是否只获取当前状态为有效的配置
     */
    List<MediaMappingInfo> getMediaMappingsByMediaAndTarget(Long taskId, String namespace, String name,
                                                            Set<MediaSourceType> targetSourceTypes, boolean justValid);

    /**
     * 通过Record的namespace和mediaName，获取某个Task下的MediaMapping集合
     */
    List<MediaMappingInfo> getMediaMappingsByMedia(Long taskId, String namespace, String mediaName, boolean justValid);

    List<MediaMappingInfo> mappingListsForQueryPage(Long mediaSourceId, Long targetMediaSourceId, Long taskId, String tableName, String targetMediaName);

    Integer mappingCount();

    List<StatisDetail> getCountByType();

    List<Long> findTaskIdsByMediaSourceId(Long mediaSourceId);

    void cleanTableMapping(Long taskId) throws Exception;

    /**
     * 获取真实数据源
     *
     * 取中心机房的数据源
     *
     * @return
     */
    MediaSourceInfo getRealDataSource(MediaSourceInfo sourceInfo);

    /**
     * 获取真实数据源
     *
     * 优先取Task所属机房对应的数据源，没有的话再取中心机房的数据源
     *
     * @param taskId
     * @return
     */
    MediaSourceInfo getRealDataSourceSpecial(Long taskId,MediaSourceInfo sourceInfo);

    /**
     *  获取真实数据源列表
     *  获取虚拟数据源下的所有真实数据源，并返回数据源列表
     *
     * @param sourceInfo
     * @return
     */
    List<MediaSourceInfo> listRealMediaSourceInfos(MediaSourceInfo sourceInfo);

    /**
     *  传入批量数据源
     *  获取DB关联的任务id
     *
     * @param mediaSourceIdList
     * @return
     */
    public List<Long> findTaskIdListByMediaSourceList(List<Long> mediaSourceIdList);

    /**
     * 获取zk集群
     * 在配置hbase task时，需要获取datalink配置的zk集群
     * @return
     */
    List<MediaSourceInfo> buildZkMediaSources(String zkServer);

    /**
     * 获取task下边所有同步的表
     * @param taskIdList
     * @return
     */
    List<TaskMediaNameVo> getMediaNamesByTaskId(List<Long> taskIdList);

    /**
     * 获取源端表名
     * @param mappingId
     * @return
     */
    TaskMediaNameVo findSourceTableInfoByMappingId(Long mappingId);

    /**
     * 根据目标端库和表获取mapping
     * @param targetMediaSourceId
     * @param targetNamespace
     * @param targetTableName
     * @return
     */
    List<MediaMappingInfo> getMappingsByTargetMediaNameAndNamespace(Long targetMediaSourceId, String targetNamespace, String targetTableName);

    /**
     * 根据目标端库和表获取mapping
     * @param srcMediaSourceId
     * @param targetMediaSourceId
     * @param targetTableName
     * @return
     */
    List<MediaMappingInfo> getMappingsByMediaSourceIdAndTargetTable(Long srcMediaSourceId, Long targetMediaSourceId, String targetTableName);

    List<MediaMappingInfo> getAllMediaMappingsByTaskId(Long taskId);
}
