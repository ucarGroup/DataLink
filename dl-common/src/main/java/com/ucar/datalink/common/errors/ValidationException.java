package com.ucar.datalink.common.errors;

/**
 * Created by lubiao on 2017/4/14.
 */
public class ValidationException extends DatalinkException{

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException() {
    }
}
