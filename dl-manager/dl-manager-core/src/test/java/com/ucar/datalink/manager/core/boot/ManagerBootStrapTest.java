package com.ucar.datalink.manager.core.boot;

/**
 * Created by lubiao on 2017/1/17.
 */
public class ManagerBootStrapTest {
    public static void main(String args[]) {
        ManagerBootStrap bootStrap = new ManagerBootStrap();
        bootStrap.boot(args);
    }
}
