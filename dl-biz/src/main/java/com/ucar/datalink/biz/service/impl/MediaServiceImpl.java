package com.ucar.datalink.biz.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.ucar.datalink.biz.dal.LabDAO;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.dal.MediaSourceDAO;
import com.ucar.datalink.biz.dal.TaskDAO;
import com.ucar.datalink.biz.meta.MetaManager;
import com.ucar.datalink.biz.service.DoubleCenterService;
import com.ucar.datalink.biz.service.LabService;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.common.utils.FutureCallback;
import com.ucar.datalink.domain.event.MediaMappingChangeEvent;
import com.ucar.datalink.domain.lab.LabInfo;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.vo.TaskMediaNameVo;
import org.apache.commons.lang.StringUtils;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by user on 2017/3/16.
 */
@Service
public class MediaServiceImpl implements MediaService {

    private static final Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);

    private LoadingCache<Long, List<MediaMappingInfo>> mediaMappingsCache;

    private LoadingCache<TaskMediaKey, List<MediaMappingInfo>> taskMediaMappingsCache;

    private LoadingCache<LoadingKey, MediaSourceInfo> mediaSourceInfoCache;

    @Autowired
    MediaDAO mediaDAO;

    @Autowired
    MediaSourceDAO mediaSourceDAO;

    @Autowired
    TaskDAO taskDAO;

    @Autowired
    LabDAO labDAO;

    @Autowired
    DoubleCenterService doubleCenterService;

    public MediaServiceImpl() {
        mediaMappingsCache = CacheBuilder.newBuilder().build(new CacheLoader<Long, List<MediaMappingInfo>>() {
            @Override
            public List<MediaMappingInfo> load(Long taskId) throws Exception {
                TaskInfo taskInfo = taskDAO.findById(taskId);
                List<MediaMappingInfo> list;

                if (taskInfo.getLeaderTaskId() != null) {
                    list = mediaDAO.findMediaMappingsByTaskId(taskInfo.getLeaderTaskId());
                    if (list != null) {
                        list.stream().forEach(t -> {
                            t.setTaskId(taskId);
                            t.setTaskInfo(taskInfo);
                        });
                    }
                } else {
                    list = mediaDAO.findMediaMappingsByTaskId(taskId);
                }

                return list == null ? Lists.newArrayList() : list;
            }
        });

        taskMediaMappingsCache = CacheBuilder.newBuilder().build(new CacheLoader<TaskMediaKey, List<MediaMappingInfo>>() {
            @Override
            public List<MediaMappingInfo> load(TaskMediaKey key) throws Exception {
                List<MediaMappingInfo> list = new ArrayList<>();
                mediaMappingsCache.getUnchecked(key.getTaskId()).forEach(m -> {
                    if (isMatch(m.getSourceMedia(), key.getNamespace(), key.getName())) {
                        list.add(m);
                    }
                });

                return list.stream()
                        .collect(Collectors.groupingBy(MediaMappingInfo::getTargetMediaSourceId))
                        .entrySet()
                        .stream()
                        .flatMap(fm ->
                                {
                                    if (fm.getValue().size() > 1) {
                                        //如果有多个，有Single配置，且又有通配符配置，则保留Single配置，重载掉通配符配置，其他情况原样返回
                                        long singleCount = fm.getValue().stream().filter(i -> i.getSourceMedia().getNameMode().getMode().isSingle()).count();
                                        if (singleCount >= 1 && singleCount < fm.getValue().size()) {
                                            return fm.getValue().stream().filter(i -> i.getSourceMedia().getNameMode().getMode().isSingle());
                                        } else {
                                            return fm.getValue().stream();
                                        }
                                    } else {
                                        return fm.getValue().stream();
                                    }
                                }
                        )
                        .collect(Collectors.toList());
            }
        });

        mediaSourceInfoCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(new CacheLoader<LoadingKey, MediaSourceInfo>() {
            @Override
            public MediaSourceInfo load(LoadingKey loadingKey) throws Exception {
                return doGetRealDataSourceSpecial(loadingKey.taskId, loadingKey.mediaSourceInfo);
            }
        });

    }

    @Override
    @Transactional
    public List<Long> insert(List<MediaInfo> mediaList, List<MediaMappingInfo> mediaMappingList, List<MediaColumnMappingInfo> mediaColumnMappingList) throws Exception {
        checkConsistency(mediaList, mediaMappingList);

        //如果目标端是ES虚拟数据源，需验证该Task是leader-task
        MediaMappingInfo mediaMappingInfo = mediaMappingList.get(0);
        MediaSourceInfo mediaSourceInfo = mediaSourceDAO.getById(mediaMappingInfo.getTargetMediaSourceId());
        if (mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL) && mediaSourceInfo.getSimulateMsType().equals(MediaSourceType.ELASTICSEARCH)) {
            TaskInfo taskInfo = taskDAO.findById(mediaMappingInfo.getTaskId());
            if (!taskInfo.isLeaderTask()) {
                throw new RuntimeException("目标端是ES虚拟数据源，Task必须是leader-task");
            }
        }

        Long mediaId = null;
        List<Long> mappingIdList = new ArrayList<Long>();
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < mediaList.size(); i++) {
            map.put("mediaSourceId", mediaList.get(i).getMediaSourceId());
            map.put("mediaName", mediaList.get(i).getName());

            //处理Media
            MediaInfo mediaInfo = mediaDAO.getMediaByMediaSourceAndMediaName(map);
            if (mediaInfo != null) {
                mediaId = mediaInfo.getId();
            } else {
                mediaDAO.mediaInsert(mediaList.get(i));
                mediaId = mediaList.get(i).getId();
            }

            //处理MediaMapping
            MediaMappingInfo mediaMappingInfoAdd = mediaMappingList.get(i);
            mediaMappingInfoAdd.setSourceMediaId(mediaId);

            //判断映射是否已经存在
            MediaMappingInfo mediaMappingInfoExists = mediaDAO.findMediaMappingByJoinIndex(mediaMappingInfoAdd);
            if(mediaMappingInfoExists != null) {
                throw new DatalinkException("该映射已经存在");
            }

            mediaDAO.mediaMappingInsert(mediaMappingInfoAdd);
            mappingIdList.add(mediaMappingInfoAdd.getId());

            //处理MediaMappingColumn
            MediaColumnMappingInfo columnMappingInfo = mediaColumnMappingList.get(i);
            String[] sourceCol = columnMappingInfo.getSourceColumn().split(",");
            String[] targetCol = columnMappingInfo.getTargetColumn().split(",");

            MediaColumnMappingInfo columnForInsert = new MediaColumnMappingInfo();
            columnForInsert.setMediaMappingId(mediaMappingList.get(i).getId());
            for (int q = 0; q < sourceCol.length; q++) {
                columnForInsert.setSourceColumn(sourceCol[q]);
                columnForInsert.setTargetColumn(targetCol[q]);
                mediaDAO.mediaColumnInsert(columnForInsert);
            }
        }
        return mappingIdList;
    }

    @Override
    @Transactional
    public void delete(long id) {
        mediaDAO.deleteMediaMappingById(id);
        mediaDAO.deleteMediaMappingColumnByMappingId(id);
    }

    @Override
    @Transactional
    public void update(MediaColumnMappingInfo mediaColumnMappingInfo, MediaMappingInfo mediaMappingInfo) throws Exception {
        String sourceColumn = mediaColumnMappingInfo.getSourceColumn();
        String targetColumn = mediaColumnMappingInfo.getTargetColumn();
        String[] sourceColumnArray = sourceColumn.split(",");
        String[] targetColumnArray = targetColumn.split(",");
        mediaDAO.deleteMediaMappingColumnByMappingId(mediaMappingInfo.getId());
        for (int i = 0; i < sourceColumnArray.length; i++) {
            MediaColumnMappingInfo columnMapping = new MediaColumnMappingInfo();
            columnMapping.setTargetColumn(targetColumnArray[i]);
            columnMapping.setSourceColumn(sourceColumnArray[i]);
            columnMapping.setMediaMappingId(mediaMappingInfo.getId());
            mediaDAO.mediaColumnInsert(columnMapping);
        }
        mediaDAO.updateMediaMapping(mediaMappingInfo);
    }

    @Override
    public MediaInfo findMediaById(long id) {
        return mediaDAO.findMediaById(id);
    }

    @Override
    public List<MediaColumnMappingInfo> findMediaColumnByMappingId(long mappingId) {
        return mediaDAO.findMediaColumnByMappingId(mappingId);
    }

    @Override
    public MediaMappingInfo findMediaMappingsById(long id) {
        return mediaDAO.findMediaMappingsById(id);
    }

    @Override
    public void clearMediaMappingCache(Long taskId) {
        List<TaskInfo> followerTasks = taskDAO.listByLeaderTaskId(taskId);
        if (followerTasks != null) {
            followerTasks.stream().forEach(i -> {
                mediaMappingsCache.invalidate(i.getId());
                logger.info("Mapping cache has been cleared for follower taskId:" + i.getId());
            });
        }

        mediaMappingsCache.invalidate(taskId);
        //taskMediaMappingsCache的懒加载不涉及DB操作，没有性能问题，简单起见，直接执行invalidateAll即可，保证同一个Task的mappinginfo的读一致
        taskMediaMappingsCache.invalidateAll();
        logger.info("Mapping cache has been cleared for taskId:" + taskId);
    }

    @Override
    public MediaSourceInfo getMediaSourceById(Long id) {
        return mediaDAO.findMediaSourceById(id);
    }

    @Override
    public void checkMediaColumnMappings(Table table, List<MediaColumnMappingInfo> mappingList, ColumnMappingMode mode) {
        if (mappingList == null || mappingList.isEmpty()) {
            return;
        }

        Index[] indices = table.getIndices();
        if (indices == null || indices.length < 1) {
            return;
        }

        List<UniqueIndex> uniqueIndices = new ArrayList<>();
        for (Index index : indices) {
            if (index instanceof UniqueIndex) {
                uniqueIndices.add((UniqueIndex) index);
            }
        }
        if (uniqueIndices.isEmpty()) {
            return;
        }

        for (UniqueIndex uniqueIndex : uniqueIndices) {
            if (uniqueIndex.getColumns().length > 1) {
                for (IndexColumn column : uniqueIndex.getColumns()) {
                    Optional<MediaColumnMappingInfo> optional = mappingList
                            .stream()
                            .filter(m -> m.getSourceColumn().equalsIgnoreCase(column.getName()))
                            .findFirst();

                    if ((mode.isInclude() && !optional.isPresent()) ||
                            (mode.isExclude() && optional.isPresent())) {
                        throw new ValidationException(
                                String.format("Column [%s] is part of the unique index [%s] in table [%s],can not be ignored in the data sync.",
                                        column.getName(),
                                        uniqueIndex.getName(),
                                        table.getName())
                        );
                    }
                }
            }
        }
    }

    @Override
    public List<MediaSourceInfo> getMediaSourcesByTypes(MediaSourceType... types) {
        return mediaDAO.findMediaSourcesByTypes(types);
    }

    /**
     * 查找单机房数据源
     *
     * @param types 数据源类型
     * @return
     */
    @Override
    public List<MediaSourceInfo> findMediaSourcesForSingleLab(List<MediaSourceType> types) {
        return mediaDAO.findMediaSourcesForSingleLab(types);
    }

    /**
     * 查找跨机房数据源
     *
     * @param types 数据源类型
     * @return
     */
    public List<MediaSourceInfo> findMediaSourcesForAcrossLab(Long labId, List<MediaSourceType> types) {
        List<MediaSourceInfo> mediaSourceInfoList = mediaDAO.findMediaSourcesForAcrossLab(labId, types);
        return mediaSourceInfoList;
    }

    /**
     * 查找跨机房数据源，根据传入的MediaSourceType，返回管理的所有虚拟数据源
     *
     * @param types 数据源类型
     * @return
     */
    @Override
    public List<MediaSourceInfo> findMediaSourcesForAllAcrossLab(List<MediaSourceType> types) {
        List<MediaSourceInfo> mediaSourceInfoList = mediaDAO.findMediaSourcesForAllAcrossLab(types);
        return mediaSourceInfoList;
    }


    @Override
    public List<MediaMappingInfo> findMediaMappingsByTask(Long taskId) {
        return mediaMappingsCache.getUnchecked(taskId);
    }

    @Override
    public List<MediaMappingInfo> getMediaMappingsByTask(Long taskId, boolean justValid) {
        return mediaMappingsCache.getUnchecked(taskId)
                .stream()
                .filter(m -> (!justValid || m.isValid()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MediaMappingInfo> getMediaMappingsByMediaAndTarget(Long taskId, String namespace, String mediaName,
                                                                   Set<MediaSourceType> targetSourceTypes,
                                                                   boolean justValid) {
        return taskMediaMappingsCache.getUnchecked(new TaskMediaKey(taskId, namespace, mediaName))
                .stream()
                .filter(m ->
                        {
                            if (m.getTargetMediaSource().getType().equals(MediaSourceType.VIRTUAL)) {
                                Boolean flag = targetSourceTypes.contains(m.getTargetMediaSource().getSimulateMsType())
                                        && (!justValid || m.isValid());
                                return flag;
                            } else {
                                Boolean flag = targetSourceTypes.contains(m.getTargetMediaSource().getType())
                                        && (!justValid || m.isValid());
                                return flag;
                            }
                        }
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<MediaMappingInfo> getMediaMappingsByMedia(Long taskId, String namespace, String mediaName, boolean justValid) {
        return taskMediaMappingsCache.getUnchecked(new TaskMediaKey(taskId, namespace, mediaName))
                .stream()
                .filter(m -> !justValid || m.isValid())
                .collect(Collectors.toList());
    }

    private static boolean isMatch(MediaInfo mediaInfo, String namespace, String mediaName) {
        boolean isMatch = true;
        if (StringUtils.isEmpty(namespace)) {
            isMatch &= StringUtils.isEmpty(mediaInfo.getNamespace());
        } else {
            if (mediaInfo.getNamespaceMode().getMode().isSingle()) {
                isMatch &= mediaInfo.getNamespace().equalsIgnoreCase(namespace);
            } else if (mediaInfo.getNamespaceMode().getMode().isMulti()) {
                isMatch &= (ModeUtils.indexIgnoreCase(mediaInfo.getNamespaceMode().getMultiValue(), namespace) != -1);
            } else if (mediaInfo.getNamespaceMode().getMode().isWildCard()) {
                isMatch &= ModeUtils.isWildCardMatch(mediaInfo.getNamespace(), namespace);
            } else if (mediaInfo.getNamespaceMode().getMode().isYearly()) {
                isMatch &= ModeUtils.isYearlyMatch(mediaInfo.getNamespace(), namespace);
            } else if (mediaInfo.getNamespaceMode().getMode().isMonthly()) {
                isMatch &= ModeUtils.isMonthlyMatch(mediaInfo.getNamespace(), namespace);
            } else {
                throw new UnsupportedOperationException("unsupport mode:" + mediaInfo.getNameMode().getMode());
            }
        }

        if (StringUtils.isEmpty(mediaName)) {
            isMatch &= StringUtils.isEmpty(mediaInfo.getName());
        } else {
            if (mediaInfo.getNameMode().getMode().isSingle()) {
                isMatch &= mediaInfo.getName().equalsIgnoreCase(mediaName);
            } else if (mediaInfo.getNameMode().getMode().isMulti()) {
                isMatch &= (ModeUtils.indexIgnoreCase(mediaInfo.getNameMode().getMultiValue(), mediaName) != -1);
            } else if (mediaInfo.getNameMode().getMode().isWildCard()) {
                isMatch &= ModeUtils.isWildCardMatch(mediaInfo.getName(), mediaName);
            } else if (mediaInfo.getNameMode().getMode().isYearly()) {
                isMatch &= ModeUtils.isYearlyMatch(mediaInfo.getName(), mediaName);
            } else if (mediaInfo.getNameMode().getMode().isMonthly()) {
                isMatch &= ModeUtils.isMonthlyMatch(mediaInfo.getName(), mediaName);
            } else {
                throw new UnsupportedOperationException("unsupport mode:" + mediaInfo.getNameMode().getMode());
            }
        }

        return isMatch;
    }

    static class TaskMediaKey {
        private Long taskId;
        private String namespace;
        private String name;

        public TaskMediaKey(Long taskId, String namespace, String name) {
            this.taskId = taskId;
            this.namespace = namespace;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TaskMediaKey that = (TaskMediaKey) o;

            if (!taskId.equals(that.taskId)) return false;
            if (!namespace.equals(that.namespace)) return false;
            return name.equals(that.name);

        }

        @Override
        public int hashCode() {
            int result = taskId.hashCode();
            result = 31 * result + namespace.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        public Long getTaskId() {
            return taskId;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getName() {
            return name;
        }
    }

    @Override
    public List<MediaMappingInfo> mappingListsForQueryPage(Long mediaSourceId, Long targetMediaSourceId, Long taskId, String mediaName, String targetMediaName) {
        MediaMappingInfo mappingInfo = new MediaMappingInfo();
        MediaInfo sourceMedia = new MediaInfo();
        sourceMedia.setMediaSourceId(mediaSourceId);
        sourceMedia.setName(mediaName);
        mappingInfo.setSourceMedia(sourceMedia);
        mappingInfo.setTargetMediaSourceId(targetMediaSourceId);
        mappingInfo.setTaskId(taskId);
        mappingInfo.setTargetMediaName(targetMediaName);
        List<MediaMappingInfo> result = mediaDAO.mappingListsForQueryPage(mappingInfo);
        return result == null ? Lists.newArrayList() : result;
    }

    @Override
    public Integer mappingCount() {
        return mediaDAO.mappingCount();
    }

    @Override
    public List<StatisDetail> getCountByType() {
        return mediaDAO.getCountByType();
    }

    @Override
    public List<Long> findTaskIdsByMediaSourceId(Long mediaSourceId) {
        return mediaDAO.findTaskIdsByMediaSourceId(mediaSourceId);
    }

    @Override
    public void cleanTableMapping(Long taskId) throws Exception {
        EventBus eventBus = EventBusFactory.getEventBus();
        MediaMappingChangeEvent event = new MediaMappingChangeEvent(new FutureCallback(), taskId);
        eventBus.post(event);
        event.getCallback().get();
    }

    private void checkConsistency(List<MediaInfo> mediaList, List<MediaMappingInfo> mediaMappingList) throws Exception {
        for (int i = 0; i < mediaList.size(); i++) {
            Long srcMediaSourceId = mediaList.get(i).getMediaSourceId();
            MediaSourceInfo srcMediaSourceInfo = mediaSourceDAO.getById(srcMediaSourceId);
            MediaSourceType srcMediaSourceType = srcMediaSourceInfo.getParameterObj().getMediaSourceType();
            String sourceMediaName = mediaList.get(i).getName();
            Long targerMediaSourceId = mediaMappingList.get(i).getTargetMediaSourceId();
            MediaSourceInfo targetMediaSourceInfo = mediaSourceDAO.getById(targerMediaSourceId);
            MediaSourceType targetMediaSourceType = targetMediaSourceInfo.getParameterObj().getMediaSourceType();
            String targetMediaName = mediaMappingList.get(i).getTargetMediaName();
            ColumnMappingMode columnMappingMode = mediaMappingList.get(i).getColumnMappingMode();
            Long interceptorId = mediaMappingList.get(i).getInterceptorId();
            if (!sourceMediaName.equals("(.*)")) {
                if ((srcMediaSourceType.isRdbms() || (srcMediaSourceType == MediaSourceType.VIRTUAL && srcMediaSourceInfo.getSimulateMsType() == MediaSourceType.MYSQL))
                        && (targetMediaSourceType.isRdbms() || (targetMediaSourceType == MediaSourceType.VIRTUAL && targetMediaSourceInfo.getSimulateMsType() == MediaSourceType.MYSQL))
                        && columnMappingMode == ColumnMappingMode.NONE && interceptorId == null) {
                    if (srcMediaSourceType == MediaSourceType.VIRTUAL) {
                        srcMediaSourceInfo = getRealDataSource(srcMediaSourceInfo);
                    }
                    if (targetMediaSourceType == MediaSourceType.VIRTUAL) {
                        targetMediaSourceInfo = getRealDataSource(targetMediaSourceInfo);
                    }
                    List<ColumnMeta> sourceMediaColumns = MetaManager.getColumns(srcMediaSourceInfo, sourceMediaName);
                    List<ColumnMeta> targetMediaColumns = MetaManager.getColumns(targetMediaSourceInfo, targetMediaName);
                    int count = 0;
                    if (sourceMediaColumns.size() <= targetMediaColumns.size()) {
                        for (ColumnMeta srcColumn : sourceMediaColumns) {
                            for (ColumnMeta targetColumn : targetMediaColumns) {
                                if (srcColumn.getName().equalsIgnoreCase(targetColumn.getName())) {
                                    count++;
                                    break;
                                }
                            }
                        }
                        if (count != sourceMediaColumns.size()) {
                            throw new ValidationException(String.format("Column name of source media [%s] and target media [%s] are not consistent.", sourceMediaName, targetMediaName));
                        }
                    } else {
                        throw new ValidationException(String.format("Column number of source media [%s] is more than target media [%s].", sourceMediaName, targetMediaName));
                    }
                }
            }
        }
    }

    /**
     * 获取真实数据源
     * <p>
     * 优先取Task所属机房对应的数据源，没有的话再取中心机房的数据源
     *
     * @return
     */
    /*private MediaSourceInfo doGetRealDataSource(String labName, MediaSourceInfo sourceInfo) {

        if (!MediaSourceType.VIRTUAL.equals(sourceInfo.getType())) {
            return sourceInfo;
        }

        //取中心机房的数据源
        LabInfo labInfo = labDAO.getLabByName(labName);
        Long labId = labInfo.getId();
        List<MediaSourceInfo> list = mediaSourceDAO.findRealListByVirtualMsId(sourceInfo.getId());
        MediaSourceInfo mediaSourceInfo = null;
        for (MediaSourceInfo info : list) {
            if (info.getLabId() == labId) {
                mediaSourceInfo = info;
                break;
            }
        }
        return mediaSourceInfo;
    }*/

    /**
     * 获取真实数据源
     * <p>
     * 优先取Task所属机房对应的数据源，没有的话再取中心机房的数据源
     *
     * @param taskId
     * @return
     */
    private MediaSourceInfo doGetRealDataSourceSpecial(Long taskId, MediaSourceInfo sourceInfo) {

        if (!MediaSourceType.VIRTUAL.equals(sourceInfo.getType())) {
            return sourceInfo;
        }

        TaskInfo taskInfo = taskDAO.findById(taskId);
        Long labId = 0L;
        if (taskInfo.getLabId() != null) {
            labId = taskInfo.getLabId();
        }
        //取中心机房的数据源
        else {
            String labName = doubleCenterService.getCenterLab(sourceInfo.getId());
            LabInfo labInfo = labDAO.getLabByName(labName);
            labId = labInfo.getId();
        }
        List<MediaSourceInfo> list = mediaSourceDAO.findRealListByVirtualMsId(sourceInfo.getId());
        MediaSourceInfo mediaSourceInfo = null;
        for (MediaSourceInfo info : list) {
            if (info.getLabId().equals(labId)) {
                mediaSourceInfo = info;
                break;
            }
        }
        return mediaSourceInfo;
    }

    /**
     *查询虚拟数据源对应的真实数据源
     *
     * @param mediaSourceInfo
     * @return
     */
    @Override
    public MediaSourceInfo getRealDataSource(MediaSourceInfo mediaSourceInfo) {

        if (!MediaSourceType.VIRTUAL.equals(mediaSourceInfo.getType())) {
            return mediaSourceInfo;
        }

        DoubleCenterService doubleCenterService = DataLinkFactory.getObject(DoubleCenterService.class);
        String labName = doubleCenterService.getCenterLab(mediaSourceInfo.getId());

        //取中心机房对应的数据源
        Long labId = DataLinkFactory.getObject(LabService.class).getLabByName(labName).getId();
        List<MediaSourceInfo> list = DataLinkFactory.getObject(MediaSourceService.class).findRealListByVirtualMsId(mediaSourceInfo.getId());
        for (MediaSourceInfo info : list) {
            if (info.getLabId().longValue() == labId.longValue()) {
                return info;
            }
        }
        return null;
    }

    /**
     *查询虚拟数据源对应的真实数据源(专门为es任务提供)
     *
     * @return
     */
    public MediaSourceInfo getRealDataSourceSpecial(Long taskId, MediaSourceInfo sourceInfo) {
        return mediaSourceInfoCache.getUnchecked(new LoadingKey(sourceInfo, taskId));
    }

    @Override
    public List<MediaSourceInfo> listRealMediaSourceInfos(MediaSourceInfo mediaSourceInfo) {
        List<MediaSourceInfo> mediaSourceInfoList = Lists.newArrayList();
        MediaSourceService mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        if (mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)) {
            //sqlserver需要取中心机房的ip,因为双机房只有其中一个能连通。
            if(mediaSourceInfo.getSimulateMsType().equals(MediaSourceType.SQLSERVER)){
                mediaSourceInfoList.add(getRealDataSource(mediaSourceInfo));
            }else {
                mediaSourceInfoList = mediaSourceService.findRealListByVirtualMsId(mediaSourceInfo.getId());
            }
        }else if(mediaSourceInfo.getType().equals(MediaSourceType.SDDL)){
            SddlMediaSrcParameter sddlParameter = mediaSourceInfo.getParameterObj();
            mediaSourceInfoList.add(mediaSourceService.getById(sddlParameter.getProxyDbId()));
        }else {
            mediaSourceInfoList.add(mediaSourceInfo);
        }
        return mediaSourceInfoList;
    }


    @Override
    public List<MediaSourceInfo> buildZkMediaSources(String zkServer) {
        List<MediaSourceInfo> list = getMediaSourcesByTypes(MediaSourceType.ZOOKEEPER);
        return list == null ?
                Lists.newArrayList() :
                list.stream().
                        filter(i -> ((ZkMediaSrcParameter) i.getParameterObj()).getServers().equals(zkServer))
                        .collect(Collectors.toList());
    }

    @Override
    public List<TaskMediaNameVo> getMediaNamesByTaskId(List<Long> taskIdList) {
        if(taskIdList.size() >0 ) {
            return mediaDAO.getTaskMediaNamesByTaskId(taskIdList);
        }
        return new ArrayList<>();
    }

    @Override
    public TaskMediaNameVo findSourceTableInfoByMappingId(Long mappingId) {
        return mediaDAO.findSourceTableInfoByMappingId(mappingId);
    }

    @Override
    public List<MediaMappingInfo> getMappingsByTargetMediaNameAndNamespace(Long targetMediaSourceId, String targetNamespace, String targetTableName) {
        return mediaDAO.getMappingsByTargetMediaNameAndNamespace(targetMediaSourceId,targetNamespace,targetTableName);
    }

    @Override
    public List<MediaMappingInfo> getMappingsByMediaSourceIdAndTargetTable(Long srcMediaSourceId, Long targetMediaSourceId, String targetTableName) {
        return mediaDAO.getMappingsByMediaSourceIdAndTargetTable(srcMediaSourceId,targetMediaSourceId,targetTableName);
    }

    @Override
    public List<MediaMappingInfo> getAllMediaMappingsByTaskId(Long taskId) {
        return mediaDAO.findMediaMappingsByTaskId(taskId);
    }

    private static class LoadingKey {
        private MediaSourceInfo mediaSourceInfo;
        private Long taskId;

        public LoadingKey(MediaSourceInfo mediaSourceInfo, Long taskId) {
            this.mediaSourceInfo = mediaSourceInfo;
            this.taskId = taskId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LoadingKey)) return false;

            LoadingKey that = (LoadingKey) o;

            return mediaSourceInfo.equals(that.mediaSourceInfo) && taskId.equals(that.taskId);
        }

        @Override
        public int hashCode() {
            int result = mediaSourceInfo.hashCode();
            result = 31 * result + taskId.hashCode();
            return result;
        }

    }

    @Override
    public List<Long> findTaskIdListByMediaSourceList(List<Long> mediaSourceIdList){
        return mediaDAO.findTaskIdListByMediaSourceList(mediaSourceIdList);
    }

}
