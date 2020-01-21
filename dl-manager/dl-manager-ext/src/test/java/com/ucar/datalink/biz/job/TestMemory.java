package com.ucar.datalink.biz.job;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;


/**
 * Created by user on 2017/10/18.
 */
public class TestMemory {

    public static void main(String[] args) {
        //TestMemory t = new TestMemory();
        getSystemMemory();
    }


    public static void getSystemMemory() {
        //StringBuffer sb=new StringBuffer();
        OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();

        System.out.println("系统物理内存总计：" + osmb.getTotalPhysicalMemorySize()
                / 1024 / 1024 + "MB");
        System.out.println("系统物理可用内存总计：" + osmb.getFreePhysicalMemorySize()
                / 1024 / 1024 + "MB");
    }


    public static void javaMemory() {
        Runtime run = Runtime.getRuntime();
        long total = run.totalMemory();
        long free = run.freeMemory();
        long max = run.maxMemory();
        System.out.println("total memory -> "+total);
        System.out.println("max memory -> "+max);
        System.out.println("free memory -> "+free);
    }

}
