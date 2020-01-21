package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.AuthorityDAO;
import com.ucar.datalink.biz.dal.MediaSourceRelationDAO;
import com.ucar.datalink.biz.service.MediaSourceRelationService;
import com.ucar.datalink.domain.media.MediaSourceRelationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yang.wang09 on 2018-10-31 17:15.
 */
@Service
public class MediaSourceRelationServiceImpl implements MediaSourceRelationService {

    @Autowired
    MediaSourceRelationDAO dao;

    @Override
    public List<MediaSourceRelationInfo> findList() {
        return dao.findList();
    }

    @Override
    public Integer insert(MediaSourceRelationInfo mediaSourceInfo) {
        return dao.insert(mediaSourceInfo);
    }

    @Override
    public Integer update(MediaSourceRelationInfo mediaSourceInfo) {
        return dao.update(mediaSourceInfo);
    }

    @Override
    public Integer delete(Long id) {
        return dao.delete(id);
    }

    @Override
    public Integer delete(MediaSourceRelationInfo info) {
        return dao.delete(info);
    }

    @Override
    public MediaSourceRelationInfo getOneById(Long id) {
        return dao.getOneById(id);
    }

    @Override
    public Integer checkExist(MediaSourceRelationInfo info) {
        return dao.checkExist(info);
    }

    @Override
    public List<MediaSourceRelationInfo> findListByVirtualId(Long id) {
        return dao.findListByVirtualId(id);
    }

    @Override
    public MediaSourceRelationInfo getOneByRealMsId(Long realMsId) {
        return dao.getOneByRealMsId(realMsId);
    }

    @Override
    public Integer checkExitOneByVritualMsId(Long virtualId) {
        return dao.checkExitOneByVritualMsId(virtualId);
    }
}
