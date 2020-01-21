package com.ucar.datalink.biz.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.ucar.datalink.biz.dal.MonitorDAO;
import com.ucar.datalink.biz.dal.TaskDAO;
import com.ucar.datalink.biz.service.MonitorService;
import com.ucar.datalink.biz.service.SysPropertiesService;
import com.ucar.datalink.biz.service.UserService;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.alarm.StrategyConfig;
import com.ucar.datalink.domain.monitor.MonitorCat;
import com.ucar.datalink.domain.monitor.MonitorInfo;
import com.ucar.datalink.domain.monitor.MonitorType;
import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;
import com.ucar.datalink.domain.task.TaskInfo;
import com.ucar.datalink.domain.user.UserInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by csf on 17/4/28.
 */
@Service
public class MonitorServiceImpl implements MonitorService {

	private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    private static final String ALARM_VOICE_TASKS = "alarm_voice_tasks";

    @Autowired
    private SysPropertiesService sysPropertiesService;

    private LoadingCache<LoadingKey, List<MonitorInfo>> monitorCahce = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build(new CacheLoader<LoadingKey, List<MonitorInfo>>() {        @Override
        public List<MonitorInfo> load(LoadingKey key) throws Exception {
            Long resourceId = key.resourceId;
            MonitorCat monitorCat = key.monitorCat;
            List<MonitorInfo> list = new ArrayList<>();
            if (monitorCat == MonitorCat.TASK_MONITOR) {
                TaskInfo taskInfo = taskDAO.findById(resourceId);
				logger.info(String.format("Receive a request in Monitor Cache for task [%s].", resourceId));
                if (taskInfo != null) {
                    if (taskInfo.getLeaderTaskId() != null) {
                        list = monitorDAO.getListByResourceAndCat(taskInfo.getLeaderTaskId(), monitorCat.getKey());
                        if (list != null) {
                            list.stream().forEach(t -> t.setResourceId(resourceId));
                            list.stream().forEach(t -> t.setResourceName(taskInfo.getTaskName()));
                        }
                    } else {
                        list = monitorDAO.getListByResourceAndCat(resourceId, monitorCat.getKey());
                    }
                }
            } else if (monitorCat == MonitorCat.WORKER_MONITOR) {
                list = monitorDAO.getListByResourceAndCat(resourceId, monitorCat.getKey());
            } else if(monitorCat == MonitorCat.DATAX_MONITOR) {
                list = monitorDAO.getListByResourceAndCat(resourceId, monitorCat.getKey());
            }
            return list != null ? list : Lists.newArrayList();
        }
    });

    @Value("${biz.monitor.defaultIntervalTime}")
    private Long defaultIntervalTime;//单位，s

    @Value("${biz.monitor.defaultDelayThreshold}")
    private Integer defaultDelayThreshold;//单位，ms

    @Value("${biz.monitor.defaultJvmUsageThreshold}")
    private Integer defaultJvmUsageThreshold;//百分比*100

    @Value("${biz.monitor.defaultBusyTimeThreshold}")
    private Integer defaultBusyTimeThreshold;//单位：ms

    @Autowired
    MonitorDAO monitorDAO;

    @Autowired
    TaskDAO taskDAO;

    @Autowired
    UserService userService;

    @Value("${extend.isSendSms}")
    private boolean isSendSms;

    @Value("${extend.isSendDingD}")
    private boolean isSendDingD;


    @Override
    public List<MonitorInfo> getList() {
        return monitorDAO.getList();
    }

    @Override
    public List<MonitorInfo> getListForQueryPage(@Param("monitorCat") Integer monitorCat, @Param("monitorType") Integer monitorType, @Param("groupId") Long groupId, @Param("resourceId") Long resourceId, @Param("isEffective") Integer isEffective) {
        return monitorDAO.getListForQueryPage(monitorCat, monitorType, groupId, resourceId, isEffective);
    }

