package com.ucar.datalink.biz.service.impl;

import com.ucar.datalink.biz.dal.SysPropertiesDAO;
import com.ucar.datalink.biz.service.SysPropertiesService;
import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by djj on 2018/7/5.
 */
@Service
public class SysPropertiesServiceImpl implements SysPropertiesService {


    @Autowired
    SysPropertiesDAO sysPropertiesDAO;

    @Override
    public Boolean insert(SysPropertiesInfo sysPropertiesInfo) {
        Integer num = sysPropertiesDAO.insert(sysPropertiesInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean update(SysPropertiesInfo sysPropertiesInfo) {
        Integer num = sysPropertiesDAO.update(sysPropertiesInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public SysPropertiesInfo getSysPropertiesById(Long id) {
        SysPropertiesInfo sysPropertiesInfo = sysPropertiesDAO.getSysPropertiesById(id);
        return sysPropertiesInfo;
    }

    public List<SysPropertiesInfo> findSysPropertieList(){
        return sysPropertiesDAO.findSysPropertieList();
    }

    @Override
    public Boolean delete(Long id) {
        Integer num = sysPropertiesDAO.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    public Map<String,String> map(){
        List<SysPropertiesInfo> list = sysPropertiesDAO.findSysPropertieList();
        if(CollectionUtils.isNotEmpty(list)){
            return list.stream().collect(Collectors.toMap(SysPropertiesInfo::getPropertiesKey,SysPropertiesInfo::getPropertiesValue));
        }else{
            return new HashMap<String,String>();
        }
    }

}
