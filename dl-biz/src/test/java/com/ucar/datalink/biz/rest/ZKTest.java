package com.ucar.datalink.biz.rest;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.meta.RDBMSUtil;
import com.ucar.datalink.common.utils.DbConfigEncryption;
import com.ucar.datalink.common.zookeeper.ZkConfig;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/8/17.
 */
public class ZKTest {

    public static void main(String[] args) throws Exception {
        ZKTest t = new ZKTest();
        //t.go();
        String password = "56ff084e20c678cc72abb548d27622de704260d818d1b91d";
        password = "ucar_dev_soa";
        //String x = DbConfigEncryption.decrypt(password);
        String x = DbConfigEncryption.encrypt(password);

        System.out.println(x);

//        t.go2();
//        t.go3();
        //t.go4();
    }


    public void go4() throws Exception {
        String content = "{\"@type\":\"com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter\"," +
                "\"dataSourceConfig\":{\"@type\":\"com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig\"," +
                "\"initialSize\":1,\"maxActive\":32,\"maxIdle\":32,\"maxWait\":60000," +
                "\"minEvictableIdleTimeMillis\":3000000,\"minIdle\":1,\"numTestsPerEvictionRun\":-1," +
                "\"removeAbandonedTimeout\":300,\"timeBetweenEvictionRunsMillis\":60000},\"desc\":\"ggg\"," +
                "\"driver\":\"com.mysql.jdbc.Driver\",\"encoding\":\"utf-8\",\"mediaSourceType\":\"MYSQL\"," +
                "\"name\":\"test\",\"namespace\":\"test\",\"port\":3306,\"readConfig\":{\"etlHost\":\"10.99.68.165\"" +
                ",\"hosts\":[\"10.99.68.165\",\"10.99.68.165\"],\"password\":\"9aa7ba09c7ddfcb2\",\"username\":\"root\"}" +
                ",\"writeConfig\":{\"password\":\"9aa7ba09c7ddfcb2\",\"username\":\"root\",\"writeHost\":" +
                "\"10.99.68.165\"}}";
        MediaSourceInfo info = new MediaSourceInfo();
        info.setParameter(content);
        info.setId(9528L);
        info.setName("test");
        info.setType(MediaSourceType.MYSQL);

        //PostgreSqlUtil.getColumns(info,"t_order");
        List<ColumnMeta> list = RDBMSUtil.getColumns(info,"t_order");
        for(ColumnMeta cm : list) {
            System.out.println(cm);
        }
    }



    public void go3() throws Exception {
        String content = "{\"@type\":\"com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter\"," +
                "\"dataSourceConfig\":{\"@type\":\"com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig\"," +
                "\"initialSize\":1,\"maxActive\":32,\"maxIdle\":32,\"maxWait\":60000," +
                "\"minEvictableIdleTimeMillis\":3000000,\"minIdle\":1,\"numTestsPerEvictionRun\":-1," +
                "\"removeAbandonedTimeout\":300,\"timeBetweenEvictionRunsMillis\":60000},\"desc\":\"ggg\"," +
                "\"driver\":\"com.mysql.jdbc.Driver\",\"encoding\":\"utf-8\",\"mediaSourceType\":\"MYSQL\"," +
                "\"name\":\"test\",\"namespace\":\"test\",\"port\":3306,\"readConfig\":{\"etlHost\":\"10.99.68.165\"" +
                ",\"hosts\":[\"10.99.68.165\",\"10.99.68.165\"],\"password\":\"9aa7ba09c7ddfcb2\",\"username\":\"root\"}" +
                ",\"writeConfig\":{\"password\":\"9aa7ba09c7ddfcb2\",\"username\":\"root\",\"writeHost\":" +
                "\"10.99.68.165\"}}";
        MediaSourceInfo info = new MediaSourceInfo();
        info.setParameter(content);
        info.setId(9528L);
        info.setName("test");
        info.setType(MediaSourceType.MYSQL);

//        info.setParameter();
//        DataSource ds = DataSourceFactory.getDataSource(info);
//
//        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        //Table table = DdlUtils.findTable(jdbcTemplate, parameter.getNamespace(), parameter.getNamespace(), tableName);

        List<ColumnMeta> list = RDBMSUtil.getColumns(info,"t_order");
        for(ColumnMeta cm : list) {
            System.out.println(cm);
        }

    }

    public void go2() {
        String json = "{}";
        List<MediaMeta> list = null;
        if(json==null || "".equals(json) || "{}".equals(json)) {
            list = new ArrayList<>();
        } else {
            list = JSONObject.parseArray(json, MediaMeta.class);
        }

        System.out.println(list.toArray());
    }

    public void go() {


        ZkConfig zkConfig = new ZkConfig("10.101.22.119:5181", 1000, 1000);
        DLinkZkUtils.init(zkConfig, "datax");
        //ZkUtils.get().createEphemeral("/ctest");
        List<String> list = DLinkZkUtils.get().zkClient().getChildren("/datax/admin/workers");
        for(String s : list) {
            System.out.println(s);
        }
    }


}
