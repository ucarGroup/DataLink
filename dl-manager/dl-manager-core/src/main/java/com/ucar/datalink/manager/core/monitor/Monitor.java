package com.ucar.datalink.manager.core.monitor;

import com.ucar.datalink.common.utils.DateUtil;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.manager.core.server.ManagerConfig;
import com.ucar.datalink.util.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by csf on 17/5/1.
 */
public abstract class Monitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final Map<Long, Long> intervalTimeMap = new ConcurrentHashMap<>();

    public abstract void doMonitor();

    public Boolean isAlarm(Long resourceId, Long threshold, MonitorInfo monitorInfo) {
        try {
            if (monitorInfo != null
                    && isValidTime(monitorInfo.getMonitorRange())
                    && isArriveInterval(monitorInfo)
                    && threshold >= monitorInfo.getThreshold() //延时阈值时间
                    && monitorInfo.getIsEffective() == 1) {

                return true;
            }
            return false;
        } catch (Throwable t) {
            LOGGER.error("something goes wrong when judge if do alarm for task {}.", resourceId);
            return false;
        }
    }


    public Boolean isArriveInterval(MonitorInfo monitorInfo) {//报警频率
        Long time = intervalTimeMap.get(monitorInfo.getId());
        if (time == null) {
            intervalTimeMap.put(monitorInfo.getId(), System.currentTimeMillis());
            return true;
        } else {
            Long currentTime = System.currentTimeMillis();
            if (currentTime - time > (monitorInfo.getIntervalTime() * 1000)) {
                intervalTimeMap.put(monitorInfo.getId(), System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }

    public boolean isValidTime(String rangeTime) throws ParseException {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        Date date = now.getTime();
        //rangeTime : 00:00-02:30
        String[] range = rangeTime.split("-");
        try {
            //测试和预生产环境周末不发邮件报警
            if (!ManagerConfig.current().getCurrentEnv().equalsIgnoreCase(Env.PROD.getName())
                    && DateUtil.isWeekend()) {
                return false;
            }
            Date start = sdf.parse(year + "-" + month + "-" + day + " " + range[0] + ":00");
            Date end = sdf.parse(year + "-" + month + "-" + day + " " + range[1] + ":00");
            return date.after(start) && date.before(end);
        } catch (ParseException e) {
            throw e;
        }
    }
}

