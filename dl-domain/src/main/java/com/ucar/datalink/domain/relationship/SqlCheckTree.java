package com.ucar.datalink.domain.relationship;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by lubiao on 2017/7/15.
 */
public class SqlCheckTree {
    private String tableName;
    private SyncNode rootNode;
    private SqlExeDirection sqlExeDirection;
    private Set<SqlCheckNote> sqlCheckNotes;

    public SqlCheckTree() {
        this.sqlCheckNotes = new HashSet<>();
    }

    public SqlCheckTree(SyncNode rootNode, String tableName) {
        this.rootNode = rootNode;
        this.tableName = tableName;
        this.sqlCheckNotes = new TreeSet<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public SyncNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(SyncNode rootNode) {
        this.rootNode = rootNode;
    }

    public SqlExeDirection getSqlExeDirection() {
        return sqlExeDirection;
    }

    public void setSqlExeDirection(SqlExeDirection sqlExeDirection) {
        this.sqlExeDirection = sqlExeDirection;
    }

    public Set<SqlCheckNote> getSqlCheckNotes() {
        return sqlCheckNotes;
    }

    public void setSqlCheckNotes(Set<SqlCheckNote> sqlCheckNotes) {
        this.sqlCheckNotes = sqlCheckNotes;
    }
}
