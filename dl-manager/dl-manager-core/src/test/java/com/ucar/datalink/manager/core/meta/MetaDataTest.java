package com.ucar.datalink.manager.core.meta;

import com.ucar.datalink.common.utils.HttpUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Demo class
 *
 * @author Administrator
 * @date 2019/8/5
 */
public class MetaDataTest {
    private static final String URL_PREFIX = "http://127.0.0.1";

   // private static final String URL_PREFIX = "http://datalinkmanagertest.10101111.com";

    @Test
    public void testGetDBMS(){
        String path = "http://dbmsgrey.10101111.com/dbms_api/dbms_api/login/spec_ips";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("db_name","uc_db");
        parameterMap.put("dbenv_name","prod");
        parameterMap.put("db_type","MySQL");
        String result = HttpUtils.doPost(path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testGetDbs(){
        String path = "/metaData/dbs";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("dbType","kafka");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testExecuteSql(){
        String path = "/jobServer/doSql";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("dbId","477");
        //parameterMap.put("sql","SELECT name,create_time FROM ucar_datalink.t_dl_test_source");
        parameterMap.put("sql","update ucar_datalink.t_dl_test_source set name='56'");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }


    @Test
    public void testGetTables(){
        String path = "/metaData/tables";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("dbType","mysql");
        parameterMap.put("dbName","hbase_ucar");
        parameterMap.put("isOnlyWildcard","0");
        parameterMap.put("wildcardTable","t_api_record_${yyyyMM}");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testGetTableInfo(){
        String path = "/metaData/tableInfo";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("dbType","mysql");
        parameterMap.put("dbName","ucar_driverteam");
        parameterMap.put("tableName","t_dl_task_position");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testGetDestTableInfo(){
        String path = "/metaData/destTableInfo";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("dbType","hbase");
        parameterMap.put("dbName","hbase_ucar");
        parameterMap.put("tableName","t_scd_device_monitor_event_${yyyyMM}");
        parameterMap.put("destDbType","HDFS");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testGetDbInfo(){
        String path = "/metaData/dbInfo";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("dbId","10");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testdbDetail(){
        String path = "/metaData/dbDetail";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("mediaSourceId","453");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testtianhang(){
        String path = "http://wxqy.tianjin-air.com/api/pay/wxpay/orderquery";
        Map<String,Object> parameterMap = new HashMap<>(8);
        //parameterMap.put("reservationCode","20190806142640300000380701");
        parameterMap.put("reservationCode","0000380701");
        String result = HttpUtils.doPost(path,parameterMap);
        System.out.println(result);
    }

}
