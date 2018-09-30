package com.ucar.datalink.writer.hdfs.handle.stream;

import com.google.common.cache.*;
import com.ucar.datalink.writer.hdfs.handle.config.HdfsConfig;
import org.apache.hadoop.fs.FileSystem;

import java.util.concurrent.TimeUnit;

/**
 * Created by lubiao on 2017/12/14.
 */
public class FileSystemManager {

    private static final LoadingCache<HdfsConfig, FileSystem> fileSystemCache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(24, TimeUnit.HOURS)
            .build(new CacheLoader<HdfsConfig, FileSystem>() {
                @Override
                public FileSystem load(HdfsConfig hdfsConfig) throws Exception {
                    return FileSystem.get(
                            hdfsConfig.getHdfsUri(),
                            hdfsConfig.getConfiguration(),
                            hdfsConfig.getHadoopUser());
                }
            });

    public static FileSystem getFileSystem(HdfsConfig hdfsConfig) {
        return fileSystemCache.getUnchecked(hdfsConfig);
    }
}
