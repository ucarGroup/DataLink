package com.ucar.datalink.flinker.core;

/**
 * Created by user on 2017/10/18.
 * 这个类用来表示DataX机器的信息，如内存大小，可用内存等
 * 目前只有总内存和free内存两个参数，以后版本可能会加入更多的参数，在选择机器的时候有更多判断条件达到更精确的目的
 */
public class DataxMachineInfo {

    /**
     * 当前机器的总内存，这是一个不可变值
     */
    private long totalMemory;

    /**
     * 当前机器的可用内存
     */
    private long freeMemory;


    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(long freeMemory) {
        this.freeMemory = freeMemory;
    }


}
