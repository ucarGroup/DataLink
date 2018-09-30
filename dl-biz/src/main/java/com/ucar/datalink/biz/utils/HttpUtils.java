package com.ucar.datalink.biz.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class HttpUtils {

    public static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 执行一个HTTP GET请求，返回请求响应的HTML
     *
     * @param url 请求的URL地址
     * @return 返回请求响应的HTML
     * @throws IOException
     */
    public static String doGet(String url, Map<String, String> params) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        url = url + "?" + map2Str(params);
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse httpResponse = client.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = httpResponse.getEntity();
                return EntityUtils.toString(entity, "utf-8");
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return "";
    }

    /**
     * Map对象转为字符串
     *
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String map2Str(Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null) {
            return "";
        }
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String str = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                str = str + key + "=" + value;
            } else {
                str = str + key + "=" + value + "&";
            }
        }
        return str;
    }
}