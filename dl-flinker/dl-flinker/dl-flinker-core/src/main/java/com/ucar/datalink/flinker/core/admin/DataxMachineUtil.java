package com.ucar.datalink.flinker.core.admin;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

/**
 * Created by user on 2017/10/18
 * 用来获取Datax机器相关的系统信息工具类.
 */
public final class DataxMachineUtil {

    public static long getMachineTotalMemory() {
        OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return osmb.getTotalPhysicalMemorySize();
    }

    public static long getMachineFreeMemory() {
        OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return osmb.getFreePhysicalMemorySize();
    }



}
