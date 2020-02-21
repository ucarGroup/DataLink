package com.ucar.datalink.flinker.plugin.writer.mysqlwriter;

import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.plugin.rdbms.util.DBUtil;
import com.ucar.datalink.flinker.plugin.rdbms.util.DBUtilErrorCode;
import com.ucar.datalink.flinker.plugin.rdbms.util.DataBaseType;
import com.ucar.datalink.flinker.plugin.rdbms.writer.CommonRdbmsWriter;
import com.ucar.datalink.flinker.plugin.rdbms.writer.Constant;
import com.ucar.datalink.flinker.plugin.rdbms.writer.Key;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlCommonRdbmsWriterTask extends CommonRdbmsWriter.Task{

    private static Configuration originalConfigWriter;

    private static String if_else_replace = "SELECT count(1)";

    private boolean isReplace = false;

    private String primaryKey = null;

    private boolean useUtf8mb4 = false;

    public MySqlCommonRdbmsWriterTask(DataBaseType dataBaseType) {
        super(dataBaseType);
    }

    public void init(Configuration writerSliceConfig) {
        this.username = writerSliceConfig.getString(Key.USERNAME);
        this.password = writerSliceConfig.getString(Key.PASSWORD);
        this.jdbcUrl = writerSliceConfig.getString(Key.JDBC_URL);
        this.table = writerSliceConfig.getString(Key.TABLE);

        this.columns = writerSliceConfig.getList(Key.COLUMN, String.class);
        this.columns = dealKeyConflict(columns);
        this.columnNumber = this.columns.size();

        this.preSqls = writerSliceConfig.getList(Key.PRE_SQL, String.class);
        this.postSqls = writerSliceConfig.getList(Key.POST_SQL, String.class);
        this.batchSize = writerSliceConfig.getInt(Key.BATCH_SIZE, Constant.DEFAULT_BATCH_SIZE);
        this.batchByteSize = writerSliceConfig.getInt(Key.BATCH_BYTE_SIZE, Constant.DEFAULT_BATCH_BYTE_SIZE);

        writeMode = writerSliceConfig.getString(Key.WRITE_MODE, "INSERT");
        emptyAsNull = writerSliceConfig.getBool(Key.EMPTY_AS_NULL, true);
        INSERT_OR_REPLACE_TEMPLATE = writerSliceConfig.getString(Constant.INSERT_OR_REPLACE_TEMPLATE_MARK);
        this.writeRecordSql = String.format(INSERT_OR_REPLACE_TEMPLATE, this.table);


        BASIC_MESSAGE = String.format("jdbcUrl:[%s], table:[%s]",
                this.jdbcUrl, this.table);



        //如果配置了 identity_insert_mode 属性为on，增加 identity_insert 语句
        this.useUtf8mb4 = Boolean.parseBoolean(writerSliceConfig.getString(Key.USE_UTF8MB4, "false"));
//        if(isOn) {
//            StringBuilder utf8mb4Mode = new StringBuilder();
//            utf8mb4Mode.append("set names 'utf8mb4';");
//            utf8mb4Mode.append(this.writeRecordSql).append(" ; ");
//            this.writeRecordSql = utf8mb4Mode.toString();
//        }
    }

    @Override
    public void doBatchInsert(Connection connection, List<Record> buffer)
            throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.writeRecordSql);
            if(this.useUtf8mb4) {
                connection.prepareStatement("SET NAMES 'utf8mb4'").executeQuery();
            }
            for (Record record : buffer) {
                preparedStatement = fillPreparedStatement(
                        preparedStatement, record);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            LOG.warn("回滚此次写入, 采用每次写入一行方式提交. 因为:" + e.getMessage());
            LOG.warn("执行的sql writeRecordSql：" + writeRecordSql);
            connection.rollback();
            doOneInsert(connection, buffer);
        } catch (Exception e) {
            throw DataXException.asDataXException(
                    DBUtilErrorCode.WRITE_DATA_ERROR, e);
        } finally {
            DBUtil.closeDBResources(preparedStatement, null);
        }
    }



}