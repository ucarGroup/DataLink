package com.ucar.datalink.worker.api.util.scan;

/**
 * 用于扫描classpath下面和外部文件系统中类.
 *
 * @author lubiao
 */
public interface ClassScanner {

    Class<?> scan(String className);
}
