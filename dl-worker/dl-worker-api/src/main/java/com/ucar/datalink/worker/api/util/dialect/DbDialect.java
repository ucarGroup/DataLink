package com.ucar.datalink.worker.api.util.dialect;

import org.apache.ddlutils.model.Table;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 关系型数据库方言抽象接口
 * Created by lubiao on 2017/4/26.
 */
public interface DbDialect {
    String getName();

    String getVersion();

    int getMajorVersion();

    int getMinorVersion();

    String getDefaultSchema();

    String getDefaultCatalog();

    boolean isCharSpacePadded();

    boolean isCharSpaceTrimmed();

    boolean isEmptyStringNulled();

    boolean isSupportMergeSql();

    boolean isGetTablesWithSchema();

    boolean isGenerateSqlWithSchema();

    LobHandler getLobHandler();

    JdbcTemplate getJdbcTemplate();

    TransactionTemplate getTransactionTemplate();

    SqlTemplate getSqlTemplate();

    Table findTable(String schema, String table);

    Table findTable(String schema, String table, boolean useCache);

    void reloadTable(String schema, String table);

    void destory();
}
