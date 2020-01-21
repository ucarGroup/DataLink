package com.ucar.datalink.biz.auto.envfilter;

import com.ucar.datalink.util.ConfigReadUtil;
import com.ucar.datalink.util.Env;

/**
 * Created by yang.wang09 on 2018-05-07 15:41.
 */
public class StrategyFactory {


    public static IEnvStrategy getStrategy() {
        String env = ConfigReadUtil.getString("datax.env");
        Env e = Env.geteEnv(env);
        IEnvStrategy strategy = null;
        switch(e) {
            case TEST1:
            case TEST2:
            case TEST3:
                strategy = new TestStrategy();
                break;
            case PRE:
                strategy = new PreStrategy();
                break;
            case PROD:
                strategy = new ProdStrategy();
                break;
            case XN2:
            case DEV:
                strategy = new DevStrategy();
            default:
        }
        return strategy;
    }
}
