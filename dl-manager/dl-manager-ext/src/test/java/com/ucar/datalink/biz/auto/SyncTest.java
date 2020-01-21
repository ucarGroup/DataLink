package com.ucar.datalink.biz.auto;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.spark.ColumnInfo;
import com.ucar.datalink.biz.spark.HBaseColumnInfo;
import com.ucar.datalink.biz.spark.SyncModifyTableInfo;
import com.ucar.datalink.biz.spark.SyncTableInfo;
import com.ucar.datalink.biz.utils.URLConnectionUtil;
import com.ucar.datalink.util.HttpUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yang.wang09 on 2018-04-23 11:44.
 */
public class SyncTest {



    public static void main(String[] args) throws IOException {
        SyncTest t = new SyncTest();
        //t.go();
        //t.execute();
        //t.hehe();
        t.lai();
    }

    public String msg = "";
    public void go() throws UnsupportedEncodingException {
        //SyncTableInfo t = new
        ColumnInfo c1 = new ColumnInfo();
        c1.setComment("c1");
        c1.setHiveType("aa");
        c1.setName("c_name");
        c1.setType("string");
        c1.setTypeLength("10");
        c1.setTypePrecision("0");

        ColumnInfo c2 = new ColumnInfo();
        c2.setComment("c2");
        c2.setHiveType("bb");
        c2.setName("c_info");
        c2.setType("string");
        c2.setTypeLength("20");
        c2.setTypePrecision("0");


        SyncTableInfo t = new SyncTableInfo();
        t.setApplicant("yang.wang09");
        ColumnInfo[] cs = new ColumnInfo[2];
        cs[0] = c1;
        cs[1] = c2;
        t.setColumns(cs);
        t.setDatabase("test_db");
        t.setDbType("MYSQL");
        t.setHdfsLocation("/user/mysqlhistory/test_db/hehe");
        //t.setIsFull(true);
        //t.setIsIncrement(true);
        t.setTable("hehe");

        String str = JSONObject.toJSONString(t);
        System.out.println(str);
        msg = str;

        String xx = URLEncoder.encode(str,"UTF-8");
        System.out.println("url encode ->"+xx);
    }


    public void execute() throws UnsupportedEncodingException {
        go();
        String addr = "http://sparkcubetest.10101111.com/sparkcube/api/datalink/syncTable";
        String encode = URLEncoder.encode(msg,"UTF-8");
        String data = "data="+encode;
        String token = "&token=cqhswmslixshygmxxfetjyeaxblikr";
        data += token;
        System.out.println(data);
        String result = URLConnectionUtil.post(addr, data);
        System.out.println("result ->" +result);

        //HttpClient client = new
    }


    public static String xx_data = " [{\n" +
            "        \"database\": \"aa\",\n" +
            "        \"table\": \"bb\",\n" +
            "        \"dbType\": \"mysql\",\n" +
            "        \"isFull\": true,\n" +
            "        \"isIncrement\": false,\n" +
            "        \"applicant\": \"yang.wang09@ucarinc.com\",\n" +
            "        \"hdfsLocation\": \"/\",\n" +
            "        \"columns\": [{\n" +
            "                \"name\": \"a1\",\n" +
            "                \"type\": \"int\",\n" +
            "                \"typeLength\": \"5\",\n" +
            "                \"typePrecision\": \"0\",\n" +
            "                \"comment\": \"none\",\n" +
            "                \"hiveType\": \"string\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"name\": \"a2\",\n" +
            "                \"type\": \"varchar\",\n" +
            "                \"typeLength\": \"20\",\n" +
            "                \"typePrecision\": \"0\",\n" +
            "                \"comment\": \"none\",\n" +
            "                \"hiveType\": \"string\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"name\": \"a3\",\n" +
            "                \"type\": \"varchar\",\n" +
            "                \"typeLength\": \"50\",\n" +
            "                \"typePrecision\": \"0\",\n" +
            "                \"comment\": \"none\",\n" +
            "                \"hiveType\": \"string\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }]";
    public void hehe() throws IOException {
        String url = "http://sparkcubetest.10101111.com/sparkcube/api/datalink/syncTable";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        //装填参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("data", xx_data));
        nvps.add(new BasicNameValuePair("token", "cqhswmslixshygmxxfetjyeaxblikr"));

        //设置参数到请求对象中
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        //执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = client.execute(httpPost);
        //获取结果实体
        HttpEntity entity = response.getEntity();
        String body = "";
        if (entity != null) {
            //按指定编码转换结果实体为String类型
            body = EntityUtils.toString(entity, "UTF-8");
        }
        EntityUtils.consume(entity);
        //释放链接
        response.close();

        System.out.println(body);



    }


