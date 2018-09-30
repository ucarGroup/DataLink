package com.ucar.datalink.domain.media;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 数据源类型
 * <p>
 * Created by lubiao on 2017/2/28.
 */
public enum MediaSourceType {
    MYSQL, SQLSERVER, ORACLE, HDFS, HBASE, ELASTICSEARCH, SDDL, ZOOKEEPER, POSTGRESQL;

    public boolean isRdbms() {
        if (MYSQL.equals(this) || SQLSERVER.equals(this) || ORACLE.equals(this) || POSTGRESQL.equals(this)) {
            return true;
        }
        return false;
    }

    public static List<MediaSourceType> getAllSrcMediaSourceTypes() {
        return Lists.newArrayList(MYSQL, SQLSERVER, HDFS, HBASE, ELASTICSEARCH, POSTGRESQL);
    }

    public static List<MediaSourceType> getTargetTypesForRDBMS() {
        return Lists.newArrayList(MYSQL, SQLSERVER, HDFS, HBASE, ELASTICSEARCH, POSTGRESQL);
    }

    public static List<MediaSourceType> getTargetTypesForHDFS() {
        return Lists.newArrayList(MYSQL, SQLSERVER, HDFS, HBASE, ELASTICSEARCH, POSTGRESQL);
    }

    public static List<MediaSourceType> getTargetTypesForHBASE() {
        return Lists.newArrayList(HDFS, HBASE, POSTGRESQL);
    }

    public static List<MediaSourceType> getTargetTypesForES() {
        return Lists.newArrayList(MYSQL, SQLSERVER, HBASE, ELASTICSEARCH, POSTGRESQL);
    }
}
