package com.ucar.datalink.common.jvm;

import java.lang.management.*;
import java.util.List;

/**
 * Created by lubiao on 2018/4/2.
 */
public class JvmUtils {

    public static JvmSnapshot buildJvmSnapshot() {
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();

        JvmSnapshot jvmSnapshot = new JvmSnapshot();
        jvmSnapshot.setStartTime(startTime);
        //计算新生代和老年代的内存使用情况
        List<MemoryPoolMXBean> mps = ManagementFactory.getMemoryPoolMXBeans();
        long edenUsed = 0, survivorUsed = 0, edenMax = 0, survivorMax = 0;
        for (MemoryPoolMXBean mp : mps) {
            MemoryType type = mp.getType();
            String name = mp.getName();
            if (type == MemoryType.HEAP) {
                switch (name) {
                    case "Par Eden Space":
                    case "PS Eden Space": {
                        MemoryUsage memoryUsage = mp.getUsage();
                        edenUsed = memoryUsage.getUsed();
                        edenMax = memoryUsage.getMax();
                        break;
                    }
                    case "Par Survivor Space":
                    case "PS Survivor Space": {
                        MemoryUsage memoryUsage = mp.getUsage();
                        survivorUsed = memoryUsage.getUsed();
                        survivorMax = memoryUsage.getMax();
                        break;
                    }
                    case "CMS Old Gen":
                    case "PS Old Gen": {
                        MemoryUsage memoryUsage = mp.getUsage();
                        jvmSnapshot.setOldUsed(memoryUsage.getUsed());
                        jvmSnapshot.setOldMax(memoryUsage.getMax());
                        break;
                    }
                }
            }
        }
        jvmSnapshot.setYoungUsed(edenUsed + survivorUsed);
        jvmSnapshot.setYoungMax(edenMax + survivorMax);
        //计算新生代和老年代的GC次数和时间
        List<GarbageCollectorMXBean> gc = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gc) {
            String name = gcBean.getName();
            switch (name) {
                case "ParNew":
                case "PS Scavenge": {
                    jvmSnapshot.setYoungCollectionCount(gcBean.getCollectionCount());
                    jvmSnapshot.setYoungCollectionTime(gcBean.getCollectionTime());
                    break;
                }
                case "ConcurrentMarkSweep":
                case "PS MarkSweep": {
                    jvmSnapshot.setOldCollectionCount(gcBean.getCollectionCount());
                    jvmSnapshot.setOldCollectionTime(gcBean.getCollectionTime());
                    break;
                }
            }
        }
        //计算当前线程数
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        jvmSnapshot.setCurrentThreadCount(threadMXBean.getThreadCount());

        return jvmSnapshot;
    }
}
