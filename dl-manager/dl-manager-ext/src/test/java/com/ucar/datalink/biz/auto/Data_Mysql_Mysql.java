package com.ucar.datalink.biz.auto;

/**
 * Created by yang.wang09 on 2018-05-17 16:03.
 */
public class Data_Mysql_Mysql {

    public static final String JSON = "{\n" +
            "  \"entry\": {\n" +
            "    \"jvm\": \"-Xms1G -Xmx1G\",\n" +
            "    \"environment\": {}\n" +
            "  },\n" +
            "  \"core\": {\n" +
            "    \"transport\": {\n" +
            "      \"exchanger\": {\n" +
            "        \"class\": \"com.alibaba.datax.core.plugin.BufferedRecordExchanger\",\n" +
            "        \"bufferSize\": 128\n" +
            "      },\n" +
            "      \"channel\": {\n" +
            "        \"byteCapacity\": 67108864,\n" +
            "        \"flowControlInterval\": 20,\n" +
            "        \"class\": \"com.alibaba.datax.core.transport.channel.memory.MemoryChannel\",\n" +
            "        \"speed\": {\n" +
            "          \"byte\": 104857600,\n" +
            "          \"record\": 1000000\n" +
            "        },\n" +
            "        \"capacity\": 1024\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"job\": {\n" +
            "    \"content\": [\n" +
            "      {\n" +
            "        \"reader\": {\n" +
            "          \"parameter\": {\n" +
            "            \"password\": \"b64e25ec550a89c1\",\n" +
            "            \"column\": [\n" +
            "              \"auth_id\",\n" +
            "              \"api_name\",\n" +
            "              \"invoker_id\",\n" +
            "              \"interval_second\",\n" +
            "              \"user_max_count\",\n" +
            "              \"total_max_count\",\n" +
            "              \"version\",\n" +
            "              \"project\"\n" +
            "            ],\n" +
            "            \"connection\": [\n" +
            "              {\n" +
            "                \"jdbcUrl\": [\n" +
            "                  \"jdbc:mysql://10.101.23.101:3306/ucar_admin\"\n" +
            "                ],\n" +
            "                \"table\": [\n" +
            "                  \"t_api_invoker_auth\"\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"splitPk\": \"auth_id\",\n" +
            "            \"username\": \"canal\"\n" +
            "          },\n" +
            "          \"name\": \"mysqlreader\"\n" +
            "        },\n" +
            "        \"writer\": {\n" +
            "          \"parameter\": {\n" +
            "            \"password\": \"61de282fbe93652ad19b1380c3c696c3\",\n" +
            "            \"column\": [\n" +
            "              \"auth_id\",\n" +
            "              \"api_name\",\n" +
            "              \"invoker_id\",\n" +
            "              \"interval_second\",\n" +
            "              \"user_max_count\",\n" +
            "              \"total_max_count\",\n" +
            "              \"version\",\n" +
            "              \"project\"\n" +
            "            ],\n" +
            "            \"connection\": [\n" +
            "              {\n" +
            "                \"jdbcUrl\": \"jdbc:mysql://10.101.23.101:3306/ucar_oam\",\n" +
            "                \"table\": [\n" +
            "                  \"t_api_invoker_auth\"\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"writeMode\": \"replace\",\n" +
            "            \"username\": \"ucar_dep\"\n" +
            "          },\n" +
            "          \"name\": \"mysqlwriter\"\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"setting\": {\n" +
            "      \"errorLimit\": {\n" +
            "        \"record\": 0,\n" +
            "        \"percentage\": 0.02\n" +
            "      },\n" +
            "      \"speed\": {\n" +
            "        \"channel\": \"10\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

}
