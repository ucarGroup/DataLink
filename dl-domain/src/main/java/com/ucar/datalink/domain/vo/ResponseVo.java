package com.ucar.datalink.domain.vo;

import java.util.HashMap;
import java.util.Map;

public class ResponseVo {
    private Integer code = 200;
    private String message = "成功";
    private Map<String,Object> data = new HashMap<>(16);

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
