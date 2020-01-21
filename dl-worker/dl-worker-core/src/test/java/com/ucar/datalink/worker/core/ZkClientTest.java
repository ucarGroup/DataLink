package com.ucar.datalink.worker.core;

import com.ucar.datalink.common.zookeeper.ZkConfig;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import org.I0Itec.zkclient.DataUpdater;
import org.I0Itec.zkclient.IZkDataListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sqq on 2017/6/26.
 */
public class ZkClientTest {
    public static void main(String[] args) throws Exception {
        ZkConfig zkConfig = new ZkConfig("localhost:2181", 1000, 1000);
        DLinkZkUtils.init(zkConfig, "ctest");
        DLinkZkUtils.get().zkClient().createEphemeral("/ctest");
        DLinkZkUtils.get().zkClient().subscribeDataChanges("/ctest", new IZkDataListener() {
                    @Override
                    public void handleDataChange(String dataPath, Object data) throws Exception {
                        System.out.println(data);
                    }

                    @Override
                    public void handleDataDeleted(String dataPath) throws Exception {

                    }
                }
        );

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                AtomicInteger count = new AtomicInteger(0);
                for (int i = 1; i <= 100; i++) {
                    count.incrementAndGet();
                    DLinkZkUtils.get().zkClient().updateDataSerialized("/ctest", new DataUpdater<Integer>() {
                        @Override
                        public Integer update(Integer currentData) {
                            return count.get();
                        }
                    });
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                AtomicInteger count = new AtomicInteger(100);
                for (int i = 1; i <= 100; i++) {
                    count.incrementAndGet();
                    DLinkZkUtils.get().zkClient().updateDataSerialized("/ctest", new DataUpdater<Integer>() {
                        @Override
                        public Integer update(Integer currentData) {
                            return count.get();
                        }
                    });
                }
            }
        });

        t2.start();
        t1.start();
        t1.join();
        t2.join();
        System.in.read();
    }
}