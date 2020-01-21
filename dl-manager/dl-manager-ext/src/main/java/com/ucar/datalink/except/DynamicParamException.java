package com.ucar.datalink.except;

/**
 * Created by yang.wang09 on 2018-12-04 17:27.
 */
public class DynamicParamException extends RuntimeException {

    public DynamicParamException() {
        super();
    }

    public DynamicParamException(String msg) {
        super(msg);
    }
}
