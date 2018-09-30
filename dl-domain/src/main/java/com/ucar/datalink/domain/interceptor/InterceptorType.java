package com.ucar.datalink.domain.interceptor;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by lubiao on 2017/3/22.
 */
public enum InterceptorType {
    Script, Class;

    public static List<InterceptorType> getAllInterceptorTypes() {
        return Lists.newArrayList(Script, Class);
    }
}
