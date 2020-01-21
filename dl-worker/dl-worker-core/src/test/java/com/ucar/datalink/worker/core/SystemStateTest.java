package com.ucar.datalink.worker.core;

import com.sun.management.OperatingSystemMXBean;
import org.hyperic.sigar.*;
import org.hyperic.sigar.util.PrintfFormat;
import org.junit.Test;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qianqian.shi on 2018/5/9.
 */
public class SystemStateTest {

    @Test
    public void gcTest() {
        String nextLine = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        List<GarbageCollectorMXBean> gc = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean GarbageCollectorMXBean : gc) {
            sb.append("name: " + GarbageCollectorMXBean.getName() + nextLine);
            sb.append("已发生的回收的总次数: " + GarbageCollectorMXBean.getCollectionCount() + nextLine);
            sb.append("近似的累积回收时间: " + GarbageCollectorMXBean.getCollectionTime() + "毫秒" + nextLine);
        }
        System.out.println(sb);
    }

    @Test
    public void systemMemoryTest() {
        OperatingSystemMXBean osmb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        System.out.println("系统总物理内存：" + osmb.getTotalPhysicalMemorySize() / 1024 / 1024 + "MB");
        System.out.println("系统可用物理内存：" + osmb.getFreePhysicalMemorySize() / 1024 / 1024 + "MB");

        Runtime run = Runtime.getRuntime();
        System.out.println("可使用内存:" + run.totalMemory() / 1024 / 1024 + "MB");
        System.out.println("最大可使用内存:" + run.maxMemory() / 1024 / 1024 + "MB");
        System.out.println("剩余内存:" + run.freeMemory() / 1024 / 1024 + "MB");
    }

    @Test
    public void threadTest() {

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        System.out.println("CurrentThreadCount:" + threadMXBean.getThreadCount());
        System.out.println("TotalStartedThreadCount:" + threadMXBean.getTotalStartedThreadCount());
        System.out.println("DaemonThreadCount:" + threadMXBean.getDaemonThreadCount());
        System.out.println("PeakThreadCount:" + threadMXBean.getPeakThreadCount());

        int processor = Runtime.getRuntime().availableProcessors();//获取cpu核心数
        System.out.println(processor);

        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while(threadGroup.getParent() != null){
            threadGroup = threadGroup.getParent();
        }
        int totalThread = threadGroup.activeCount();
        System.out.println(totalThread);

        Long m = 52456L;
        Long chu = m/4;
        System.out.println("chu=:"+chu);
        BigDecimal n = new BigDecimal((double)m / 1024 / 1024 * 8).setScale(2, BigDecimal.ROUND_HALF_UP);
        System.out.println("n=:"+n);
    }


    @Test
    public void sigar() throws SigarException {
        //String path2 = "F:\\workbenchucar\\hyperic-sigar-1.6.4\\sigar-bin\\lib";
        String path2 = "F:\\workbenchucar\\UCARDATALINK\\trunk\\dl-worker\\dl-worker-core\\target\\classes\\sigar";
        String path = System.getProperty("java.library.path");
        System.setProperty("java.library.path", path + ";" + path2);
        System.out.println(System.getProperty("java.library.path"));
        Sigar sigar = new Sigar();
        //CPU
        CpuInfo infos[] = sigar.getCpuInfoList();
        CpuPerc cpuList[] = sigar.getCpuPercList();
        double userSum = 0;
        double sysSum = 0;
        double combinedSum = 0;
        for (int i = 0; i < infos.length; i++) {// 不管是单块CPU还是多CPU都适用
            CpuInfo info = infos[i];
            System.out.println("第" + (i + 1) + "块CPU信息");
            System.out.println("CPU的总量MHz:    " + info.getMhz());// CPU的总量MHz
            System.out.println("CPU生产商:    " + info.getVendor());// 获得CPU的卖主，如：Intel
            System.out.println("CPU类别:    " + info.getModel());// 获得CPU的类别，如：Celeron
            System.out.println("CPU缓存数量:    " + info.getCacheSize());// 缓冲存储器数量
            printCpuPerc(cpuList[i]);
            userSum += cpuList[i].getUser();
            sysSum += cpuList[i].getSys();
            combinedSum += cpuList[i].getCombined();
        }
        BigDecimal userUtilization = new BigDecimal(userSum * 100.0D / 4).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal sysUtilization = new BigDecimal(sysSum * 100.0D / 4).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal combinedUtilization = new BigDecimal(combinedSum * 100.0D / 4).setScale(2, BigDecimal.ROUND_HALF_UP);
        System.out.println("-----CPU用户平均使用率:    " + userUtilization);
        System.out.println("-----CPU用户平均使用率:    " + CpuPerc.format(userSum / 4));
        System.out.println("-----CPU系统平均使用率:    " + sysUtilization);
        System.out.println("-----CPU系统平均使用率:    " + CpuPerc.format(sysSum / 4));
        System.out.println("-----CPU总的平均使用率:    " + combinedUtilization);
        System.out.println("-----CPU总的平均使用率:    " + CpuPerc.format(combinedSum / 4));
        //TCP
        Tcp tcp = sigar.getTcp();
        long tcpActive = tcp.getActiveOpens();
        long passiveOpens = tcp.getPassiveOpens();
        long attemptFails = tcp.getAttemptFails();
        long estabResets = tcp.getEstabResets();
        long currEstab = tcp.getCurrEstab();
        System.out.println("-----TcpActiveOpens:" + tcpActive);
        System.out.println("-----passiveOpens:" + passiveOpens);
        System.out.println("-----attemptFails:" + attemptFails);
        System.out.println("-----estabResets:" + estabResets);
        System.out.println("-----currEstab:" + currEstab);

        //Network Traffic
        long incomingNetworkTraffic;
        long outgoingNetworkTraffic;
        Map<String, Long> incomingNetworkTrafficMap = new HashMap<>();
        Map<String, Long> outgoingNetworkTrafficMap = new HashMap<>();
        String ifNames[] = sigar.getNetInterfaceList();
        for (String name : ifNames) {
            NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
            if ((ifconfig.getFlags() & 1L) <= 0L) {
                System.out.println("!IFF_UP...skipping getNetInterfaceStat");
                continue;
            }
            if ((ifconfig.getFlags() & 1L) <= 0L) {
                System.out.println("!IFF_UP...skipping getNetInterfaceStat");
                continue;
            }
            NetInterfaceStat ifstat = sigar.getNetInterfaceStat(name);
            incomingNetworkTraffic = ifstat.getRxBytes();
            outgoingNetworkTraffic = ifstat.getTxBytes();
            incomingNetworkTrafficMap.put(name, incomingNetworkTraffic);
            outgoingNetworkTrafficMap.put(name, outgoingNetworkTraffic);
            System.out.println("网络设备名:    " + name);// 网络设备名
            System.out.println("IP地址:    " + ifconfig.getAddress());// IP地址
            System.out.println("子网掩码:    " + ifconfig.getNetmask());// 子网掩码
            System.out.println(name + "接收到的总字节数:" + ifstat.getRxBytes());// 接收到的总字节数
            System.out.println(name + "发送的总字节数:" + ifstat.getTxBytes());// 发送的总字节数
        }

        System.out.println("incomingNetworkTrafficMap:" + incomingNetworkTrafficMap);
        System.out.println("outgoingNetworkTrafficMap:" + outgoingNetworkTrafficMap);
        System.out.println( "eth6:"+ incomingNetworkTrafficMap.get("eth6"));
//        BigDecimal incomingNetworkTrafficMbps = new BigDecimal((double)incomingNetworkTraffic / 1024 / 1024 * 8).setScale(2, BigDecimal.ROUND_HALF_UP);
//        BigDecimal outgoingNetworkTrafficMbps = new BigDecimal((double)outgoingNetworkTraffic / 1024 / 1024 * 8).setScale(2, BigDecimal.ROUND_HALF_UP);
//        System.out.println("-----incoming Network Traffic(Mbps):" + incomingNetworkTrafficMbps);
//        System.out.println("-----接收到的总字节数:" + incomingNetworkTraffic);
//        System.out.println("-----outgoing Network Traffic(Mbps):" + outgoingNetworkTrafficMbps);
//        System.out.println("-----发送的总字节数:" + outgoingNetworkTraffic);

        //loadAverage
        org.hyperic.sigar.cmd.Uptime.getInfo(sigar);
        double uptime = sigar.getUptime().getUptime();
        Object[] loadAvg = new Object[3];
        PrintfFormat formatter = new PrintfFormat("%.2f, %.2f, %.2f");
        String loadAverage;

        try {
            double[] avg = sigar.getLoadAverage();
            loadAvg[0] = new Double(avg[0]);
            loadAvg[1] = new Double(avg[1]);
            loadAvg[2] = new Double(avg[2]);

            loadAverage = "load average: " + formatter.sprintf(loadAvg);

        } catch (SigarNotImplementedException e) {
            loadAverage = "(load average unknown)";
        }
        System.out.println(loadAverage);
    }

    private static void printCpuPerc(CpuPerc cpu) {
        System.out.println("CPU用户使用率:    " + CpuPerc.format(cpu.getUser()));// 用户使用率
        System.out.println("CPU系统使用率:    " + CpuPerc.format(cpu.getSys()));// 系统使用率
        System.out.println("CPU当前等待率:    " + CpuPerc.format(cpu.getWait()));// 当前等待率
        System.out.println("CPU当前错误率:    " + CpuPerc.format(cpu.getNice()));//
        System.out.println("CPU当前空闲率:    " + CpuPerc.format(cpu.getIdle()));// 当前空闲率
        System.out.println("CPU总的使用率:    " + CpuPerc.format(cpu.getCombined()));// 总的使用率
    }
}
