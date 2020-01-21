package com.ucar.datalink.writer.sddl.exception;

import com.ucar.datalink.common.errors.DatalinkException;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 16/11/2017.
 */
public class SddlCfCentException extends DatalinkException {

    private final static long serialVersionUID = 1L;

    public SddlCfCentException(String message, Throwable cause) {
        super(message, cause);
    }

    public SddlCfCentException(String message) {
        super(message);
    }

    public SddlCfCentException(Throwable cause) {
        super(cause);
    }

    public SddlCfCentException() {
        super();
    }

}
