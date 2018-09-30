package com.ucar.datalink.biz.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

/**
 * Created by user on 2017/7/4.
 * HTTP请求的工具类，用来发送一个简单的REST格式的请求，包括GET，带认证的GET，POST三种请求
 */
public class URLConnectionUtil {

    private static final Logger logger = LoggerFactory.getLogger(URLConnectionUtil.class);

    /**
     * 每次连接重试三次
     */
    private static final int RETRY_TIMES = 3;


    public static String retryGET(String serverUrl) {
        for(int i=0;i<RETRY_TIMES;i++) {
            try {
                return get(serverUrl);
            } catch (Exception e) {
                //ignore
            }
        }
        return "{}";
    }

    public static String retryGETWithAuth(String serverUrl,String name,String pass) {
        for(int i=0;i<RETRY_TIMES;i++) {
            try {
                return getWithAuth(serverUrl,name,pass);
            } catch(Exception e) {
                //ignore
            }
        }
        return "{}";
    }

    public static String retryPOST(String serverUrl, String data) {
        for(int i=0;i<RETRY_TIMES;i++) {
            try {
                return post(serverUrl,data);
            } catch (Exception e) {
                //ignore
            }
        }
        return "{}";
    }


    /**
     * 发送一个 GET 请求并等待返回的数据
     * @param serverUrl
     * @return
     */
    public static String get(String serverUrl) {
        StringBuilder responseBuilder = null;
        BufferedReader reader = null;

        URL url;
        try {
            url = new URL(serverUrl);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Content-type", "application/json");
            conn.connect();

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            responseBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
            return responseBuilder.toString().trim();
        } catch (IOException e) {
            logger.error("connect failure",e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("close reader stream failure",e);
                }
            }
        }
        return "{}";
    }


    /**
     * 发送一个 GET 请求并等待返回的数据
     * @param serverUrl
     * @return
     */
    public static String getWithAuth(String serverUrl,String name,String pass) {
        StringBuilder responseBuilder = null;
        BufferedReader reader = null;

        URL url;
        try {
            url = new URL(serverUrl);
            URLConnection conn = url.openConnection();
            String basic = name+":"+pass;
            String authContent = "Basic "+Base64.getEncoder().encodeToString(basic.getBytes());
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("Authorization",authContent);
            conn.connect();

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8" ));
            responseBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
            return responseBuilder.toString().trim();
        } catch (IOException e) {
            logger.error("connect failure",e);
        } catch(Exception e) {
            logger.error("unknown error",e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("close reader stream failure",e);
                }
            }
        }
        return "{}";
    }




    /**
     * 发起一个POST请求，获取数据
     * @param serverUrl
     * @param data
     * @return
     */
    public static String post(String serverUrl, String data) {
        StringBuilder responseBuilder = null;
        BufferedReader reader = null;
        OutputStreamWriter wr = null;
        URL url;
        try {
            url = new URL(serverUrl);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-type", "application/json");
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            responseBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
            return responseBuilder.toString().trim();
        } catch (IOException e) {
           logger.error("connect failure",e);
        } catch(Exception e) {
          logger.error("unknown error",e);
        } finally {
            if (wr != null) {
                try {
                    wr.close();
                } catch (IOException e) {
                    logger.error("close write stream failure",e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("close reader stream failure",e);
                }
            }
        }
        return "{}";
    }

}
