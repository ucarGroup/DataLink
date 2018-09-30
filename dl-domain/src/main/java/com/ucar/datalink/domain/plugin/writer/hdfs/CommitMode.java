package com.ucar.datalink.domain.plugin.writer.hdfs;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by sqq on 2017/7/18.
 */
public enum CommitMode {
    Hflush, Hsync;
    public static List<CommitMode> getAllCommitModes() {
        return Lists.newArrayList(Hflush, Hsync);
    }
}
