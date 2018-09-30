package com.ucar.datalink.manager.core.boot;

import com.ucar.datalink.manager.core.server.ServerContainer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by lubiao on 2016/12/6.
 *
 * Datalink Manager进程的启动类.
 */
public class ManagerBootStrap {
    private static final Logger logger = LoggerFactory.getLogger(ManagerBootStrap.class);
    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    public static void main(String args[]) {
        ManagerBootStrap bootStrap = new ManagerBootStrap();
        bootStrap.boot(args);
    }

    public void boot(String args[]) {
        try {
            Properties properties = buildProperties();

            logger.info("## start the datalink manager.");
            final ServerContainer container = ServerContainer.getInstance();
            container.init(properties);
            container.startup();
            logger.info("## the datalink manager is running now ......");
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    try {
                        logger.info("## stop the datalink manager");
                        container.shutdown();
                    } catch (Throwable e) {
                        logger.warn("##something goes wrong when stopping datalink manager:", e);
                    } finally {
                        logger.info("## datalink manager is down.");
                    }
                }
            });
        } catch (Throwable e) {
            logger.error("## Something goes wrong when starting up the datalink manager:", e);
            System.exit(1);
        }
    }

    private static Properties buildProperties() throws IOException {
        Properties properties = new Properties();
        properties.put("port", 8898);
        properties.put("http.port", 8080);
        String conf = System.getProperty("manager.conf");
        logger.info("Manager Config File Path is :" + conf);
        if (!StringUtils.isEmpty(conf)) {
            if (conf.startsWith(CLASSPATH_URL_PREFIX)) {
                conf = StringUtils.substringAfter(conf, CLASSPATH_URL_PREFIX);
                properties.load(ManagerBootStrap.class.getClassLoader().getResourceAsStream(conf));
            } else {
                properties.load(new FileInputStream(conf));
            }
        }
        return properties;
    }
}
