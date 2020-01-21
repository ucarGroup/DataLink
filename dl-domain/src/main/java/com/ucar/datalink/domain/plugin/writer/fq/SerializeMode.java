package com.ucar.datalink.domain.plugin.writer.fq;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by qianqian.shi on 2018/5/22.
 */
public enum SerializeMode {
    Hessian, Json;
    public static List<SerializeMode> getAllSerializeModes() {
        return Lists.newArrayList(Hessian, Json);
    }
}
