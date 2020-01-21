package com.ucar.datalink.reader.mysql;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lubiao on 2017/10/10.
 */
public class TimerTest {

    public static void main(String args[]) throws Exception {
        Timer timer = new Timer("Test", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(new Date(System.currentTimeMillis()));
            }
        }, 1000L, 1000L);

        System.in.read();
    }
}
