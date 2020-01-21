package com.ucar.datalink.biz.utils;

import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.utils.ddl.DdlUtils;
import com.ucar.datalink.domain.media.MediaInfo;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameterFactory;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/6/23.
 */
public class DataSourceFactoryTest2 {


    public static void main(String[] args)throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("biz/spring/*.xml");
        //DataLinkFactory.init("biz/spring/*.xml");
        MediaDAO dao = context.getBean(MediaDAO.class);
        MediaMappingInfo mappingInfo = dao.findMediaMappingsById(81L);

        //MediaMappingInfo mappingInfo = DataLinkFactory.getObject(MediaDAO.class).findMediaMappingsById(14L);

        MediaInfo sourceMedia = mappingInfo.getSourceMedia();
        MediaSourceInfo sourceMediaSource = sourceMedia.getMediaSource();
//        MediaSourceInfo targetMediaSource = mappingInfo.getTargetMediaSource();

        //sourceMediaSource query
        sourceMediaSource = dao.findMediaSourceById(14L);

        DataSource dataSource = DataSourceFactory.getDataSource(sourceMediaSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        System.out.println("ok~~~");

        String namespace = sourceMediaSource.getParameterObj().getNamespace();
        String tableName = sourceMedia.getName();
        //String tableName =sourceMediaSource.getName();
        //Table table = DdlUtils.findTable(jdbcTemplate, namespace, namespace, tableName);

        List<Table> list = DdlUtils.findTables(jdbcTemplate,namespace,namespace,"%",null,null);
        for(Table t : list) {
            System.out.println(t.toString());

        }

//        System.out.println("table info = "+table.toString());
//        System.out.println("================================");
//
//        if(table != null) {
//            Column[] columns = table.getColumns();
//            for(Column c : columns) {
//                System.out.println(c.toString());
//            }
//        }

    }



    public static void main2(String[] args) {
        RdbMediaSrcParameter parameter = MediaSrcParameterFactory.create(MediaSourceType.MYSQL);
        parameter.setNamespace("test");
        RdbMediaSrcParameter.WriteConfig wc = new RdbMediaSrcParameter.WriteConfig();
        wc.setUsername("root");
        wc.setPassword("123456");
        wc.setWriteHost("localhost");
        parameter.setWriteConfig(wc);

        RdbMediaSrcParameter.ReadConfig rc = new RdbMediaSrcParameter.ReadConfig();
        rc.setUsername("root");
        rc.setPassword("123456");
        List<String> list = new ArrayList<>();
        list.add("localhost:3306");
        rc.setHosts(list);
        parameter.setReadConfig(rc);


        MediaSourceInfo info = new MediaSourceInfo();
        info.setId(50L);
        info.setType(MediaSourceType.MYSQL);


        //DataSource ds = DataSourceFactory.createTomcatDataSource(parameter, new BasicDataSourceConfig());
        //DataSource ds = DataSourceFactory.getDataSource(sourceMediaSource);
        //ds.getConnection();
        //System.out.println("finished.");

        System.out.println(info.toString());
        //info.setParameter();

        DataSource dataSource = DataSourceFactory.getDataSource(info);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        System.out.println("ok~~");

    }


}
