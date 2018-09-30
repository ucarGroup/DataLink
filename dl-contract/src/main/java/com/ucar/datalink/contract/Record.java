package com.ucar.datalink.contract;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Record<T> implements Serializable {
    private Map<String, Object> metaData = new HashMap<>();

    public abstract T getId();

    public Map<String, Object> metaData() {
        return metaData;
    }

    public abstract RSI RSI();
}
