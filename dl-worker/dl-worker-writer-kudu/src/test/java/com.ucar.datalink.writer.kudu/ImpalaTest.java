package com.ucar.datalink.writer.kudu;


import java.sql.*;


public class ImpalaTest {


    static String JDBC_DRIVER = "com.cloudera.impala.jdbc41.Driver";
    static String CONNECTION_URL = "jdbc:impala://10.104.132.73:21050/kudu_test";

    public static void main(String[] args) {

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            Class.forName(JDBC_DRIVER);
            con = DriverManager.getConnection(CONNECTION_URL);
            Statement statement = con.createStatement();
            String sql = "alter table t_dl_test_xxx add columns(aaaa  STRING)";
//            String sql = "select 1";
            boolean execute = statement.execute(sql);
            System.out.println(execute);

//            ps = con.prepareStatement("select count(1) from t_dl_test_xxx");
//            rs = ps.executeQuery();
//            while (rs.next()) {
//                System.out.println(rs.getLong(1));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭rs、ps和con
//            try {
//                rs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

//            try {
//                ps.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

}