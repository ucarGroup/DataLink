package com.ucar.datalink.biz.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yifan.liu02
 * @date 2018/12/27
 */
public enum AuditLogOperType {
    insert("1", "新增"), update("2", "修改"), delete("3", "删除"), other("4", "其他"),;
    private String value;
    private String desc;
    private static final Map<String, String> map = new HashMap<>();

    static {
        for (AuditLogOperType auditLogOperType : AuditLogOperType.values()) {
            map.put(auditLogOperType.getValue(), auditLogOperType.getDesc());
        }
    }

    AuditLogOperType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static String getDescFromValue(String value) {
        return map.get(value);
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
