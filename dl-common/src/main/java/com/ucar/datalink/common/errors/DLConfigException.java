package com.ucar.datalink.common.errors;

/**
 * Created by lubiao on 2016/12/22.
 */
public class DLConfigException extends DatalinkException {
    public DLConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public DLConfigException(String message) {
        super(message);
    }

    public DLConfigException(Throwable cause) {
        super(cause);
    }

    public DLConfigException() {
    }
}
