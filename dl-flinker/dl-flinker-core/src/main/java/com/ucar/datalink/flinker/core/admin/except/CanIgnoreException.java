package com.ucar.datalink.flinker.core.admin.except;

/**
 * Created by yang.wang09 on 2019-03-05 11:32.
 * @author yang.wang09
 */
public class CanIgnoreException extends RuntimeException {

    public CanIgnoreException() {
        super();
    }

    public CanIgnoreException(String msg) {
        super(msg);
    }

}
