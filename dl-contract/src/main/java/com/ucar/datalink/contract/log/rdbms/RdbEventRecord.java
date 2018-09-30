package com.ucar.datalink.contract.log.rdbms;

import com.ucar.datalink.contract.RSI;
import com.ucar.datalink.contract.Record;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 每条Mysql变更数据.
 *
 * @author lubiao
 */
public class RdbEventRecord extends Record<String> implements Serializable {

    private static final long serialVersionUID = -7071677425383765372L;

    /**
     * 数据表名
     */
    private String tableName;

    /**
     * 数据库（实例）名称
     */
    private String schemaName;

    /**
     * 变更数据的业务类型(I/U/D/C/A/E)
     */
    private EventType eventType;

    /**
     * 发生数据变更的业务时间.
     */
    private long executeTime;

    /**
     * 变更前的主键值，和oldColumns不同的是，只有主键发生变化时，才需要给oldKeys设置值
     * 而oldColumns，不管前后是否发生更新变化，都会赋值
     */
    private List<EventColumn> oldKeys = new ArrayList<>();

    /**
     * 变更后的主键值,如果是insert/delete,变更前和变更后的主键值是一样的.
     */
    private List<EventColumn> keys = new ArrayList<>();

    /**
     * 变更前非主键的其他字段
     */
    private List<EventColumn> oldColumns = new ArrayList<>();

    /**
     * 变更后非主键的其他字段
     */
    private List<EventColumn> columns = new ArrayList<>();

    /**
     * 当eventType =
     * CREATE/ALTER/ERASE时，就是对应的sql语句，其他情况为动态生成的INSERT/UPDATE/DELETE sql
     */
    private String sql;

    /**
     * ddl/query的schemaName，会存在跨库ddl，需要保留执行ddl的当前schemaName
     */
    private String ddlSchemaName;

    /**
     * 生成对应的hint内容
     */
    private String hint;

    /**
     * Record资源标识符
     */
    private RSI rsi;

    @Override
    public String getId() {
        if (keys.size() == 1) {
            return keys.get(0).getColumnValue();
        } else if (keys.size() > 1) {
            List<String> keyValueList = new ArrayList<>();
            for (EventColumn key : keys) {
                keyValueList.add(key.getColumnValue());
            }
            return String.join("|", keyValueList);
        }
        return null;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        this.rsi = null;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        this.rsi = null;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public List<EventColumn> getKeys() {
        return keys;
    }

    public void setKeys(List<EventColumn> keys) {
        this.keys = keys;
    }

    public List<EventColumn> getOldColumns() {
        return oldColumns;
    }

    public void setOldColumns(List<EventColumn> oldColumns) {
        this.oldColumns = oldColumns;
    }

    public List<EventColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<EventColumn> columns) {
        this.columns = columns;
    }

    public List<EventColumn> getOldKeys() {
        return oldKeys;
    }

    public void setOldKeys(List<EventColumn> oldKeys) {
        this.oldKeys = oldKeys;
    }

    public String getDdlSchemaName() {
        return ddlSchemaName;
    }

    public void setDdlSchemaName(String ddlSchemaName) {
        this.ddlSchemaName = ddlSchemaName;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public RSI RSI() {
        if (rsi == null) {
            rsi = new RSI(schemaName, tableName);
        }
        return rsi;
    }

    /**
     * 返回所有实际变更的字段
     */
    public List<EventColumn> getUpdatedColumns() {
        List<EventColumn> columns = new ArrayList<>();
        for (EventColumn column : this.columns) {
            if (column.isUpdate()) {
                columns.add(column);
            }
        }

        return columns;
    }

    public EventColumn getOldColumn(String columnName) {
        if (this.oldColumns == null || this.oldColumns.isEmpty())
            return null;

        for (EventColumn column : this.oldColumns) {
            if (column.getColumnName().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    public EventColumn getColumn(String columnName) {
        if (this.columns == null || this.columns.isEmpty())
            return null;

        for (EventColumn column : this.columns) {
            if (column.getColumnName().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    //rsi不参与equals判断
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RdbEventRecord)) return false;

        RdbEventRecord record = (RdbEventRecord) o;

        if (executeTime != record.executeTime) return false;
        if (!columns.equals(record.columns)) return false;
        if (ddlSchemaName != null ? !ddlSchemaName.equals(record.ddlSchemaName) : record.ddlSchemaName != null)
            return false;
        if (eventType != record.eventType) return false;
        if (hint != null ? !hint.equals(record.hint) : record.hint != null) return false;
        if (!keys.equals(record.keys)) return false;
        if (!oldKeys.equals(record.oldKeys)) return false;
        if (!schemaName.equals(record.schemaName)) return false;
        if (sql != null ? !sql.equals(record.sql) : record.sql != null) return false;
        if (!tableName.equals(record.tableName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = 31 * result + schemaName.hashCode();
        result = 31 * result + eventType.hashCode();
        result = 31 * result + (int) (executeTime ^ (executeTime >>> 32));
        result = 31 * result + oldKeys.hashCode();
        result = 31 * result + keys.hashCode();
        result = 31 * result + columns.hashCode();
        result = 31 * result + (sql != null ? sql.hashCode() : 0);
        result = 31 * result + (ddlSchemaName != null ? ddlSchemaName.hashCode() : 0);
        result = 31 * result + (hint != null ? hint.hashCode() : 0);
        return result;
    }

}
