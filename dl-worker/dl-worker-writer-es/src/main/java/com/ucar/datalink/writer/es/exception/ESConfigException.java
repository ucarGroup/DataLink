package com.ucar.datalink.writer.es.exception;

import com.ucar.datalink.common.errors.DatalinkException;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 23/03/2018.
 */
public class ESConfigException extends DatalinkException {

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
