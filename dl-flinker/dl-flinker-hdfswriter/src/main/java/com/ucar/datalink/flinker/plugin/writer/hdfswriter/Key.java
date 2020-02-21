package com.ucar.datalink.flinker.plugin.writer.hdfswriter;

/**
 * Created by shf on 15/10/8.
 */
public class Key {
    // must have
    public static final String PATH = "path";
    //must have
    public final static String DEFAULT_FS = "defaultFS";
    //must have
    public final static String FILE_TYPE = "fileType";
    // must have
    public static final String FILE_NAME = "fileName";
    // must have for column
    public static final String COLUMN = "column";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String DATE_FORMAT = "dateFormat";
    // must have
    public static final String WRITE_MODE = "writeMode";
    // must have
    public static final String FIELD_DELIMITER = "fieldDelimiter";
    // not must, default UTF-8
    public static final String ENCODING = "encoding";
    // not must, default no compress
    public static final String COMPRESS = "compress";
    // not must, not default \N
    public static final String NULL_FORMAT = "nullFormat";
	// not must
	public static final String HADOOP_USER_NAME = "hadoopUserName";//added by lubiao
	// not must
	public static final String CREATE_PATH_IF_NOT_EXIST = "createPathIfNotExist";//added by lubiao
	// not must 出现异常时重试次数
	public static final String ERROR_RETRY_TIMES = "errorRetryTimes";//added by lubiao
	// not must 并行度
	public static final String PARALLEL_SIZE = "parallelSize";// added by lubiao
	// not must 并行消费时每一个task一次处理record的数量
	public static final String TASK_RECORD_SIZE = "taskRecordSize";// added by lubiao

    public static final String HADOOP_CONFIG = "hadoopConfig";

    /**
     * 执行前先清空数据
     */
    public static final String PRE_DEL = "preDel";
}
