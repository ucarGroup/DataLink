package com.ucar.datalink.domain.relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lubiao on 2017/7/11.
 */
public class SqlCheckResult {
    private String sqlString;
    private List<SqlCheckTree> sqlCheckTrees;

    public SqlCheckResult() {
        this.sqlString = "";
        this.sqlCheckTrees = new ArrayList<>();
    }

    public SqlCheckResult(String sqlString) {
        this.sqlString = sqlString;
        this.sqlCheckTrees = new ArrayList<>();
    }

    public String getSqlString() {
        return sqlString;
    }

    public void setSqlString(String sqlString) {
        this.sqlString = sqlString;
    }

    public List<SqlCheckTree> getSqlCheckTrees() {
        return sqlCheckTrees;
    }

    public void setSqlCheckTrees(List<SqlCheckTree> sqlCheckTrees) {
        this.sqlCheckTrees = sqlCheckTrees;
    }
}
