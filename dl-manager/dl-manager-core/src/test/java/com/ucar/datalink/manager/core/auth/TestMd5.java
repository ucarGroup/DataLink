package com.ucar.datalink.manager.core.auth;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.GregorianCalendar;

public class TestMd5 {

    public static void main(String[] args) throws IOException {

        GregorianCalendar.getInstance();
        //秘钥
        String key = "D@t@link_s3cure_key";
        Long currentTime = 11111111L;

        String sign = DigestUtils.md5Hex( currentTime + key);

        System.out.println("sign：" + sign);

        System.out.println("1467187578000");
        System.out.println( System.currentTimeMillis() );
        //12240000
        //1467187578000
    }

}
