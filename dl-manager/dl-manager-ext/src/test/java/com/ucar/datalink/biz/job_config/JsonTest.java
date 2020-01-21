package com.ucar.datalink.biz.job_config;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.module.AdvanceJobProperty;
import com.ucar.datalink.biz.module.HDFSJobExtendProperty;
import com.ucar.datalink.biz.module.JobExtendProperty;
import com.ucar.datalink.biz.module.MySqlJobExtendProperty;

import java.util.Map;

/**
 * Created by user on 2017/7/27.
 */
public class JsonTest {

//    public static final String json = "{\n" +
//            "    \"advance\": {\n" +
//            "        \"channel\": \"10\"\n" +
//            "    },\n" +
//            "    \"reader\": {\n" +
//            "        \"type\": \"MySql\",\n" +
//            "        \"where\": \"\",\n" +
//            "        \"querySql\": \"SELECT * FROM xxx WHERE id>10 LIMIT 10\"\n" +
//            "    },\n" +
//            "    \"writer\": {\n" +
//            "        \"type\": \"HDFS\",\n" +
//            "        \"compress\": \"NONE\",\n" +
//            "        \"path\": \"/data0/test/hehe/xx\"\n" +
//            "    }\n" +
//            "}";
//            ;

    public static final String json = "{\"advance\":{\"channel\":\"10\"},\"reader\":{},\"writer\":{}}";


    public static void main(String[] args) {
        JobExtendProperty property = JSONObject.parseObject(json, JobExtendProperty.class);
        AdvanceJobProperty advance = property.getAdvance();
        Map<String,String> reader = property.getReader();
        Map<String,String> writer = property.getWriter();

        //System.out.println("ok~");

        System.out.println(reader);
        System.out.println(writer);

        String r = JSONObject.toJSONString(reader);
        String w = JSONObject.toJSONString(writer);
        System.out.println(r);
        System.out.println(w);

        MySqlJobExtendProperty mysql = JSONObject.parseObject(r, MySqlJobExtendProperty.class);
        HDFSJobExtendProperty hdfs = JSONObject.parseObject(w,HDFSJobExtendProperty.class);
        System.out.println("ok~");
    }


}
