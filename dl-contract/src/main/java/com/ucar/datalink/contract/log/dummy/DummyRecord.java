package com.ucar.datalink.contract.log.dummy;


import com.ucar.datalink.contract.RSI;
import com.ucar.datalink.contract.Record;

import java.io.Serializable;

/**
 * Created by lubiao on 2017/2/17.
 */
public class DummyRecord extends Record<String> implements Serializable{

    @Override
    public RSI RSI() {
        return new RSI("xxx", "xxx");
    }

    @Override
    public String getId() {
        return null;
    }
}
