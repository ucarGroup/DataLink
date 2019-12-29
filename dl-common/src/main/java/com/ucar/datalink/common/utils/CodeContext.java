package com.ucar.datalink.common.utils;

import java.util.HashMap;
import java.util.Map;

public class CodeContext {

    private static Map<Integer,String> codeMap = new HashMap<>(256);

    public static final Integer SUCCESS_CODE = 200;

    public static final Integer SQL_VALIDATE_ERROR_CODE = 2001;

    public static final Integer SQL_EXECUTE_ERROR_CODE = 2002;

    public static final Integer SERVER_RUNTIME_ERROR_CODE = 2003;

    public static final Integer RESULT_RETURN_ERROR_CODE = 2004;

    public static final Integer SQL_COUNT_ERROR_CODE = 2005;

    public static final Integer SQL_NOTADD_ERROR_CODE = 2006;

    public static final Integer NOTFIND_COLUMN_ERROR_CODE = 2007;

    public static final Integer NOTFIND_MAPPING_ERROR_CODE = 2008;

    public static final Integer NOTFIND_ESCONFIG_ERROR_CODE = 2009;

    public static final Integer NOTFIND_ADDRESS_ERROR_CODE = 2010;

    public static final Integer MEDIASOURCEID_ISNULL_ERROR_CODE = 2011;

    public static final Integer MAPPINGID_ISNULL_ERROR_CODE = 2012;

    public static final Integer SQL_ISNULL_ERROR_CODE = 2012;

    public static final Integer CLUSTERSTATE_ISNULL_ERROR_CODE = 2013;

    public static final Integer MEMBERINFO_ISNULL_ERROR_CODE = 2014;

    public static final Integer ES_RETURNNULL_ERROR_CODE = 2015;

    public static final Integer ES_EXECUTE_ERROR_CODE = 2016;

    public static final Integer COLUMN_EXISTS_ERROR_CODE = 2017;

    public static final Integer SQLTYPE_NOTSUPPORT_ERROR_CODE = 2018;

    public static final Integer SQL_SYNTAX_ERROR_CODE = 2019;

    public static final Integer JOBNUM_ISNULL_ERROR_CODE = 2020;

    public static final Integer DBNAME_ISNULL_ERROR_CODE = 2021;

    public static final Integer MEDIA_TYPE_ERROR_CODE = 2022;

    public static final Integer HIVE_MODIFY_ERROR_CODE = 2023;
	
	public static final Integer KUDU_CONNECTION_ERROR_CODE = 2024;

    public static final Integer COLUMN_TYPE_NOT_SUPPORT_ERROR_CODE = 2025;

    public static final Integer HTTP_INVOKE_ERROR_CODE = 2026;

    public static final Integer DBTYPE_NOTNULL_ERROR_CODE = 2027;

    public static final Integer NOTSUPPORT_DATABASE_ERROR_CODE = 2028;

    public static final Integer DBNAME_NOTNULL_ERROR_CODE = 2029;

    public static final Integer DATABASE_NOTFOUND_ERROR_CODE = 2030;

    public static final Integer EXCEPTION_TRYAGAIN_ERROR_CODE = 2031;

    public static final Integer DBID_NOTNULL_ERROR_CODE = 2032;

    public static final Integer JOBID_NOTNULL_ERROR_CODE = 2033;

    public static final Integer CONFIG_NOTFOUND_ERROR_CODE = 2034;

    public static final Integer JOB_ISRUNNING_ERROR_CODE = 2035;

    public static final Integer EXECUTEID_NOTNULL_ERROR_CODE = 2036;

    public static final Integer JOB_EXECUTE_ERROR_CODE = 2037;

    public static final Integer SRCDBID_NOTNULL_ERROR_CODE = 2038;

    public static final Integer DESTDBNAME_NOTNULL_ERROR_CODE = 2039;

    public static final Integer TABLE_NOTNULL_ERROR_CODE = 2040;

    public static final Integer SOURCE_NOTNULL_ERROR_CODE = 2041;

    public static final Integer GEN_CONFIG_ERROR_CODE = 2042;

    public static final Integer TARGETMEDIA_NOTEXISTS_ERROR_CODE = 2043;

    public static final Integer STRING_NOTNUMBER_ERROR_CODE = 2044;

    public static final Integer GROUP_NOTFOUND_ERROR_CODE = 2045;

    public static final Integer HBASE_COLUMNMETA_ERROR_CODE = 2046;

    public static final Integer TABLEMAPPINGS_NOTNULL_ERROR_CODE = 2047;

    public static final Integer TASKID_NOTNULL_ERROR_CODE = 2048;

    public static final Integer TASK_START_ERROR_CODE = 2049;

    public static final Integer MAPPINGID_NOTNULL_ERROR_CODE = 2050;

    public static final Integer TARGET_TYPE_ERROR_CODE = 2051;

