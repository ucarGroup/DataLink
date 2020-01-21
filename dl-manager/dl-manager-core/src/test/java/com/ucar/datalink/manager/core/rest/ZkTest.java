package com.ucar.datalink.manager.core.rest;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.module.RunningData;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.job.TimingParameter;
import com.ucar.datalink.manager.core.boot.ManagerBootStrap;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.manager.core.server.ServerContainer;

import org.I0Itec.zkclient.DataUpdater;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.*;

/**
 * Created by user on 2017/8/17.
 */
public class ZkTest {

    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    private static final Logger logger = LoggerFactory.getLogger(ZkTest.class);

    private static final String DATAX_ADMIN_JOBS_RUNNING = "/datax/admin/jobs/running";


    public static void main(String[] args) throws Exception {
        ZkTest t = new ZkTest();
        //t.go2();
        //t.go3();
        //t.go4();
        //t.go5();
        //t.go8();
        //t.go9();
        //printCallStatck();
        //g2();
        String s = "pwwkew";
        //lengthOfLongestSubstring(s);

        t.fetchMySQL();
    }

    public void fetchMySQL() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://10.104.116.88:3306/test";
        String name = "ucar_dep";
        String pass = "ucar_deptest";
        String sql = "SELECT startlatitude,odograph FROM wokao_xx";
        Connection conn = DriverManager.getConnection(url,name,pass);
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            //double xx = rs.getInt("startlatitude");
            Blob b = rs.getBlob("startlatitude");
            Blob b2 = rs.getBlob("odograph");
            //System.out.println("xx->"+xx+"   b->"+b);
            System.out.println("   b->"+b);
            System.out.println("   b2->"+b2);

