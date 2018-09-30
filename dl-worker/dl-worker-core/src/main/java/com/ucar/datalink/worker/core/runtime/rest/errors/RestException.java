package com.ucar.datalink.worker.core.runtime.rest.errors;

import com.ucar.datalink.common.errors.DatalinkException;

import javax.ws.rs.core.Response;

public class RestException extends DatalinkException {
    private final int statusCode;
    private final int errorCode;

    public RestException(int statusCode, int errorCode, String message, Throwable t) {
        super(message, t);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public RestException(Response.Status status, int errorCode, String message, Throwable t) {
        this(status.getStatusCode(), errorCode, message, t);
    }

    public RestException(int statusCode, int errorCode, String message) {
        this(statusCode, errorCode, message, null);
    }

    public RestException(Response.Status status, int errorCode, String message) {
        this(status, errorCode, message, null);
    }

    public RestException(int statusCode, String message, Throwable t) {
        this(statusCode, statusCode, message, t);
    }

    public RestException(Response.Status status, String message, Throwable t) {
        this(status, status.getStatusCode(), message, t);
    }

    public RestException(int statusCode, String message) {
        this(statusCode, statusCode, message, null);
    }

    public RestException(Response.Status status, String message) {
        this(status.getStatusCode(), status.getStatusCode(), message, null);
    }


    public int statusCode() {
        return statusCode;
    }

    public int errorCode() {
        return errorCode;
    }
}