    public static final Integer MODIFY_ES_TYPE_ERROR_CODE = 2061;

    public static final Integer SQL_NOTNULL_ERROR_CODE = 2062;

    public static final Integer DBID_NOTNUMBER_ERROR_CODE = 2063;

    public static final Integer SELECT_MUST_ERROR_CODE = 2064;

    public static final Integer TASK_CREATED_ERROR_CODE = 2065;

    public static final Integer TARGET_NOTFUND_ERROR_CODE = 2066;

    public static final Integer DOUBLECENTER_SWITCH_ERROR_CODE = 2067;

    public static final Integer SRCSOURCE_NOTFUND_ERROR_CODE = 2068;

    public static final Integer JOB_NOTFUND_ERROR_CODE = 2069;

    public static final Integer TASK_RESTART_ERROR_CODE = 2070;

    public static final Integer NOTFOUND_WORKER_ERROR_CODE = 2071;

    public static final Integer TARGETMEDIASOURCENAME_NOTNULL_ERROR_CODE = 2072;

    public static final Integer TARGETNAMESPACE_NOTNULL_ERROR_CODE = 2073;

    public static final Integer TARGETTABLENAME_NOTNULL_ERROR_CODE = 2074;

    public static final Integer PARAMETER_NONULL_ERROR_CODE = 2075;

    public static final Integer DATABASE_NONFIND_ERROR_CODE = 2076;

    public static final Integer DATABASE_MOREONE_ERROR_CODE = 2077;

    public static final Integer AESKEY_16BITES_ERROR_CODE = 2078;

    public static final Integer MUSTBE_LEADERTASK_ERROR_CODE = 2079;

    public static final Integer SOURCE_TYPE_ERROR_CODE = 2080;

    public static final Integer DATABASE_COLUMN_TYPE_ERROR_CODE = 2081;

