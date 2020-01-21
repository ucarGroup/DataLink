package com.ucar.datalink.flinker.plugin.rdbms.util;

public final class Constant {
    /**
     *
     * 连接数据库的超时时间，默认是3秒，在测试mysql8的时，发现是这个参数导致了连接失败，测试中改成20秒连接就正常了
     * jdbc url后面的参数不影响连接，去掉jdbc url后面的参数一样可以连接成功
     * 现将这个参数值调大卫30秒
     */
    static final int TIMEOUT_SECONDS = 30;
    static final int MAX_TRY_TIMES = 4;
    static final int SOCKET_TIMEOUT_INSECOND = 172800;

    public static final String MYSQL_DATABASE = "Unknown database";
    public static final String MYSQL_CONNEXP = "Communications link failure";
    public static final String MYSQL_ACCDENIED = "Access denied";
    public static final String MYSQL_TABLE_NAME_ERR1 = "Table";
    public static final String MYSQL_TABLE_NAME_ERR2 = "doesn't exist";
    public static final String MYSQL_SELECT_PRI = "SELECT command denied to user";
    public static final String MYSQL_COLUMN1 = "Unknown column";
    public static final String MYSQL_COLUMN2 = "field list";
    public static final String MYSQL_WHERE = "where clause";

    public static final String ORACLE_DATABASE = "ORA-12505";
    public static final String ORACLE_CONNEXP = "The Network Adapter could not establish the connection";
    public static final String ORACLE_ACCDENIED = "ORA-01017";
    public static final String ORACLE_TABLE_NAME = "table or view does not exist";
    public static final String ORACLE_SELECT_PRI = "insufficient privileges";
    public static final String ORACLE_SQL = "invalid identifier";



}
