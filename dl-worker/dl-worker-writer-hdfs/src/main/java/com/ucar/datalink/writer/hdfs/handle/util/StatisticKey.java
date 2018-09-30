package com.ucar.datalink.writer.hdfs.handle.util;

/**
 * Created by lubiao on 2017/8/8.
 */
public class StatisticKey {

    public static final String TABLE_GROUP_RECORDS_COUNT = "tableGroupRecordsCount";
    public static final String TABLE_GROUP_TABLE_COUNT = "tableGroupTableCount";
    public static final String TABLE_GROUP_TIME_THROUGH = "tableGroupTimeThrough";
    public static final String TABLE_GROUP_TIME_PER_RECORD = "tableGroupTimePerRecord";

    public static final String WRITE_RECORDS_COUNT = "writeRecordsCount";
    public static final String WRITE_TIME_THROUGH = "writeTimeThrough";
    public static final String WRITE_TIME_TOTAL = "writeTimeTotal";//所有线程的执行时间总和，计算每条记录的执行时间时用
    public static final String WRITE_TIME_PER_RECORD = "writeTimePerRecord";
    public static final String WRITE_TPS = "writeTps";

}
