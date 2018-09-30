package com.ucar.datalink.common.errors;

/**
 * Created by lubiao on 2017/12/15.
 */
public class TaskClosedException extends DatalinkException{

    public TaskClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskClosedException(String message) {
        super(message);
    }

    public TaskClosedException(Throwable cause) {
        super(cause);
    }

    public TaskClosedException() {
    }
}
