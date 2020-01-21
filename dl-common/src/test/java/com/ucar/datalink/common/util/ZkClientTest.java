package com.ucar.datalink.common.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1.IZkDataListener非原子性，接收事件和获取数据是两个分离操作
 * 2.乐观锁，悲观锁
 * 3.Daemon线程(当所有非Daemon线程都结束时，进程会退出，不关心Daemon类型的线程是否已经结束）
 * 4.Join
 * 5.主线程，一个java进程至少有一个主线程，用于执行main方法
 *
 * Created by user on 2017/6/22.
 */
public class ZkClientTest {

    public static void main(String[] args) throws Exception {
        /*ZkConfig zkConfig = new ZkConfig("ctest", "localhost:2181", 1000, 1000);
        ZkUtils.init(zkConfig);
        ZkUtils.get().createEphemeral("/ctest");*/
        /*ZkUtils.get().subscribeDataChanges("/ctest", new IZkDataListener() {
                    @Override
                    public void handleDataChange(String dataPath, Object data) throws Exception {
                        System.out.println(data);
                    }

                    @Override
                    public void handleDataDeleted(String dataPath) throws Exception {

                    }
                }

        );*/

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                AtomicInteger count = new AtomicInteger(0);
                for (int i = 1; i <= 10000; i++) {
                    count.incrementAndGet();
                    System.out.println(count.get());
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                    /*ZkUtils.get().updateDataSerialized("/ctest", new DataUpdater<Integer>() {

                        @Override
                        public Integer update(Integer currentData) {
                            return count.get();
                        }
                    });*/
                }
            }
        });
        t1.setDaemon(true);

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                AtomicInteger count = new AtomicInteger(100);

                for (int i = 1; i <= 10000; i++) {
                    count.incrementAndGet();
                    System.out.println(count.get());
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                    /*ZkUtils.get().updateDataSerialized("/ctest", new DataUpdater<Integer>() {

                        @Override
                        public Integer update(Integer currentData) {
                            return count.get();
                        }
                    });*/
                }
            }
        });
        //t2.setDaemon(true);

        t2.start();
        t1.start();

    }
}
