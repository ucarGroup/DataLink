package com.ucar.datalink.biz.utils.ddl;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableAlterColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableChangeColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableModifyColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRenameTableStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerExecStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.google.common.collect.Lists;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.relationship.SqlCheckColumnInfo;
import com.ucar.datalink.domain.relationship.SqlType;
import com.ucar.datalink.domain.relationship.SqlCheckItem;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lubiao on 2017/7/12.
 */
public class SQLStatementHolder {
    private SQLStatement sqlStatement;
    private SchemaStatVisitor schemaStatVisitor;
    private MediaSourceType mediaSourceType;
    private String sqlString;
    private SqlType sqlType = SqlType.Other;
    private List<SqlCheckItem> sqlCheckItems;

    public SQLStatementHolder(SQLStatement sqlStatement, SchemaStatVisitor schemaStatVisitor, MediaSourceType mediaSourceType) {
        this.sqlStatement = sqlStatement;
        this.schemaStatVisitor = schemaStatVisitor;
        this.mediaSourceType = mediaSourceType;
        this.sqlString = buildSqlString();
        this.sqlCheckItems = Lists.newArrayList();
    }

    public void check() {
        if (checkIfTableCreate()) {
            this.sqlType = SqlType.CreateTable;
        } else if (checkIfTableAlter()) {
            this.sqlType = SqlType.AlterTable;
        } else if (checkIfTableDrop()) {
            this.sqlType = SqlType.DropTable;
        } else {
            this.sqlType = SqlType.Other;
        }
    }

    private boolean checkIfTableCreate() {
        if (shouldIgnore()) {
            return false;
        }

        boolean flag = false;
        Map<TableStat.Name, TableStat> tables = schemaStatVisitor.getTables();
        for (Map.Entry<TableStat.Name, TableStat> entry : tables.entrySet()) {
            String tableName = entry.getKey().getName();
            TableStat tStat = entry.getValue();

            if (tStat.getCreateCount() > 0) {
                buildSqlCheckItem(tableName, SqlType.CreateTable);
                flag = true;
            }
        }

        return flag;
    }

    private boolean checkIfTableDrop() {
        if (shouldIgnore()) {
            return false;
        }

        // drop语句支持一个sql对多个表进行操作
        boolean flag = false;
        Map<TableStat.Name, TableStat> tables = schemaStatVisitor.getTables();
        for (Map.Entry<TableStat.Name, TableStat> entry : tables.entrySet()) {
            String tableName = entry.getKey().getName();
            TableStat tStat = entry.getValue();

            if (tStat.getDropCount() > 0) {
                buildSqlCheckItem(tableName, SqlType.DropTable);
                flag = true;
            }
        }

        return flag;
    }

    private boolean checkIfTableAlter() {
        if (shouldIgnore()) {
            return false;
        }

        if (sqlStatement instanceof MySqlRenameTableStatement) {
            MySqlRenameTableStatement renameStmt = (MySqlRenameTableStatement) sqlStatement;
            for (MySqlRenameTableStatement.Item item : renameStmt.getItems()) {
                if (item.getName() instanceof SQLPropertyExpr) {// 带库名前缀时会进入这个分支
                    SQLPropertyExpr expr = (SQLPropertyExpr) item.getName();
                    buildSqlCheckItem(expr.getName(), SqlType.AlterTable);
                } else if (item.getName() instanceof SQLIdentifierExpr) {// 不带库名前缀时进入这个分支
                    SQLIdentifierExpr expr = (SQLIdentifierExpr) item.getName();
                    buildSqlCheckItem(expr.getName(), SqlType.AlterTable);
                } else {
                    throw new RuntimeException("invalid Item type : " + item.getClass().getName());
                }
            }
            return true;
        } else if (sqlStatement instanceof SQLServerExecStatement) {
            SQLServerExecStatement execStmt = (SQLServerExecStatement) sqlStatement;
            String moduleName = execStmt.getModuleName().getSimpleName();
            if ("sp_rename".equalsIgnoreCase(moduleName)) {
                String tableName = ((SQLCharExpr) execStmt.getParameters().get(0).getExpr()).getText();
                buildSqlCheckItem(tableName, SqlType.AlterTable);
                return true;
            }
        } else {
            boolean flag = false;
            Map<TableStat.Name, TableStat> tables = schemaStatVisitor.getTables();
            for (Map.Entry<TableStat.Name, TableStat> entry : tables.entrySet()) {
                String tableName = entry.getKey().getName();
                TableStat tStat = entry.getValue();

                if (tStat.getAlterCount() > 0 && isAlterAffectSync()) {
                    buildSqlCheckItem(tableName, SqlType.AlterTable);
                    flag = true;
                }
            }

            return flag;
        }

        return false;
    }

