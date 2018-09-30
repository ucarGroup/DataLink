package com.ucar.datalink.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.dal.InterceptorDAO;
import com.ucar.datalink.biz.dal.MediaDAO;
import com.ucar.datalink.biz.service.InterceptorService;
import com.ucar.datalink.common.errors.ValidationException;
import com.ucar.datalink.domain.interceptor.InterceptorInfo;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by user on 2017/3/22.
 */
@Service
public class InterceptorServiceImpl implements InterceptorService {

    @Autowired
    private InterceptorDAO interceptorDAO;

    @Autowired
    private MediaDAO mediaDAO;

    @Override
    public List<InterceptorInfo> getList() {
        return interceptorDAO.getList();
    }

    @Override
    public InterceptorInfo getInterceptorById(Long id) {
        return interceptorDAO.findInterceptorById(id);
    }

    @Override
    public Boolean insert(InterceptorInfo interceptorInfo) {
        Integer num = interceptorDAO.insert(interceptorInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean update(InterceptorInfo interceptorInfo) {
        Integer num = interceptorDAO.update(interceptorInfo);
        if (num > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean delete(Long id) {
        checkAvailable(id);
        Integer num = interceptorDAO.delete(id);
        if (num > 0) {
            return true;
        }
        return false;
    }

    private void checkAvailable(Long interceptorId) {
        List<MediaMappingInfo> mediaMappingInfos = mediaDAO.findMediaMappingsByInterceptorId(interceptorId);
        if (mediaMappingInfos.size() > 0) {
            throw new ValidationException(String.format("该拦截器正在应用于id为%s的映射，不能执行删除操作！",
                    JSONObject.toJSONString(mediaMappingInfos.stream().map(m -> m.getId()).distinct().collect(Collectors.toList()))));
        }
    }
}
