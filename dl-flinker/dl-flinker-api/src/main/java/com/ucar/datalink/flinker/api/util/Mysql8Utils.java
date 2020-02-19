package com.ucar.datalink.flinker.api.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Mysql8Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Mysql8Utils.class);

    public static String appendParams(String jdbcURL){
        LOG.info(String.format("source url[%s]",jdbcURL));

        if(StringUtils.isNotBlank(jdbcURL)){
            Map<String,String> pararms = new HashMap<String,String>();
            pararms.put("rewriteBatchedStatements","true");
            pararms.put("useSSL","false");
            pararms.put("serverTimezone","Asia/Shanghai");
            pararms.put("zeroDateTimeBehavior","CONVERT_TO_NULL");
            pararms.put("yearIsDateType","false");
            pararms.put("noDatetimeStringSync","true");
			
			

            StringBuffer jdbcURLSB = new StringBuffer();
            for(Map.Entry<String,String> param : pararms.entrySet()){
                if(jdbcURL.contains(param.getKey())){
                    continue;
                }
                jdbcURLSB.append("&");
                jdbcURLSB.append(param.getKey());
                jdbcURLSB.append("=");
                jdbcURLSB.append(param.getValue());
            }
            String appendURL = jdbcURLSB.toString();
            if(appendURL.equalsIgnoreCase("")){
                return jdbcURL;
            }

            if(jdbcURL.contains("?")){
                jdbcURL = jdbcURL + appendURL;
            }else{
                jdbcURL = jdbcURL + "?" + appendURL.substring(1,appendURL.length());;
            }

            LOG.info(String.format("format url[%s]",jdbcURL));
            return jdbcURL;
        }
        return null;
    }



    public static Connection getMysqlConnection(String url,
                                             Properties prop) throws SQLException {
//        try {
//            testGAB();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        prop.put("rewriteBatchedStatements","true");
        prop.put("useSSL","false");
        prop.put("serverTimezone","Asia/Shanghai");
        prop.put("zeroDateTimeBehavior","CONVERT_TO_NULL");
        prop.put("yearIsDateType","false");
        prop.put("noDatetimeStringSync","true");
        LOG.info(String.format("===========mysql connection mysql param url[%s] pror[%s]",url,prop.toString()));
        return DriverManager.getConnection(url,prop);
    }



    public static Connection getMysqlConnection(String url,
                                           String user, String password) throws SQLException {
        Properties prop = new Properties();
        prop.put("user", user);
        prop.put("password", password);
        return getMysqlConnection(url,prop);
    }

    public static void main(String[] args) {
        testGAB();
    }

    public static void testGAB(){
        String url = "jdbc:mysql://10.206.1.19:3306/ucar_order?rewriteBatchedStatements=true&useSSL=false&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=CONVERT_TO_NULL&yearIsDateType=false&noDatetimeStringSync=true";
        try {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Connection connection = DriverManager.getConnection(url, "canal", "canal@P15#pducar");
            Statement statement = connection.createStatement();
            boolean execute = statement.execute("select *");
            LOG.info("============================================================================");


        } catch (SQLException e) {
            LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            e.printStackTrace();
        }
    }














}
