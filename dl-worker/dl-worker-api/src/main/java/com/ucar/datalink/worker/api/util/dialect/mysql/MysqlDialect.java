package com.ucar.datalink.worker.api.util.dialect.mysql;

import com.ucar.datalink.worker.api.util.dialect.AbstractDbDialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Mysql数据库方言实现类
 * Created by lubiao on 2017/3/8.
 */
public class MysqlDialect extends AbstractDbDialect {

    public MysqlDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler) {
        super(jdbcTemplate, lobHandler);
        sqlTemplate = new MysqlSqlTemplate(this);
    }

    public MysqlDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler, String name, int majorVersion,
                        int minorVersion) {
        super(jdbcTemplate, lobHandler, name, majorVersion, minorVersion);
        sqlTemplate = new MysqlSqlTemplate(this);
    }

    @Override
    public boolean isCharSpacePadded() {
        return false;
    }

    @Override
    public boolean isCharSpaceTrimmed() {
        return true;
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
        return true;
    }

    @Override
    public boolean isGenerateSqlWithSchema() {
        return true;
    }

    @Override
    public String getDefaultSchema() {
        return null;
    }

    @Override
    public String getDefaultCatalog() {
        return (String) jdbcTemplate.queryForObject("select database()", String.class);
    }

}

