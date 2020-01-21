package com.ucar.datalink.manager.core.boot;

import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class RestTest {
    public static void main(String[] args) throws IOException {
        RestTest t = new RestTest();
        //t.count();
        //t.tables();
        t.showTable();
    }

    /**
     * 查询所有表
     * @throws IOException
     */
    public void tables() throws IOException {
        String type = "mysql";
        String name = "ucar_crm";
        String url = "http://datalinkmanagertest03.10101111.com/rdbms/tables?DB_TYPE=" + type+ "&DB_NAME=" +name;
        String x = execute(url);
        System.out.println(x);
    }

    /**
     * 查询表结构
     * @throws IOException
     */
    public void showTable() throws IOException {
        String type = "mysql";
        String name = "ucar_crm";
        String table_name = "t_b_city";
        String url = "http://datalinkmanagertest03.10101111.com/rdbms/tables?" +
                "DB_TYPE=" + type+ "&DB_NAME=" +name + "&TABLE_NAME="+table_name;
        String x = execute(url);
        System.out.println(x);
    }

    /**
     * 查询
     * @throws IOException
     */
    public void count() throws IOException {
        String type = "mysql";
        String name = "ucar_crm";
        String sql = "select+count(*)+from+t_employee";
        String url = "http://datalinkmanagertest03.10101111.com/rdbms/count?DB_TYPE=" + type+ "&DB_NAME=" +name+ "&SQL="+sql;
        String x = execute(url);
        System.out.println(x);
    }


    /**
     * 执行http请求并返还
     * @param serverUrl
     * @return
     * @throws IOException
     */
    public static String execute(String serverUrl) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        BufferedReader reader = null;
        URL url = new URL(serverUrl);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("Content-type", "application/json");
        conn.connect();
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line).append("\n");
        }
        return responseBuilder.toString().trim();
    }

}