package com.ucar.datalink.biz.utils.flinker;

/**
 * Created by user on 2017/7/19.
 */
public class FlinkerJobConfigConstant {


    public static final String COLUMN = "\"@column\"";

    public static final String JDBCURL = "@jdbcUrl";

    public static final String HDFSURL = "@hdfsUrl";

    public static final String TABLE = "@table";

    public static final String PATH = "@path";

    public static final String PASSWORD = "@password";

    public static final String USERNAME = "@username";

    public static final String ES_READER = "biz/jobjson/esreader.json";

    public static final String ES_WRITER = "biz/jobjson/eswriter.json";

    public static final String HDFS_READER = "biz/jobjson/hdfsreader.json";

    public static final String HDFS_WRITER = "biz/jobjson/hdfswriter.json";

    public static final String MYSQL_READER = "biz/jobjson/mysqlreader.json";

    public static final String MYSQL_WRITER = "biz/jobjson/mysqlwriter.json";

    public static final String HBASE_READER = "biz/jobjson/hbasereader.json";

    public static final String HBASE_WRITER = "biz/jobjson/hbasewriter.json";

    public static final String SQLSERVER_READER = "biz/jobjson/sqlserverreader.json";

    public static final String SQLSERVER_WRITER = "biz/jobjson/sqlserverwriter.json";

    public static final String GREENPLUM_READER = "biz/jobjson/greenplumreader.json";

    public static final String GREENPLUM_WRITER = "biz/jobjson/greenplumwriter.json";

    public static final String POSTGRE_SQL_READER = "biz/jobjson/postgresqlreader.json";

    public static final String POSTGRE_SQL_WRITER = "biz/jobjson/postgresqlwriter.json";

    public static final String KUDU_WRITER = "biz/jobjson/kuduwriter.json";

    public static final String ORACLE_READER = "biz/jobjson/oraclereader.json";

    public static final String ORACLE_WRITER = "biz/jobjson/oraclewriter.json";

    public static final String HANA_READER = "biz/jobjson/hanareader.json";

    public static final String DB_PORT = "dbPort";

    public static final String DB_TYPE = "dbType";

    public static final String DB_SCHEMA = "dbSchema";

    public static final String DB_CODE = "dbCharacterEncoding";

    public static final String DB_DEPUSERNAME = "dbDepUsername";

    public static final String DB_DEPPASSWORD = "dbDepPassword";

    public static final String DB_CANALUSERNAME = "dbCanalUsername";

    public static final String DB_CANALPASSWORD = "dbCanalPassword";

    public static final String MYSQL_URL = "jdbc:mysql://{0}:{1}/{2}";

    public static final String HDFS_URL = "hdfs://{0}:{1}";

    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

    public static final String DB_CANALMASTERADDRESS = "dbCanalMasterIp";

    public static final String DB_CANALSTANDYADDRESS = "dbCanalStandyIp";

    public static final String DB_DEP_ADDRESS = "dbDepIp";

    public static final String SQLSERVER_URL = "jdbc:sqlserver://{0}:{1};DatabaseName={2}";

    public static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    public static final String GREENPLUM_URL = "jdbc:postgresql://{0}:{1}/{2}";

    public static final String GREENPLUM_DRIVER = "org.postgresql.Driver";

    public static final String POSTGRE_SQL_URL = "jdbc:postgresql://{0}:{1}/{2}";

    public static final String POSTGRE_SQL_DRIVER = "org.postgresql.Driver";

    public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    public static final String ORACLE_URL = "jdbc:oracle:thin:@//{0}:{1}/{2}";

    public static final String HANA_URL = "jdbc:sap://{0}:{1}/{2}";

    public static final String HTTP_PORT = "@httpPort";

    public static final String TCP_PORT = "@tcpPort";

    public static final String HOSTS = "@hosts";

    public static final String HBASE_ZOOKEEPER_QUORUM = "hbase.zookeeper.quorum";

    public static final String HBASE_ZK_IP = "@hbaseIp";

    public static final String HBASE_ZK_ZNODE = "@hbase_znode";

    public static final String ES_INDEX = "@esIndex";

    public static final String ES_TYPE = "@esType";

    public static final String RMDBS_SPLIT_PK = "@splitPk";

    public static final String WRITE_PK_NAME = "@pkname";




    public static final String DATAX_PRE_DATE_DOLLAR_PREFIX = "$DATAX_PRE_DATE";

    public static final String DATAX_CURRENT_DATE_DOLLAR_PREFIX = "$DATAX_CURRENT_DATE";

    public static final String DATAX_LAST_EXECUTE_TIME_DOLLAR_PREFIX = "$DATAX_LAST_EXECUTE_TIME";

    public static final String DATAX_CURRENT_TIME_DOLLAR_PREFIX = "$DATAX_CURRENT_TIME";

    public static final String DATAX_SPECIFIED_PRE_DATE_DOLLAR_PREFIX = "$DATAX_SPECIFIED_PRE_DATE";

    public static final String DATAX_PRE_DATA_PATH = "/dt=$DATAX_PRE_DATE";


    public static final String DATAX_PRE_DATE_ESCAPE = "\\$DATAX_PRE_DATE";

    public static final String DATAX_CURRENT_DATE_ESCAPE = "\\$DATAX_CURRENT_DATE";

    public static final String DATAX_LAST_EXECUTE_TIME_ESCAPE = "\\$DATAX_LAST_EXECUTE_TIME";

    public static final String DATAX_CURRENT_TIME_ESCAPE = "\\$DATAX_CURRENT_TIME";

    public static final String DATAX_SPECIFIED_PRE_DATE_ESCAPE = "\\$DATAX_SPECIFIED_PRE_DATE";




    public static final String DATAX_PRE_DATE = "DATAX_PRE_DATE";

    public static final String DATAX_CURRENT_DATE = "DATAX_CURRENT_DATE";

    public static final String DATAX_LAST_EXECUTE_TIME = "DATAX_LAST_EXECUTE_TIME";

    public static final String DATAX_CURRENT_TIME = "DATAX_CURRENT_TIME";

    public static final String DATAX_SPECIFIED_PRE_DATE = "DATAX_SPECIFIED_PRE_DATE";


    /**
     * 通过http的url参数形式传递的last execute time
     */
    public static final String HTTP_PARAMETER_LAST_EXECUTE_TIME = "HTTP_PARAMETER_LAST_EXECUTE_TIME";

    public static final String HTTP_PARAMETER_DATE = "date";

    public static final String HTTP_PARAMETER_TIME = "time";

    /**
     * 补数据时用的标示key
     */
    public static final String DATAX_FILL_DATA = "DATAX_FILL_DATA";


    //Kudu
    public static final String KUDU_MASTER_ADDRESSES = "@master_addresses";
    public static final String KUDU_BUFFER_SIZE = "@bufferSize";
    public static final String KUDU_TABLE = "@table";
    public static final String KUDU_COLUMN = "@column";





}
