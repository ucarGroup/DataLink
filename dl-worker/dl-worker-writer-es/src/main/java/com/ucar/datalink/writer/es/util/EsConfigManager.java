package com.ucar.datalink.writer.es.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.es.EsMediaSrcParameter;
import com.ucar.datalink.writer.es.client.rest.loadBalance.ESConfigVo;
import com.ucar.datalink.writer.es.client.rest.loadBalance.ESMultiClusterManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lubiao on 2017/6/20.
 */
public class EsConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(EsConfigManager.class);

    private static final LoadingCache<MediaSourceInfo, ESConfigVo> cache = CacheBuilder.newBuilder().build(
            new CacheLoader<MediaSourceInfo, ESConfigVo>() {
                @Override
                public ESConfigVo load(MediaSourceInfo key) throws Exception {
                    EsMediaSrcParameter parameter = key.getParameterObj();
                    ESConfigVo esConfigVo = new ESConfigVo();
                    esConfigVo.setUser(parameter.getUserName());
                    esConfigVo.setPass(parameter.getPassword());
                    esConfigVo.setHosts(parameter.getClusterHosts());
                    esConfigVo.setHttp_port(parameter.getHttpPort());
                    esConfigVo.setTcp_port(parameter.getTcpPort());
                    esConfigVo.setClusterName(key.getName());
                    ESMultiClusterManage.addESConfigs(Lists.newArrayList(esConfigVo));
                    return esConfigVo;
                }
            }
    );

    public static ESConfigVo getESConfig(MediaSourceInfo mediaSourceInfo) {
        if (mediaSourceInfo.getType() != MediaSourceType.ELASTICSEARCH) {
            throw new ValidationException(String.format("The MediaSource type of [%s] is not ELASTICSEARCH,can not get ESConfig", mediaSourceInfo.getId()));
        }
        return cache.getUnchecked(mediaSourceInfo);
    }

    public static void invalidate(MediaSourceInfo mediaSourceInfo) {
        cache.invalidate(mediaSourceInfo);
        logger.info(String.format("ESConfig for MediaSource [%s] is cleared.", mediaSourceInfo.getId()));
    }
}
