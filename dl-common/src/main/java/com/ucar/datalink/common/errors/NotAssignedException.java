package com.ucar.datalink.common.errors;


/**
 * Thrown when a request intended for the owner of a task  is received by a worker which doesn't
 * own it.
 */
public class NotAssignedException extends DatalinkException {

    public NotAssignedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAssignedException(String message) {
        super(message);
    }

    public NotAssignedException(Throwable cause) {
        super(cause);
    }

    public NotAssignedException() {
    }
}