    @Override
    public Boolean insert(MonitorInfo monitorInfo) {
        Integer num = monitorDAO.insert(monitorInfo);
        if (num > 0) {
            clearCache();
            return true;
        }

        return false;
    }

    @Override
    public Boolean update(MonitorInfo monitorInfo) {
        Integer num = monitorDAO.update(monitorInfo);
        if (num > 0) {
            clearCache();
            return true;
        }

        return false;
    }

    @Override
    public Boolean delete(Long id) {
        Integer num = monitorDAO.delete(id);
        if (num > 0) {
            clearCache();
            return true;
        }

        return false;
    }

    @Override
    public MonitorInfo getById(Long id) {
        return monitorDAO.getById(id);
    }

    @Override
    public Boolean updateIsAlarm(int status) {
        Integer num = monitorDAO.updateIsAlarm(status);
        if (num > 0) {
            clearCache();
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public void createAllMonitor(Long resourceId, MonitorCat monitorCat) {
        List<UserInfo> userInfoList = userService.getList();
        if (userInfoList == null || userInfoList.size() == 0) {
            return;
        }

        if (monitorCat == MonitorCat.TASK_MONITOR) {
            MonitorInfo delayMonitorInfo = new MonitorInfo();
            delayMonitorInfo.setIsEffective(1);
            delayMonitorInfo.setMonitorRange("06:00-23:59");
            delayMonitorInfo.setIntervalTime(defaultIntervalTime);
            delayMonitorInfo.setResourceId(resourceId);
            TaskInfo taskInfo = taskDAO.findById(resourceId);
            String taskName = taskInfo.getTaskName();
            if (taskName.toLowerCase().contains("_2_hdfs")) {
                delayMonitorInfo.setThreshold(600000);
            }
            //改为默认一分钟
            else if (taskName.toUpperCase().contains("_A_2_") || taskName.toUpperCase().contains("_B_2_")) {
                delayMonitorInfo.setThreshold(60000);
            }
            else {
                delayMonitorInfo.setThreshold(defaultDelayThreshold);
            }
            delayMonitorInfo.setMonitorType(MonitorType.TASK_DELAY_MONITOR.getKey());
            delayMonitorInfo.setMonitorCat(MonitorCat.TASK_MONITOR.getKey());

            MonitorInfo exceptionMonitorInfo = new MonitorInfo();
            exceptionMonitorInfo.setIsEffective(1);
            exceptionMonitorInfo.setMonitorRange("00:00-23:59");
            exceptionMonitorInfo.setIntervalTime(defaultIntervalTime);
            exceptionMonitorInfo.setResourceId(resourceId);
            exceptionMonitorInfo.setThreshold(1);
            exceptionMonitorInfo.setMonitorType(MonitorType.TASK_EXCEPTION_MONITOR.getKey());
            exceptionMonitorInfo.setMonitorCat(MonitorCat.TASK_MONITOR.getKey());

            MonitorInfo taskStatusMonitorInfo = new MonitorInfo();
            taskStatusMonitorInfo.setIsEffective(1);
            taskStatusMonitorInfo.setMonitorRange("06:00-23:59");
            taskStatusMonitorInfo.setIntervalTime(defaultIntervalTime);
            taskStatusMonitorInfo.setResourceId(resourceId);
            taskStatusMonitorInfo.setThreshold(1);
            taskStatusMonitorInfo.setMonitorType(MonitorType.TASK_STATUS_MONITOR.getKey());
            taskStatusMonitorInfo.setMonitorCat(MonitorCat.TASK_MONITOR.getKey());

            MonitorInfo taskStatusMismatchMonitorInfo = new MonitorInfo();
            taskStatusMismatchMonitorInfo.setIsEffective(1);
            taskStatusMismatchMonitorInfo.setMonitorRange("06:00-23:59");
            taskStatusMismatchMonitorInfo.setIntervalTime(defaultIntervalTime);
            taskStatusMismatchMonitorInfo.setResourceId(resourceId);
            taskStatusMismatchMonitorInfo.setThreshold(1);
            taskStatusMismatchMonitorInfo.setMonitorType(MonitorType.TASK_STATUS_MISMATCH_MONITOR.getKey());
            taskStatusMismatchMonitorInfo.setMonitorCat(MonitorCat.TASK_MONITOR.getKey());

            MonitorInfo taskSyncStatusMonitorInfo = new MonitorInfo();
            taskSyncStatusMonitorInfo.setIsEffective(1);
            taskSyncStatusMonitorInfo.setMonitorRange("06:00-23:59");
            taskSyncStatusMonitorInfo.setIntervalTime(defaultIntervalTime);
            taskSyncStatusMonitorInfo.setResourceId(resourceId);
            taskSyncStatusMonitorInfo.setThreshold(defaultBusyTimeThreshold);
            taskSyncStatusMonitorInfo.setMonitorType(MonitorType.TASK_SYNC_STATUS_MONITOR.getKey());
            taskSyncStatusMonitorInfo.setMonitorCat(MonitorCat.TASK_MONITOR.getKey());

            Integer num1 = monitorDAO.insert(delayMonitorInfo);
            Integer num2 = monitorDAO.insert(exceptionMonitorInfo);
            Integer num3 = monitorDAO.insert(taskStatusMonitorInfo);
            Integer num4 = monitorDAO.insert(taskStatusMismatchMonitorInfo);
            Integer num5 = monitorDAO.insert(taskSyncStatusMonitorInfo);

            if (num1 <= 0 || num2 <= 0 || num3 <= 0 || num4 <= 0 || num5 <= 0) {
                throw new DatalinkException("create task monitor fail.");
            }
        }
        if (monitorCat == MonitorCat.WORKER_MONITOR) {
            MonitorInfo workerJvmStateMonitorInfo = new MonitorInfo();
            workerJvmStateMonitorInfo.setIsEffective(1);
            workerJvmStateMonitorInfo.setMonitorRange("06:00-23:59");
            workerJvmStateMonitorInfo.setIntervalTime(defaultIntervalTime);
            workerJvmStateMonitorInfo.setResourceId(resourceId);
            workerJvmStateMonitorInfo.setThreshold(defaultJvmUsageThreshold);
            workerJvmStateMonitorInfo.setMonitorType(MonitorType.WORKER_JVM_STATE_MONITOR.getKey());
            workerJvmStateMonitorInfo.setMonitorCat(MonitorCat.WORKER_MONITOR.getKey());

            MonitorInfo workerRunningStateMonitorInfo = new MonitorInfo();
            workerRunningStateMonitorInfo.setIsEffective(1);
            workerRunningStateMonitorInfo.setMonitorRange("06:00-23:59");
            workerRunningStateMonitorInfo.setIntervalTime(defaultIntervalTime);
            workerRunningStateMonitorInfo.setResourceId(resourceId);
            workerRunningStateMonitorInfo.setThreshold(1);
            workerRunningStateMonitorInfo.setMonitorType(MonitorType.WORKER_RUNNING_STATE_MONITOR.getKey());
            workerRunningStateMonitorInfo.setMonitorCat(MonitorCat.WORKER_MONITOR.getKey());

            Integer num4 = monitorDAO.insert(workerJvmStateMonitorInfo);
            Integer num5 = monitorDAO.insert(workerRunningStateMonitorInfo);

            if (num4 <= 0 || num5 <= 0) {
                throw new DatalinkException("create worker monitor fail.");
            }
        }

        if(monitorCat == MonitorCat.DATAX_MONITOR) {
            MonitorInfo dataxMonitorInfo = new MonitorInfo();
            dataxMonitorInfo.setIsEffective(1);
            dataxMonitorInfo.setMonitorRange("06:00-23:59");
            dataxMonitorInfo.setIntervalTime(defaultIntervalTime);
            dataxMonitorInfo.setResourceId(resourceId);
            dataxMonitorInfo.setThreshold(defaultJvmUsageThreshold);
            dataxMonitorInfo.setMonitorType(MonitorType.TASK_EXCEPTION_MONITOR.getKey());
            dataxMonitorInfo.setMonitorCat(MonitorCat.DATAX_MONITOR.getKey());
            Integer num = monitorDAO.insert(dataxMonitorInfo);

            if (num <= 0) {
                throw new DatalinkException("create datax monitor fail.");
            }
        }
    }

    public List<MonitorInfo> getDataxResourceAndTypeList(Long resourceId, MonitorType monitorType) {
        List<MonitorInfo> list = monitorDAO.getListByResourceAndCat(resourceId, monitorType.getKey());
        return list;
    }

    @Override
    public MonitorInfo getByResourceAndType(Long resourceId, MonitorType monitorType) {
        LoadingKey loadingKey = new LoadingKey(resourceId, monitorType.getMonitorCat());
        Optional<MonitorInfo> optional = monitorCahce.getUnchecked(loadingKey).stream().filter(i -> i.getMonitorType().equals(monitorType.getKey())).findFirst();
        MonitorInfo monitorInfo = optional.isPresent() ? optional.get() : null;
        if(monitorInfo != null) {
            monitorInfo.setSMS(isSendSms);
            monitorInfo.setDingD(isSendDingD);
        }
        return monitorInfo;
    }

    @Override
    public void clearCache() {
        monitorCahce.invalidateAll();
    }

    @Override
    public void copyStrategy(StrategyConfig config, MonitorInfo monitorInfo) {
        monitorInfo.setIntervalTime(config.getIntervalTime());
        monitorInfo.setMonitorRange(config.getTimeRange());
        monitorInfo.setThreshold(config.getThreshold()*1000);
        monitorInfo.setSMS(config.isSMS());
        monitorInfo.setPhone(config.isPhone());
        monitorInfo.setDingD(config.isDingD());
        if(!StringUtils.isEmpty(config.getOtherConfig())) {
            monitorInfo.setOtherConfig(config.getOtherConfig());
        }
    }

    @Override
    public void initMonitor(MonitorInfo monitorInfo) {
        setAlarmVoiceTasks(monitorInfo);
    }

    private void setAlarmVoiceTasks(MonitorInfo monitorInfo) {
        if(monitorInfo == null) {
            return;
        }
        SysPropertiesInfo sysPropertiesInfo = sysPropertiesService.getSysPropertiesByKey(ALARM_VOICE_TASKS);
        if(sysPropertiesInfo==null) {
            return;
        }
        String taskIdStr = sysPropertiesInfo.getPropertiesValue();
        if(StringUtils.isEmpty(taskIdStr)) {
            return;
        }
        String[] taskIds = taskIdStr.split(",");
        for (String taskId : taskIds) {
            if(taskId.equals(String.valueOf(monitorInfo.getResourceId()))) {
                monitorInfo.setPhone(true);
                break;
            }
        }
    }

    /**
     * 演示ClassLoader机制用
     */
    /*public LoadingCache<Long, List<MonitorInfo>> getMonitorCahce() {
        return monitorCahce;
    }*/

    private static class LoadingKey {
        private Long resourceId;
        private MonitorCat monitorCat;

        public LoadingKey (Long resourceId, MonitorCat monitorCat) {
            this.resourceId = resourceId;
            this.monitorCat = monitorCat;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LoadingKey)) return false;

            LoadingKey that = (LoadingKey) o;

            return monitorCat == that.monitorCat && resourceId.equals(that.resourceId);

        }

        @Override
        public int hashCode() {
            int result = resourceId.hashCode();
            result = 31 * result + monitorCat.hashCode();
            return result;
        }
    }
}
