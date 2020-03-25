package com.ucar.datalink.flinker.core.admin.record;

import com.ucar.datalink.flinker.api.util.Mysql8Utils;
import com.ucar.datalink.flinker.core.admin.bean.JobConfigBean;
import com.ucar.datalink.flinker.core.admin.util.DataSourceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.MessageFormat;

/**
 * Created by user on 2017/8/17.
 */
public class JobConfigDbUtils {

    private static final Logger logger = LoggerFactory.getLogger(JobConfigDbUtils.class);

    private static final String QUERY_JOB_CONFIG_SQL = "SELECT * FROM t_dl_flinker_job_config WHERE id=";

    private static String address;
    private static Integer port;
    private static String schema;
    private static String userName;
    private static String password;
    private static Connection connection;

    /**
     * 重试三次
     */
    private static final int RETRY_TIMES = 3;

    static {
        try {
            parseDbConfig();
            initConnection();
        } catch (Exception e) {
            logger.error("something goes wrong when initializing jdbc connection for " + JobConfigDbUtils.class.getName(), e);
        }
    }

    /**
     * 先拼接一下，就不考虑什么注入之类的情况了
     * @param id
     */
    public static JobConfigBean readConfig(long id) throws Exception {
        for(int i=0;i<RETRY_TIMES;i++) {
            String sql = QUERY_JOB_CONFIG_SQL+id;
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            JobConfigBean bean = null;
            try {
                conn = getConnection();
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                bean = new JobConfigBean();
                while(rs.next()) {
                    bean.setId( rs.getInt("id") );
                    bean.setJob_name( rs.getString("job_name") );
                    bean.setJob_desc( rs.getString("job_desc") );
                    bean.setJob_content( rs.getString("job_content") );
                    bean.setJob_media_name( rs.getString("job_media_name") );
                    bean.setJob_src_media_source_id( rs.getInt("job_src_media_source_id") );
                    bean.setJob_target_media_source_id( rs.getInt("job_target_media_source_id") );
                    bean.setTiming_transfer_type( rs.getString("timing_transfer_type") );
                    bean.setTiming_expression( rs.getString("timing_expression") );
                    if( rs.getBoolean("timing_yn") ) {
                        bean.setTiming_yn(true);
                    } else {
                        bean.setTiming_yn(false);
                    }
                    if( rs.getBoolean("timing_on_yn") ) {
                        bean.setTiming_on_yn(true);
                    } else {
                        bean.setTiming_on_yn(false);
                    }
                    bean.setTiming_parameter( rs.getString("timing_parameter") );
                    bean.setTiming_target_worker( rs.getString("timing_target_worker") );
                    bean.setCreate_time( rs.getTimestamp("create_time") );
                    bean.setModify_time( rs.getTimestamp("modify_time") );
                    bean.setIs_delete( rs.getBoolean("is_delete") );
                }
                if(bean != null) {
                    return bean;
                }
                throw new RuntimeException("can not get job config "+id);
            } catch(Exception e) {
                logger.error(e.getMessage(),e);
                initConnection();
            } finally {
                closeResultSet(rs);
                closeStatement(ps);
                closeConnection(conn);
            }
        }
        throw new RuntimeException("can not get job config "+id);
    }

    private static void parseDbConfig() throws Exception {
        address = DataSourceController.getInstance().getAddress();
        port = DataSourceController.getInstance().getPort();
        schema = DataSourceController.getInstance().getSchema();
        userName = DataSourceController.getInstance().getUserName();
        password = DataSourceController.getInstance().getPassword();
        logger.info("JobConfigDbUtils address->"+address);
        logger.info("JobConfigDbUtils port->"+port);
        logger.info("JobConfigDbUtils schema->"+schema);
        logger.info("JobConfigDbUtils userName->"+userName);
        logger.info("JobConfigDbUtils password->"+password);

    }

    private static void initConnection() throws Exception {
        parseDbConfig();
        String url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", address, String.valueOf(port), schema);
        Class.forName("com.mysql.jdbc.Driver");
        connection = Mysql8Utils.getMysqlConnection(url, userName, password);
    }

    private static Connection getConnection() throws Exception {
        String url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", address, String.valueOf(port), schema);
        Class.forName("com.mysql.jdbc.Driver");
        return Mysql8Utils.getMysqlConnection(url, userName, password);
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.error("error occurred when close ResultSet", e);
            }
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("error occurred when close Statement", e);
            }
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("error occurred when close Connection", e);
            }
        }
    }


    public static void reConnect() {
        if(connection != null) {
            try {
                connection.close();
                initConnection();
            } catch (SQLException e) {
                logger.error("error occurred when close Connection", e);
            } catch (Exception e) {
                logger.error("re init connect failure", e);
            }
        }
    }

}
