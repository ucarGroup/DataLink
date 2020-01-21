package com.ucar.datalink.biz.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ucar.datalink.biz.dal.LabDAO;
import com.ucar.datalink.biz.service.LabService;
import com.ucar.datalink.domain.lab.LabInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by djj on 2018/8/15.
 */
@Service
public class LabServiceImpl implements LabService {

    private LoadingCache<String, LabInfo> labInfoCache;

    @Autowired
    LabDAO labDAO;

    public LabServiceImpl() {
        labInfoCache = CacheBuilder.newBuilder().build(new CacheLoader<String, LabInfo>() {
            @Override
            public LabInfo load(String key) throws Exception {
                LabInfo labInfo = labDAO.getLabByName(key);
                return labInfo;
            }
        });
    }

    @Override
    public Boolean insert(LabInfo labInfo) {
        Integer num = labDAO.insert(labInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean update(LabInfo labInfo) {
        Integer num = labDAO.update(labInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean updateCenterLab(LabInfo labInfo) {
        Integer num = labDAO.updateCenterLab(labInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public LabInfo getLabById(Long id) {
        LabInfo labInfo = labDAO.getLabById(id);
        return labInfo;
    }

    public List<LabInfo> findLabList(){
        return labDAO.findLabList();
    }

    @Override
    public LabInfo getLabByName(String name) {
        return labInfoCache.getUnchecked(name);
    }

    @Override
    public Boolean delete(Long id) {
        Integer num = labDAO.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }


}