            //byte[] bs = b.getBytes(0,(int)b.length());
            //Integer.to
            //BitSet bb = BitSet.valueOf(bs);
            //oibb.


        }
    }


    public static int lengthOfLongestSubstring(String s) {
        if(s==null || "".equals(s)) {
            return 0;
        }
        Map<Character,Integer> map = new HashMap<Character,Integer>();
        int left = 0;
        int res = 0;
        for(int i=0;i<s.length();i++) {
            if( map.containsKey(s.charAt(i)) ) {
                left = Math.max(left,map.get(s.charAt(i))+1);
            }
            map.put(s.charAt(i),i);
            res = Math.max(res,i-left+1);
        }
        return res;
    }


    public static void g2() {
        String id = "12345_test1";
        if(id.endsWith("_test1")) {
            id = id.substring(0,id.length()-"test1".length());
        }
        System.out.println(id);

        //com.ucar.datalink.util.DataxUtil.dynamicChoosenDataxMacheine();
    }

    public static void printCallStatck() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        StringBuilder sb = new StringBuilder();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                //System.out.print(stackElements[i].getClassName()+"/t");
                //System.out.print(stackElements[i].getFileName()+"/t");
                //System.out.print(stackElements[i].getLineNumber()+"/t");
                //System.out.println(stackElements[i].getMethodName());
                //System.out.println("-----------------------------------");

                sb.append(stackElements[i].getClassName()).append("#");
                sb.append(stackElements[i].getMethodName());
                sb.append("(").append(stackElements[i].getFileName());
                sb.append(" ").append(stackElements[i].getLineNumber()).append(")");
                sb.append("\n");
            }
            System.out.println(sb.toString());
        }
    }

    public void go9() {
        TimingParameter p = new TimingParameter();
        p.setJvmMemory("-Xmx5G -Xms5G");
        String x = JSONObject.toJSONString(p);
        System.out.println(x);
    }

    public void go0() {
        String value = "\n" +
                "<label class=\"col-sm-4 control-label\">源库名称</label>\n" +
                "                                <div class=\"col-sm-8\" id=\"srcNameDiv\">\n" +
                "                                    <!--\n" +
                "                                    <select id=\"srcName\" class=\"width-100 chosen-select\" id=\"srcName\" style=\"width:100%\">\n" +
                "                                    </select>\n" +
                "                                    -->\n" +
                "                                    <select id=\"srcName\" class=\"width-100 chosen-select\" id=\"srcName\" style=\"width:100%\">\n" +
                "                                        <option value=\"-1\">全部</option>\n" +
                "                                        <option value='89'>hdfs_test1</option>\n" +
                "                                        <option value='94'>hdfs_wokao_1</option>\n" +
                "                                        <option value='95'>hdfs_wokao_2</option>\n" +
                "                                    </select>\n" +
                "                                </div>";
    }

    public void go8() {
        //-DDATAX_PRE_DATE=2017-09-28 -DDATAX_CURRENT_DATE=2017-09-29 -DDATAX_LAST_EXECUTE_TIME=2017-09-18
        String s1 = System.getProperty("DATAX_PRE_DATE");
        System.out.println(s1);

        String s2 = System.getProperty("DATAX_CURRENT_DATE");
        System.out.println(s2);
    }


    public void go7() {
        //{"ip":"10.104.90.63","jobId":84,"jobName":"job_test_2","jobQueueExecutionId":-1,"pid":12197}

        RunningData rd = new RunningData();
        rd.setIp("10.99.68.165");
        rd.setJobId(1L);
        rd.setJobName("job_test_2");
        rd.setJobQueueExecutionId(-1L);
        rd.setPid(12197);
        String ip = "10.99.68.165:2181";
        ZkClient client = new ZkClient(ip, 20*1000, 20*1000);
        List<String> list = client.getChildren("/");
        for(String s : list) {
            System.out.println(s);
        }
        client.setZkSerializer(new MyStringSerializer());
        client.updateDataSerialized("/datax/admin/jobs/running/job_test_2", new DataUpdater(){
            @Override
            public Object update(Object currentData) {
                return JSONObject.toJSONString(rd);
            }
        });
    }

    public void go6() {
        String content = "{\n" +
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
                "            \"password\": \"canal\",\n" +
                "            \"column\": [\n" +
                "              \"id\",\n" +
                "              \"uuid\",\n" +
                "              \"secret_key\",\n" +
                "              \"update_time\"\n" +
                "            ],\n" +
                "            \"connection\": [\n" +
                "              {\n" +
                "                \"jdbcUrl\": [\n" +
                "                  \"jdbc:mysql://10.104.51.12:3306/lucky_admin\",\n" +
                "                  \"jdbc:mysql://10.104.51.12:3306/lucky_admin\",\n" +
                "                  \"\"\n" +
                "                ],\n" +
                "                \"table\": [\n" +
                "                  \"t_api_client_key\"\n" +
                "                ]\n" +
                "              }\n" +
                "            ],\n" +
                "            \"splitPk\": \"id\",\n" +
                "            \"username\": \"canal\"\n" +
                "          },\n" +
                "          \"name\": \"mysqlreader\"\n" +
                "        },\n" +
                "        \"writer\": {\n" +
                "          \"parameter\": {\n" +
                "            \"hadoopUserName\": \"increment\",\n" +
                "            \"path\": \"@path\",\n" +
                "            \"fileName\": \"t_api_client_key\",\n" +
                "            \"createPathIfNotExist\": true,\n" +
                "            \"compress\": \"snappy\",\n" +
                "            \"column\": [\n" +
                "              \"@column\"\n" +
                "            ],\n" +
                "            \"defaultFS\": \"@hdfsUrl\",\n" +
                "            \"errorRetryTimes\": 100,\n" +
                "            \"writeMode\": \"append\",\n" +
                "            \"fieldDelimiter\": \"\\t\",\n" +
                "            \"fileType\": \"orc\"\n" +
                "          },\n" +
                "          \"name\": \"hdfswriter\"\n" +
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
        String path = "/usr/mysqlhistory/db/table/\\$DATAX_PRE_DATE";

        String json = content.replaceAll("@path",path);
        System.out.println(json);

    }


    public void go5() {
        //jdbc:mysql://10.104.50.32:3306/tcar_order_m[0-3]
        String url = "jdbc:mysql://10.104.50.32:3306/tcar_order_m[0-3]";
        if(url!=null && url.lastIndexOf("/")!=-1) {
            int index = url.lastIndexOf("/");
            String prefix = url.substring(0,index);
            url = prefix + "/" + "hehe";
        }
        System.out.println(url);
    }

    public void go4() {
        String ip = "10.203.1.31:5181";
        ZkClient client = new ZkClient(ip, 20*1000, 20*1000);
        List<String> list = client.getChildren("/");
        for(String s : list) {
            System.out.println(s);
        }
    }


    public void go3() {
        String ip = "10.101.22.119:5181";
        ZkClient client = new ZkClient(ip, 20*1000, 20*1000);
        //com.alibaba.otter.canal.common.zookeeper.ZkClientx client = new com.alibaba.otter.canal.common.zookeeper.ZkClientx();
        client.setZkSerializer(new ZkSerializer(){

            @Override
            public byte[] serialize(Object data) throws ZkMarshallingError {
                try {
                    return ((String) data).getBytes("utf-8");
                } catch (final UnsupportedEncodingException e) {
                    throw new ZkMarshallingError(e);
                }
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                try {
                    return new String(bytes, "utf-8");
                } catch (final UnsupportedEncodingException e) {
                    throw new ZkMarshallingError(e);
                }
            }
        });


        String path = DATAX_ADMIN_JOBS_RUNNING + "/" + "wokao";

        //path = "/datalink/managers/cluster/10.104.106.49@8898";
        String data = client.readData(path, true);
        //client.
        //System.out.println(data);
        RunningData rd = new RunningData();
        if(data != null) {
            //String content = new String(data);
            rd = JSONObject.parseObject(data, RunningData.class);
        }
        System.out.println(rd);
    }



    public void go2() throws IOException {
        Properties props = buildProperties();
        final ServerContainer container = ServerContainer.getInstance();
        container.init(props);
        ManagerConfig config = ManagerConfig.current();
//        ZkUtils zkUtils = null;
//        zkUtils = ZkUtils.init(new ZkConfig("/datax", config.getZkServer(), config.getZkSessionTimeoutMs(), config.getZkConnectionTimeoutMs()));

        //System.out.println(zkUtils.zkRoot());

//        zkUtils = new ZkUtils(config.getZkServer(), config.getZkSessionTimeoutMs(), config.getZkConnectionTimeoutMs());

        ZkClient client = new ZkClient("10.101.22.119:5181", config.getZkSessionTimeoutMs(), 20 * 1000);

        List<String> list = client.getChildren("/datax/admin/workers");
        for(String s : list) {
            System.out.println("     获取到的 datax 机器ip --> "+s);
        }

//        List<String> list = zkUtils.getChildren("/datax/admin/workers");
//        for(String s : list) {
//            System.out.println(s);
//        }

    }

    public void go()throws Exception {
        Properties properties = buildProperties();
        logger.info("## start the datalink manager.");
        //ManagerConfig config = ManagerConfig.fromProps(props, true);
        //ZkUtils zkUtils = ZkUtils.init(new ZkConfig(config.getZkRoot(), config.getZkServer(), config.getZkSessionTimeoutMs(), config.getZkConnectionTimeoutMs()));
        final ServerContainer container = ServerContainer.getInstance();
        container.init(properties);

        String root = DLinkZkUtils.get().zkRoot();
        System.out.println(root);

        List<String> list = DLinkZkUtils.get().zkClient().getChildren("/datax/admin/workers");
        for(String s : list) {
            System.out.println(s);
        }
    }




    private static Properties buildProperties() throws IOException {
        Properties properties = new Properties();
        properties.put("port", 8898);
        properties.put("http.port", 8080);
        String conf = System.getProperty("manager.conf");
        logger.info("Manager Config File Path is :" + conf);
        if (!StringUtils.isEmpty(conf)) {
            if (conf.startsWith(CLASSPATH_URL_PREFIX)) {
                conf = StringUtils.substringAfter(conf, CLASSPATH_URL_PREFIX);
                properties.load(ManagerBootStrap.class.getClassLoader().getResourceAsStream(conf));
            } else {
                properties.load(new FileInputStream(conf));
            }
        }
        return properties;
    }

    public void xx() {
//        PreparedStatement pstmt = null;
//        pstmt.setTimestamp();
    }

}
