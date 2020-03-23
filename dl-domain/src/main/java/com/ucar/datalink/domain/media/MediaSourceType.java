package com.ucar.datalink.domain.media;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 数据源类型
 * <p>
 * Created by lubiao on 2017/2/28.
 */
public enum MediaSourceType {
    MYSQL, SQLSERVER, ORACLE, HDFS, HBASE, ELASTICSEARCH, SDDL, ZOOKEEPER, POSTGRESQL,KUDU, HANA, KAFKA;

    public boolean isRdbms() {
        if (MYSQL.equals(this) || SQLSERVER.equals(this) || ORACLE.equals(this) || POSTGRESQL.equals(this) || HANA.equals(this)) {
            return true;
        }
        return false;
    }

    public static List<MediaSourceType> getAllSrcMediaSourceTypes() {
        return Lists.newArrayList(MYSQL, SQLSERVER, HDFS, HBASE, ELASTICSEARCH, POSTGRESQL,ORACLE);
    }

	public static List<MediaSourceType> getAllSrcTypesForIncrement() {
        return Lists.newArrayList(MYSQL, HBASE, SDDL);
    }
    public static List<MediaSourceType> getTargetTypesForRDBMS() {
        return Lists.newArrayList(MYSQL, SQLSERVER, HBASE, ELASTICSEARCH);
    }

    public static List<MediaSourceType> getTargetTypesForHDFS() {
        return Lists.newArrayList(MYSQL, SQLSERVER, HDFS, ELASTICSEARCH);
    }

    public static List<MediaSourceType> getTargetTypesForHBASE() {
        return Lists.newArrayList(HBASE, ELASTICSEARCH);
    }

    public static List<MediaSourceType> getTargetTypesForSDDL() {
        return Lists.newArrayList(MYSQL, ELASTICSEARCH);
    }

    public static List<MediaSourceType> getAllMediaSourceTypesForBidData() {
        return  Lists.newArrayList(MYSQL, SQLSERVER, HDFS, HBASE, ORACLE, HANA,ELASTICSEARCH, KUDU,POSTGRESQL,KAFKA);
    }
	public static List<MediaSourceType> getMysqlTaskSrcTypes() {
        return Lists.newArrayList(MYSQL, SDDL);
    }

    public static List<MediaSourceType> getHBaseTaskSrcTypes() {
        return Lists.newArrayList(HBASE);
    }
}
