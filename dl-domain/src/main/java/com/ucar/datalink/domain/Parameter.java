package com.ucar.datalink.domain;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.Serializable;
import java.util.List;

/**
 * 所有model对象内含的parameter属性的基类.
 * <p>
 * Created by lubiao on 2017/2/23.
 */
public abstract class Parameter implements Serializable {

    public String toJsonString() {
        return JSONObject.toJSONString(this, SerializerFeature.WriteClassName);
    }

    public static String listToJsonString(List list) {
        return JSONObject.toJSONString(list, SerializerFeature.WriteClassName);
    }
}
