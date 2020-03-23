package com.ucar.datalink.flinker.plugin.writer.eswriter.exception;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 23/03/2018.
 */
public class ESConfigException extends RuntimeException {

    public ESConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ESConfigException(String message) {
        super(message);
    }

    public ESConfigException(Throwable cause) {
        super(cause);
    }

    public ESConfigException() {
        super();
    }
}
