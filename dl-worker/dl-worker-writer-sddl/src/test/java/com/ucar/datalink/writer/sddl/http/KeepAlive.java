package com.ucar.datalink.writer.sddl.http;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 16/04/2018.
 */
public class KeepAlive implements Serializable {
    private static final long serialVersionUID = -2813120366138988480L;

    /* 覆盖该方法，仅用于测试使用。
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\t维持连接包";
    }
}
