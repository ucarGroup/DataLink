package com.ucar.datalink.flinker.core.admin;

import com.ucar.datalink.flinker.core.util.container.CoreConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by user on 2017/7/11.
 *
 * 废弃以前的zk事件监听模式，采用rest server来管理任务的启动，停止
 */
public class DataxRestServerLauncher {

    private static final Logger logger = LoggerFactory.getLogger(DataxRestServerLauncher.class);

        public static void main(String args[]) {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(CoreConstant.DATAX_ADMIN_CONF));

            logger.info("## start the datax admin server.");
            final DataxController controller = new DataxController(properties);
            controller.start();
            logger.info("## the datax admin server is running now ......");

            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    try {
                        logger.info("## stop the datax admin server");
                        controller.stop();
                    } catch (Throwable e) {
                        logger.warn("##something goes wrong when stopping canal Server.", e);
                    } finally {
                        logger.info("## datax admin server is down.");
                    }
                }

            });
        } catch (Throwable e) {
            logger.error("## Something goes wrong when starting up the datax admin Server.", e);
            System.exit(0);
        }

        while(true){
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

}
