package com.ucar.datalink.flinker.core.admin.bean;

/**
 * Created by user on 2018/3/19.
 */
public class ChannelData {

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
        return "ChannelData{" +
                "byteSpeedByZk=" + byteSpeedByZk +
                ", recoredSpeedByZk=" + recoredSpeedByZk +
                '}';
    }


    public static void main(String[] args) {

    }
}