package com.ucar.datalink.biz.meta;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.hdfs.HDFSMediaSrcParameter;
import com.ucar.datalink.domain.meta.ColumnMeta;
import com.ucar.datalink.domain.meta.MediaMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2017/7/7.
 */
public class HDFSUtil {


    /**
     * 获取所有表信息的后缀URL，通过MediaSourceInfo中的URL前缀和这个后缀，完整的拼出一个可访问的地址
     */
    private static final String SPARK_CUBE_META_TABLES = "/schema/table/tablesToSync";

    /**
     * 获取表所有列信息的后缀URL，通过MediaSourceInfo中的URL前缀和这个后缀，完整的拼出一个可访问的地址
     */
    private static final String SPARK_CUBE_META_COLUMNS = "/schema/table/detailToSync?database=@db_name@&tableName=@table_name@";

    /**
     * 根据传入的MediaSourceInfo 获取所有表的元信息
     * @param info
     * @return
     */
    public static List<MediaMeta> getTables(MediaSourceInfo info) {
        check(info.getParameterObj());
        HDFSMediaSrcParameter hdfsParameter = info.getParameterObj();
        String address = hdfsParameter.getSparkcubeAddress();
        String json = executeGetTables(address);
        return parseTablesJson(json);
    }


    /**
     * 根据传入的MediaSourceInfo和表名，获取这个表下的所有列的元信息
     * @param info
     * @param tableName
     * @return
     */
    public static List<ColumnMeta> getColumns(MediaSourceInfo info, String tableName) {
        check(info.getParameterObj());
        HDFSMediaSrcParameter hdfsParameter = info.getParameterObj();
        String address = hdfsParameter.getSparkcubeAddress();
        String[] names = tableName.split("\\.");
        if(names==null || names.length<2) {
            throw new RuntimeException("elastic search table error");
        }
        String json = executeGetColumns(address,names[0],names[1]);
        return parseColumnsJson(json);
    }


    /**
     * 发送带重试的get请求，获取所有表的名字
     * @param url
     * @return
     */
    private static String executeGetTables(String url) {
        String send_url = url + SPARK_CUBE_META_TABLES;
        return URLConnectionUtil.retryGET(send_url);
    }

    /**
     * 发送带重试的get请求，根据库名和表名获取所有字段类型
     * @param url
     * @param dbName
     * @param tableName
     * @return
     */
    private static String executeGetColumns(String url, String dbName, String tableName) {
        String send_url = url + SPARK_CUBE_META_COLUMNS;
        send_url = send_url.replace("@db_name@",dbName);
        send_url = send_url.replace("@table_name@",tableName);
        return URLConnectionUtil.retryGET(send_url);
    }


    /**
     * 解析json串，返回List<MediaMeta>
     * @param json
     * @return
     */
    private static List<MediaMeta> parseTablesJson(String json) {
        LinkedHashMap<String, String> jsonMap = JSON.parseObject(json, new TypeReference<LinkedHashMap<String, String>>() {});
        List<MediaMeta> tables = new ArrayList<>();

        //第一层遍历所有db
        for (Map.Entry<String, String> hdfs_meta : jsonMap.entrySet()) {
            ArrayList<String> list = JSON.parseObject(hdfs_meta.getValue(), new TypeReference<ArrayList<String>>() {});
            //第二层循环遍历db下的表名
            for(String table_name : list) {
                MediaMeta tm = new MediaMeta();
                String db_name = hdfs_meta.getKey();
                tm.setNameSpace(db_name);
                tm.setName( table_name );
                tables.add(tm);
            }
        }
        return tables;
    }


    /**
     * 解析json串，返回List<ColumnMeta>
     * @param json
     * @return
     */
    private static List<ColumnMeta> parseColumnsJson(String json) {
        LinkedHashMap<String, String> jsonMap = JSON.parseObject(json, new TypeReference<LinkedHashMap<String, String>>() {});
        List<ColumnMeta> columns = new ArrayList<>();
        for (Map.Entry<String, String> hdfs_meta : jsonMap.entrySet()) {
            if("fields".equals(hdfs_meta.getKey())) {
                //如果key是fields，则继续遍历获取所有字段类型
                ArrayList<String> list = JSON.parseObject(hdfs_meta.getValue(), new TypeReference<ArrayList<String>>() {});
                for(String types : list) {
                    //第三层遍历，获取所有类型
                    LinkedHashMap<String, String> types_info = JSON.parseObject(types, new TypeReference<LinkedHashMap<String, String>>() {});
                    for(Map.Entry<String,String> field_type : types_info.entrySet()) {
                        ColumnMeta cm = new ColumnMeta();
                        cm.setName(field_type.getKey());
                        cm.setType(field_type.getValue());
                        //cm.set
                        columns.add(cm);
                    }
                }
            }
        }

        return columns;
    }


    /**
     * 检查当前的MediaSrcParameter类似是否是HDFS类型，如果不是则抛异常
     *
     * @param msp
     */
    private static void check(MediaSrcParameter msp) {
        if (!(msp instanceof HDFSMediaSrcParameter)) {
            throw new RuntimeException("当前的MediaSrcParameter类型错误 " + msp);
        }
    }


}