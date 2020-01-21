package com.ucar.datalink.biz.utils;

import com.ucar.datalink.biz.utils.ddl.DdlUtils;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameterFactory;
import com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/3/17.
 */
public class DataSourceFactoryTest {
    @Test
    public void testGetDataSource() throws Exception{
        RdbMediaSrcParameter parameter = MediaSrcParameterFactory.create(MediaSourceType.MYSQL);
        parameter.setNamespace("test");
        RdbMediaSrcParameter.WriteConfig wc = new RdbMediaSrcParameter.WriteConfig();
        wc.setUsername("root");
        wc.setPassword("123456");
        wc.setWriteHost("localhost");
        wc.setEncryptPassword("123456");
        parameter.setWriteConfig(wc);

        RdbMediaSrcParameter.ReadConfig rc = new RdbMediaSrcParameter.ReadConfig();
        List<String> list = new ArrayList<>();
        list.add("localhost");
        rc.setHosts(list);
        rc.setUsername("root");
        rc.setPassword("123456");
        rc.setEncryptPassword("123456");
        parameter.setReadConfig(rc);


        DataSource ds = DataSourceFactory.createTomcatDataSource(parameter, new BasicDataSourceConfig());
        //DataSource ds = DataSourceFactory.getDataSource(sourceMediaSource);
        //ds.getConnection();
        //System.out.println("finished.");

        //DataSource dataSource = DataSourceFactory.getDataSource(sourceMediaSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        List<Table> tables = DdlUtils.findTables(jdbcTemplate, "test", "test", "%", null, null);
        for(Table t : tables) {
            System.out.println(t.toString());

        }

        Table table = DdlUtils.findTable(jdbcTemplate, "test", "test", "aa");
        Column[] columns = table.getColumns();
        for(Column c : columns) {
            System.out.println(c.toString());
        }
    }
}
