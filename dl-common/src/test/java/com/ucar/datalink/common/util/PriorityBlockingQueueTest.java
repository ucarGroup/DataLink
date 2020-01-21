package com.ucar.datalink.common.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by lubiao on 2017/9/26.
 */
public class PriorityBlockingQueueTest {

    public static void main(String args[]) {
        BlockingQueue<Long> lastTimestamps = new PriorityBlockingQueue<Long>();
        Long l = System.currentTimeMillis();
        System.out.println(l);

        lastTimestamps.add(l);
        lastTimestamps.add(l);
        lastTimestamps.add(l);
        System.out.println(lastTimestamps.size());
        System.out.println(lastTimestamps.peek());

        lastTimestamps.remove(l);
        System.out.println(lastTimestamps.size());
    }
}
