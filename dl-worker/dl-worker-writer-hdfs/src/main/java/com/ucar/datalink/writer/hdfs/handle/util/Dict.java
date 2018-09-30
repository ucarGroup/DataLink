package com.ucar.datalink.writer.hdfs.handle.util;

/**
 * Created by sqq on 2017/7/25.
 */
public class Dict {
    // DB类型
    public static final String HDFS_DB_TYPE_BINLOG = "BINLOG";
    public static final String HDFS_DB_TYPE_HBASE = "HBASE";

    // 数据类型
    public static final String HDFS_TRANSFER_DATA_TYPE_NORMAL = "NORMAL";//insert&update
    public static final String HDFS_TRANSFER_DATA_TYPE_DELETE = "DELETE";//delete

    //事件类型
    public static final String  EVENT_CLOSE_STREAM ="hdfsWriter.event.colseStream";
    public static final String EVENT_CLOSE_STREAM_FILE_NAME="hdfsWriter.event.closeStream.fileName";
}
