package com.ucar.datalink.domain.event;

/**
 * Created by user on 2017/9/13.
 */
public class HBaseRange {

    private String startRowkey;
    private String endRowkey;
    private int count;

    public String getStartRowkey() {
        return startRowkey;
    }

    public void setStartRowkey(String startRowkey) {
        this.startRowkey = startRowkey;
    }

    public String getEndRowkey() {
        return endRowkey;
    }

    public void setEndRowkey(String endRowkey) {
        this.endRowkey = endRowkey;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
