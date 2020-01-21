package com.ucar.datalink.biz.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.helper.TestHelper;
import com.ucar.datalink.biz.utils.DataSourceFactory;
import com.ucar.datalink.biz.utils.ddl.DdlUtils;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.domain.statis.StatisDetail;
import com.ucar.datalink.domain.vo.TaskMediaNameVo;
import org.apache.ddlutils.model.Table;
import org.apache.ibatis.annotations.Param;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2017/3/20.
 */
public class MediaServiceTest {

    private MediaDAO mediaDAO;

    private MediaServiceImpl mediaService;

    @Before
    public void before() {
        mediaService = new MediaServiceImpl();
        mediaService.mediaDAO = mediaDAO;
    }

    @Test
    public void stressTestGetMediaMappingsByMediaAndTarget() {

        mediaService.mediaDAO = new MediaDAO() {
            @Override
            public List<MediaMappingInfo> findMediaMappingsByTaskId(Long taskId) {
                ArrayList<MediaMappingInfo> list = new ArrayList<>();
                for (int i = 0; i < 1000; i++) {
                    list.add(buildMapping(1L));
                }
                return list;
            }

            @Override
            public MediaSourceInfo findMediaSourceById(Long id) {
                return null;
            }

            @Override
            public void updateMediaColumnMapping(MediaColumnMappingInfo mediaColumnMappingInfo) {

            }

            @Override
            public void updateMediaMapping(MediaMappingInfo mediaMappingInfo) {

            }

            @Override
            public long mediaInsert(MediaInfo mediaInfo) {
                return 0;
            }

            @Override
            public long mediaMappingInsert(MediaMappingInfo mediaMappingInfo) {
                return 0;
            }

            @Override
            public long mediaColumnInsert(MediaColumnMappingInfo mediaColumnMappingInfo) {
                return 0;
            }

            @Override
            public int deleteMediaById(long id) {
                return 0;
            }

            @Override
            public int deleteMediaByMediaSourceId(Long mediaSourceId) {
                return 0;
            }

            @Override
            public MediaInfo getMediaByMediaSourceAndMediaName(Map<String, Object> mapPram) {
                return null;
            }

            @Override
            public int deleteMediaMappingById(long id) {
                return 0;
            }

            @Override
            public int deleteMediaMappingColumnByMappingId(long id) {
                return 0;
            }

            @Override
            public int deleteMediaMappingByTaskId(Long taskId) {
                return 0;
            }

            @Override
            public int deleteMediaMappingColumnByTaskId(Long taskId) {
                return 0;
            }

            @Override
            public MediaMappingInfo findMediaMappingsById(long id) {
                return null;
            }

            @Override
            public MediaInfo findMediaById(long id) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMediaMappingsBySrcMediaSourceId(Long srcMediaSourceId) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMediaMappingsByTargetMediaSourceId(Long targetMediaSourceId) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMediaMappingsByTaskIdAndTargetMediaSourceId(Map<String, Object> mapParam) {
                return null;
            }

            @Override
            public List<MediaColumnMappingInfo> findMediaColumnByMappingId(long id) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> getAllMediaMappings() {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMediaMappingsByInterceptorId(Long interceptorId) {
                return null;
            }

            @Override
            public List<MediaSourceInfo> findMediaSourcesByTypes(MediaSourceType... types) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> mappingListsForQueryPage(MediaMappingInfo mediaMappingInfo) {
                return null;
            }

            @Override
            public Integer mappingCount() {
                return null;
            }

            @Override
            public List<StatisDetail> getCountByType() {
                return null;
            }

            @Override
            public List<Long> findTaskIdsByMediaSourceId(Long mediaSourceId) {
                return null;
            }

            @Override
            public List<MediaSourceInfo> findMediaSourcesForSingleLab(List<MediaSourceType> types) {
                return null;
            }

            @Override
            public List<MediaSourceInfo> findMediaSourcesForAcrossLab(Long sourceLabId, List<MediaSourceType> types) {
                return null;
            }

            @Override
            public List<MediaSourceInfo> findMediaSourcesForAllAcrossLab(@Param("types") List<MediaSourceType> types) {
                return null;
            }

            @Override
            public Integer updateMedia(MediaInfo mediaInfo) {
                return null;
            }

            @Override
            public Integer updateTargetMediaSource(MediaMappingInfo mediaMappingInfo) {
                return null;
            }

            @Override
            public List<MediaInfo> findMediaByMediaSourceId(Long mediaSourceId) {
                return null;
            }

            @Override
            public List<Long> findTaskIdListByMediaSourceList(List<Long> mediaSourceIdList) {
                return null;
            }

            @Override
            public MediaMappingInfo getMediaMappingOfSpecial(String tableName, Long mediaSourceId) {
                return null;
            }

            @Override
            public List<TaskMediaNameVo> getTaskMediaNamesByTaskId(List<Long> taskIdList) {
                return null;
            }

            @Override
            public TaskMediaNameVo findSourceTableInfoByMappingId(Long mappingId) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMappingListByCondition(MediaMappingInfo mediaMappingInfo) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> getMappingsByTargetMediaNameAndNamespace(Long targetMediaSourceId, String targetNamespace, String targetTableName) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> getMappingsByMediaSourceIdAndTargetTable(Long srcMediaSourceId, Long targetMediaSourceId, String targetTableName) {
                return null;
            }

            @Override
            public MediaMappingInfo findMediaMappingByJoinIndex(MediaMappingInfo mediaMappingInfo) {
                return null;
            }


        };
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            mediaService.getMediaMappingsByMediaAndTarget(1L, "namespace", "name", Sets.newHashSet(MediaSourceType.HBASE), true);
        }
        long end = System.currentTimeMillis();
        System.out.print(end - start);
    }

