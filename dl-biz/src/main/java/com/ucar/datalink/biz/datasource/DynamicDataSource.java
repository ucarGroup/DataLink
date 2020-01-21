package com.ucar.datalink.biz.datasource;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.service.DoubleCenterService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.zookeeper.DLinkZkPathDef;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.domain.lab.LabInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Zk每秒能支持10万的读请求
 */
public class DynamicDataSource extends AbstractRoutingDataSource{

    /**
     * 10秒后自动清除缓存
     */
    private static final LoadingCache<String, String > dataSourceCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build(
            new CacheLoader<String, String>() {
                @Override
                public String load(String virtualMsId) throws Exception {

                    //中心机房
                    DoubleCenterService doubleCenterService = DataLinkFactory.getObject(DoubleCenterService.class);
                    String centerLab = doubleCenterService.getCenterLab(virtualMsId);
                    //通过中心机房取数据源
                    String path = DLinkZkPathDef.labInfoList;
                    DLinkZkUtils zkUtils = DLinkZkUtils.get();
                    byte[] bytes = zkUtils.zkClient().readData(path, true);
                    if(bytes != null){
                        List<LabInfo> labInfolist = JSONObject.parseArray(new String(bytes), LabInfo.class);
                        for(LabInfo labInfo : labInfolist){
                            if(StringUtils.equals(labInfo.getLabName(), centerLab)){
                                return labInfo.getDataSource();
                            }
                        }
                    }

                    //第一次给默认数据源
                    return "dataSourceLogicA";
                }
            }
    );

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceName = dataSourceCache.getUnchecked(Constants.DB_DATALINK);
        return dataSourceName;
    }

}
