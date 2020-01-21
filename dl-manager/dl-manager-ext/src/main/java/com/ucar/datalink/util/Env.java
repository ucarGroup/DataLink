package com.ucar.datalink.util;

/**
 * Created by yang.wang09 on 2018-05-07 17:03.
 */
public enum Env {

    TEST1("test"),
    TEST2("test2"),
    TEST3("test3"),
    PRE("pre"),
    PROD("prod"),
    XN2("xn2"),
    DEV("dev");

    private String name;

    Env(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public static Env geteEnv(String env) {
        if(TEST1.getName().equals(env)) {
            return TEST1;
        }
        else if(TEST2.getName().equals(env)) {
            return TEST2;
        }
        else if(TEST3.getName().equals(env)) {
            return TEST3;
        }
        else if(PRE.getName().equals(env)) {
            return PRE;
        }
        else if(PROD.getName().equals(env)) {
            return PROD;
        }
        else if(XN2.getName().equals(env)) {
            return XN2;
        }
        else if(DEV.getName().equals(env)) {
            return DEV;
        }
        else {
            //不支持
            throw new UnsupportedOperationException("unknow Env type");
        }
    }


}
