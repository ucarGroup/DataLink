package com.ucar.datalink.biz.service;

import java.util.Map;

public interface ElasticSearchService {

    /**
     * 获取es routing信息
     *
     * @return
     */
    Map<String,String> getEsRoutingInfo(String ip, String indexName);

}
