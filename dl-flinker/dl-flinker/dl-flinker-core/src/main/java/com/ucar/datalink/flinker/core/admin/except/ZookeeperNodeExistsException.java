package com.ucar.datalink.flinker.core.admin.except;

/**
 * Created by yang.wang09 on 2019-03-05 11:33.
 * @author yang.wang09
 */
public class ZookeeperNodeExistsException extends CanIgnoreException {

    public ZookeeperNodeExistsException() {
        super();
    }

    public ZookeeperNodeExistsException(String msg) {
        super(msg);
    }

}
