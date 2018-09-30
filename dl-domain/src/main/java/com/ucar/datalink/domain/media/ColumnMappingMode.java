package com.ucar.datalink.domain.media;

/**
 * Created by user on 2017/3/6.
 */
public enum ColumnMappingMode {
    NONE, INCLUDE, EXCLUDE;

    public boolean isNone() {
        return this == NONE;
    }

    public boolean isInclude() {
        return this == INCLUDE;
    }

    public boolean isExclude() {
        return this == EXCLUDE;
    }

}
