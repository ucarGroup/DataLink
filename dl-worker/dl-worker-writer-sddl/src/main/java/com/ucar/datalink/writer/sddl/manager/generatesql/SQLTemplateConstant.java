package com.ucar.datalink.writer.sddl.manager.generatesql;

import java.text.MessageFormat;


public class SQLTemplateConstant {

    private static final String INSERT_TEMPLATE = " INSERT INTO {0} ({1}) VALUES ({2}) ";

    private static final String UPDATE_TEMPLATE = " UPDATE {0} SET {1} WHERE {2} ";

    private static final String DELETE_TEMPLATE = " DELETE FROM {0} WHERE {1} ";

    public static final String MYSQL_INSERT_TEMPLATE = INSERT_TEMPLATE;


    public static String getMysqlInsertSql(String tableName, String params , String values){
        return getCommonInsertSql(tableName, params, values);
    }

    private static String getCommonInsertSql(String tableName, String params , String values){
        return MessageFormat.format(MYSQL_INSERT_TEMPLATE, tableName, params, values);
    }

    public static String getMysqlUpdateSql(String tableName, String setValues, String whereCon){
        return getCommonUpdateSql(tableName, setValues, whereCon);
    }

    public static String getSqlserverUpdateSql(String tableName, String setValues, String whereCon){
        return getCommonUpdateSql(tableName, setValues, whereCon);
    }

    private static String getCommonUpdateSql(String tableName, String setValues, String whereCon){
        return MessageFormat.format(UPDATE_TEMPLATE, tableName, setValues, whereCon);
    }


    public static String getMysqlDeleteSql(String tableName, String whereCon){
        return getCommonDeleteSql(tableName, whereCon);
    }

    public static String getSqlserverDeleteSql(String tableName, String whereCon){
        return getCommonDeleteSql(tableName, whereCon);
    }

    private static String getCommonDeleteSql(String tableName, String whereCon){
        return MessageFormat.format(DELETE_TEMPLATE, tableName, whereCon);
    }
}
