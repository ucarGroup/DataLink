package com.ucar.datalink.writer.hdfs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.domain.plugin.writer.hdfs.FileSplitMode;
import com.ucar.datalink.domain.plugin.writer.hdfs.FileSplitStrategy;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsFileParameter;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by sqq on 2017/7/25.
 */
public class StringTest {

    @Test
    public void testString() {
        /*String[] array = {"aaa", "bbb"};
        List<String> arrayList = new ArrayList<>();
        Collections.addAll(arrayList, array);
        String ss = String.join(":", arrayList);
        System.out.println(ss);*/

        String table = "t_oss_vehicle_dispatch_0001";
        String sub = table.substring(table.length()-5, table.length());
        System.out.println(sub);
        if (sub.startsWith("_0")) {
            table = table.substring(0, table.length()-5);
        }
        System.out.println(table);
//        String s = "[22-36]";
//        int begin = s.indexOf("[");
//        int middle = s.indexOf(",", begin + 1);
//        int end = s.indexOf("]", middle + 1);
//        int skipIdBegin = Integer.parseInt(s.substring(begin + 1, middle));
//        int skipIdEnd = Integer.parseInt(s.substring(middle + 1, end));
//        System.out.println(skipIdBegin);
//        System.out.println(skipIdEnd);
//        int keyVal = Integer.parseInt("23");
//        if ((skipIdBegin <= keyVal) && (keyVal <= skipIdEnd)) {
//            System.out.println("intercept!");
//        }
//
//        String[] key = {"12345.[d,h]", "sdfsdfsd[]"};
//        System.out.println(Arrays.toString(key));
//
//        String sss = "10";
//        System.out.println(Arrays.toString(sss.split("\\|")));
    }

    @Test
    public void testJson() {
        //list转换为json
//        List<CustPhone> list = new ArrayList<CustPhone>();
//        String str= JSON.toJSON(list).toString();

        //json转换为list
//        List<FileSplitStrategy> lists = new ArrayList<FileSplitStrategy>();
//        lists = JSONObject.parseArray(jasonArray, FileSplitStrategy.class);

        //String->JSONObject
        String ldapuser="{\"admin\":false,\"authorityC\":false,\"authorityChain\":false,\"authorityL\":false,\"authorityS\":false,\"enbaled\":false,\"loginId\":\"qianqian.shi\",\"userName\":\"qianqian.shi\",\"xUser\":false}";
        JSONObject jsonObject = JSON.parseObject(ldapuser);
        System.out.println(jsonObject);
        System.out.println(jsonObject.get("userName"));
    }

    @Test
    public void testFastJson() {
        HdfsFileParameter hdfsFileParameter = new HdfsFileParameter();
        FileSplitStrategy fileSplitStrategy = new FileSplitStrategy();
        Date date = new Date();
        fileSplitStrategy.setEffectiveDate(date);
        fileSplitStrategy.setFileSplitMode(FileSplitMode.HALFHOUR);
        List<FileSplitStrategy> list = new ArrayList<>();
        list.add(fileSplitStrategy);
        hdfsFileParameter.setFileSplitStrategieList(list);
        String parameter = hdfsFileParameter.toJsonString();

        List<Long> list1 = new ArrayList<>();
        list1.add(12L);
        list1.add(13L);
        System.out.println(list1.toString());
    }

    @Test
    public void testLogger() {
        Logger logger = LoggerFactory.getLogger(StringTest.class);
        logger.error("hdfs write data error:" + new RuntimeException("hhhhhhh"));
    }

    @Test
    public void testBigInteger() {
        // create 3 BigInteger objects
        BigInteger bi1, bi2, bi3;

        // assign values to bi1, bi2
        bi1 = new BigInteger("123");
        bi2 = new BigInteger("50");

        // perform add operation on bi1 using bi2
        bi3 = bi1.add(bi2);

        String str = "Result of addition is " + bi3;

        // print bi3 value
        System.out.println(str);
        System.out.println(bi1);
        System.out.println(bi2);
    }

    @Test
    public void testLong() {
        Long l1 = new Long("127");
        Long l2 = new Long("127");
        System.out.println(l1 == l2);

        Long l3 = Long.valueOf("127");
        Long l4 = Long.valueOf("127");
        System.out.println(l3 == l4);

        Long l5 = 127L;
        Long l6 = 127L;
        System.out.println(l5 == l6);

        Long l7 = 128L;
        Long l8 = 128L;
        System.out.println(l7 == l8);


        long ll = Long.MIN_VALUE;
        String s = String.valueOf(ll);
        System.out.println(ll == Long.parseLong(s));
    }

    @Test
    public void testDate() {
        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.get(Calendar.YEAR));
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime yesterday = localDateTime.minusDays(1);
        System.out.println(localDateTime.getYear());
        System.out.println(localDateTime);
        System.out.println(yesterday);
    }

    @Test
    public void testLine() {
        String line = "abcdebcfg";
        String word = "bc";
        int index = -1;
        int counter = 0;
        while (line.length() >= word.length() && (index = line.indexOf(word)) >= 0) {
            counter++;
            System.out.println(line);
            line = line.substring(index + word.length());
            System.out.println(index);
        }
        System.out.println(counter);
    }

    @Test
    public void stringTest() throws Exception{
        String aString = "a1b3c5";
        String[] strArray = aString.split("\\s");
        System.out.println(Arrays.toString(strArray));

        String sub = aString.substring(1, 4);//outlength = 4-1
        System.out.println(sub);

        String repeated = StringUtils.repeat(aString, 2);//repeat 2 times
        System.out.println(repeated);

        int n = StringUtils.countMatches("11112222", "1");
        System.out.println(n);

        String str = "Sep 17, 2013";
        Date date = new SimpleDateFormat("MMMM d, yy", Locale.ENGLISH).parse(str);
        System.out.println(date);
//Tue Sep 17 00:00:00 EDT 2013
    }

    @Test
    public void blobTest() {
        Object param = "2018-11-01";
        byte[] bytes = (byte[]) param;//java.lang.ClassCastException: java.lang.String cannot be cast to [B
        System.out.println(bytes);
    }

}
