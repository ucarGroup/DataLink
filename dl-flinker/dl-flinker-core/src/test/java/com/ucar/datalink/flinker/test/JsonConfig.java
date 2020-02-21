package com.ucar.datalink.flinker.test;

public class JsonConfig {
    public static String json = "{\n" +
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
            "          \"record\": 5000\n" +
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
            "            \"hadoopUserName\": \"hadoop\",\n" +
            "            \"path\": \"/user/hive-0.13.1/warehouse/bi_zuche.db/rpt_dept_model_income_stat/dt=\\$DATAX_PRE_DATE/*\",\n" +
            "            \"hadoopConfig\": {\n" +
            "              \"dfs.ha.namenodes.hadoop2cluster\": \"n1,n2\",\n" +
            "              \"dfs.namenode.rpc-address.hadoop2cluster.n1\": \"namenode01.bi.10101111.com:8020\",\n" +
            "              \"dfs.namenode.rpc-address.hadoop2cluster.n2\": \"namenode02.bi.10101111.com:8020\",\n" +
            "              \"dfs.nameservices\": \"hadoop2cluster\",\n" +
            "              \"dfs.client.failover.proxy.provider.hadoop2cluster\": \"org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider\"\n" +
            "            },\n" +
            "            \"column\": [\n" +
            "              {\n" +
            "                \"index\": \"0\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"1\",\n" +
            "                \"type\": \"String\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"2\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"3\",\n" +
            "                \"type\": \"String\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"4\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"5\",\n" +
            "                \"type\": \"String\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"6\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"7\",\n" +
            "                \"type\": \"String\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"8\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"9\",\n" +
            "                \"type\": \"String\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"10\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"11\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"12\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"13\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"14\",\n" +
            "                \"type\": \"Double\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"15\",\n" +
            "                \"type\": \"Double\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"16\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"17\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"18\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"19\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"20\",\n" +
            "                \"type\": \"Double\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"21\",\n" +
            "                \"type\": \"Double\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"22\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"23\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"24\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"25\",\n" +
            "                \"type\": \"Long\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"26\",\n" +
            "                \"type\": \"Double\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"27\",\n" +
            "                \"type\": \"Double\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"index\": \"28\",\n" +
            "                \"type\": \"String\"\n" +
            "              }\n" +
            "            ],\n" +
            "            \"defaultFS\": \"hdfs://hadoop2cluster\",\n" +
            "            \"encoding\": \"UTF-8\",\n" +
            "            \"fieldDelimiter\": \",\",\n" +
            "            \"ignoreException\": \"false\",\n" +
            "            \"fileType\": \"orc\"\n" +
            "          },\n" +
            "          \"name\": \"hdfsreader\"\n" +
            "        },\n" +
            "        \"writer\": {\n" +
            "          \"parameter\": {\n" +
            "            \"password\": \"b20e06df566b28ea9f7628dca7d40915\",\n" +
            "            \"column\": [\n" +
            "              \"branch_id\",\n" +
            "              \"branch_name\",\n" +
            "              \"region_id\",\n" +
            "              \"region_name\",\n" +
            "              \"pickup_dept_id\",\n" +
            "              \"pickup_dept_name\",\n" +
            "              \"provide_dept_id\",\n" +
            "              \"provide_dept_name\",\n" +
            "              \"model_id\",\n" +
            "              \"model_name\",\n" +
            "              \"rent_cars\",\n" +
            "              \"rent_orders\",\n" +
            "              \"nowdept_cars\",\n" +
            "              \"nowdept_cars_rentable\",\n" +
            "              \"rent_income\",\n" +
            "              \"return_income\",\n" +
            "              \"rent_cars_tr\",\n" +
            "              \"rent_orders_tr\",\n" +
            "              \"nowdept_cars_tr\",\n" +
            "              \"nowdept_cars_rentable_tr\",\n" +
            "              \"rent_income_tr\",\n" +
            "              \"return_income_tr\",\n" +
            "              \"rent_cars_tz\",\n" +
            "              \"rent_orders_tz\",\n" +
            "              \"nowdept_cars_tz\",\n" +
            "              \"nowdept_cars_rentable_tz\",\n" +
            "              \"rent_income_tz\",\n" +
            "              \"return_income_tz\",\n" +
            "              \"dt\"\n" +
            "            ],\n" +
            "            \"connection\": [\n" +
            "              {\n" +
            "                \"jdbcUrl\": \"jdbc:mysql://10.216.4.73:3306/bi_rpt_zuche\",\n" +
            "                \"table\": [\n" +
            "                  \"rpt_dept_model_income_stat\"\n" +
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
            "      \"adaptiveFieldModify\": \"false\",\n" +
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
