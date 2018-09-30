package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.statis.StatisDetail;
import org.apache.ddlutils.model.Table;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lubiao on 2017/3/8.
 */
public interface MediaService {

    void insert(List<MediaInfo> mediaList, List<MediaMappingInfo> mediaMappingList, List<MediaColumnMappingInfo> mediaColumnMappingList) throws Exception;

    void delete(long id);

    List<MediaMappingInfo> findMediaMappingsByTaskIdAndTargetMediaSourceId(Map<String, Object> mapParam);

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

    List<MediaMappingInfo> mappingListsForQueryPage(Long mediaSourceId, Long targetMediaSourceId, Long taskId, String tableName);

    Integer mappingCount();

    List<StatisDetail> getCountByType();

    List<Long> findTaskIdsByMediaSourceId(Long mediaSourceId);
}
