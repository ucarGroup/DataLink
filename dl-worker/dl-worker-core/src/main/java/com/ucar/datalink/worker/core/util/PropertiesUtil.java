package com.ucar.datalink.worker.core.util;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 06/02/2018.
 */
public class PropertiesUtil {
    private static Map<String, Properties> propertiesMap = new ConcurrentHashMap<>();


    /**
     * 获取properties文件内容
     *
     * @param propertiesPath :绝对路径
     * @return
     */
    public static Properties getProperties(String propertiesPath) throws IOException {

        Properties resultProperties = propertiesMap.get(propertiesPath);

        if (resultProperties == null) {
            Resource resource = new PathResource(propertiesPath);
            resultProperties = PropertiesLoaderUtils.loadProperties(resource);

            if (resultProperties != null)
                propertiesMap.put(propertiesPath, resultProperties);
        }

        return resultProperties;
    }

    /**
     * @Description:
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 4:38 PM 06/02/2018
     * @param  propertiesPath :绝对路径
     */
    public static void updateProperties(String propertiesPath, String content) throws IOException {

        OutputStreamWriter outputStreamWriter = null;

        try {
            OutputStream outputStream = new FileOutputStream(propertiesPath);
            outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            outputStreamWriter.write(content.toString());
        } finally {
            propertiesMap.clear();
            if (outputStreamWriter != null)
                outputStreamWriter.close();
        }

    }

    /**
     * @Description:
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 4:38 PM 06/02/2018
     * @param  propertiesPath :绝对路径
     */
    public static void updatePropertiesByMap(String propertiesPath, Map<String, String> content) throws IOException {
        Properties prop = new Properties();

        for (Map.Entry entry : content.entrySet()) {

            prop.setProperty((String) entry.getKey(), (String) entry.getValue());
        }

        FileOutputStream outputFile = null;
        try {
            outputFile = new FileOutputStream(propertiesPath);
            prop.store(outputFile, "modify " + new Date());
            outputFile.flush();

        } finally {
            propertiesMap.clear();
            if (outputFile != null)
                outputFile.close();
        }
    }

}
