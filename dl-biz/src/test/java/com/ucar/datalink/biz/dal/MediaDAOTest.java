package com.ucar.datalink.biz.dal;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 2017/3/16.
 */
public class MediaDAOTest {
    private ApplicationContext context;

    @Before
    public void before() {
        context = new ClassPathXmlApplicationContext("biz/spring/datalink-biz.xml");
    }

    @Test
    public void testMediaDAO() {
        MediaDAO mediaDAO = context.getBean(MediaDAO.class);
        List<MediaMappingInfo> mappingInfo = mediaDAO.findMediaMappingsByTaskId(21L);
        mappingInfo.stream().forEach(t -> System.out.println(t.getSourceMedia()));
    }

    @Test
    public void testPaging() {
        MediaSourceDAO mediaSourceDAO = context.getBean(MediaSourceDAO.class);
        Set<MediaSourceType> setMediaSource = new HashSet<MediaSourceType>();
        setMediaSource.add(MediaSourceType.MYSQL);
        setMediaSource.add(MediaSourceType.SQLSERVER);
        PageHelper.startPage(2, 10);
        List<MediaSourceInfo> mediaSourceList = mediaSourceDAO.getListByType(setMediaSource);
        PageInfo<MediaSourceInfo> pageInfo = new PageInfo<>(mediaSourceList);
        System.out.println(pageInfo);
    }
}
