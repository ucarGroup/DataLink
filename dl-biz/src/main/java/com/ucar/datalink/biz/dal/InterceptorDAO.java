package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.interceptor.InterceptorInfo;

import java.util.List;

/**
 * Created by user on 2017/3/22.
 */
public interface InterceptorDAO {

    List<InterceptorInfo> getList();

    InterceptorInfo findInterceptorById(Long id);

    Integer insert(InterceptorInfo interceptorInfo);

    Integer update(InterceptorInfo interceptorInfo);

    Integer delete(Long id);
}
