package com.ucar.datalink.manager.core.rest;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseTest {

    public static void main(String[] args) throws SQLException {
        List<String> list = Lists.newArrayList();
        list.add("user");
       // list.add("person");
        System.out.println(StringUtils.join(list,","));
       // Connection connection = DriverManager.getConnection("jdbc:mysql://10.104.20.123:3306/ucar_datalink","ucar_dev_soa","ucar_dev_soa");
        Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/myTest","root","123456");
        //Connection connection = DriverManager.getConnection("jdbc:mysql://10.101.23.101:3306/ucar_clbs","ucar_dep","61de282fbe93652ad19b1380c3c696c3");
        ResultSet resultSet = connection.getMetaData().getPrimaryKeys(null,null,"user");
        System.out.println(resultSet.next());
        close(connection,resultSet);
    }

    @Test
    public void existsTable() throws SQLException {
        Connection connection = getConnection();
        ResultSet rs = connection.getMetaData().getTables(null, null, "t_dl_test_source3", null);
        if(rs.next()){
            System.out.println(rs.getString("TABLE_NAME"));
            System.out.println("cunzai");
        }
        close(connection,rs);
    }

    @Test
    public void  getAllTables() throws SQLException {
        Connection connection = getConnection();
        ResultSet resultSet = connection.getMetaData().getTables(null,"ucar_datalink",null,new String[] { "TABLE" });
        while (resultSet.next()){
            System.out.println(resultSet.getString("TABLE_NAME") + "  "
                    + resultSet.getString("TABLE_TYPE"));
        }
        close(connection,resultSet);
    }


    public static Connection getConnection() throws SQLException {
        return   DriverManager.getConnection("jdbc:mysql://10.104.20.123:3306/ucar_datalink","ucar_dev_soa","ucar_dev_soa");
    }

    public static void close(Connection connection,ResultSet resultSet) throws SQLException {
        closeResult(resultSet);
        closeConnection(connection);
    }

    public static void closeResult(ResultSet resultSet) throws SQLException {
        resultSet.close();
    }

    public static void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }
}
