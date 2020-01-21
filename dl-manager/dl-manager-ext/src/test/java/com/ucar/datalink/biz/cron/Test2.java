package com.ucar.datalink.biz.cron;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by yang.wang09 on 2018-07-31 16:15.
 */
public class Test2 {

    public static void main(String[] args) {
        Test2 t = new Test2();
        t.go();
    }


    public void go() {
        String str = "{\"msg\":\"not+timing+job%21\",\"executId\":\"54f1ac93-8d72-4b30-8304-9e6d5c57a882\"}";
        JSONObject x = (JSONObject)JSONObject.parse(str);
        System.out.println(x);
        Object msg = x.get("msg");
        Object executeId = x.get("executId");
        System.out.println(msg);
        System.out.println(executeId);
    }
}
