package com.ucar.datalink.flinker.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 *
 */
public class GsonUtil {
    private GsonUtil(){}

    public static final Gson gson = new Gson();

    public static String toJson(Object obj){
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json,Class<T> clazz){
        return gson.fromJson(json,clazz);
    }

    public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        return gson.fromJson(json, typeOfT);
    }

    public static <T> T fromJson(String json, Class<T> clazz, Type type, Object typeAdapter) {
        return new GsonBuilder().registerTypeAdapter(type, typeAdapter).create().fromJson(json, clazz);
    }
}
