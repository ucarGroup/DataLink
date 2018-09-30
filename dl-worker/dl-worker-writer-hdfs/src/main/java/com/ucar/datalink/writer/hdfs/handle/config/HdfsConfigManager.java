package com.ucar.datalink.writer.hdfs.handle.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lubiao on 2017/11/21.
 */
public class HdfsConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(HdfsConfigManager.class);

    private static LoadingCache<LoadingKey, HdfsConfig> cache = CacheBuilder
            .newBuilder()
            .build(new CacheLoader<LoadingKey, HdfsConfig>() {
                @Override
                public HdfsConfig load(LoadingKey key) throws Exception {
                    HdfsConfig config = new HdfsConfig(key.mediaSourceInfo, key.hdfsWriterParameter);
                    logger.info("Hdfs Config is created for MediaSource : " + key.mediaSourceInfo.getId());
                    return config;
                }
            });

    public static HdfsConfig getHdfsConfig(MediaSourceInfo mediaSourceInfo, HdfsWriterParameter hdfsWriterParameter) {
        return cache.getUnchecked(
                new LoadingKey(mediaSourceInfo, hdfsWriterParameter)
        );
    }

    private static class LoadingKey {
        private MediaSourceInfo mediaSourceInfo;
        private HdfsWriterParameter hdfsWriterParameter;

        public LoadingKey(MediaSourceInfo mediaSourceInfo, HdfsWriterParameter hdfsWriterParameter) {
            this.mediaSourceInfo = mediaSourceInfo;
            this.hdfsWriterParameter = hdfsWriterParameter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LoadingKey loadingKey = (LoadingKey) o;

            return mediaSourceInfo.equals(loadingKey.mediaSourceInfo);

        }

        @Override
        public int hashCode() {
            return mediaSourceInfo.hashCode();
        }
    }
}
