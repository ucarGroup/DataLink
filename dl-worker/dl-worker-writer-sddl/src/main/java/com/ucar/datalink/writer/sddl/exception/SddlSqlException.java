package com.ucar.datalink.writer.sddl.exception;

import com.ucar.datalink.common.errors.DatalinkException;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 16/11/2017.
 */
public class SddlSqlException extends DatalinkException {

    private final static long serialVersionUID = 1L;

    public SddlSqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public SddlSqlException(String message) {
        super(message);
    }

    public SddlSqlException(Throwable cause) {
        super(cause);
    }

    public SddlSqlException() {
        super();
    }

}
