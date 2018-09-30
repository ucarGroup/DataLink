package com.ucar.datalink.biz.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.common.zookeeper.ManagerMetaData;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by csf on 17/4/27.
 */
public class RestServerUtils {

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final LoadingCache<String, ManagerMetaData> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(10L, TimeUnit.SECONDS)
            .build(new CacheLoader<String, ManagerMetaData>() {
                @Override
                public ManagerMetaData load(String key) throws Exception {
                    byte[] bytes = DLinkZkUtils.get().zkClient().readData(DLinkZkPathDef.ManagerActiveNode, true);
                    if (bytes != null) {
                        return JSON.parseObject(bytes, ManagerMetaData.class);
                    }
                    return null;
                }
            });

    public static void executeRemote(Object obj, String url) throws Exception {
        ManagerMetaData managerMetaData = cache.getUnchecked(DLinkZkPathDef.ManagerActiveNode);
        if (managerMetaData != null) {
            String prefix = "http://" + managerMetaData.getAddress() + ":" + managerMetaData.getHttpPort();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity request = new HttpEntity(obj != null ? JSONObject.toJSONString(obj, SerializerFeature.WriteClassName) : null, headers);
            restTemplate.postForObject(prefix + url, request, Map.class);
        }
    }
}
