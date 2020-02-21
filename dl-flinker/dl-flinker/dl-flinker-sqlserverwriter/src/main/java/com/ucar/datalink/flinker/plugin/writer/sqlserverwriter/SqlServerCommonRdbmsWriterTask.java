package com.ucar.datalink.flinker.plugin.writer.sqlserverwriter;

import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.rdbms.util.DataBaseType;
import com.ucar.datalink.flinker.plugin.rdbms.writer.CommonRdbmsWriter;
import com.ucar.datalink.flinker.plugin.rdbms.writer.CommonRdbmsWriter.Task;
import com.ucar.datalink.flinker.plugin.rdbms.writer.Constant;
import com.ucar.datalink.flinker.plugin.rdbms.writer.Key;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2018/3/8.
 */
public class SqlServerCommonRdbmsWriterTask extends CommonRdbmsWriter.Task {

    private static Configuration originalConfigWriter;

    private static String if_else_replace = "SELECT count(1)";

    private boolean isReplace = false;

    private String primaryKey = null;

    public SqlServerCommonRdbmsWriterTask(DataBaseType dataBaseType) {
        super(dataBaseType);
    }

    public void init(Configuration writerSliceConfig) {
        this.username = writerSliceConfig.getString(Key.USERNAME);
        this.password = writerSliceConfig.getString(Key.PASSWORD);
        this.jdbcUrl = writerSliceConfig.getString(Key.JDBC_URL);
        this.table = writerSliceConfig.getString(Key.TABLE);

        this.columns = writerSliceConfig.getList(Key.COLUMN, String.class);
        this.columnNumber = this.columns.size();

        this.preSqls = writerSliceConfig.getList(Key.PRE_SQL, String.class);
        this.postSqls = writerSliceConfig.getList(Key.POST_SQL, String.class);
        this.batchSize = writerSliceConfig.getInt(Key.BATCH_SIZE, Constant.DEFAULT_BATCH_SIZE);
        this.batchByteSize = writerSliceConfig.getInt(Key.BATCH_BYTE_SIZE, Constant.DEFAULT_BATCH_BYTE_SIZE);
        writeMode = writerSliceConfig.getString(Key.WRITE_MODE, "INSERT");
        emptyAsNull = writerSliceConfig.getBool(Key.EMPTY_AS_NULL, true);
        this.primaryKey = writerSliceConfig.getString(Key.PRIMARY_KEY);

        INSERT_OR_REPLACE_TEMPLATE = writerSliceConfig.getString(Constant.INSERT_OR_REPLACE_TEMPLATE_MARK);
        if(INSERT_OR_REPLACE_TEMPLATE.contains(if_else_replace)) {
            if(primaryKey==null || "".equals(primaryKey)) {
                throw new IllegalArgumentException("sqlserver use replace mode,primary not null");
            }
            isReplace = true;
            this.writeRecordSql = String.format(INSERT_OR_REPLACE_TEMPLATE, this.table,this.table,this.table);
        } else {
            this.writeRecordSql = String.format(INSERT_OR_REPLACE_TEMPLATE, this.table);
        }
        BASIC_MESSAGE = String.format("jdbcUrl:[%s], table:[%s]",
                this.jdbcUrl, this.table);

        //如果配置了 identity_insert_mode 属性为on，增加 identity_insert 语句
        boolean isOn = Boolean.parseBoolean(writerSliceConfig.getString(Key.IDENTITY_INSERT, "false"));
        if(isOn) {
            StringBuilder identityInsertMode = new StringBuilder();
            identityInsertMode.append("set IDENTITY_INSERT ").append(this.table).append(" on ;");
            identityInsertMode.append(this.writeRecordSql).append(" ; ");
            identityInsertMode.append("set IDENTITY_INSERT ").append(this.table).append(" off ;");
            this.writeRecordSql = identityInsertMode.toString();
        }
    }


    protected PreparedStatement fillPreparedStatement(PreparedStatement preparedStatement, Record record)
            throws SQLException {
        if(isReplace) {
            Triple<List<String>, List<Integer>, List<String>> copy = new ImmutableTriple<List<String>, List<Integer>, List<String>>(
                    new ArrayList<String>(), new ArrayList<Integer>(),
                    new ArrayList<String>());
            copy.getLeft().addAll(this.resultSetMetaData.getLeft());
            copy.getMiddle().addAll(this.resultSetMetaData.getMiddle());
            copy.getRight().addAll(this.resultSetMetaData.getRight());

            List<String> list = this.resultSetMetaData.getLeft();
            int index = -1;
            for(int i=0;i<list.size();i++) {
                if(list.get(i).equals(this.primaryKey)) {
                    index = i;
                    copy.getLeft().remove(index);
                    copy.getMiddle().remove(index);
                    copy.getRight().remove(index);
                    break;
                }
            }
            if(index == -1) {
                throw new RuntimeException("cannot parse primary key");
            }
            //设置 SELECT count(1) FROM [TABLE] WHERE id=? 中的主键
            int columnSqltype = this.resultSetMetaData.getMiddle().get(index);
            preparedStatement = fillPreparedStatementColumnType(preparedStatement, 0, columnSqltype, record.getColumn(index));

            //设置 update中的set 列，因为这个for循环的下标是从0开始的，而要设置的位置是从第一个开始
            for (int i=0,recordIndex=0; i < this.columnNumber-1; i++,recordIndex++) {
                int columnType = copy.getMiddle().get(i);
                if(i == index) {
                    recordIndex++;
                }
                preparedStatement = fillPreparedStatementColumnType(preparedStatement, i+1, columnType, record.getColumn(recordIndex));
            }
            //设置 update中的 where id=?
            columnSqltype = this.resultSetMetaData.getMiddle().get(index);
            preparedStatement = fillPreparedStatementColumnType(preparedStatement, this.columnNumber, columnSqltype, record.getColumn(index));

            //设置 insert语句
            for (int i = 0; i < this.columnNumber; i++) {
                int columnType = this.resultSetMetaData.getMiddle().get(i);
                preparedStatement = fillPreparedStatementColumnType(preparedStatement, i+this.columnNumber+1, columnType, record.getColumn(i));
            }

        }
        else {
            // mode 为insert的情况
            for (int i = 0; i < this.columnNumber; i++) {
                int columnSqltype = this.resultSetMetaData.getMiddle().get(i);
                preparedStatement = fillPreparedStatementColumnType(preparedStatement, i, columnSqltype, record.getColumn(i));
            }
        }
        return preparedStatement;
    }

}
