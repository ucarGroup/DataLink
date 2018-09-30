package com.ucar.datalink.worker.api.util.dialect.sqlserver;

import com.ucar.datalink.worker.api.util.dialect.AbstractDbDialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * SqlServer方言实现类
 * Created by lubiao on 2017/3/15.
 */
public class SqlServerDialect extends AbstractDbDialect {

    public SqlServerDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler) {
        super(jdbcTemplate, lobHandler);
        sqlTemplate = new SqlServerTemplate(this);
    }

    public SqlServerDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler, String name, int majorVersion, int minorVersion) {
        super(jdbcTemplate, lobHandler, name, majorVersion, minorVersion);
        sqlTemplate = new SqlServerTemplate(this);
    }

    @Override
    public String getDefaultSchema() {
        return jdbcTemplate.queryForObject("select db_name()", String.class);
    }

    @Override
    public String getDefaultCatalog() {
        return null;
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
        return true;
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
