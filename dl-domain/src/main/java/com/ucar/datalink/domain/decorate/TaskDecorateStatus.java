package com.ucar.datalink.domain.decorate;

/**
 * @author xy.li
 * @date 2019/06/04
 */
public enum TaskDecorateStatus {
    NEW_CREATED(0, "新建"), RUNNING(1, "补录中"), SUCESSED(2, "成功"), FAILED(3, "失败");
    private String value;
    private int code;

    TaskDecorateStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public int getCode() {
        return code;
    }

    public String getValueByCode(int code) {
        for (TaskDecorateStatus t : TaskDecorateStatus.values()) {
            if (t.getCode() == code) {
                return t.getValue();
            }
        }
        return "";
    }
}
