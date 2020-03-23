package com.ucar.datalink.manager.core.web.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * json转换工具类
 *
 * @author wenbin.song
 * @date 2019/03/06
 */
public class JsonUtil {

    /**
     * json转成map
     * @param json
     * @return
     */
    public static Map<String, String> jsonStringToMap(String json) {
        if (StringUtils.isBlank(json)) {
            return new HashMap<>();
        }
        return JSONObject.parseObject(json, Map.class);
    }

}
