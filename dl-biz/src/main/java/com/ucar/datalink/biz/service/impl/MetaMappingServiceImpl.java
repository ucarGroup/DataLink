package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.MetaMappingDAO;
import com.ucar.datalink.biz.service.MetaMappingService;
import com.ucar.datalink.domain.meta.MetaMappingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Created by user on 2017/11/3.
 */
@Service
public class MetaMappingServiceImpl implements MetaMappingService {

    @Autowired
    MetaMappingDAO dao;

    @Override
    public List<MetaMappingInfo> queryAllMetaMapping() {
        return dao.selectAllMetaMapping();
    }

    @Override
    public MetaMappingInfo queryMetaMappingById(long id) {
        return dao.selectMetaMappingById(id);
    }

    @Override
    public void createMetaMapping(MetaMappingInfo info) {
        dao.addMetaMapping(info);
    }

    @Override
    public void modifyMetaMapping(MetaMappingInfo info) {
        dao.updateMetaMapping(info);
    }

    @Override
    public void deleteMetaMappingById(long id) {
        dao.deleteMetaMapping(id);
    }

    @Override
    public List<MetaMappingInfo> queryAllMetaMappingByType(String srcMediaSourceType, String targetMediaSourceType) {
        return dao.selectAllMetaMappingByType(srcMediaSourceType,targetMediaSourceType);
    }
}
