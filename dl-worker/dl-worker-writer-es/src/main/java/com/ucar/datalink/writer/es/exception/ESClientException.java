package com.ucar.datalink.writer.es.exception;

import com.ucar.datalink.common.errors.DatalinkException;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 23/03/2018.
 */
public class ESClientException extends DatalinkException{
    public ESClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ESClientException(String message) {
        super(message);
    }

    public ESClientException(Throwable cause) {
        super(cause);
    }

    public ESClientException() {
        super();
    }
}
