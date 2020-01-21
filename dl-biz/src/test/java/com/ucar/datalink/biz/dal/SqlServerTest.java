package com.ucar.datalink.biz.dal;

import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.meta.MediaMeta;

import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2018/2/26.
 */
public class SqlServerTest {


    private static final String SQLSERVER_URL = "jdbc:sqlserver://{0}:{1};DatabaseName={2}";

    public static final String insert = "INSERT INTO news2(news_id,news_title,news_author,news_summary) values(?,?,?,?)";

    public static final String sql = "" +
            "IF ( (SELECT count(1) FROM news2 WHERE news_id=?) >0 ) " +
            "BEGIN" +
            "    UPDATE news2" +
            "    SET news_title=?,news_author=?,news_summary=? " +
            "    WHERE news_id=? " +
            "END " +
            "ELSE " +
            "BEGIN " +
            "    INSERT INTO news2(news_id,news_title,news_author,news_summary) values(?,?,?,?) " +
            "END";

    public static void main(String[] args) throws Exception {
        SqlServerTest t = new SqlServerTest();
        t.go();
    }


    public void go()throws Exception  {
        ResultSet rs = null;
        Connection connection = null;
        List<MediaMeta> tables = new ArrayList<MediaMeta>();
        try {
            String ip = "10.104.20.40";
            int port = 1433;
            String schema = "BI_COMMON";
            String username = "bi_ucar";
            String password = "BI_ucar2015";

            String url = MessageFormat.format(SQLSERVER_URL, ip, port + "", schema);
            connection = DriverManager.getConnection(url, username, password);
            PreparedStatement ps = connection.prepareStatement(sql);
            connection.setAutoCommit(false);
            ps.setInt(1,9);

            ps.setString(2,"haahahaha111111");
            ps.setString(3,"waerwerewr!1111");
            ps.setString(4,"@@@@@@@@@@@111");
            ps.setInt(5,9);

            ps.setInt(6,9);
            ps.setString(7,"haahahaha");
            ps.setString(8,"waerwerewr!");
            ps.setString(9,"@@@@@@@@@@@");



            ps.execute();
            connection.commit();
            closeStatement(ps);

            System.out.println("ok!!!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResultSet(rs);
            closeConnection(connection);
        }
    }






    private static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }










}