    @Test
    public void testGetMediaMappingsByTask() {
        List<MediaMappingInfo> list1 = mediaService.getMediaMappingsByTask(1L, true);
        Assert.assertEquals(5, list1.size());

        List<MediaMappingInfo> list2 = mediaService.getMediaMappingsByTask(1L, false);
        Assert.assertEquals(7, list2.size());

        List<MediaMappingInfo> list3 = mediaService.getMediaMappingsByTask(2L, false);
        Assert.assertEquals(0, list3.size());
    }

    @Test
    public void testGetMediaMappingsByMediaAndTarget() {
        mediaService.mediaDAO = mediaDAO = new MediaDAO() {

            @Override
            public long mediaInsert(MediaInfo mediaInfo) {
                return 0;
            }

            @Override
            public long mediaMappingInsert(MediaMappingInfo mediaMappingInfo) {
                return 0;
            }

            @Override
            public long mediaColumnInsert(MediaColumnMappingInfo mediaColumnMappingInfo) {
                return 0;
            }

            @Override
            public int deleteMediaById(long id) {
                return 0;
            }

            @Override
            public int deleteMediaByMediaSourceId(Long mediaSourceId) {
                return 0;
            }

            @Override
            public MediaInfo getMediaByMediaSourceAndMediaName(Map<String, Object> mapPram) {
                return null;
            }

            @Override
            public int deleteMediaMappingById(long id) {
                return 0;
            }

            @Override
            public int deleteMediaMappingColumnByMappingId(long id) {
                return 0;
            }

            @Override
            public int deleteMediaMappingByTaskId(Long taskId) {
                return 0;
            }

            @Override
            public int deleteMediaMappingColumnByTaskId(Long taskId) {
                return 0;
            }

            @Override
            public MediaMappingInfo findMediaMappingsById(long id) {
                return null;
            }

            @Override
            public MediaInfo findMediaById(long id) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMediaMappingsBySrcMediaSourceId(Long srcMediaSourceId) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMediaMappingsByTargetMediaSourceId(Long targetMediaSourceId) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMediaMappingsByTaskIdAndTargetMediaSourceId(Map<String, Object> mapParam) {
                return null;
            }

            @Override
            public List<MediaColumnMappingInfo> findMediaColumnByMappingId(long id) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> getAllMediaMappings() {
                return null;
            }

            @Override
            public List<MediaSourceInfo> findMediaSourcesByTypes(MediaSourceType... types) {
                return null;
            }

            @Override
            public MediaSourceInfo findMediaSourceById(Long id) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMediaMappingsByInterceptorId(Long interceptorId) {
                return null;
            }

            @Override
            public void updateMediaColumnMapping(MediaColumnMappingInfo mediaColumnMappingInfo) {

            }

            @Override
            public void updateMediaMapping(MediaMappingInfo mediaMappingInfo) {

            }

            @Override
            public List<MediaMappingInfo> findMediaMappingsByTaskId(Long taskId) {
                if (taskId == 1L) {
                    List<MediaMappingInfo> mappingInfos = new ArrayList<>();

                    MediaMappingInfo m1 = buildMapping(1L);
                    mappingInfos.add(m1);

                    MediaMappingInfo m2 = buildMapping(2L);
                    m2.setValid(false);
                    mappingInfos.add(m2);

                    MediaMappingInfo m3 = buildMapping(3L);
                    m3.getTargetMediaSource().setType(MediaSourceType.HDFS);
                    mappingInfos.add(m3);

                    MediaMappingInfo m4 = buildMapping(4L);
                    m4.getSourceMedia().setName(".*");
                    mappingInfos.add(m4);

                    return mappingInfos;
                } else {
                    return Lists.newArrayList();
                }
            }

            @Override
            public List<MediaMappingInfo> mappingListsForQueryPage(MediaMappingInfo mediaMappingInfo) {
                return null;
            }

            @Override
            public Integer mappingCount() {
                return null;
            }

            @Override
            public List<StatisDetail> getCountByType() {
                return null;
            }

            @Override
            public List<Long> findTaskIdsByMediaSourceId(Long mediaSourceId) {
                return null;
            }

            @Override
            public List<MediaSourceInfo> findMediaSourcesForSingleLab(List<MediaSourceType> types) {
                return null;
            }

            @Override
            public List<MediaSourceInfo> findMediaSourcesForAcrossLab(Long sourceLabId, List<MediaSourceType> types) {
                return null;
            }

            @Override
            public List<MediaSourceInfo> findMediaSourcesForAllAcrossLab(@Param("types") List<MediaSourceType> types) {
                return null;
            }

            @Override
            public Integer updateMedia(MediaInfo mediaInfo) {
                return null;
            }

            @Override
            public Integer updateTargetMediaSource(MediaMappingInfo mediaMappingInfo) {
                return null;
            }

            @Override
            public List<MediaInfo> findMediaByMediaSourceId(Long mediaSourceId) {
                return null;
            }

            @Override
            public List<Long> findTaskIdListByMediaSourceList(List<Long> mediaSourceIdList) {
                return null;
            }
            @Override
            public MediaMappingInfo getMediaMappingOfSpecial(String tableName, Long mediaSourceId) {
                return null;
            }

            @Override
            public List<TaskMediaNameVo> getTaskMediaNamesByTaskId(List<Long> taskIdList) {
                return null;
            }

            @Override
            public TaskMediaNameVo findSourceTableInfoByMappingId(Long mappingId) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> findMappingListByCondition(MediaMappingInfo mediaMappingInfo) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> getMappingsByTargetMediaNameAndNamespace(Long targetMediaSourceId, String targetNamespace, String targetTableName) {
                return null;
            }

            @Override
            public List<MediaMappingInfo> getMappingsByMediaSourceIdAndTargetTable(Long srcMediaSourceId, Long targetMediaSourceId, String targetTableName) {
                return null;
            }

            @Override
            public MediaMappingInfo findMediaMappingByJoinIndex(MediaMappingInfo mediaMappingInfo) {
                return null;
            }

        };

        List<MediaMappingInfo> list0 = mediaService.getMediaMappingsByMediaAndTarget(1L, "namespace", "name", Sets.newHashSet(MediaSourceType.HBASE), true);
        Assert.assertEquals(1, list0.size());
        Assert.assertEquals(".*", list0.get(0).getSourceMedia().getName());

        List<MediaMappingInfo> list1 = mediaService.getMediaMappingsByMediaAndTarget(1L, "namespace", "name1", Sets.newHashSet(MediaSourceType.HBASE), true);
        Assert.assertEquals(1, list1.size());

        Assert.assertEquals("name1", list1.get(0).getSourceMedia().getName());

        List<MediaMappingInfo> list2 = mediaService.getMediaMappingsByMediaAndTarget(1L, "namespace", "name2", Sets.newHashSet(MediaSourceType.HBASE), true);
        Assert.assertEquals(0, list2.size());

        List<MediaMappingInfo> list3 = mediaService.getMediaMappingsByMediaAndTarget(1L, "namespace", "name2", Sets.newHashSet(MediaSourceType.HBASE), false);
        Assert.assertEquals(1, list3.size());

        List<MediaMappingInfo> list4 = mediaService.getMediaMappingsByMediaAndTarget(1L, "namespace", "name3", Sets.newHashSet(MediaSourceType.HDFS), true);
        Assert.assertEquals(1, list4.size());

        List<MediaMappingInfo> list5 = mediaService.getMediaMappingsByMediaAndTarget(1L, "namespace", "name4", Sets.newHashSet(MediaSourceType.HDFS), true);
        Assert.assertEquals(0, list5.size());

        List<MediaMappingInfo> list6 = mediaService.getMediaMappingsByMediaAndTarget(1L, "namespace", "name3", Sets.newHashSet(MediaSourceType.HBASE, MediaSourceType.HDFS), true);
        Assert.assertEquals(1, list6.size());
    }

