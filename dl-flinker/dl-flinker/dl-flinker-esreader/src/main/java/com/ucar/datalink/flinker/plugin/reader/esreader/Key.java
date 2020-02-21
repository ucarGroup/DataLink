package com.ucar.datalink.flinker.plugin.reader.esreader;

/**
 * Created by yw.zhang02 on 2016/7/27.
 */
public class Key {
    // 集群信息
    public static final String ES_HOSTS = "hosts"; // 必填
    public static final String ES_USERNAME = "username";
    public static final String ES_PASSWORD = "password";
    public static final String ES_HTTP_PORT = "httpPort";
    public static final String ES_TCP_PORT = "tcpPort";

    // 索引信息
    public static final String ES_INDEX = "esIndex"; // 必填
    public static final String ES_TYPE = "esType"; // 必填
    public static final String COLUMNS = "columns"; // 必须
    public static final String ES_QUERY = "esQuery";

    // 参数
    public static final String ES_SEARCH_TIMEOUT = "esSearchTimeout";
    public static final String ES_BATCH_SIZE = "batchSize";
}
