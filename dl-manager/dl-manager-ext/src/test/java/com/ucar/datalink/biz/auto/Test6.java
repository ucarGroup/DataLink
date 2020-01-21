package com.ucar.datalink.biz.auto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.job.DataxJobConfigConstant;
import com.ucar.datalink.biz.spark.ModifyCheckColumnInfo;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.common.utils.DbConfigEncryption;

import java.util.regex.Matcher;

/**
 * Created by yang.wang09 on 2018-05-17 16:05.
 */
public class Test6 {

    public static void main(String[] args) {

        //go1();
        //String x = DbConfigEncryption.encrypt("ucar_dev_soa");
        //System.out.println(x);

        //x = DbConfigEncryption.decrypt("fbae5687eca8d53e39722c1b448c821a");
        //System.out.println("密码->"+x);
        go7();
    }


    public static void go7() {
        String reader = "abcd  \"@column\" hehe";
        String content = "$woqu,$hehe,$wokao";
        //if(content.contains("$")) {
        //    content = content.replaceAll("$", Matcher.quoteReplacement(b));
        //}
        String x = reader.replaceAll(DataxJobConfigConstant.COLUMN, Matcher.quoteReplacement(content));
        System.out.println(x);
    }

    public static void go1() {
        String json = Data_Mysql_Mysql.JSON;
        DLConfig connConf = DLConfig.parseFrom(json);
        JSONArray array = (JSONArray)connConf.get("job.content[0].reader.parameter.column");
        ModifyCheckColumnInfo columns = new ModifyCheckColumnInfo();
        for(int i=0;i<array.size();i++) {
            columns.addName( array.getString(i) );
        }
    }
}
