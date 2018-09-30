package com.ucar.datalink.worker.api.util.dialect.postgresql;

import com.ucar.datalink.worker.api.util.dialect.AbstractDbDialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Created by lubiao on 2017/9/8.
 */
public class PostgreSqlDialect extends AbstractDbDialect {

    public PostgreSqlDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler) {
        super(jdbcTemplate, lobHandler);
        sqlTemplate = new PostgreSqlTemplate(this);
    }

    public PostgreSqlDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler, String name, int majorVersion, int minorVersion) {
        super(jdbcTemplate, lobHandler, name, majorVersion, minorVersion);
        sqlTemplate = new PostgreSqlTemplate(this);
    }

    @Override
    public String getDefaultSchema() {
        return null;
    }

    @Override
    public String getDefaultCatalog() {
        return (String) jdbcTemplate.queryForObject("select current_database()", String.class);
    }

    @Override
    public boolean isCharSpacePadded() {
        return false;
    }

    @Override
    public boolean isCharSpaceTrimmed() {
        return false;
    }

    @Override
    public boolean isEmptyStringNulled() {
        return false;
    }

    @Override
    public boolean isSupportMergeSql() {
        return false;
    }

    @Override
    public boolean isGetTablesWithSchema() {
        return false;
    }

    @Override
    public boolean isGenerateSqlWithSchema() {
        return false;
    }

}
