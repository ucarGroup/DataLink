package com.ucar.datalink.flinker.plugin.reader.hdfsreader;

public final class Key {

    /**
     * 此处声明插件用到的需要插件使用者提供的配置项
     */
    public final static String PATH = "path";
    public static final String COLUMN = "column";
    public final static String DEFAULT_FS = "defaultFS";
    public final static String ENCODING = "encoding";
    public static final String TYPE = "type";
    public static final String INDEX = "index";
    public static final String VALUE = "value";
    public static final String FORMAT = "format";
    public static final String FILETYPE = "fileType";
    public static final String NULL_FORMAT = "nullFormat";

    public static final String HADOOP_CONFIG = "hadoopConfig";

    /**
     * 当读到的path路径为空时，是否忽略异常
     */
    public static final String IGNORE_EXCEPTION = "ignoreException";

    /**
     * 指定hadoop用户
     */
    public static final String HADOOP_USER_NAME = "hadoopUserName";
}
