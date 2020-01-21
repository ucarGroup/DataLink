package com.ucar.datalink.manager.core.auth;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.common.utils.DbConfigEncryption;
import com.ucar.datalink.manager.core.web.controller.login.UserLoginController;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 2017/10/20.
 */
public class AuthTest {


    public static void main(String[] args) {
        //go();
        byte[] buf = Base64.getEncoder().encode("123456aB@".getBytes());
        System.out.println( new String(buf) );
        //parse();
        //go2();
        go3();
    }

    @Test
    public void decrypt() {
        System.out.println(DbConfigEncryption.decrypt("fbae5687eca8d53e39722c1b448c821a"));
    }

    public static void go3() {
        //String x = "$DATAX_SPCIFIED_PRE_DATE";
/*
        String json = ZZ.JSON;
        String reglar = "DATAX_SPCIFIED_PRE_DATE@@[0-9]*@@";
        json = "sssssssssssssssss@@11@@sssss";
        json = ZZ.JSON;
*/

        /*//boolean match = Pattern.matches(reglar, json);
        Pattern pattern = Pattern.compile(reglar);
        Matcher match = pattern.matcher(json);
        boolean find = match.find();
        //match.
        String newJson = match.replaceFirst("2019-02-02");
        //String str = json.replace(json,"2019-02-01");
        System.out.println(newJson);*/
    }


    public static void go2() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = new Date();
        String dateFormat = sdf.format(d);
        System.out.println(dateFormat);

        int day = 100;
        Calendar no = Calendar.getInstance();
        no.setTime(d);
        no.set(Calendar.DATE, no.get(Calendar.DATE) - day);
        Date d2 = no.getTime();
        String d2Str = sdf.format(d2);
        System.out.println(d2Str);


    }


    public static void go() {
        String email = "yang.wang09";
        String pass = "123456aB@";
       // boolean b = UserLoginController.ucarPassportCheck(email, pass);
       // System.out.println("结果 -> "+b);

    }



    public static final String xx = "{\"time\": \"2017-10-25T11:47:53+08:00\", \"proxy_add_x_forwarded\": \"101.90.255.67, 100.116.237.18\", \"upstream_response_time\": \"0.051\", \"bytes_sent\": \"643\", \"status\": \"200\", \"cookie_USERID\": \"-\", \"cookie_userCode\": \"-\", \"cookie_webCode\": \"-\", \"cookie_xhRecord\": \"-\", \"cookie_xhCode\": \"-\", \"cookie_device\": \"-\", \"cookie_dsUser\": \"-\", \"cookie_xhWebStat\": \"-\", \"XH_CLIENT_DATA\": \"%7B%22device%22%3A%22and%23vivo%20X9%237.1.1%235.6.5%231080%231920%23developer.vivo.com.cn%234G_LTE%23811%23com.xiangha%231%2372593269861%22%2C%22xhCode%22%3A%22938f931b6a6e2d7ea0d211de59a25412%22%2C%22umCode%22%3A%22AhLKPzVjEAL3KltDiMnRU2u53KCQqniNlIJEjPxhXBwL%22%2C%22xgCode%22%3A%2217a8c4715fcea1e347ee9f9e118b6aa0a078cfec%22%2C%22geo%22%3A%22121.738194%2331.352529%22%7D\", \"XH_PARAMETER\": \"CV96xWueBOjKZ89sSpZF\\x5C/olnS7QJ52giTInJ\\x5C+pF\\x5C/\\x5C/9nBi7ckfFkeICoS62n42qMTgVQXkr2MmLCeuVmean5H9CwLNjy5i\\x5C+XNMwojiQBzN\\x5C+d32GULZj5H6Uf1OF8lqViwZ\\x5C/Yj6wX851y00\\x5C/gP9PKFJqMkPwjE1hydmgLQe4KEhZw=_5Ez2gmc5Md9nLSqXe4BMvyTvDDo8o\\x5C+VK\\x5C/Xbt0S5L7WyZIT5Bo0wSABshWS0OAn7wu9tANrkdMWvV5QzTPwxqlCSC\\x5C/GFQ7s2fn66xL0aPPp9iVB4hnUeuucaTxuoBn\\x5C+8X4jM5QLTs\\x5C+VCE6kRsJ7dT6FrPIN93stlwx85Xtbj8DuM=\", \"http_host\": \"apph5_api.xiangha.com\", \"request\": \"POST /Main7/Dish/getDishQaUserInfo HTTP/1.0\", \"request_body\": \"code=90034370\", \"http_referer\": \"-\", \"http_user_agent\": \"Mozilla/5.0 (Linux; Android 7.1.1; vivo X9 Build/NMF26F; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/55.0.2883.91 Mobile Safari/537.36\", \"request_time\": \"0.053\", \"upstream_addr\": \"10.251.192.118:9000\", \"SLB_ID\": \"lb-2ze7pxk0w2v0cs3jbn9p8\", \"SLB_IP\": \"47.95.168.34\", \"SLB_X_Forwarded_Proto\": \"https\"}";

    public static void parse() {
        Object x = JSONObject.parseObject(xx);
        System.out.println(x);
    }

}
