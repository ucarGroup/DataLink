package com.ucar.datalink.writer.es.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceRelationService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceRelationInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.es.EsMediaSrcParameter;
import com.ucar.datalink.writer.es.client.rest.loadBalance.ESConfigVo;
import com.ucar.datalink.writer.es.client.rest.loadBalance.ESMultiClusterManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static ESConfigVo getESConfig(MediaSourceInfo mediaSourceInfo, Long taskId) {
        if (!(mediaSourceInfo.getType() == MediaSourceType.ELASTICSEARCH || mediaSourceInfo.getSimulateMsType() == MediaSourceType.ELASTICSEARCH)) {
            throw new ValidationException(String.format("The MediaSource type of [%s] is not ELASTICSEARCH,can not get ESConfig", mediaSourceInfo.getId()));
        }
        if (mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)) {
            //虚拟数据源，优先取Task所属机房对应的数据源，没有的话再取中心机房的数据源
            mediaSourceInfo = DataLinkFactory.getObject(MediaService.class).getRealDataSourceSpecial(taskId, mediaSourceInfo);
            return cache.getUnchecked(mediaSourceInfo);
        } else {
            return cache.getUnchecked(mediaSourceInfo);
        }

    }

    public static List<ESConfigVo> getESConfigList(MediaSourceInfo mediaSourceInfo) {
        List<ESConfigVo> list = new ArrayList<ESConfigVo>();
        if (!(mediaSourceInfo.getType() == MediaSourceType.ELASTICSEARCH || mediaSourceInfo.getSimulateMsType() == MediaSourceType.ELASTICSEARCH)) {
            throw new ValidationException(String.format("The MediaSource type of [%s] is not ELASTICSEARCH,can not get ESConfig", mediaSourceInfo.getId()));
        }
        if (mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)) {
            //虚拟数据源，优先取Task所属机房对应的数据源，没有的话再取中心机房的数据源
            List<MediaSourceInfo> mediaSourceInfoListlist = DataLinkFactory.getObject(MediaSourceService.class).findRealListByVirtualMsId(mediaSourceInfo.getId());
            if(list!=null){
                return  mediaSourceInfoListlist.stream().map(e->{return cache.getUnchecked(e);}).
                        collect(Collectors.toList());
            }
        } else {
            list.add(cache.getUnchecked(mediaSourceInfo));
        }
        return  list;
    }

    public static void invalidate(MediaSourceInfo mediaSourceInfo) {
        if (mediaSourceInfo.getType().equals(MediaSourceType.VIRTUAL)) {
            //清理虚拟数据源对应的真实数据源缓存
            List<MediaSourceInfo> list = DataLinkFactory.getObject(MediaSourceService.class).findRealListByVirtualMsId(mediaSourceInfo.getId());
            for (MediaSourceInfo info : list) {
                ESConfigVo config = cache.getIfPresent(info);
                if (config != null) {
                    cache.invalidate(info);
                    logger.info("ES config invalidate successfully with mediaSoruceId = " + mediaSourceInfo.getId() + " and labName = " + mediaSourceInfo.getLabName());
                }
            }

            //清理虚拟和真实数据源的对应关系
            DataLinkFactory.getObject(MediaSourceService.class).clearRealMediaSourceListCache(mediaSourceInfo.getId());
            logger.info("ES realListCache invalidate successfully with virtualMediaSoruceId = " + mediaSourceInfo.getId());
        } else {
            ESConfigVo config = cache.getIfPresent(mediaSourceInfo);
            if (config != null) {
                cache.invalidate(mediaSourceInfo);
                logger.info("ES config invalidate successfully with mediaSoruceId = " + mediaSourceInfo.getId() + " and labName = " + mediaSourceInfo.getLabName());
            }
            //如果有对应的虚拟数据源，则清理虚拟数据源对应的真实数据源的MediaSourceInfo缓存
            MediaSourceRelationInfo relationInfo = DataLinkFactory.getObject(MediaSourceRelationService.class).getOneByRealMsId(mediaSourceInfo.getId());
            if (relationInfo != null) {
                DataLinkFactory.getObject(MediaSourceService.class).clearRealMediaSourceListCache(relationInfo.getVirtualMsId());
                logger.info("ES realListCache invalidate successfully with virtualMediaSoruceId = " + relationInfo.getVirtualMsId());
            }
        }
    }
}
