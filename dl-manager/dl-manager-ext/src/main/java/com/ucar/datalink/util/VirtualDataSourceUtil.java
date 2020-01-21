package com.ucar.datalink.util;

import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.MediaSourceRelationService;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceRelationInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.virtual.VirtualMediaSrcParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by yang.wang09 on 2018-10-26 14:19.
 */
public class VirtualDataSourceUtil {

    private static final Logger logger = LoggerFactory.getLogger(VirtualDataSourceUtil.class);

    private static MediaSourceService mediaSourceService;

    private static MediaService mediaService;

    private static MediaSourceRelationService relationService;

    static {
        mediaSourceService = DataLinkFactory.getObject(MediaSourceService.class);
        mediaService = DataLinkFactory.getObject(MediaService.class);
        relationService = DataLinkFactory.getObject(MediaSourceRelationService.class);
    }


    public static MediaSourceInfo filterMediaSourceInfo(MediaSourceInfo info) {
        Set<MediaSourceType> set = new HashSet<>();
        set.add(MediaSourceType.VIRTUAL);
        List<MediaSourceInfo> list = mediaSourceService.getListByType(set);
        Map<Long,MediaSourceInfo> map = new HashMap<>();

        list.forEach( virtual -> {
            VirtualMediaSrcParameter v = virtual.getParameterObj();
            v.getRealDbsId().forEach(num -> map.put(num, virtual));
        });
        if(map.containsKey(info.getId())) {
            MediaSourceInfo virtualInfo = map.get(info.getId());
            return virtualInfo;
        }
        return info;
    }

    public static List<MediaSourceInfo> filterMediaSourceInfoList(List<MediaSourceInfo> infos) {
        Set<MediaSourceType> set = new HashSet<>();
        set.add(MediaSourceType.VIRTUAL);
        List<MediaSourceInfo> list = mediaSourceService.getListByType(set);
        Map<Long,MediaSourceInfo> map = new HashMap<>();
        list.forEach(virtual -> {
            VirtualMediaSrcParameter v = virtual.getParameterObj();
            v.getRealDbsId().forEach(num -> map.put(num,virtual));
        });
        Set<MediaSourceInfo> fiterMediaSourceInfos = new HashSet<>();
        infos.forEach(info -> {
            if(map.containsKey(info.getId())) {
                fiterMediaSourceInfos.add( map.get(info.getId()) );
            } else {
                fiterMediaSourceInfos.add( info );
            }
        });
        return new ArrayList<>(fiterMediaSourceInfos);
    }


    public static MediaSourceInfo getRealMediaSourceInfoById(Long id) {
        MediaSourceInfo info = mediaSourceService.getById(id);
        return mediaService.getRealDataSource(info);
    }


    public static List<MediaSourceInfo> findMediaSourcesForSingleLab(List<MediaSourceType> types) {
        return mediaService.findMediaSourcesForSingleLab(types);
    }

    public static List<MediaSourceInfo> fromRealMediaSourceToAllMediaSource(Long id) {
        boolean isVirtual = relationService.checkExitOneByVritualMsId(id)>=1 ? true:false;
        List<MediaSourceInfo> mediaSourceInfos = new ArrayList<>();
        if( isVirtual ) {
            List<MediaSourceRelationInfo> list = relationService.findListByVirtualId(id);
            list.forEach( info -> {
                mediaSourceInfos.add( mediaSourceService.getById(info.getRealMsId()) );
            });
        } else {
            mediaSourceInfos.add( mediaSourceService.getById(id) );
        }

        return mediaSourceInfos;
    }


    public static List<MediaSourceInfo> findMediaSourcesForAcrossLab(List<MediaSourceType> types) {
        return mediaService.findMediaSourcesForAllAcrossLab(types);
    }

    public static MediaSourceInfo getRealMediaSourceInfoByInfo(MediaSourceInfo targetMediaSourceInfo) {
        return mediaService.getRealDataSource(targetMediaSourceInfo);
    }
}
