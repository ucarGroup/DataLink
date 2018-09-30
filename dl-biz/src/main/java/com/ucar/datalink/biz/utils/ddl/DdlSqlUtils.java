package com.ucar.datalink.biz.utils.ddl;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.util.JdbcConstants;
import com.google.common.collect.Lists;
import com.ucar.datalink.domain.media.MediaSourceType;

import java.util.List;

/**
 * Created by lubiao on 2017/7/12.
 */
public class DdlSqlUtils {

    public static List<SQLStatementHolder> buildSQLStatement(MediaSourceType mediaSourceType, String sqls) {
        sqls = preHandleBeforeCheck(sqls);

        List<SQLStatement> list = null;
        if (MediaSourceType.MYSQL.equals(mediaSourceType) || MediaSourceType.SDDL.equals(mediaSourceType)) {
            list = SQLUtils.parseStatements(sqls, JdbcConstants.MYSQL);
        } else if (MediaSourceType.SQLSERVER.equals(mediaSourceType)) {
            list = SQLUtils.parseStatements(sqls, JdbcConstants.SQL_SERVER);
        } else if (MediaSourceType.POSTGRESQL.equals(mediaSourceType)) {
            list = SQLUtils.parseStatements(sqls, JdbcConstants.POSTGRESQL);
        }

        if (list != null && !list.isEmpty()) {
            List<SQLStatementHolder> result = Lists.newArrayList();
            for (SQLStatement st : list) {
                SchemaStatVisitor visitor = null;
                if (MediaSourceType.MYSQL.equals(mediaSourceType) || MediaSourceType.SDDL.equals(mediaSourceType)) {
                    visitor = new MySqlSchemaStatVisitor();
                } else if (MediaSourceType.SQLSERVER.equals(mediaSourceType)) {
                    visitor = new SQLServerSchemaStatVisitor();
                } else if (MediaSourceType.POSTGRESQL.equals(mediaSourceType)) {
                    visitor = new PGSchemaStatVisitor();
                }
                st.accept(visitor);
                result.add(new SQLStatementHolder(st, visitor, mediaSourceType));
            }
            return result;
        }
        return Lists.newArrayList();
    }

    private static String preHandleBeforeCheck(String sqls) {
        sqls = sqls.toLowerCase();
        sqls = sqls.replaceAll("using\\s+btree", "");
        return sqls;
    }
}
