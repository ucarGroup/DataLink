package com.ucar.datalink.worker.api.util.dialect.oracle;

import com.ucar.datalink.worker.api.util.dialect.AbstractSqlTemplate;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;

/**
 * Oracle数据库对应的sql生成模板
 * Created by lubiao on 2017/3/8.
 */
public class OracleSqlTemplate extends AbstractSqlTemplate {

    public OracleSqlTemplate(DbDialect dbDialect) {
        super(dbDialect);
    }

    /**
     * http://en.wikipedia.org/wiki/Merge_(SQL)
     */
    public String getMergeSql(String schemaName, String tableName, String[] keyNames, String[] columnNames,
                              String[] viewColumnNames) {
        final String aliasA = "a";
        final String aliasB = "b";
        StringBuilder sql = new StringBuilder();

        sql.append("merge /*+ use_nl(a b)*/ into ").append(getFullName(schemaName, tableName)).append(" ").append(aliasA);
        sql.append(" using (select ");

        int size = columnNames.length;
        // 构建 (select ? as col1, ? as col2 from dual)
        for (int i = 0; i < size; i++) {
            sql.append("? as " + columnNames[i]).append(" , ");
        }
        size = keyNames.length;
        for (int i = 0; i < size; i++) {
            sql.append("? as " + keyNames[i]).append((i + 1 < size) ? " , " : "");
        }
        sql.append(" from dual) ").append(aliasB);
        sql.append(" on (");

        size = keyNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasA + "." + keyNames[i]).append("=").append(aliasB + "." + keyNames[i]);
            sql.append((i + 1 < size) ? " and " : "");
        }

        sql.append(") when matched then update set ");

        size = columnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasA + "." + columnNames[i]).append("=").append(aliasB + "." + columnNames[i]);
            sql.append((i + 1 < size) ? " , " : "");
        }

        sql.append(" when not matched then insert (");
        size = columnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasA + "." + columnNames[i]).append(" , ");
        }
        size = keyNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasA + "." + keyNames[i]).append((i + 1 < size) ? " , " : "");
        }

        sql.append(" ) values (");
        size = columnNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasB + "." + columnNames[i]).append(" , ");
        }
        size = keyNames.length;
        for (int i = 0; i < size; i++) {
            sql.append(aliasB + "." + keyNames[i]).append((i + 1 < size) ? " , " : "");
        }
        sql.append(" )");
        return sql.toString().intern(); // intern优化，避免出现大量相同的字符串
    }

}
