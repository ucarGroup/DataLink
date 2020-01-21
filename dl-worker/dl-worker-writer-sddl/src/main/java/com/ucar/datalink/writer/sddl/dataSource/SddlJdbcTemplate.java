package com.ucar.datalink.writer.sddl.dataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 27/11/2017.
 */
public class SddlJdbcTemplate {

    private JdbcTemplate jdbcTemplate;
    private String       schemaName;

    public SddlJdbcTemplate() {
    }

    public SddlJdbcTemplate(JdbcTemplate jdbcTemplate, String schemaName) {
        this.jdbcTemplate = jdbcTemplate;
        this.schemaName = schemaName;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
