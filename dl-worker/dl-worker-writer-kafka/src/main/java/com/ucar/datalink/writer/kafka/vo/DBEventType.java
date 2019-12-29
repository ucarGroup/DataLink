package com.ucar.datalink.writer.kafka.vo;

import java.io.Serializable;

/**
 * @auther yifan.liu02
 * @date 2019/12/11
 */
public enum DBEventType implements Serializable {
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private String value;

    private DBEventType(String value) {
        this.value = value;
    }

    public static DBEventType getDBEventTypeFromCode(String code) {
        if ("INSERT".equals(code)) {
            return INSERT;
        } else if ("UPDATE".equals(code)) {
            return UPDATE;
        } else {
            return "DELETE".equals(code) ? DELETE : null;
        }
    }

    public String getValue() {
        return this.value;
    }
}
