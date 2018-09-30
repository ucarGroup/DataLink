package com.ucar.datalink.writer.es.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Created by lubiao on 2017/6/20.
 */
public abstract class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper jsonMapper;
    private static final ObjectMapper notNullJsonMapper;
    private static final ObjectMapper prettyJsonMapper;

    static {
        jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        notNullJsonMapper = new ObjectMapper();
        notNullJsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        notNullJsonMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        prettyJsonMapper = new ObjectMapper();
        prettyJsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(SerializationFeature.INDENT_OUTPUT, true);
        prettyJsonMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    }

    public static <T> T parse(String json, Class<T> clazz) {
        try {
            if (StringUtils.isBlank(json))
                return null;
            return jsonMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.info("deserialize json failure: {}", e);
            return null;
        }
    }

    public static <T> T parse(String json, TypeReference<T> clazz) {
        try {
            if (StringUtils.isBlank(json))
                return null;
            return jsonMapper.readValue(json, clazz);
        } catch (IOException e) {
            logger.info("deserialize json failure: {}", e);
            return null;
        }
    }

    public static JsonNode parse(String json) {
        try {
            if (StringUtils.isBlank(json))
                return null;
            return jsonMapper.readTree(json);
        } catch (IOException e) {
            logger.info("deserialize json failure: {}", e);
            return null;
        }
    }

    public static String json(Object obj) {
        return json(obj, jsonMapper);
    }

    public static String compactedJson(Object obj) {
        return json(obj, notNullJsonMapper);
    }

    public static String prettifiedJson(Object obj) {
        return json(obj, prettyJsonMapper);
    }

    private static String json(Object obj, ObjectMapper mapper) {
        try {
            if (obj == null)
                return null;
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.info("serialize json failure: {}", e);
            return null;
        }
    }
}