    private boolean shouldIgnore() {
        // 如果是在备份库中进行操作则不需要检测
        return sqlString.toLowerCase().contains("backup_tables.");
    }

    private void buildSqlCheckItem(String tableName, SqlType ddlSqlType) {
        if (tableName.contains(".")) {
            String[] array = org.apache.commons.lang.StringUtils.split(tableName, ".");
            tableName = array[array.length - 1];
        }

        SqlCheckItem sqlCheckItem = new SqlCheckItem();
        sqlCheckItem.setSqlString(this.getSqlString());
        sqlCheckItem.setTableName(tableName);
        sqlCheckItem.setSqlType(ddlSqlType);
        sqlCheckItem.setContainsTableRename(containsTableRename());
        sqlCheckItem.setContainsColumnAdd(containsColumnAdd());
        sqlCheckItem.setContainsColumnAddAfter(containsColumnAddAfter());
        sqlCheckItem.setContainsColumnRename(containsColumnRename());
        sqlCheckItem.setContainsColumnDrop(containsColumnDrop());
        sqlCheckItem.setContainsColumnModify(containsColumnModify());
        sqlCheckItem.setContainsColumnModifyAfter(containsColumnModifyAfter());
        sqlCheckItem.setContainsIndexesAdd(CollectionUtils.isNotEmpty(buildIndexAddInfo()));
        sqlCheckItem.setContainsUniqueKeysDrop(CollectionUtils.isNotEmpty(buildUniqueKeysDropInfo()));
        sqlCheckItem.setColumnsAddInfo(buildColumnAddInfo());
        sqlCheckItem.setUniqueKeysDropInfo(buildUniqueKeysDropInfo());
        sqlCheckItem.setIndexesAddInfo(buildIndexAddInfo());

        if (SqlType.AlterTable.equals(ddlSqlType)) {
            sqlCheckItem.setAlterAffectColumn(isAlterAffectColumn());
        }

        this.sqlCheckItems.add(sqlCheckItem);
    }

    private String buildSqlString() {
        if (MediaSourceType.MYSQL.equals(mediaSourceType) || MediaSourceType.SDDL.equals(mediaSourceType)) {
            return SQLUtils.toMySqlString(sqlStatement).toLowerCase();
        } else if (MediaSourceType.SQLSERVER.equals(mediaSourceType)) {
            return SQLUtils.toSQLServerString(sqlStatement).toLowerCase();
        } else if (MediaSourceType.POSTGRESQL.equals(mediaSourceType)) {
            return SQLUtils.toPGString(sqlStatement).toLowerCase();
        }
        return "";
    }

