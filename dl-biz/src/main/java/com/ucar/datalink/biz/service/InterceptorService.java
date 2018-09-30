package com.ucar.datalink.biz.service;

import com.ucar.datalink.domain.interceptor.InterceptorInfo;

import java.util.List;

/**
 * Created by user on 2017/3/22.
 */
public interface InterceptorService {

    List<InterceptorInfo> getList();

    InterceptorInfo getInterceptorById(Long id);

    Boolean insert(InterceptorInfo interceptorInfo);

    Boolean update(InterceptorInfo interceptorInfo);

    Boolean delete(Long id);
}
