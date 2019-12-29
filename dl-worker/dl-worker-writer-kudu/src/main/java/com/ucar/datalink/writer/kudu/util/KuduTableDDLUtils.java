package com.ucar.datalink.writer.kudu.util;

import com.ucar.datalink.common.utils.CodeContext;
import com.ucar.datalink.domain.media.parameter.kudu.KuduMediaSrcParameter;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KuduTableDDLUtils {

    private static final Logger LOG = LoggerFactory.getLogger(KuduTableDDLUtils.class);

    public static void addColumn(List<KuduMediaSrcParameter.ImpalaCconfig> impalaConfig, String database, KuduColumnDDL column) throws Exception {
        Assert.notNull(impalaConfig,"impalaConfig is not null");
        Assert.notNull(database,"database is not null");

        if(database.contains("impala::")){
            database = database.replaceAll("impala::","");
        }

        LOG.info("connection impala config{}", ArrayUtils.toString(impalaConfig));
        Connection connection = getConnection(impalaConfig, database);
        Assert.notNull(connection,"Failure of impala connection");
        Statement statement = null;
        try {
            statement = connection.createStatement();
            checkColumnExists(connection,column);
            statement.execute(column.toString());
        } catch (Exception e) {
            String message = String.format("添加列执行失败,执行SQL[%s]",column.toString());
            LOG.info(message,e);
            throw e;
        } finally {
            close(statement,connection);
        }
    }


    private static void checkColumnExists( Connection connection, KuduColumnDDL column) throws SQLException {
        String exeuteSQL = String.format("select * from %s where 1 = 2",column.getTable());

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Set<String> columnName = new HashSet<String>();
        try {
             statement = connection.prepareStatement(exeuteSQL);
             resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            for(int i = 1;  i <= metaData.getColumnCount(); i++){
                columnName.add(metaData.getColumnName(i));
            }
        } catch (SQLException e) {
            LOG.info(exeuteSQL,e);
            throw e;
        } finally {
            close(resultSet,statement);
        }
        Assert.isTrue( !columnName.contains(column.getColumnName()), CodeContext.getErrorDesc(CodeContext.SQLTYPE_NOTSUPPORT_ERROR_CODE ));
    }


    private static void close(AutoCloseable ... closeables){
        for(AutoCloseable c : closeables){
            if(c == null){
                continue;
            }
            try {
                c.close();
            } catch (Exception e) {
            }
        }
    }



    private static Connection getConnection(List<KuduMediaSrcParameter.ImpalaCconfig> impalaConfig, String database) {
        Assert.notNull(impalaConfig,"impalaConfig is not null");
        String driver = "com.cloudera.impala.jdbc41.Driver";
        Connection con;
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            LOG.error("",e);
            return null;
        }

        for(KuduMediaSrcParameter.ImpalaCconfig config  : impalaConfig){
                String url = String.format( "jdbc:impala://%s:%s/%s",config.getHost(),config.getPort(),database);
            try {
                con = DriverManager.getConnection(url);
                return con;
            } catch (Exception e) {
               LOG.error("impala not connection{}",config.toString(),e);
            }
        }
        return null;
    }


    public static class KuduColumnDDL {
        private String table;
        private String columnName;
        private String columnType;


        public KuduColumnDDL(String table, String columnName, String columnType) {
            this.table = table;
            this.columnName = columnName;
            this.columnType = columnType;
        }

        public String getTable() {
            return table;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getColumnType() {
            return columnType;
        }

        @Override
        public String toString() {
            String baseSQL = "alter table %s add columns ( %s  %s)";
            return String.format(baseSQL,table,columnName,columnType);
        }

    }


}
