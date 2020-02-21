package com.ucar.datalink.flinker.plugin.writer.eswriter;

/**
 * Created by yw.zhang02 on 2016/7/27.
 */
public class Key {
    //must have
    public static final String ES_USERNAME = "username";
    //must have
    public static final String ES_PASSWORD = "password";
    //must have
    public static final String ES_HOSTS = "hosts";
    //must have
    public static final String ES_HTTP_PORT = "httpPort";
    //must have
    public static final String ES_TCP_PORT = "tcpPort";
    //must have
    public static final String ES_BATCH_SIZE = "batchSize";
    //must have
    public static final String ES_INDEX = "esIndex";
    // must have
    public static final String ES_TYPE = "esType";
    // must have for column
    public static final String COLUMN = "column";

    public static final String OPERATE_TYPE = "operateType";

    public static final String RETRY_TIMES = "retryTimes";
    // must have
    public static final String TABLE = "table";

    public static final String ES_MERGE_COLUMN = "mergeColumn";

    public static final String ES_IS_ADD_TABLE_PREFIX = "isAddTablePrefix";

    public static final String ES_ROUTING = "esRouting";
    
    public static final String ES_ROUTING_IGNORE = "esRoutingIgnore";
    
    public static final String ES_LON_LAT_MERGE_TYPE = "1";

    public static final String ES_OTHER_MERGE_TYPE = "2";

}