    private boolean isAlterAffectSync() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if ((item instanceof SQLAlterTableRenameColumn) || (item instanceof MySqlAlterTableChangeColumn)
                        || (item instanceof SQLAlterTableDropColumnItem) || (item instanceof SQLAlterTableAddColumn)
                        || (item instanceof SQLAlterTableRename) || (item instanceof SQLAlterTableAlterColumn)
                        || (item instanceof MySqlAlterTableModifyColumn) || (item instanceof MySqlAlterTableAlterColumn)
                        || (item instanceof SQLAlterTableDropIndex) || (item instanceof SQLAlterTableAddIndex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAlterAffectColumn() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if ((item instanceof SQLAlterTableRenameColumn) || (item instanceof MySqlAlterTableChangeColumn)
                        || (item instanceof SQLAlterTableDropColumnItem) || (item instanceof SQLAlterTableAddColumn)
                        || (item instanceof SQLAlterTableAlterColumn) || (item instanceof MySqlAlterTableModifyColumn)
                        || (item instanceof MySqlAlterTableAlterColumn)
                        ) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsTableRename() {
        if (sqlStatement instanceof MySqlRenameTableStatement) {
            return true;
        } else if (sqlStatement instanceof SQLServerExecStatement) {
            String moduleName = ((SQLServerExecStatement) sqlStatement).getModuleName().getSimpleName();
            if ("sp_rename".equalsIgnoreCase(moduleName)) {
                return true;
            }
        } else if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if (item instanceof SQLAlterTableRename) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsColumnRename() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if (isColumnRenameSql(item)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsColumnDrop() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if (item instanceof SQLAlterTableDropColumnItem) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsColumnAdd() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if (item instanceof SQLAlterTableAddColumn) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsColumnModify() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if ((item instanceof MySqlAlterTableChangeColumn) || (item instanceof SQLAlterTableAlterColumn)
                        || (item instanceof MySqlAlterTableModifyColumn)
                        || (item instanceof MySqlAlterTableAlterColumn)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<SqlCheckColumnInfo> buildColumnAddInfo() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            List<SqlCheckColumnInfo> columnsAddList = new ArrayList<>();
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if (item instanceof SQLAlterTableAddColumn) {
                    SQLAlterTableAddColumn columnAdd = (SQLAlterTableAddColumn) item;
                    List<SQLColumnDefinition> columnDefinitions = columnAdd.getColumns();
                    List<SqlCheckColumnInfo> columnInfos = new ArrayList<>();
                    for (SQLColumnDefinition columnDefinition : columnDefinitions) {
                        SqlCheckColumnInfo columnInfo = new SqlCheckColumnInfo();
                        columnInfo.setName(columnDefinition.getName().getSimpleName());
                        columnInfo.setDataType(columnDefinition.getDataType().getName());
                        columnInfos.add(columnInfo);
                    }
                    columnsAddList.addAll(columnInfos);
                }
            }
            return columnsAddList;
        }
        return Lists.newArrayList();
    }

    private List<String> buildUniqueKeysDropInfo() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            List<String> indexList = new ArrayList<>();
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if (item instanceof SQLAlterTableDropIndex) {
                    SQLAlterTableDropIndex dropIndex = (SQLAlterTableDropIndex) item;
                    String indexName = dropIndex.getIndexName().getSimpleName();
                    indexList.add(indexName);
                }
            }
            return indexList;
        }
        return Lists.newArrayList();
    }

    private List<String> buildIndexAddInfo() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            List<String> indexList = new ArrayList<>();
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if (item instanceof SQLAlterTableAddIndex) {
                    SQLAlterTableAddIndex addIndex = (SQLAlterTableAddIndex) item;
                    String indexName = addIndex.getName().getSimpleName();
                    indexList.add(indexName);
                }
            }
            return indexList;
        }
        return Lists.newArrayList();
    }

    private boolean containsColumnAddAfter() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if (item instanceof SQLAlterTableAddColumn && (((SQLAlterTableAddColumn) item).getAfterColumn() != null)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsColumnModifyAfter() {
        if (sqlStatement instanceof SQLAlterTableStatement) {
            SQLAlterTableStatement alterStmt = (SQLAlterTableStatement) sqlStatement;
            for (SQLAlterTableItem item : alterStmt.getItems()) {
                if (item instanceof MySqlAlterTableModifyColumn && (((MySqlAlterTableModifyColumn) item).getAfterColumn() != null)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isColumnRenameSql(SQLAlterTableItem item) {
        if (item instanceof SQLAlterTableRenameColumn) {
            return true;
        }

        if (item instanceof MySqlAlterTableChangeColumn) {
            MySqlAlterTableChangeColumn cc = (MySqlAlterTableChangeColumn) item;
            String oldName = cc.getColumnName().getSimpleName();
            String newName = cc.getNewColumnDefinition().getName().getSimpleName();
            if (!oldName.equalsIgnoreCase(newName)) {// 新老列名不相等时才是rename
                return true;
            }
        }

        return false;
    }

    //---------------------------------------------getters && setters---------------------------------

    public SQLStatement getSqlStatement() {
        return sqlStatement;
    }

    public void setSqlStatement(SQLStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    public SchemaStatVisitor getSchemaStatVisitor() {
        return schemaStatVisitor;
    }

    public void setSchemaStatVisitor(SchemaStatVisitor schemaStatVisitor) {
        this.schemaStatVisitor = schemaStatVisitor;
    }

    public MediaSourceType getMediaSourceType() {
        return mediaSourceType;
    }

    public void setMediaSourceType(MediaSourceType mediaSourceType) {
        this.mediaSourceType = mediaSourceType;
    }

    public String getSqlString() {
        return sqlString;
    }

    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    public List<SqlCheckItem> getSqlCheckItems() {
        return sqlCheckItems;
    }

    public void setSqlCheckItems(List<SqlCheckItem> sqlCheckItems) {
        this.sqlCheckItems = sqlCheckItems;
    }

    public SqlType getSqlType() {
        return sqlType;
    }

    public void setSqlType(SqlType sqlType) {
        this.sqlType = sqlType;
    }
}