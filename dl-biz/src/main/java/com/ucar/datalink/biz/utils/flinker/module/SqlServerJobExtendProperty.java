package com.ucar.datalink.biz.utils.flinker.module;

/**
 * Created by user on 2017/7/26.
 */
public class SqlServerJobExtendProperty extends AbstractJobExtendProperty {

    private String where;

    private String querySql;

    private String preSql;

    private String postSql;

    private String jdbcReaderUrl;

    private String identityInsertMode;

    private String isAutoIncrement;

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getQuerySql() {
        return querySql;
    }

    public void setQuerySql(String querySql) {
        this.querySql = querySql;
    }

    public String getPreSql() {
        return preSql;
    }

    public void setPreSql(String preSql) {
        this.preSql = preSql;
    }

    public String getPostSql() {
        return postSql;
    }

    public void setPostSql(String postSql) {
        this.postSql = postSql;
    }

    public String getJdbcReaderUrl() {
        return jdbcReaderUrl;
    }

    public void setJdbcReaderUrl(String jdbcReaderUrl) {
        this.jdbcReaderUrl = jdbcReaderUrl;
    }

    public String getIdentityInsertMode() {
        return identityInsertMode;
    }

    public void setIdentityInsertMode(String identityInsertMode) {
        this.identityInsertMode = identityInsertMode;
    }

    public String getIsAutoIncrement() {
        return isAutoIncrement;
    }

    public void setIsAutoIncrement(String isAutoIncrement) {
        this.isAutoIncrement = isAutoIncrement;
    }

    @Override
    public String toString() {
        return "SqlServerJobExtendProperty{" +
                "where='" + where + '\'' +
                ", querySql='" + querySql + '\'' +
                '}';
    }

}
