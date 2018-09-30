package com.ucar.datalink.biz.utils.ddl;

/**
 * Created by lubiao on 2017/3/8.
 */
public interface DdlTableNameFilter {

    boolean accept(String catalogName, String schemaName, String tableName);
}
