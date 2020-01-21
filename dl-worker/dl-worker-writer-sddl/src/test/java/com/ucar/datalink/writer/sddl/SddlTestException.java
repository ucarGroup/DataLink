package com.ucar.datalink.writer.sddl;

import com.ucar.datalink.common.errors.DatalinkException;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 28/11/2017.
 */
public class SddlTestException extends DatalinkException {
    private final static long serialVersionUID = 1L;

    public SddlTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public SddlTestException(String message) {
        super(message);
    }

    public SddlTestException(Throwable cause) {
        super(cause);
    }

    public SddlTestException() {
        super();
    }
}
