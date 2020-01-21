package com.ucar.datalink.domain.plugin.writer.dove;

import com.google.common.collect.Lists;

import java.util.List;

public enum SerializeMode {
    Hessian, Json;
    public static List<SerializeMode> getAllSerializeModes() {
        return Lists.newArrayList(Hessian, Json);
    }
}
