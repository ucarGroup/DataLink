package com.ucar.datalink.writer.hdfs.handle.util;

import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by sqq on 2017/7/25.
 */
public class FileLockUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileLockUtils.class);

    private static final LoadingCache<String, ReentrantLock> lockCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(24, TimeUnit.HOURS)
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    logger.info(String.format("Lock for [%s] was removed , cause is [%s]",
                                    notification.getKey(),
                                    notification.getCause())
                    );
                }
            })
            .build(new CacheLoader<String, ReentrantLock>() {
                @Override
                public ReentrantLock load(String key) throws Exception {
                    return new ReentrantLock();
                }
            });

    public static ReentrantLock getLock(String hdfsFilePath) {
        return lockCache.getUnchecked(hdfsFilePath);
    }
}
