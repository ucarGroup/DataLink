package com.ucar.datalink.biz.auto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import com.ucar.datalink.biz.spark.ModifyCheckColumnInfo;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.domain.meta.ColumnMeta;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.wang09 on 2018-05-15 20:50.
 */
public class Test5 {

    public static void main(String[] args) {

        String json = Data.JSON;
        go2(json);
        List<ColumnMeta> columnMetas = new ArrayList<>();
        ColumnMeta cm1 = new ColumnMeta();
        cm1.setName("tt1");
        cm1.setType("VARCHAR");
        columnMetas.add(cm1);

        ColumnMeta cm2 = new ColumnMeta();
        cm2.setName("tt2");
        cm2.setType("INTEGER");
        columnMetas.add(cm2);

        go3(json,columnMetas);

    }


    public static void go3(String json, List<ColumnMeta> columns) {
        DLConfig connConf = DLConfig.parseFrom(json);
        JSONArray array = new JSONArray();
        for(ColumnMeta cm : columns) {
            //Map<String,String> name = new HashMap<>();
            //name.put("name",cm.getName());
            //Map<String,String> type = new HashMap<>();
            //type.put("type", cm.getType());

            JSONObject obj = new JSONObject();
            obj.put("name",cm.getName());
            obj.put("type",cm.getType());
            array.add(obj);

        }
        connConf.get("job.content[0].writer.parameter.column");
        connConf.set("job.content[0].writer.parameter.column",array);
        System.out.println("???s");
    }


    public static void go2(String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        JSONArray array = (JSONArray)connConf.get("job.content[0].writer.parameter.column");
        ModifyCheckColumnInfo column = new ModifyCheckColumnInfo();
        for(int i=0;i<array.size();i++) {
            JSONObject jo = (JSONObject)array.get(i);
            column.addNameType(jo.getString("name"),jo.getString("type"));
        }
    }


    public static String go(String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        String path = (String)connConf.get("job.content[0].writer.parameter.path");
        if( StringUtils.isBlank(path) ) {
            return "";
        } else {
            return path;
        }
    }




}
