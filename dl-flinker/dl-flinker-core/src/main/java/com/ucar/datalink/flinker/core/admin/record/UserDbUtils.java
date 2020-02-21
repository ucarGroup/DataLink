package com.ucar.datalink.flinker.core.admin.record;

import com.ucar.datalink.flinker.api.util.Mysql8Utils;
import com.ucar.datalink.flinker.core.admin.AdminConstants;
import com.ucar.datalink.flinker.core.admin.bean.JobConfigBean;
import com.ucar.datalink.flinker.core.admin.bean.UserBean;
import com.ucar.datalink.flinker.core.admin.util.DataSourceController;
import com.ucar.datalink.flinker.core.util.container.CoreConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by user on 2018/2/1.
 */
public class UserDbUtils {


    private static final Logger logger = LoggerFactory.getLogger(JobConfigDbUtils.class);

    private static final String QUERY_USER_SQL = "SELECT id,user_name,ucar_email FROM t_dl_user WHERE isReceiveDataxMail=1";

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
     */
    public static List<UserBean> readUserBeans() throws Exception {
        for(int i=0;i<RETRY_TIMES;i++) {
            String sql = QUERY_USER_SQL;
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            List<UserBean> list = new ArrayList<UserBean>();
            try {
                conn = getConnection();
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                while(rs.next()) {
                    UserBean bean = new UserBean();
                    bean.setId( rs.getLong("id") );
                    bean.setUserName( rs.getString("user_name") );
                    bean.setUcarEmail( rs.getString("ucar_email") );
                    list.add(bean);
                }
                return list;
            } catch(Exception e) {
                logger.error(e.getMessage(),e);
                initConnection();
            } finally {
                closeResultSet(rs);
                closeStatement(ps);
                closeConnection(conn);
            }
        }
        throw new RuntimeException("can not get user bean ");
    }

    private static void parseDbConfig() throws Exception {
/*
        Properties properties = new Properties();
        properties.load(new FileInputStream(CoreConstant.DATAX_ADMIN_CONF));
        address = properties.getProperty(AdminConstants.DATAX_DB_ADDRESS);
        port = Integer.valueOf(properties.getProperty(AdminConstants.DATAX_DB_PORT));
        schema = properties.getProperty(AdminConstants.DATAX_DB_SCHEMA);
        userName = properties.getProperty(AdminConstants.DATAX_DB_USERNAME);
        password = Encryption.decrypt(properties.getProperty(AdminConstants.DATAX_DB_PASSWORD));
 */
        address = DataSourceController.getInstance().getAddress();
        port = DataSourceController.getInstance().getPort();
        schema = DataSourceController.getInstance().getSchema();
        userName = DataSourceController.getInstance().getUserName();
        password = DataSourceController.getInstance().getPassword();
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
