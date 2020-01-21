package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.meta.ElasticSearchUtil;
import com.ucar.datalink.biz.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Value("${biz.elasticSearch.routing.url}")
    private String esRoutingUrl;

    /**
     * 获取es routing信息
     *
     * @return
     */
    @Override
    public Map<String,String> getEsRoutingInfo(String ip, String indexName){
        return ElasticSearchUtil.getEsRoutingInfo(ip,indexName,esRoutingUrl);
    }

}