    String GLOBAL = "[{\n" +
            "\t\"applicant\": \"yang.wang09@ucarinc.com\",\n" +
            "  \t\"database\": \"test_db\",\n" +
            "\t\"dbType\": \"mysql\",\n" +
            "\t\"isFull\": true,\n" +
            "\t\"hdfsLocation\": \"/\",\n" +
            "\t\"isIncrement\": true,\n" +
            "\t\"table\": \"hehe\",\n" +
            "\t\"columns\": [{\n" +
            "\t\t\"colName\": \"h_xx\",\n" +
            "\t\t\"comment\": \"aaaaa\",\n" +
            "\t\t\"familyName\": \"test\",\n" +
            "\t\t\"hiveType\": \"string\"\n" +
            "\t},\n" +
            "  {\n" +
            "\t\t\"colName\": \"h_yy\",\n" +
            "\t\t\"comment\": \"bbbb\",\n" +
            "\t\t\"familyName\": \"test\",\n" +
            "\t\t\"hiveType\": \"string\"\n" +
            "\t}\n" +
            "    ]\n" +
            "}]";
    public void lai() {
        String url = "http://sparkcubetest.10101111.com/sparkcube/api/datalink/syncTable";

        ColumnInfo c1 = new ColumnInfo();
        c1.setComment("c1");
        c1.setHiveType("string");
        c1.setName("c_name");
        c1.setType("string");
        c1.setTypeLength("10");
        c1.setTypePrecision("0");

        ColumnInfo c2 = new ColumnInfo();
        c2.setComment("c2");
        c2.setHiveType("string");
        c2.setName("c_info");
        c2.setType("string");
        c2.setTypeLength("20");
        c2.setTypePrecision("0");


        HBaseColumnInfo h1 = new HBaseColumnInfo();
        h1.setColName("h_xx");
        h1.setComment("aaaaa");
        h1.setFamilyName("test");
        h1.setHiveType("string");

        HBaseColumnInfo h2 = new HBaseColumnInfo();
        h2.setColName("h_yy");
        h2.setComment("bbbb");
        h2.setFamilyName("test");
        h2.setHiveType("string");


        SyncTableInfo t = new SyncTableInfo();
        t.setApplicant("yang.wang09@ucarinc.com");
        ColumnInfo[] cs = new ColumnInfo[2];
        cs[0] = c1;
        cs[1] = c2;
        //cs[0] = h1;
        //cs[1] = h2;

        t.setColumns(cs);
        t.setDatabase("test_db");
        t.setDbType("mysql");
        t.setHdfsLocation("/");
        t.setIsFull(true);
        t.setIsIncrement(true);
        t.setTable("hehe");

        SyncTableInfo[] tt = new SyncTableInfo[1];
        tt[0] = t;
        String msg = JSONObject.toJSONString(tt);
        System.out.println(msg);

        Map<String,String> kv = new HashMap<String,String>();
        kv.put("data",msg);
        kv.put("token","cqhswmslixshygmxxfetjyeaxblikr");

        try {
            String x = HttpUtils.post(url,kv);
            System.out.println(x);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void nan() {
        String url = "http://sparkcubetest.10101111.com/sparkcube/api/datalink/syncColumn";

        ColumnInfo c1 = new ColumnInfo();
        c1.setComment("c1");
        c1.setHiveType("string");
        c1.setName("c_name");
        c1.setType("string");
        c1.setTypeLength("10");
        c1.setTypePrecision("0");

        ColumnInfo c2 = new ColumnInfo();
        c2.setComment("c2");
        c2.setHiveType("string");
        c2.setName("c_info");
        c2.setType("string");
        c2.setTypeLength("20");
        c2.setTypePrecision("0");


        HBaseColumnInfo h1 = new HBaseColumnInfo();
        h1.setColName("h_xx");
        h1.setComment("aaaaa");
        h1.setFamilyName("test");
        h1.setHiveType("string");

        HBaseColumnInfo h2 = new HBaseColumnInfo();
        h2.setColName("h_yy");
        h2.setComment("bbbb");
        h2.setFamilyName("test");
        h2.setHiveType("string");



        SyncModifyTableInfo t = new SyncModifyTableInfo();
        ColumnInfo[] cs = new ColumnInfo[2];
        //cs[0] = c1;
        //cs[1] = c2;
        cs[0] = h1;
        cs[1] = h2;

        t.setColumns(cs);
        t.setDatabase("test_db");
        t.setDbType("mysql");
        t.setTable("hehe");

        SyncModifyTableInfo[] tt = new SyncModifyTableInfo[1];
        tt[0] = t;
        String msg = JSONObject.toJSONString(tt);
        System.out.println(msg);

        Map<String,String> kv = new HashMap<String,String>();
        kv.put("data",xx_data);
        kv.put("token","cqhswmslixshygmxxfetjyeaxblikr");

        try {
            String x = HttpUtils.post(url,kv);
            System.out.println(x);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
