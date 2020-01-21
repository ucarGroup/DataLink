package com.ucar.datalink.biz.job_config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ucar.datalink.biz.module.MySqlJobExtendProperty;
import com.ucar.datalink.domain.media.MediaSourceType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by user on 2017/7/24.
 */
public class JobExtendTest {


    public static void main(String[] args) {
        JobExtendTest j = new JobExtendTest();
        //j.go();
        j.go_mysql_parse();
    }


    public void go() {
        MySqlJobExtendProperty job = new MySqlJobExtendProperty();
        job.setType(MediaSourceType.MYSQL.name());

        job.setWhere("id > 10");
        job.setQuerySql("SELECT * FROM person WHERE id > 10 LIMIT 10");

        String x = JSONObject.toJSONString(job);
        System.out.println(x);

    }

    public void go_mysql_parse() {
        String json = "{\"querySql\":\"SELECT * FROM person WHERE id > 10 LIMIT 10\",\"type\":\"MYSQL\",\"where\":\"id > 10\"}";
        Map<String, String> jsonMap = JSON.parseObject(json, new TypeReference<LinkedHashMap<String, String>>() {});

        for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
            System.out.println("key->"+entry.getKey() +"\t value->"+entry.getValue());
        }

        MySqlJobExtendProperty xx = JSONObject.parseObject(json, MySqlJobExtendProperty.class);
        System.out.println(xx);
    }


}
