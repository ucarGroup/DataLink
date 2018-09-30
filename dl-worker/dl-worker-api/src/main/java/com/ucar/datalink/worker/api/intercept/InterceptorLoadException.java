package com.ucar.datalink.worker.api.intercept;

/**
 * Created by user on 2017/3/22.
 */
public class InterceptorLoadException extends RuntimeException{
    public InterceptorLoadException() {
    }

    public InterceptorLoadException(String message) {
        super(message);
    }

    public InterceptorLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterceptorLoadException(Throwable cause) {
        super(cause);
    }

    public InterceptorLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
