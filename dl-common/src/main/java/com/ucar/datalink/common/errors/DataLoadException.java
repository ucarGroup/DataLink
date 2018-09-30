package com.ucar.datalink.common.errors;

/**
 * Created by lubiao on 2017/3/17.
 */
public class DataLoadException extends RuntimeException{
    public DataLoadException() {
    }

    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataLoadException(Throwable cause) {
        super(cause);
    }

    public DataLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
