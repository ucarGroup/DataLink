package com.ucar.datalink.writer.hdfs.handle.stream;

import com.ucar.datalink.common.Constants;
import com.ucar.datalink.common.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2017/11/21.
 */
public class FileStreamKeeper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStreamKeeper.class);
    private static final long CHECK_RATE = 5000;// 单位ms
    private static final long LEISURE_CHECK_PERIOD = 60000;//单位ms
    private static final long LEAK_CHECK_PERIOD = 5000;//单位ms

    private static ScheduledExecutorService executorService;
    private static List<FileStreamHolder> holders = new ArrayList<>();
    private static List<FileStreamHolder> leakHolders = new ArrayList<>();

    private static long lastLeisureCheck = 0;
    private static long lastLeakCheck = 0;

    public static void start() {
        executorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("File-Stream-Holder"));
        executorService.scheduleAtFixedRate(
                FileStreamKeeper::check,
                CHECK_RATE,
                CHECK_RATE,
                TimeUnit.MILLISECONDS
        );
        LOGGER.info("File Stream Keeper is started.");
    }

    public static void closeStreamLocal(String hdfsFilePath) {
        holders.stream().forEach(h -> {
                    try {
                        h.closeStreamToken(hdfsFilePath);
                    } catch (Throwable t) {
                        LOGGER.error("stream close failed for file : " + hdfsFilePath);
                    }
                }
        );
    }

    static synchronized void register(FileStreamHolder fileStreamHolder) {
        holders.add(fileStreamHolder);
    }

    static synchronized void unRegister(FileStreamHolder fileStreamHolder) {
        holders.remove(fileStreamHolder);
        if (fileStreamHolder.tokenSize() > 0) {
            //FileStreamHolder在关闭的时候有些流可能会关闭失败
            //需要把tokenSize大于0的Holder放到leakHolders队列中，后台线程定时清理这些holder
            //如果不进行该操作，将引发Lease的ReCreate或OtherCreate问题
            leakHolders.add(fileStreamHolder);
        }
    }

    private static void check() {
        leisureCheck();
        leakCheck();
    }

    private static void leisureCheck() {
        if (System.currentTimeMillis() - lastLeisureCheck < LEISURE_CHECK_PERIOD) {
            return;
        }
        try {
            holders.stream().forEach(h -> {
                MDC.put(Constants.MDC_TASKID, h.getTaskId());

                try {
                    LOGGER.info("leisure streams check begin.");
                    h.closeLeisureStreamTokens();
                    LOGGER.info("leisure streams check end.");
                } finally {
                    MDC.remove(Constants.MDC_TASKID);
                }
            });
            lastLeisureCheck = System.currentTimeMillis();
        } catch (Throwable t) {
            LOGGER.error("something goes wrong when do leisure check.", t);
        }
    }

    private static void leakCheck() {
        if (System.currentTimeMillis() - lastLeakCheck < LEAK_CHECK_PERIOD) {
            return;
        }
        try {
            leakHolders.stream().map(l -> l).collect(Collectors.toList()).forEach(h -> {
                MDC.put(Constants.MDC_TASKID, h.getTaskId());

                try {
                    LOGGER.info("leak holders check begin.");
                    h.closeAllStreamTokens();
                    if (h.tokenSize() == 0) {
                        leakHolders.remove(h);
                    }
                    LOGGER.info("leak holders check end.");
                } finally {
                    MDC.remove(Constants.MDC_TASKID);
                }
            });
            lastLeakCheck = System.currentTimeMillis();
        } catch (Throwable t) {
            LOGGER.error("something goew wrong when do leak check.", t);
        }
    }
}
