package com.ucar.datalink.worker.api.util.compile.errors;

/**
 *@author lubiao
 */
public class CompileExprException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CompileExprException(String message) {
        super(message);
    }

    public CompileExprException(String message, Throwable cause) {
        super(message, cause);
    }
}
