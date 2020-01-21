package com.ucar.datalink.biz.auto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.spark.ModifyCheckColumnInfo;
import com.ucar.datalink.common.utils.DLConfig;
import com.ucar.datalink.util.SyncUtil;

/**
 * Created by yang.wang09 on 2018-05-04 11:23.
 */
public class Test4 {

    public static String json = "{\n" +
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
            "                            \"id\",\n" +
            "                            \"app_position\",\n" +
            "                            \"position\",\n" +
            "                            \"position_type\",\n" +
            "                            \"status\",\n" +
            "                            \"position_property\",\n" +
            "                            \"create_emp\",\n" +
            "                            \"create_time\",\n" +
            "                            \"modify_emp\",\n" +
            "                            \"modify_time\",\n" +
            "                            \"show_type\",\n" +
            "                            \"sort\",\n" +
            "                            \"is_support_link\",\n" +
            "                            \"is_support_sort\",\n" +
            "                            \"remark\"\n" +
            "                        ],\n" +
            "                        \"connection\": [\n" +
            "                            {\n" +
            "                                \"jdbcUrl\": [\n" +
            "                                    \"jdbc:mysql://10.104.50.41:3308/fcar_admin\"\n" +
            "                                ],\n" +
            "                                \"table\": [\n" +
            "                                    \"t_admin_position\"\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"splitPk\": \"id\",\n" +
            "                        \"username\": \"canal\"\n" +
            "                    },\n" +
            "                    \"name\": \"mysqlreader\"\n" +
            "                },\n" +
            "                \"writer\": {\n" +
            "                    \"parameter\": {\n" +
            "                        \"hadoopUserName\": \"increment\",\n" +
            "                        \"path\": \"/user/mysqlhistory/fcar_admin/t_admin_position\",\n" +
            "                        \"fileName\": \"t_admin_position\",\n" +
            "                        \"createPathIfNotExist\": true,\n" +
            "                        \"compress\": \"snappy\",\n" +
            "                        \"hadoopConfig\": {\n" +
            "                            \"dfs.ha.namenodes.hadoop2cluster\": \"n1,n2\",\n" +
            "                            \"dfs.namenode.rpc-address.hadoop2cluster.n1\": \"10.104.104.127:9001\",\n" +
            "                            \"dfs.namenode.rpc-address.hadoop2cluster.n2\": \"10.104.104.128:9001\",\n" +
            "                            \"dfs.nameservices\": \"hadoop2cluster\",\n" +
            "                            \"dfs.client.failover.proxy.provider.hadoop2cluster\": \"org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider\"\n" +
            "                        },\n" +
            "                        \"column\": [\n" +
            "                            {\n" +
            "                                \"name\": \"id\",\n" +
            "                                \"type\": \"bigint\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"app_position\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"position\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"position_type\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"status\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"position_property\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"create_emp\",\n" +
            "                                \"type\": \"bigint\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"create_time\",\n" +
            "                                \"type\": \"timestamp\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"modify_emp\",\n" +
            "                                \"type\": \"bigint\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"modify_time\",\n" +
            "                                \"type\": \"timestamp\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"show_type\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"sort\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"is_support_link\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"is_support_sort\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"remark\",\n" +
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



    public static void main(String[] args) {
        Test4 t = new Test4();
        t.gg();
    }




    public static ModifyCheckColumnInfo parseColumnInfo(String json) {
        DLConfig connConf = DLConfig.parseFrom(json);
        JSONArray array = (JSONArray)connConf.get("job.content[0].writer.parameter.column");
        ModifyCheckColumnInfo column = new ModifyCheckColumnInfo();
        for(int i=0;i<array.size();i++) {
            JSONObject jo = (JSONObject)array.get(i);
            column.addNameType(jo.getString("name"),jo.getString("type"));
        }
        return column;
    }

    public void gg() {
        DLConfig connConf = DLConfig.parseFrom(json);
        JSONArray array = (JSONArray)connConf.get("job.content[0].writer.parameter.column");
        JSONObject jo = array.getJSONObject(0);
        System.out.println(jo.getString("name"));
        System.out.println( jo.getString("type") );

        System.out.println( "??????????" );

        ModifyCheckColumnInfo info = parseColumnInfo(json);
        System.out.println(info);


        String path = SyncUtil.parseHDFSPath(json);
        System.out.println(path);
        //array.get
        //System.out.println(obj);


    }

}
