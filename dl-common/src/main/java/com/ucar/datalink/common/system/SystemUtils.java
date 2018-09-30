package com.ucar.datalink.common.system;

import com.ucar.datalink.common.Constants;
import org.hyperic.sigar.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qianqian.shi on 2018/5/9.
 */
public class SystemUtils {
    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    private static Sigar instance;

    private static Sigar initSigar() {
        try {
            String classPath = System.getProperty(Constants.SIGAR_PATH);

            String path = System.getProperty("java.library.path");
            if (OsCheck.getOperatingSystemType() == OsCheck.OSType.Windows) {
                path += ";" + classPath;
            } else {
                path += ":" + classPath;
            }
            System.setProperty("java.library.path", path);
            return new Sigar();
        } catch (Exception e) {
            logger.info("init sigar error.", e);
            return null;
        }
    }

    public static synchronized Sigar getInstance() {
        if (instance == null) {
            instance = initSigar();
        }
        return instance;
    }

    public static SystemSnapshot buildSystemSnapshot() throws SigarException {
        SystemSnapshot systemSnapshot = new SystemSnapshot();
        Sigar sigar = SystemUtils.getInstance();

        //CPU Utilization
        CpuPerc cpuList[] = sigar.getCpuPercList();
        double userSum = 0;
        double sysSum = 0;
        for (CpuPerc aCpuList : cpuList) {
            userSum += aCpuList.getUser();
            sysSum += aCpuList.getSys();
        }
        BigDecimal userUtilization = new BigDecimal(userSum * 100.0D / 4).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal sysUtilization = new BigDecimal(sysSum * 100.0D / 4).setScale(2, BigDecimal.ROUND_HALF_UP);
        //Network Traffic
        long incomingNetworkTraffic;
        long outgoingNetworkTraffic;
        Map<String, Long> incomingNetworkTrafficMap = new HashMap<>();
        Map<String, Long> outgoingNetworkTrafficMap = new HashMap<>();
        String ifNames[] = sigar.getNetInterfaceList();
        for (String name : ifNames) {
            NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
            if ((ifconfig.getFlags() & 1L) <= 0L) {
                logger.info("!IFF_UP...skipping getNetInterfaceStat");
                continue;
            }
            NetInterfaceStat ifstat = sigar.getNetInterfaceStat(name);
            incomingNetworkTraffic = ifstat.getRxBytes();
            outgoingNetworkTraffic = ifstat.getTxBytes();
            incomingNetworkTrafficMap.put(name, incomingNetworkTraffic);
            outgoingNetworkTrafficMap.put(name, outgoingNetworkTraffic);
        }

        //TCP
        Tcp tcp = sigar.getTcp();
        long currEstab = tcp.getCurrEstab();
        //load Average
        BigDecimal loadAverage = new BigDecimal(0.00);
        try {
            double[] avg = sigar.getLoadAverage();
            loadAverage = new BigDecimal(avg[0]);

        } catch (SigarNotImplementedException e) {
            logger.info("load average unknown", e);
        }

        systemSnapshot.setUserCPUUtilization(userUtilization);
        systemSnapshot.setSysCPUUtilization(sysUtilization);
        systemSnapshot.setIncomingNetworkTrafficMap(incomingNetworkTrafficMap);
        systemSnapshot.setOutgoingNetworkTrafficMap(outgoingNetworkTrafficMap);
        systemSnapshot.setTcpCurrentEstab(currEstab);
        systemSnapshot.setLoadAverage(loadAverage);
        return systemSnapshot;
    }
}
