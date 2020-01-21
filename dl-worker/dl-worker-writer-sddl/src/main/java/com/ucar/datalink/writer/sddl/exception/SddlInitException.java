package com.ucar.datalink.writer.sddl.exception;

import com.ucar.datalink.common.errors.DatalinkException;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 16/11/2017.
 */
public class SddlInitException extends DatalinkException {

    private final static long serialVersionUID = 1L;

    public SddlInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public SddlInitException(String message) {
        super(message);
    }

    public SddlInitException(Throwable cause) {
        super(cause);
    }

    public SddlInitException() {
        super();
    }

}
