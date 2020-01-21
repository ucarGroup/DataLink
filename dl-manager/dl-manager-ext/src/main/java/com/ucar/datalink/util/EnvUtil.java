package com.ucar.datalink.util;

/**
 * Created by yang.wang09 on 2018-05-07 16:41.
 */
public class EnvUtil {


    public static final String ENV = ConfigReadUtil.getString("datax.env");



    public static String maxJVMMemory() {
        com.ucar.datalink.util.Env envEnum = com.ucar.datalink.util.Env.geteEnv(ENV);
        switch (envEnum) {
            case DEV:
            case TEST1:
            case TEST2:
            case TEST3:
            case PRE:
            case XN2:
                return "1G";
            case PROD:
                return "5G";
            default:
                return "1G";
        }
    }

    public static String minJVMMemory() {
        com.ucar.datalink.util.Env envEnum = com.ucar.datalink.util.Env.geteEnv(ENV);
        switch (envEnum) {
            case DEV:
            case TEST1:
            case TEST2:
            case TEST3:
            case PRE:
            case XN2:
                return "1G";
            case PROD:
                return "2G";
            default:
                return "1G";
        }
    }
}
