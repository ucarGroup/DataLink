package com.ucar.datalink.writer.rdbms.utils;

/**
 * Created by lubiao on 2017/8/8.
 */
public class StatisticKey {
    public static final String SQL_BUILD_RECORDS_COUNT = "sqlBuildRecordsCount";
    public static final String SQL_BUILD_TIME_THROUGH = "sqlBuildTimeThrough";
    public static final String SQL_BUILD_TIME_PER_RECORD = "sqlBuildTimePerRecord";

    public static final String TABLE_GROUP_RECORDS_COUNT = "tableGroupRecordsCount";
    public static final String TABLE_GROUP_TABLE_COUNT = "tableGroupTableCount";
    public static final String TABLE_GROUP_TIME_THROUGH = "tableGroupTimeThrough";
    public static final String TABLE_GROUP_TIME_PER_RECORD = "tableGroupTimePerRecord";

    public static final String SQL_EXE_RECORDS_BLOCK_COUNT="sqlExeRecordsBlockCount";
    public static final String SQL_EXE_RECORDS_COUNT = "sqlExeRecordsCount";
    public static final String SQL_EXE_TIME_THROUGH = "sqlExeTimeThrough";
    public static final String SQL_EXE_TIME_TOTAL = "sqlExeTimeTotal";//所有线程的执行时间总和，计算每条记录的执行时间时用
    public static final String SQL_EXE_TIME_PER_RECORD = "sqlExeTimePerRecord";
    public static final String SQL_EXE_TPS = "sqlExeTps";

}
