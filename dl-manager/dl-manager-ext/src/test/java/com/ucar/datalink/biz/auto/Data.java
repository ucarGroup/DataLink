package com.ucar.datalink.biz.auto;

/**
 * Created by yang.wang09 on 2018-05-15 20:51.
 */
public class Data {

    public static final String JSON = "{\n" +
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
            "                        \"password\": \"123456\",\n" +
            "                        \"column\": [\n" +
            "                            \"tt\",\n" +
            "                            \"t1\",\n" +
            "                            \"t2\",\n" +
            "                            \"t3\"\n" +
            "                        ],\n" +
            "                        \"connection\": [\n" +
            "                            {\n" +
            "                                \"jdbcUrl\": [\n" +
            "                                    \"jdbc:mysql://10.99.64.126:3306/xx\"\n" +
            "                                ],\n" +
            "                                \"table\": [\n" +
            "                                    \"aa\"\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"splitPk\": \"tt\",\n" +
            "                        \"username\": \"root\"\n" +
            "                    },\n" +
            "                    \"name\": \"mysqlreader\"\n" +
            "                },\n" +
            "                \"writer\": {\n" +
            "                    \"parameter\": {\n" +
            "                        \"hadoopUserName\": \"increment\",\n" +
            "                        \"path\": \"/user/mysqlhistory/xx/aa/20180515203857\",\n" +
            "                        \"fileName\": \"aa\",\n" +
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
            "                                \"name\": \"tt\",\n" +
            "                                \"type\": \"int\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"t1\",\n" +
            "                                \"type\": \"string\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"t2\",\n" +
            "                                \"type\": \"string\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"t3\",\n" +
            "                                \"type\": \"decimal\"\n" +
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
}
