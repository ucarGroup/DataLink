package com.ucar.datalink.manager.core.rest;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.domain.job.DataxCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2017/11/13.
 */
public class CommandTest {

    public static void main(String[] args) {
        CommandTest t = new CommandTest();
        //t.go();
        t.a1();
    }


    public void a1() {
        String header = "HTTP_PARAMETER_LAST_EXECUTE_TIME";
        String value = "2016-06-13 14:26:30";

        Map<String,String> map = new HashMap<>();
        map.put(header,value);

        String x = JSONObject.toJSONString(map);
        System.out.println(x);
    }


    public void go() {
        DataxCommand c = new DataxCommand();
        c.setDebug(false);
        c.setJobId(1L);
        c.setJobName("test");
        c.setJobQueueExecutionId(-1L);
        c.setJvmArgs("-Xms1G -Xmx1G");
        c.setType(DataxCommand.Type.Start);
        String x = JSONObject.toJSONString(c);
        System.out.println(x);

    }

}
