package com.ucar.datalink.worker.api.util.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class path 类文件扫描器
 *
 * @author lubiao
 */
public class ClasspathClassScanner implements ClassScanner {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathClassScanner.class);

    @Override
    public Class<?> scan(String className) {
        try {
            //必须从线程上线文获取classloader，即：取插件的classloader，不能用Class.getClassLoader
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            logger.error("ERROR ## can not found this class ,the name = " + className);
        }

        return null;
    }

}
