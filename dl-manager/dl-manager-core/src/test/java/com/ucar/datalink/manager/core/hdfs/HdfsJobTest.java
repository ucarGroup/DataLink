package com.ucar.datalink.manager.core.hdfs;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.utils.HttpUtils;
import org.junit.Test;

import java.nio.channels.Selector;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class HdfsJobTest {

   //private static final String URL_PREFIX = "http://127.0.0.1";

    private static final String URL_PREFIX = "http://datalinkmanagertest.10101111.com";

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
        parameterMap.put("dbType","mysql");
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
        parameterMap.put("dbName","ucar_datalink");
        parameterMap.put("isOnlyWildcard","0");
        parameterMap.put("wildcardTable","data_router_pay_order_detail_${yyyyMM}");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testGetTableInfo(){
        String path = "/metaData/tableInfo";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("dbType","mysql");
        parameterMap.put("dbName","ucar_datalink");
        parameterMap.put("tableName","t_dl_task");
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
    public void testCreateJob(){
        String path = "/jobServer/createJob";
        Map<String,Object> parameterMap = new HashMap<>(8);
/*        parameterMap.put("srcDbId","477");
        parameterMap.put("destDbType","hdfs");
        parameterMap.put("destDbName","hdfs_lucky");
        parameterMap.put("table","t_dl_test_source_2");
        parameterMap.put("timingYn","0");
        parameterMap.put("timingTransferType","FULL");
        parameterMap.put("source","lucky");
        parameterMap.put("columns","[{\"name\": \"source_id\"},{\"name\": \"name\" }]");
        parameterMap.put("where","create_time > '$DATAX_LAST_EXECUTE_TIME'");*/

        parameterMap.put("srcDbId","206");
        parameterMap.put("destDbType","elasticsearch");
        parameterMap.put("destDbName","es_ycc");
        parameterMap.put("table","t_oss_operation_task_[0000-0031]");
        parameterMap.put("tarTableName","ycc_order.ycc_order");
        //parameterMap.put("timingYn","0");
        //parameterMap.put("timingTransferType","INCREMENT");
        //parameterMap.put("where","xtime > '$DATAX_LAST_EXECUTE_TIME'");
        parameterMap.put("source","lucky");

/*        parameterMap.put("srcDbId","476");
        parameterMap.put("destDbType","MYSQL");
        parameterMap.put("destDbName","myTestLocal");
        parameterMap.put("table","radar_result_info_201805");
        parameterMap.put("timingYn","0");
        parameterMap.put("timingTransferType","FULL");
        parameterMap.put("source","lucky");*/
/*        parameterMap.put("srcDbId","594");
        parameterMap.put("destDbType","hdfs");
        parameterMap.put("destDbName","hdfs_lucky");
        parameterMap.put("table","DEALER");
        parameterMap.put("timingYn","0");
        parameterMap.put("timingTransferType","FULL");
        parameterMap.put("source","ucar");
        parameterMap.put("where","null");*/

       // Map<String,String> extended = new HashMap<String,String>();
        //extended.put("isCleanTarget","true");
        //String extended_msg = JSONObject.toJSON(extended).toString();
        //parameterMap.put("extendedParam",extended_msg);

        //parameterMap.put("hbaseColumns","[{\"name\":\"rowkey\"},{\"name\":\"content_productId\"},{\"name\":\"content_measurements_durationdispense\"},{\"name\":\"content_measurements_temperaturethermoblock\"},{\"name\":\"content_measurements_temperaturehotwater\"},{\"name\":\"content_type\"},{\"name\":\"content_productName\"},{\"name\":\"content_measurements_temperaturesteam\"},{\"name\":\"content_uniqueCode\"},{\"name\":\"content_measurements_durationbrewingprocess\"},{\"name\":\"content_measurements_temperaturemilk2\"},{\"name\":\"content_measurements_temperaturemilk1\"},{\"name\":\"content_createDate\"},{\"name\":\"content_measurements_temperaturecoffeewater\"}]");


       /* parameterMap.put("srcDbId","58");
        parameterMap.put("destDbType","SQLSEVER");
        parameterMap.put("destDbName","uardb");
        parameterMap.put("table","lubiao_test.t_b_city");
        parameterMap.put("timingYn","1");
        parameterMap.put("timingTransferType","FULL");
        //parameterMap.put("where","xtime > '$DATAX_LAST_EXECUTE_TIME'");
        parameterMap.put("source","lucky");*/
        //parameterMap.put("hbaseColumns","[{\"name\":\"rowkey\"},{\"name\":\"base_totalcountafternum\"},{\"name\":\"base_reason\"},{\"name\":\"base_bizno\"},{\"name\":\"base_theorysaleablecountafternum\"},{\"name\":\"base_useablecountafternum\"},{\"name\":\"base_orderid\"},{\"name\":\"base_goodsid\"},{\"name\":\"base_saleablecountadjustnum\"},{\"name\":\"base_theorysaleablecountadjustnum\"},{\"name\":\"base_commodityid\"},{\"name\":\"base_ordercommodityid\"},{\"name\":\"base_reasontype\"},{\"name\":\"base_theoryuseablecountafternum\"},{\"name\":\"base_theorytotalcountafternum\"},{\"name\":\"base_theoryuseablecountadjustnum\"},{\"name\":\"base_useablecountadjustnum\"},{\"name\":\"base_saleablecountafternum\"},{\"name\":\"base_shopid\"},{\"name\":\"base_totalcountadjustnum\"},{\"name\":\"base_reasoncategory\"},{\"name\":\"base_theorytotalcountadjustnum\"},{\"name\":\"base_commodityname\"},{\"name\":\"oper_operatortime\"},{\"name\":\"oper_operatorby\"},{\"name\":\"oper_operatororigin\"},{\"name\":\"oper_remark\"},{\"name\":\"oper_operatortype\"}]");



/*
        parameterMap.put("srcDbId","476");
        parameterMap.put("destDbType","HDFS");
        parameterMap.put("destDbName","hdfs_ucar");
        parameterMap.put("source","ucar");
        parameterMap.put("timingYn","0");
    parameterMap.put("hbaseColumns","[{\"name\":\"classloggerdes_date\"},{\"name\":\"classloggerdes_inserttime\"},{\"name\":\"classloggerdes_parameter\"},{\"name\":\"classloggerdes_linenum\"},{\"name\":\"classloggerdes_methodname\"},{\"name\":\"classloggerdes_serverip\"},{\"name\":\"classloggerdes_classname\"},{\"name\":\"classloggerdes_remark\"},{\"name\":\"classloggerdes_id\"},{\"name\":\"rowkey\"}]");
        parameterMap.put("timingTransferType","FULL");
        parameterMap.put("table","ucardispatch_custom_business_new");
        parameterMap.put("tarTableName","ucardispatch_custom_business_new");
*/

        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testJobInfo(){
        String path = "/jobServer/jobInfo";
        Map<String,Object> parameterMap = new HashMap<>(8);
        //parameterMap.put("jobId","2879");
        parameterMap.put("jobId","10210");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testJobConfig(){
        String path = "/jobServer/jobConfig";
        Map<String,Object> parameterMap = new HashMap<>(8);
        //parameterMap.put("jobId","2879");
        parameterMap.put("jobId","1158");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testStartJob(){
        String path = "/jobServer/start";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("jobId","6572");
        parameterMap.put("jobSignal","a06a7a4eda4cc35ef51317fd6167a27e");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testStartServiecJob(){
        String path = "/jobService/start";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("CONFIG_ID","4833");
        parameterMap.put("JOB_ID_SIGNAL","a06a7a4eda4cc35ef51317fd6167a27e");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testJobState(){
        String path = "/jobServer/state";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("executeId","2515161_test");
        parameterMap.put("jobSignal","14a12e51d5bc87e66349023639562ac3");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testConfigMapping(){
        String path = "/jobServer/configMapping";
        Map<String,Object> parameterMap = new HashMap<>(8);

        parameterMap.put("srcDbId","477");
        parameterMap.put("destDbType","mysql");
        parameterMap.put("destDbName","ucar_datalink_my");
        parameterMap.put("source","lucky");
        parameterMap.put("tableMappings","[{\"t_dl_menu\":\"t_dl_menu\"},{\"t_dl_monitor\":\"t_dl_monitor\"}]");
        //parameterMap.put("resetTime","1561132800000");

  /*      parameterMap.put("srcDbId","471");
        parameterMap.put("destDbType","MYSQL");
        parameterMap.put("destDbName","ucar_datalink_A");
        parameterMap.put("source","lucky");
        parameterMap.put("tableMappings","[{\"t_dl_test_source\":\"t_dl_test_source\"}]");*/
/*
        parameterMap.put("srcDbId","114");
        parameterMap.put("destDbType","HDFS");
        parameterMap.put("destDbName","hdfs_lucky");
        parameterMap.put("source","lucky");
        parameterMap.put("fileSplitMode","HOUR");
        parameterMap.put("schema","test");
        parameterMap.put("tableMappings","[{\"data_router_add_to_shopping_cart_${yyyyMM}\":\"data_router_add_to_shopping_cart\"}]");
*/
/*        parameterMap.put("srcDbId","500");
        parameterMap.put("destDbType","MYSQL");
        parameterMap.put("destDbName","myTestLocal");
        parameterMap.put("source","lucky");
        parameterMap.put("tableMappings","[{\"t_dl_test_source\":\"t_dl_test_source_2\"}]");*/

      /*  parameterMap.put("srcDbId","230");
        parameterMap.put("destDbType","HDFS");
        parameterMap.put("destDbName","hdfs_lucky");
        parameterMap.put("source","lucky");
        parameterMap.put("tableMappings","[{\"qa_auto_ddl_split_02\":\"qa_auto_ddl_split\"}]");
        parameterMap.put("fileSplitMode","DAY");*/


//        parameterMap.put("srcDbId","206");
//        parameterMap.put("destDbType","hdfs");
//        parameterMap.put("destDbName","hdfs_ucar");
//        parameterMap.put("source","ucar");
//        parameterMap.put("tableMappings","[{\"t_oss_operation_task_[0000-0031]\":\"t_oss_vehicle_oil_034dev\"}]");
//        parameterMap.put("fileSplitMode","DAY");
        //parameterMap.put("resetTime","1561132800000");

        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testStartTask(){
        String path = "/jobServer/startTask";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("taskId","321");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void hiveTest(){
        JSONObject params = new JSONObject();
        params.put("sql","alter table t_ehr_oa_emp_schedule add schedule_hours varchar(5) DEFAULT '0' COMMENT '排班工时',add data_source tinyint(1) DEFAULT NULL COMMENT '数据来源 1|手工排班 2|AI智能排班 3|导入 4|补卡申请");
        params.put("jobNum","test-lucky-53174");
        params.put("dbName","lucky_ehr");
        String result = HttpUtils.doPost("http://luckyseamantest.luckincoffee.com/luckyseaman/schema/ddlSubmit", params.toString());
        System.out.println(result);
    }

    @Test
    public void testMappingId(){
        String path = "/jobServer/mapping";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("mappingId","1984");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testGetMappingId(){
        String path = "/jobServer/getMappingId";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("targetMediaSourceName","ucar_datalink_my");
        parameterMap.put("targetNamespace","ucar_datalink_bbb");
        parameterMap.put("targetTableName","t_dl_test_source");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }

    @Test
    public void testToHiveType(){
        String path = "/jobServer/transformToHiveType";
        Map<String,Object> parameterMap = new HashMap<>(8);
        parameterMap.put("sourceType","MYSQL");
        parameterMap.put("columnType","varchar");
        String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        System.out.println(result);
    }


    @Test
    public void testCreateJob_2(){
        String path = "/jobServer/createJob";
        Map<String,Object> parameterMap = new HashMap<>(8);

        parameterMap.put("timingTransferType","FULL");
        //parameterMap.put("srcDbId","492");
        parameterMap.put("srcDbId","97");
        //parameterMap.put("destDbName","BI_COMMON");
        parameterMap.put("destDbName","ucar_dspider");
        //parameterMap.put("tarTableName","123");
        parameterMap.put("tarTableName","alliance_driver_stat_hour_api_1");
        parameterMap.put("destDbType","SQLSERVER");
        parameterMap.put("source","ucar");
        parameterMap.put("timingYn","1");
        //parameterMap.put("table","test.test11");
        parameterMap.put("table","bi_ucar.alliance_driver_stat_hour_api");

        Map<String,String> extended = new HashMap<String,String>();
        extended.put("isCleanTarget","false");
        //extended.put("channel","3");
        String extended_msg = JSONObject.toJSON(extended).toString();
        parameterMap.put("extendedParam",extended_msg);

        String url = "http://datalinkmanagertest.10101111.com";
        //String result = HttpUtils.doPost(URL_PREFIX+path,parameterMap);
        String result = HttpUtils.doPost(url+path,parameterMap);
        System.out.println(result);
    }






    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time  = sdf.parse("2019-06-22 00:00:00").getTime();
        System.out.println(time);
    }
}
