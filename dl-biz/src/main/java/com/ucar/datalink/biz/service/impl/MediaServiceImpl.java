package com.ucar.datalink.biz.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.dal.MediaSourceDAO;
import com.ucar.datalink.biz.dal.TaskDAO;
import com.ucar.datalink.biz.meta.MetaManager;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.task.TaskInfo;
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
import java.util.stream.Collectors;

/**
 * Created by user on 2017/3/16.
 */
@Service
public class MediaServiceImpl implements MediaService {

    private static final Logger logger = LoggerFactory.getLogger(MediaServiceImpl.class);

    private LoadingCache<Long, List<MediaMappingInfo>> mediaMappingsCache;

    private LoadingCache<TaskMediaKey, List<MediaMappingInfo>> taskMediaMappingsCache;

    @Autowired
    MediaDAO mediaDAO;

    @Autowired
    MediaSourceDAO mediaSourceDAO;

    @Autowired
    TaskDAO taskDAO;

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
                        .map(m ->
                                {
                                    if (m.getValue().size() > 1) {
                                        //如果有多个，说明既有通配符配置，又有Single配置，保留Single配置，重载掉通配符配置
                                        return m.getValue().stream().filter(i -> i.getSourceMedia().getNameMode().getMode().isSingle()).findFirst().get();
                                    } else {
                                        return m.getValue().get(0);
                                    }
                                }
                        )
                        .collect(Collectors.toList());
            }
        });
    }

    @Override
    @Transactional
    public void insert(List<MediaInfo> mediaList, List<MediaMappingInfo> mediaMappingList, List<MediaColumnMappingInfo> mediaColumnMappingList) throws Exception {
        checkConsistency(mediaList, mediaMappingList);
        Long mediaId = null;
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
            mediaMappingList.get(i).setSourceMediaId(mediaId);
            mediaDAO.mediaMappingInsert(mediaMappingList.get(i));

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
    }

    @Override
    @Transactional
    public void delete(long id) {
        mediaDAO.deleteMediaMappingById(id);
        mediaDAO.deleteMediaMappingColumnByMappingId(id);
    }

    @Override
    public List<MediaMappingInfo> findMediaMappingsByTaskIdAndTargetMediaSourceId(Map<String, Object> mapParam) {
        return mediaDAO.findMediaMappingsByTaskIdAndTargetMediaSourceId(mapParam);
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
                        targetSourceTypes.contains(m.getTargetMediaSource().getType())
                                && (!justValid || m.isValid()))
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
    public List<MediaMappingInfo> mappingListsForQueryPage(Long mediaSourceId, Long targetMediaSourceId, Long taskId, String mediaName) {
        MediaMappingInfo mappingInfo = new MediaMappingInfo();
        MediaInfo sourceMedia = new MediaInfo();
        sourceMedia.setMediaSourceId(mediaSourceId);
        sourceMedia.setName(mediaName);
        mappingInfo.setSourceMedia(sourceMedia);
        mappingInfo.setTargetMediaSourceId(targetMediaSourceId);
        mappingInfo.setTaskId(taskId);
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
                if ((srcMediaSourceType == MediaSourceType.MYSQL || srcMediaSourceType == MediaSourceType.SQLSERVER)
                        && (targetMediaSourceType == MediaSourceType.MYSQL || targetMediaSourceType == MediaSourceType.SQLSERVER || targetMediaSourceType == MediaSourceType.POSTGRESQL)
                        && columnMappingMode == ColumnMappingMode.NONE && interceptorId == null) {
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

}
