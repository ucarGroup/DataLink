package com.ucar.datalink.biz.utils;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.ucar.datalink.biz.utils.ddl.DdlSqlUtils;
import com.ucar.datalink.biz.utils.ddl.SQLStatementHolder;
import com.ucar.datalink.domain.media.MediaSourceType;
import org.junit.Test;

import java.util.List;

/**
 * Created by lubiao on 2017/7/21.
 */
public class SqlStatementTest {

    @Test
    public void testColumns() {
        String sql = "alter table t_b_city add column test1 int,add column test2 int;";
        List<SQLStatementHolder> holders = DdlSqlUtils.buildSQLStatement(MediaSourceType.MYSQL, sql);
        SQLStatementHolder holder = holders.get(0);
        SQLStatement sqlStatement = holder.getSqlStatement();
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement sqlAlterTableStatement = (SQLAlterTableStatement) sqlStatement;
        }

    }
}
