package com.ucar.datalink.common.errors;

import com.ucar.datalink.common.utils.CodeContext;

public class ErrorException extends RuntimeException {
    private Integer code;
    private String message;

    public ErrorException(Integer code) {
        super(CodeContext.getErrorDesc(code));
        this.code = code;
        this.message = CodeContext.getErrorDesc(code);
    }

    public ErrorException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

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
}
