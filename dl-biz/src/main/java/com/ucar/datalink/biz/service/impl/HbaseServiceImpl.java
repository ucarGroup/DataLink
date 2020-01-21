package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.HbaseService;
import com.ucar.datalink.common.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HbaseServiceImpl implements HbaseService {
    private static final Logger logger = LoggerFactory.getLogger(HbaseServiceImpl.class);
    private static final String USER = "tomcat";

    @Value("${biz.hbase.url}")
    private String hbase_url;

    @Value("${biz.zkServers.datalink}")
    private String zkServer_datalink;

/*    @Value("${biz.hbase.peerId}")
    private String hbase_peerId;*/

    public String doAddTable(String sourceClusterZk, String sourceClusterZkBasePath, String targetClusterZk,
                             String targetClusterZkBasePath, String peerId, String tableName) {
        Map<String, Object> parameterMap = new HashMap<>(8);
        parameterMap.put("sourceClusterZk", sourceClusterZk);
        parameterMap.put("sourceClusterZkBasePath", sourceClusterZkBasePath);
        parameterMap.put("targetClusterZk", targetClusterZk);
        parameterMap.put("targetClusterZkBasePath", targetClusterZkBasePath);
        parameterMap.put("peerId", peerId);
        parameterMap.put("tableName", tableName);
        parameterMap.put("user", USER);

        logger.info("开启表复制请求参数:" + JSONObject.toJSON(parameterMap).toString());

        return HttpUtils.doPost(hbase_url + "datalinkSync/addTableToPeer", parameterMap);
    }

    public String doGetTables(String sourceClusterZk, String sourceClusterZkBasePath, String targetClusterZk,
                              String targetClusterZkBasePath, String peerId) {
        Map<String, Object> parameterMap = new HashMap<>(8);
        parameterMap.put("sourceClusterZk", sourceClusterZk);
        parameterMap.put("sourceClusterZkBasePath", sourceClusterZkBasePath);
        parameterMap.put("targetClusterZk", targetClusterZk);
        parameterMap.put("targetClusterZkBasePath", targetClusterZkBasePath);
        parameterMap.put("peerId", peerId);
        parameterMap.put("locate", "true");

        logger.info("检测是否开启表复制请求参数:" + JSONObject.toJSON(parameterMap).toString());

        String result = HttpUtils.doPost(hbase_url + "datalinkSync/getPeerTables", parameterMap);

        logger.info("检测结果信息:" + result);

        return result;
    }

    public String getZkServer_datalink() {
        return zkServer_datalink;
    }
/*
    public String getHbase_peerId() {
        return hbase_peerId;
    }*/

}
