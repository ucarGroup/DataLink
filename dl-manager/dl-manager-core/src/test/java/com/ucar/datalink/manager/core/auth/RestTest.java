package com.ucar.datalink.manager.core.auth;


import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by user on 2017/11/1.
 */
public class RestTest {

    public static void main(String[] args) {
        go();
    }

    public static void go() {
        String json = "aaaaa";
        json = "\"{\n" +
                "    \n" +
                "}\"";
        json = "{\n" +
                "    \"entry\": {\n" +
                "        \"jvm\": \"-Xms1G -Xmx1G\",\n" +
                "        \"environment\": {\n" +
                "            \n" +
                "        }\n" +
                "    },\n" +
                "    \"core\": {\n" +
                "        \"transport\": {\n" +
                "            \"exchanger\": {\n" +
                "                \"class\": \"com.alibaba.datax.core.plugin.BufferedRecordExchanger\",\n" +
                "                \"bufferSize\": 128\n" +
                "            },\n" +
                "            \"channel\": {\n" +
                "                \"byteCapacity\": 67108864,\n" +
                "                \"flowControlInterval\": 20,\n" +
                "                \"class\": \"com.alibaba.datax.core.transport.channel.memory.MemoryChannel\",\n" +
                "                \"speed\": {\n" +
                "                    \"byte\": 104857600,\n" +
                "                    \"record\": 1000000\n" +
                "                },\n" +
                "                \"capacity\": 1024\n" +
                "            }\n" +
                "        }\n" +
                "    },\n" +
                "    \"job\": {\n" +
                "        \"content\": [\n" +
                "            {\n" +
                "                \"reader\": {\n" +
                "                    \"parameter\": {\n" +
                "                        \"password\": \"canal\",\n" +
                "                        \"column\": [\n" +
                "                            \"name\"\n" +
                "                        ],\n" +
                "                        \"connection\": [\n" +
                "                            {\n" +
                "                                \"jdbcUrl\": [\n" +
                "                                    \"jdbc:mysql://10.104.50.41:3306/fcar_loan\"\n" +
                "                                ],\n" +
                "                                \"table\": [\n" +
                "                                    \"pufa_city_temp\"\n" +
                "                                ]\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"splitPk\": \"name\",\n" +
                "                        \"username\": \"canal\"\n" +
                "                    },\n" +
                "                    \"name\": \"mysqlreader\"\n" +
                "                },\n" +
                "                \"writer\": {\n" +
                "                    \"parameter\": {\n" +
                "                        \"hadoopUserName\": \"increment\",\n" +
                "                        \"path\": \"/user/mysqlhistory/fcar_loan/pufa_city_temp\",\n" +
                "                        \"fileName\": \"pufa_city_temp\",\n" +
                "                        \"createPathIfNotExist\": true,\n" +
                "                        \"compress\": \"snappy\",\n" +
                "                        \"column\": [\n" +
                "                            {\n" +
                "                                \"name\": \"name\",\n" +
                "                                \"type\": \"string\"\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"defaultFS\": \"hdfs://hadoop2cluster\",\n" +
                "                        \"errorRetryTimes\": 100,\n" +
                "                        \"writeMode\": \"append\",\n" +
                "                        \"fieldDelimiter\": \"\\t\",\n" +
                "                        \"fileType\": \"orc\"\n" +
                "                    },\n" +
                "                    \"name\": \"hdfswriter\"\n" +
                "                }\n" +
                "            }\n" +
                "        ],\n" +
                "        \"setting\": {\n" +
                "            \"errorLimit\": {\n" +
                "                \"record\": 0,\n" +
                "                \"percentage\": 0.02\n" +
                "            },\n" +
                "            \"speed\": {\n" +
                "                \"channel\": \"10\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Object x = JSONObject.parse(json);

        System.out.println("???");
    }




    /**
     * 启动一个job
     * @throws IOException
     */
    public void start() throws IOException {
        String job_id = " 108";
        String url = "http://datalinkmanagertest.10101111.com/jobService/start/" + job_id;
        //http请求头
        Map<String,String> map = new HashMap<>();
        map.put("JOB_ID_SIGNAL", "fdc3f89a73f09e51fecd4b03c5a915bd");
        execute(url, map);
    }

    /**
     * 停止一个job
     * @throws IOException
     */
    public void stop() throws IOException {
        String execution_id = " 123";
        String url = "http://datalinkmanagertest.10101111.com/jobService/stop/" + execution_id;
        //http请求头
        Map<String,String> map = new HashMap<>();
        map.put("JOB_ID_SIGNAL", "fdc3f89a73f09e51fecd4b03c5a915bd");
        execute(url, map);
    }


    /**
     * 查询一个job_config_id下的所有运行状态信息
     * @throws IOException
     */
    public void history() throws IOException {
        String job_id = "108";
        String url = "http://datalinkmanagertest.10101111.com/jobService/history/" + job_id;
        //http请求头
        Map<String,String> map = new HashMap<>();
        map.put("JOB_ID_SIGNAL", "fdc3f89a73f09e51fecd4b03c5a915bd");
        execute(url, map);
    }

    /**
     * 根据job_execution_id查询一个job运行的状态信息
     * @throws IOException
     */
    public void state() throws IOException {
        String job_execution_id = "123";
        String url = "http://datalinkmanagertest.10101111.com/jobService/state/" + job_execution_id;
        //http请求头
        Map<String,String> map = new HashMap<>();
        map.put("JOB_ID_SIGNAL", "fdc3f89a73f09e51fecd4b03c5a915bd");
        execute(url, map);
    }


    /**
     * 强制停止job
     * @throws IOException
     */
    public void forceStop() throws IOException {
        String execution_id = " 123";
        String url = "http://datalinkmanagertest.10101111.com/jobService/forceStop/" + execution_id;
        //http请求头
        Map<String,String> map = new HashMap<>();
        map.put("JOB_ID_SIGNAL", "fdc3f89a73f09e51fecd4b03c5a915bd");
        execute(url, map);
    }



    /**
     * 执行http请求并返还
     * @param serverUrl
     * @param kv
     * @return
     * @throws IOException
     */
    public static String execute(String serverUrl, Map<String,String> kv) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        BufferedReader reader = null;
        URL url = new URL(serverUrl);
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("Content-type", "application/json");
        //设置http请求头
        for(Iterator<Map.Entry<String,String>> iter=kv.entrySet().iterator();iter.hasNext();) {
            Map.Entry<String,String> entry = iter.next();
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        conn.connect();
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line).append("\n");
        }
        return responseBuilder.toString().trim();
    }


}
