package com.ucar.datalink.writer.hdfs.handle.stream;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ucar.datalink.common.event.CommonEvent;
import com.ucar.datalink.writer.hdfs.handle.util.Dict;
import org.apache.hadoop.hdfs.protocol.AlreadyBeingCreatedException;
import org.apache.hadoop.ipc.RemoteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 2017/11/21.
 */
public class RemoteUtil {
    private static final Logger logger = LoggerFactory.getLogger(RemoteUtil.class);
    private static final String RECREATE_IDENTIFIER = "because this file is already being created by";

    public static void tryRemoteClose(String hdfsFilePath, Exception e) {
        try {
            if (e instanceof RemoteException) {
                RemoteException re = (RemoteException) e;
                String className = re.getClassName();
                if (className.equals(AlreadyBeingCreatedException.class.getName()) && e.getMessage().contains(RECREATE_IDENTIFIER)) {
                    logger.info("stream remote close begin for file : " + hdfsFilePath);
                    colseInternal(hdfsFilePath, parseIp(e.getMessage()));
                    logger.info("stream remote close end for file : " + hdfsFilePath);
                }
            }
        } catch (Exception ex) {
            logger.error("stream remote close failed for file : " + hdfsFilePath, ex);
        }
    }

    static void colseInternal(String hdfsFilePath, String address) {
        Map<String, String> map = new HashMap<>();
        map.put(CommonEvent.EVENT_NAME_KEY, Dict.EVENT_CLOSE_STREAM);
        map.put(Dict.EVENT_CLOSE_STREAM_FILE_NAME, hdfsFilePath);

        String url = "http://" + address + ":8083" + "/worker/eventProcess";//TODO,暂时写死为8083，后期改为动态获取
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity request = new HttpEntity(JSONObject.toJSONString(map, SerializerFeature.WriteClassName), headers);
        new RestTemplate().postForObject(url, request, Map.class);
    }

    private static String parseIp(String message) {
        Pattern p = Pattern.compile("\\[.*?\\]");
        Matcher m = p.matcher(message);
        String ip = null;
        while (m.find()) {
            ip = m.group().replace("[", "").replace("]", "");
        }
        return ip;
    }
}
