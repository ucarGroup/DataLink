package com.ucar.datalink.flinker.core.admin.bean;

/**
 * Created by user on 2018/3/22.
 */
public class FlowControlData {

    private int byteSpeedByZk;

    private int recoredSpeedByZk;

    public int getByteSpeedByZk() {
        return byteSpeedByZk;
    }

    public void setByteSpeedByZk(int byteSpeedByZk) {
        this.byteSpeedByZk = byteSpeedByZk;
    }

    public int getRecoredSpeedByZk() {
        return recoredSpeedByZk;
    }

    public void setRecoredSpeedByZk(int recoredSpeedByZk) {
        this.recoredSpeedByZk = recoredSpeedByZk;
    }

    @Override
    public String toString() {
        return "FlowControlData{" +
                "byteSpeedByZk=" + byteSpeedByZk +
                ", recoredSpeedByZk=" + recoredSpeedByZk +
                '}';
    }

}
