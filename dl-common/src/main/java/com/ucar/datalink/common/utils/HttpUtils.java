package com.ucar.datalink.common.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * http https工具类
 *      有三个公开方法
 *
 *  @author djj
 */
public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    private static final int MAX_TIMEOUT = 30000;
    private static final int REQUEST_RETRY_COUNT = 1; //默认一次重试

    private static final PoolingHttpClientConnectionManager connectionManager;
    private static final RequestConfig requestConfig;
    private static final String CHARSET = "UTF-8";

    static {

        LayeredConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory())
                .build();

        // 设置连接池
        connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 设置连接池大小
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(connectionManager.getMaxTotal());

        requestConfig = RequestConfig.custom()
                // 设置连接超时
                .setConnectTimeout(MAX_TIMEOUT)
                // 设置读取超时
                .setSocketTimeout(MAX_TIMEOUT)
                // 设置从连接池获取连接实例的超时
                .setConnectionRequestTimeout(MAX_TIMEOUT)
                .build();
    }

    private static HttpRequestRetryHandler getDefaultRetryHandler() {
        return new DefaultHttpRequestRetryHandler(REQUEST_RETRY_COUNT, false);
    }

    private static CloseableHttpClient getHttpClient() {

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setRetryHandler(getDefaultRetryHandler())
                .setDefaultRequestConfig(requestConfig)
                .build();

        return httpClient;
    }

    /**
     * 发送 GET 请求（HTTP、HTTPS都支持）
     *
     * @return json
     */
    public static String doGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        return doRequest(url,httpGet);
    }

    /**
     * 发送 POST 请求（HTTP、HTTPS都支持）— 不支持传入header
     *
     * @param json
     * @return json
     */
    public static String doPost(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(json, CHARSET);// 解决中文乱码问题
        stringEntity.setContentEncoding(CHARSET);
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        return doRequest(url,httpPost);
    }

    /**
     * 发送 POST 请求（HTTP、HTTPS都支持）— 支持传入header
     *
     * @param json 请求体
     * @param headerMap 请求头
     * @return json
     */
    public static String doPost(String url, String json, Map<String, String> headerMap) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(json, CHARSET);// 解决中文乱码问题
        stringEntity.setContentEncoding(CHARSET);
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        if(headerMap != null && headerMap.size() > 0){
            Iterator<Map.Entry<String, String>> iterator = headerMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> entry =  iterator.next();
                httpPost.setHeader(entry.getKey(),entry.getValue());
            }
        }
        return doRequest(url,httpPost);
    }

    /**
     * 发送 POST 请求（HTTP、HTTPS都支持）
     *
     * @param map
     * @return json
     */
    public static String doPost(String url, Map<String, Object> map) {
        return doPost(url,map,null);
    }


    /**
     * 发送 POST 请求（HTTP、HTTPS都支持）
     *
     * @param map
     * @return json
     */
    public static String doPost(String url, Map<String, Object> map,Map<String,String> headerMap) {
        List<NameValuePair> pairList = new ArrayList<NameValuePair>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
            pairList.add(pair);
        }
        StringEntity stringEntity = null;
        try {
            stringEntity = new UrlEncodedFormEntity(pairList, CHARSET);
        } catch (UnsupportedEncodingException e) {
            LOGGER.info(e.getMessage());
        }
        HttpPost httpPost = new HttpPost(url);
        if(headerMap != null) {
            for (Map.Entry<String,String> entry : headerMap.entrySet()) {
                httpPost.addHeader(entry.getKey(),entry.getValue());
            }
        }
        httpPost.setEntity(stringEntity);
        return doRequest(url,httpPost);
    }

    /**
     * HTTP、HTTPS都支持
     *
     * @param request
     * @param url
     * @return
     */
    private static String doRequest(String url,HttpRequestBase request) {
        CloseableHttpClient httpClient = getHttpClient();
        HttpEntity entity = null;
        CloseableHttpResponse response = null;
        try {
            request.setConfig(requestConfig);
            response = httpClient.execute(request);
            entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity != null) {
                return EntityUtils.toString(entity, CHARSET);
            }else {
                LOGGER.error("httpclient response. url is:{},status code [{}]",url,response.getStatusLine().getStatusCode());
                try{
                    LOGGER.error("httpclient response. entity toString:{},", EntityUtils.toString(entity, CHARSET));
                }catch (Exception e){
                    LOGGER.info(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("httpclient response. url is:{},errorMsg is:{}\r\n", url,e.getMessage());
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(entity);
                    response.close();
                }
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
            }
        }

        return "";
    }

    /**
     * 获取字节数据，如下载文件
     *
     * @param url
     * @return
     */
    public static byte[] doGetByte(String url,Map<String, Object> map) {

        List<NameValuePair> pairList = new ArrayList<NameValuePair>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
            pairList.add(pair);
        }
        StringEntity stringEntity = null;
        try {
            stringEntity = new UrlEncodedFormEntity(pairList, CHARSET);
        } catch (UnsupportedEncodingException e) {
            LOGGER.info(e.getMessage());
        }
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(stringEntity);

        CloseableHttpClient httpClient = getHttpClient();
        HttpEntity entity = null;
        CloseableHttpResponse response = null;
        try {
            httpPost.setConfig(requestConfig);
            response = httpClient.execute(httpPost);
            entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity != null) {
                if(entity.isStreaming()){
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    IOUtils.copy(entity.getContent(), outputStream);
                    return outputStream.toByteArray();
                }
            }
        } catch (IOException e) {
            LOGGER.error("httpclient respnose. url is:{}", url);
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(entity);
                    response.close();
                }
            } catch (IOException e) {
                LOGGER.info(e.getMessage());
            }
        }

        return null;
    }

}