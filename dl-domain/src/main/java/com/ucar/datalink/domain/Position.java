package com.ucar.datalink.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Created by lubiao on 2017/2/23.
 */
public abstract class Position implements Serializable {
    private static final long serialVersionUID = 2332798099928474975L;

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
