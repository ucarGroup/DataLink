package com.ucar.datalink.worker.api.util.dialect.oracle;

import com.ucar.datalink.worker.api.util.dialect.AbstractDbDialect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * Oracle数据库方言实现类
 * Created by lubiao on 2017/3/8.
 */
public class OracleDialect extends AbstractDbDialect {

    public OracleDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler){
        super(jdbcTemplate, lobHandler);
        sqlTemplate = new OracleSqlTemplate(this);
    }

    public OracleDialect(JdbcTemplate jdbcTemplate, LobHandler lobHandler, String name, int majorVersion,
                         int minorVersion){
        super(jdbcTemplate, lobHandler, name, majorVersion, minorVersion);
        sqlTemplate = new OracleSqlTemplate(this);
    }

    @Override
    public boolean isCharSpacePadded() {
        return true;
    }

    @Override
    public boolean isCharSpaceTrimmed() {
        return false;
    }

    @Override
    public boolean isEmptyStringNulled() {
        return true;
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
    public String getDefaultCatalog() {
        return null;
    }

    @Override
    public String getDefaultSchema() {
        return (String) jdbcTemplate.queryForObject("SELECT sys_context('USERENV', 'CURRENT_SCHEMA') FROM dual",
                                                    String.class);
    }

}