    static {
        codeMap.put(SUCCESS_CODE,"成功");
        codeMap.put(SQL_VALIDATE_ERROR_CODE,"sql语句校验有误,请检查sql语句");
        codeMap.put(SQL_EXECUTE_ERROR_CODE,"sql执行失败");
        codeMap.put(SERVER_RUNTIME_ERROR_CODE,"服务器出现异常");
        codeMap.put(RESULT_RETURN_ERROR_CODE,"worker执行结果为空");
        codeMap.put(SQL_COUNT_ERROR_CODE,"sql语句的条数不正确");
        codeMap.put(SQL_NOTADD_ERROR_CODE,"该sql语句没有增加字段");
        codeMap.put(NOTFIND_COLUMN_ERROR_CODE,"没有找到要新添加的字段");
        codeMap.put(NOTFIND_MAPPING_ERROR_CODE,"没有找到mapping映射");
        codeMap.put(NOTFIND_ESCONFIG_ERROR_CODE,"没有找到ES的相关配置");
        codeMap.put(NOTFIND_ADDRESS_ERROR_CODE,"没有找到服务器地址");
        codeMap.put(MEDIASOURCEID_ISNULL_ERROR_CODE,"mediaSourceId参数为空");
        codeMap.put(MAPPINGID_ISNULL_ERROR_CODE,"mappingId参数为空");
        codeMap.put(SQL_ISNULL_ERROR_CODE,"sql参数为空");
        codeMap.put(CLUSTERSTATE_ISNULL_ERROR_CODE,"获取clusterState时出错");
        codeMap.put(MEMBERINFO_ISNULL_ERROR_CODE,"没有获取到clusterState下的member信息");
        codeMap.put(ES_RETURNNULL_ERROR_CODE,"es执行结果为空");
        codeMap.put(ES_EXECUTE_ERROR_CODE,"es执行失败");
        codeMap.put(COLUMN_EXISTS_ERROR_CODE,"新增字段已经添加,请检查sql语句");
        codeMap.put(SQLTYPE_NOTSUPPORT_ERROR_CODE,"目前只支持新增字段的同步,请检查sql语句");
        codeMap.put(SQL_SYNTAX_ERROR_CODE,"sql语句语法有误,请检查sql语句");
        codeMap.put(KUDU_CONNECTION_ERROR_CODE,"KUDU链接异常");
        codeMap.put(COLUMN_TYPE_NOT_SUPPORT_ERROR_CODE,"字段类型不支持");
        codeMap.put(JOBNUM_ISNULL_ERROR_CODE,"jobNum参数为空");
        codeMap.put(DBNAME_ISNULL_ERROR_CODE,"dbName参数为空");
        codeMap.put(MEDIA_TYPE_ERROR_CODE,"数据类型有误");
        codeMap.put(HIVE_MODIFY_ERROR_CODE,"修改hive元数据失败");
        codeMap.put(HTTP_INVOKE_ERROR_CODE,"http调用失败");
        codeMap.put(DBTYPE_NOTNULL_ERROR_CODE,"dbType不能为空");
        codeMap.put(NOTSUPPORT_DATABASE_ERROR_CODE,"不支持的数据库类型");
        codeMap.put(DBNAME_NOTNULL_ERROR_CODE,"dbName不能为空");
        codeMap.put(DATABASE_NOTFOUND_ERROR_CODE,"在配置中没有找到该数据库");
        codeMap.put(EXCEPTION_TRYAGAIN_ERROR_CODE,"异常,请重试！");
        codeMap.put(DBID_NOTNULL_ERROR_CODE,"dbId不能为空");
        codeMap.put(JOBID_NOTNULL_ERROR_CODE,"jobId为空或不合法");
        codeMap.put(CONFIG_NOTFOUND_ERROR_CODE,"没有找到配置");
        codeMap.put(JOB_ISRUNNING_ERROR_CODE,"该job正在执行中");
        codeMap.put(EXECUTEID_NOTNULL_ERROR_CODE,"executeId为空或不合法");
        codeMap.put(JOB_EXECUTE_ERROR_CODE,"任务执行出错");
        codeMap.put(SRCDBID_NOTNULL_ERROR_CODE,"srcDbId不能为空");
        codeMap.put(DESTDBNAME_NOTNULL_ERROR_CODE,"destDbName不能为空");
        codeMap.put(TABLE_NOTNULL_ERROR_CODE,"table不能为空");
        codeMap.put(SOURCE_NOTNULL_ERROR_CODE,"source不能为空");
        codeMap.put(GEN_CONFIG_ERROR_CODE,"生成job配置文件时出错");
        codeMap.put(TARGETMEDIA_NOTEXISTS_ERROR_CODE,"目标数据源不存在");
        codeMap.put(STRING_NOTNUMBER_ERROR_CODE,"srcDbId必须是数字类型的字符串");
        codeMap.put(GROUP_NOTFOUND_ERROR_CODE,"没有找到对应的组");
        codeMap.put(HBASE_COLUMNMETA_ERROR_CODE,"没有获取hbase列信息");
        codeMap.put(TABLEMAPPINGS_NOTNULL_ERROR_CODE,"参数tableMappings不能为空");
        codeMap.put(TASK_START_ERROR_CODE,"任务启动失败");
        codeMap.put(MODIFY_ES_TYPE_ERROR_CODE,"不支持修改es字段");
        codeMap.put(MAPPINGID_NOTNULL_ERROR_CODE,"mappingId不能为空");
        codeMap.put(TARGET_TYPE_ERROR_CODE,"目标数据源类型有误");
        codeMap.put(SQL_NOTNULL_ERROR_CODE,"sql不能为空");
        codeMap.put(DBID_NOTNUMBER_ERROR_CODE,"dbId必须为数字");
        codeMap.put(SELECT_MUST_ERROR_CODE,"必须是select语句");
        codeMap.put(TASK_CREATED_ERROR_CODE,"task自动创建失败");
        codeMap.put(TARGET_NOTFUND_ERROR_CODE,"没有找到目标端数据源");
        codeMap.put(DOUBLECENTER_SWITCH_ERROR_CODE,"当前job关联的数据源正在切机房中，job暂时不能启动，请稍后再试！");
        codeMap.put(TARGETMEDIASOURCENAME_NOTNULL_ERROR_CODE,"targetMediaSourceName不能为空");
        codeMap.put(TARGETNAMESPACE_NOTNULL_ERROR_CODE,"targetNamespace不能为空");
        codeMap.put(SRCSOURCE_NOTFUND_ERROR_CODE,"源端数据源不存在");
        codeMap.put(JOB_NOTFUND_ERROR_CODE,"Job不存在");
        codeMap.put(TASK_RESTART_ERROR_CODE,"任务重启失败，请人工处理");
        codeMap.put(NOTFOUND_WORKER_ERROR_CODE,"没有找到合适的worker");
        codeMap.put(TARGETTABLENAME_NOTNULL_ERROR_CODE,"targetTableName不能为空");
        codeMap.put(PARAMETER_NONULL_ERROR_CODE,"请求参数有误");
        codeMap.put(DATABASE_NONFIND_ERROR_CODE,"没有找到数据库");
        codeMap.put(TASKID_NOTNULL_ERROR_CODE,"taskId不能为空");
        codeMap.put(DATABASE_MOREONE_ERROR_CODE,"存在多个数据库");
        codeMap.put(AESKEY_16BITES_ERROR_CODE,"AES秘钥必须是16位");
        codeMap.put(MUSTBE_LEADERTASK_ERROR_CODE,"必须是leader task");
        codeMap.put(SOURCE_TYPE_ERROR_CODE,"源端类型不能为空");
        codeMap.put(DATABASE_COLUMN_TYPE_ERROR_CODE,"数据库字段类型不能为空");
    }

    public static String getErrorDesc(Integer code){
        return codeMap.get(code);
    }

}