    @Test
    public void testCheckMediaColumnMappings() throws Exception {
        MediaServiceImpl mediaService = new MediaServiceImpl();
        DataSource dataSource = DataSourceFactory.getDataSource(TestHelper.buildMediaSource());
        Table table = DdlUtils.findTable(new JdbcTemplate(dataSource), "ucar_datalink", "ucar_datalink", "t_dl_media");

        List<MediaColumnMappingInfo> list = new ArrayList<>();
        MediaColumnMappingInfo m1 = new MediaColumnMappingInfo();
        m1.setSourceColumn("media_namespace");
        list.add(m1);
        MediaColumnMappingInfo m2 = new MediaColumnMappingInfo();
        m2.setSourceColumn("media_name");
        list.add(m2);

        try {
            mediaService.checkMediaColumnMappings(table, list, ColumnMappingMode.INCLUDE);
            assert false;
        } catch (ValidationException e) {
            assert true;
        }

        MediaColumnMappingInfo m3 = new MediaColumnMappingInfo();
        m3.setSourceColumn("media_source_id");
        list.add(m3);
        MediaColumnMappingInfo m4 = new MediaColumnMappingInfo();
        m4.setSourceColumn("id");
        list.add(m4);

        mediaService.checkMediaColumnMappings(table, list, ColumnMappingMode.INCLUDE);

        try {
            mediaService.checkMediaColumnMappings(table, list, ColumnMappingMode.EXCLUDE);
            assert false;
        } catch (ValidationException e) {
            assert true;
        }

        list.clear();
        mediaService.checkMediaColumnMappings(table, list, ColumnMappingMode.EXCLUDE);
    }

    private MediaMappingInfo buildMapping(Long mediaId) {
        MediaMappingInfo mappingInfo = new MediaMappingInfo();
        mappingInfo.setTaskId(1L);
        mappingInfo.setValid(true);
        mappingInfo.setSourceMediaId(mediaId);
        mappingInfo.setTargetMediaSourceId(1L);

        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.setId(mediaId);
        mediaInfo.setNamespace("namespace");
        mediaInfo.setName("name" + mediaId);
        mappingInfo.setSourceMedia(mediaInfo);

        MediaSourceInfo sourceInfo = new MediaSourceInfo();
        sourceInfo.setId(1L);
        sourceInfo.setType(MediaSourceType.HBASE);
        mappingInfo.setTargetMediaSource(sourceInfo);

        return mappingInfo;
    }
}
