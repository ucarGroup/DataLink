package com.ucar.datalink.flinker.plugin.writer.eswriter.exception;

/**
 * Created by yw.zhang02 on 2016/8/22.
 */
public class VersionConflictException extends RuntimeException{

    public VersionConflictException() {
        super();
    }

    public VersionConflictException(String message) {
        super(message);
    }

    public VersionConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public VersionConflictException(Throwable cause) {
        super(cause);
    }
}
