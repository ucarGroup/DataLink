package com.ucar.datalink.contract;

import java.io.Serializable;

/**
 * Record Source Identifier.
 * <p>
 * Identify the Source of the Record,consist of two parts:namespace and name,
 * in different case they have different meanings.
 * <p>
 * e.g.
 *      1,in database case,namespace may means schema, name may means table-name.
 *      2,in file case ,namespace may meas directory,names may means file-name.
 * <p>
 * Created by lubiao on 2017/3/17.
 */
public class RSI implements Serializable{
    private String namespace;
    private String name;

    public RSI(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RSI rsi = (RSI) o;

        if (!namespace.equals(rsi.namespace)) return false;
        return name.equals(rsi.name);

    }

    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }
}
