package com.ucar.datalink.util;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by user on 2018/3/9.
 */
public class ConfigReadUtil {

    private static Properties pros = new Properties();

    private ConfigReadUtil() {

    }

    static {
        InputStream is = ConfigReadUtil.class.getClassLoader().getResourceAsStream("datax.properties");

        try {
            pros.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static String getString(String name,String defaultValue) {
        String value = pros.getProperty(name);
        if(StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    public static String getString(String name) {
        String value = pros.getProperty(name);
        if(StringUtils.isBlank(value)) {
            return null;
        }
        return value;
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return getInt(key);
        } catch(Exception e) {
            return defaultValue;
        }
    }

    public static int getInt(String key) {
        String str = getString(key);
        return Integer.parseInt(str);
    }

    public static void main(String[] args) {
        getInt("sss");
    }


}
