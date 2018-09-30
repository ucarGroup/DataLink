package com.ucar.datalink.worker.api.util.copy;

/**
 * Created by lubiao on 2017/3/22.
 */
public class RecordCopyException extends RuntimeException{
    public RecordCopyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RecordCopyException() {
    }

    public RecordCopyException(String message) {
        super(message);
    }

    public RecordCopyException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecordCopyException(Throwable cause) {
        super(cause);
    }
}
