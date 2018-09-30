package com.ucar.datalink.common.errors;

/**
 * Indicates that an operation attempted to modify or delete a  task that is not present on the worker.
 */
public class NotFoundException extends DatalinkException {
    public NotFoundException(String s) {
        super(s);
    }

    public NotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NotFoundException(Throwable throwable) {
        super(throwable);
    }
}
