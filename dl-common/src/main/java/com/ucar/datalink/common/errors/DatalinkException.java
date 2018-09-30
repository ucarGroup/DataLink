package com.ucar.datalink.common.errors;

/**
 * The base class of all other Datalink exceptions
 */
public class DatalinkException extends RuntimeException {

    private final static long serialVersionUID = 1L;

    public DatalinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatalinkException(String message) {
        super(message);
    }

    public DatalinkException(Throwable cause) {
        super(cause);
    }

    public DatalinkException() {
        super();
    }

}
