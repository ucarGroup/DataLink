package com.ucar.datalink.worker.api.util.dialect.postgresql;

import com.ucar.datalink.worker.api.util.dialect.AbstractSqlTemplate;
import com.ucar.datalink.worker.api.util.dialect.DbDialect;

/**
 * Created by lubiao on 2017/9/8.
 */
public class PostgreSqlTemplate extends AbstractSqlTemplate {

    public PostgreSqlTemplate(DbDialect dbDialect) {
        super(dbDialect);
    }

    @Override
    public String getMergeSql(String schemaName, String tableName, String[] pkNames, String[] columnNames, String[] viewColumnNames) {
        return null;
    }
}
