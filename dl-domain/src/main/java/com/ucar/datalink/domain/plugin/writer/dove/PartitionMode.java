package com.ucar.datalink.domain.plugin.writer.dove;

import com.google.common.collect.Lists;

import java.util.List;

public enum PartitionMode {
    COLUMN,TABLE;
    public static List<PartitionMode> getAllPartitionModes() {
        return Lists.newArrayList(COLUMN, TABLE);
    }
}
