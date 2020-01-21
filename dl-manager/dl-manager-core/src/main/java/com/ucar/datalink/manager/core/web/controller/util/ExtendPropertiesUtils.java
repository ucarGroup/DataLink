package com.ucar.datalink.manager.core.web.controller.util;

import com.ucar.datalink.manager.core.boot.ManagerBootStrap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author xy.li
 * @date 2019/06/24
 */
public class ExtendPropertiesUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ExtendPropertiesUtils.class);

    private static String techplatURL;
    private static String datalinkURL;
    static{
        try {
            Properties properties = new Properties();
            String conf = System.getProperty("manager.conf");
            if (!StringUtils.isEmpty(conf)) {
                if (conf.startsWith("classpath:")) {
                    conf = StringUtils.substringAfter(conf, "classpath:");
                    properties.load(ManagerBootStrap.class.getClassLoader().getResourceAsStream(conf));
                } else {
                    properties.load(new FileInputStream(conf));
                }
            }
            techplatURL  = properties.getProperty("techplat.url");
            datalinkURL = properties.getProperty("datalinkmanager.url");
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    public static String getTechplatURL(){
        return techplatURL;
    }

    public static String getDatalink(){
        return datalinkURL;
    }


}
