package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.meta.MetaMappingInfo;

import java.util.List;

/**
 * Created by user on 2017/11/3.
 */
public interface MetaMappingService {

    public List<MetaMappingInfo> queryAllMetaMapping();

    public MetaMappingInfo queryMetaMappingById(long id);

    public void createMetaMapping(MetaMappingInfo info);

    public void modifyMetaMapping(MetaMappingInfo info);

    public void deleteMetaMappingById(long id);

    public List<MetaMappingInfo> queryAllMetaMappingByType(String srcMediaSourceType, String targetMediaSourceType);

}
