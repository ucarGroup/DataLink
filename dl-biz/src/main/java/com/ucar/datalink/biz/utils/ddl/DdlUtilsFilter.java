package com.ucar.datalink.biz.utils.ddl;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Created by lubiao on 2017/3/8.
 */
public abstract class DdlUtilsFilter {

    /**
     * 返回要获取 {@linkplain DatabaseMetaData} 的 {@linkplain Connection}，不能返回null
     *
     * @param con
     * @return
     */
    public Connection filterConnection(Connection con) throws Exception {
        return con;
    }

    /**
     * 对 databaseMetaData 做一些过滤,返回 {@linkplain DatabaseMetaData}，不能为 null
     *
     * @param databaseMetaData
     * @return
     */
    public DatabaseMetaData filterDataBaseMetaData(JdbcTemplate jdbcTemplate, Connection con,
                                                   DatabaseMetaData databaseMetaData) throws Exception {
        return databaseMetaData;
    }

}
