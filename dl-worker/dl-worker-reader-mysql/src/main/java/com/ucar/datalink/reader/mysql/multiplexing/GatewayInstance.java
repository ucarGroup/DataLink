package com.ucar.datalink.reader.mysql.multiplexing;

import com.alibaba.otter.canal.common.alarm.LogAlarmHandler;
import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.manager.CanalInstanceWithManager;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.ucar.datalink.common.Constants;
import com.ucar.datalink.reader.mysql.CanalReaderAlarmHandler;
import com.ucar.datalink.reader.mysql.extend.CustomCanalInstanceWithManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * GatewayInstance:
 * 用来代理数据库实例，即有：在一个Worker进程中，对于物理上的同一个数据库实例，对应的GatewayInstance对象也只有一个
 * 当MysqlTaskReader处于MultiplexingRead模式运行的时候，会将其管理的CanalInstance注册给对应的GatewayInstance(匹配原则是相同的【ip+port】)
 * <p>
 * <p>
 * Created by lubiao on 2019/5/9.
 */
public class GatewayInstance {
    private final static Logger logger = LoggerFactory.getLogger(GatewayInstance.class);
    private final static Long LESS_THAN_AVG_LOG_TIME_THRESHOLD = 7200 * 1000L;//ms

    private final Canal gwCanal;
    private final Map<String, RegisterItem> endpointInstances;
    private CanalInstanceWithManager workingInstance;

    public GatewayInstance(Canal gwCanal) {
        this.gwCanal = gwCanal;
        this.endpointInstances = new ConcurrentHashMap<>();
    }

    /**
     * 并发量不大，直接用互斥锁就行了
     */
    public synchronized void registerEndpointInstance(String destination, CanalInstance canalInstance, KickoutListener kickoutListener, String filter) {
        String oldMDC = MDC.get(Constants.MDC_TASKID);
        try {
            MDC.put(Constants.MDC_TASKID, gwCanal.getName());

            logger.info("register an endpoint instance with name {} begin.", destination);
            this.endpointInstances.put(destination, new RegisterItem(canalInstance, kickoutListener, filter));
            logger.info("register an endpoint instance with name {} end.", destination);
        } finally {
            if (StringUtils.isNotBlank(oldMDC)) {
                MDC.put(Constants.MDC_TASKID, oldMDC);
            } else {
                MDC.remove(Constants.MDC_TASKID);
            }
        }
    }

    public synchronized void unRegisterEndpointInstance(String destination) {
        String oldMDC = MDC.get(Constants.MDC_TASKID);
        try {
            MDC.put(Constants.MDC_TASKID, gwCanal.getName());

            logger.info("unregister an endpoint instance with name {} begin.", destination);
            if (endpointInstances.containsKey(destination)) {
                endpointInstances.remove(destination);
                unRegisterInternal(destination);
                refreshAlarmHandler();
            } else {
                logger.info("no need to execute unregister,because this instance has been kicked out in other time.");
            }
            logger.info("unregister an endpoint instance with name {} end.", destination);
        } finally {
            if (StringUtils.isNotBlank(oldMDC)) {
                MDC.put(Constants.MDC_TASKID, oldMDC);
            } else {
                MDC.remove(Constants.MDC_TASKID);
            }
        }
    }

    public synchronized void refresh() {
        if (needNextRound()) {
            logger.info("######## gateway instance next round begin.");

            if (!isWorkingInstanceStarted()) {
                logger.info("working instance is not started , no need to stop.");
            } else {
                stopWorkingInstance();
            }

            if (endpointInstances.isEmpty()) {
                logger.info("endpointInstances is empty,no need to start working instance.");
            } else {
                startWorkingInstance();
                endpointInstances.values().forEach(i -> i.isNewlyRegister = false);
            }

            logger.info("######## gateway instance next round end.\r\n");
            return;
        }

        if (isWorkingInstanceStarted() && ((GatewayEventStore) workingInstance.getEventStore()).isSuspend()) {
            logger.info("######## gateway instance kickout begin.");
            Set<String> result = ((GatewayEventStore) workingInstance.getEventStore()).checkKickout();
            result.forEach(i -> kickout(i, true, "cause eventstore suspend."));
            logger.info("######## gateway instance kickout end.\r\n");
        }

    }

    private void startWorkingInstance() {
        logger.info("working instance starting.");

        this.workingInstance = buildWorkingInstance();
        this.registerInternal();
        this.refreshAlarmHandler();
        this.workingInstance.start();

        logger.info("working instance started.");
    }

    private void stopWorkingInstance() {
        logger.info("working instance stopping.");

        this.workingInstance.stop();
        this.workingInstance = null;

        logger.info("working instance stopped.");
    }

    private void registerInternal() {
        Map<String, Long> timeStamps = new HashMap<>();
        Map<String, RegisterItem> validInstances = new HashMap<>();
        Map<String, RegisterItem> copiedInstances = new HashMap<>(this.endpointInstances);
        copiedInstances.entrySet().forEach(e -> {
            LogPosition position = (LogPosition) e.getValue().instance.getMetaManager()
                    .getCursor(new ClientIdentity(e.getKey(), (short) 1001, ""));
            timeStamps.put(e.getKey(), position == null ? System.currentTimeMillis() : position.getPostion().getTimestamp());
        });
        double avgTime = timeStamps.values().stream().mapToLong(p -> p).summaryStatistics().getAverage();
        copiedInstances.entrySet().forEach(e -> {
            //位点时间比平均时间小于两个小时以上时，不参与多路复用
            double diff = avgTime - timeStamps.get(e.getKey());
            if (diff > LESS_THAN_AVG_LOG_TIME_THRESHOLD) {
                kickout(e.getKey(), false,
                        String.format("exceeded threshold than the average log time, this time is [%s] ms, avg time is [%s] ms ," +
                                        " diff time is [%s] ms, threshold is [%s].",
                                timeStamps.get(e.getKey()), avgTime, diff, LESS_THAN_AVG_LOG_TIME_THRESHOLD));
            } else {
                validInstances.put(e.getKey(), e.getValue());
            }
        });


        validInstances.entrySet().forEach(e -> {
            ((GatewayEventSink) workingInstance.getEventSink())
                    .registerEventSink(e.getKey(), e.getValue().instance.getEventSink());
            ((GatewayEventStore) workingInstance.getEventStore())
                    .registerEventStore(e.getKey(), e.getValue().instance.getEventStore());
            ((GatewayMetaManager) workingInstance.getMetaManager())
                    .registerMetaManager(e.getKey(), e.getValue().instance.getMetaManager());
        });
    }

    private void unRegisterInternal(String destination) {
        if (isWorkingInstanceStarted()) {
            ((GatewayEventSink) workingInstance.getEventSink())
                    .unRegisterEventSink(destination);
            ((GatewayEventStore) workingInstance.getEventStore())
                    .unRegisterEventStore(destination);
            ((GatewayMetaManager) workingInstance.getMetaManager())
                    .unRegisterMetaManager(destination);
        }
    }

    /**
     * 1.如果workingInstance已经启动，但是注册过的endpointInstance都离开了，有必要把workingInstance停止
     * 2.只要有新的endpointInstance进行了注册，就需要进行workingInstance的启动或重启
     */
    private boolean needNextRound() {
        if (isWorkingInstanceStarted() && endpointInstances.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, RegisterItem> r : endpointInstances.entrySet()) {
            if (r.getValue().isNewlyRegister) {
                return true;
            }
        }
        return false;
    }

    private boolean isWorkingInstanceStarted() {
        return workingInstance != null && workingInstance.isStart();
    }

    @SuppressWarnings("unchecked")
    private CanalInstanceWithManager buildWorkingInstance() {

        StringBuilder gwFilters = new StringBuilder();
        for (Map.Entry<String, RegisterItem> entry : endpointInstances.entrySet()) {
            if (gwFilters.length() > 0) {
                gwFilters.append(",");
            }
            gwFilters.append(entry.getValue().filter);
        }
        String gwFilter = gwFilters.toString();
        logger.info("The gwFilter for GatewayInstance is {}.", gwFilter);

        //多路复用的情况下，filter为注册GatewayInstance的所有endpointInstances filter的并集
        return new CustomCanalInstanceWithManager(gwCanal, gwFilter) {

            @Override
            protected void initEventSink() {
                //因为sddl数据源暂不支持读端数据复用,所以目前initEventSink()方法创建的肯定是EntryEventSink
                logger.info("init gateway eventSink begin...");
                super.initEventSink();
                eventSink = new GatewayEventSink(gwCanal, eventSink, (GatewayEventStore) eventStore);
                logger.info("init gateway eventSink end! \n\t load CanalEventSink:{}", eventSink.getClass().getName());
            }

            @Override
            protected void initEventStore() {
                logger.info("init gateway eventStore begin...");
                eventStore = new GatewayEventStore(gwCanal);
                logger.info("init gateway eventStore end! \n\t load CanalEventStore:{}", eventStore.getClass().getName());
            }

            @Override
            protected void initMetaManager() {
                logger.info("init gateway metaManager begin...");
                metaManager = new GatewayMetaManager(gwCanal);
                logger.info("init gateway metaManager end! \n\t load CanalMetaManager:{}", metaManager.getClass().getName());
            }
        };
    }

    private void kickout(String destination, boolean isRegistered, String kickoutReason) {
        logger.info("kickout endpoint instance {} begin.", destination);
        logger.info("kickout reason is : " + kickoutReason);

        this.endpointInstances.get(destination).kickoutListener.kickout();
        if (isRegistered) {
            unRegisterEndpointInstance(destination);
        } else {
            endpointInstances.remove(destination);
        }

        logger.info("kickout endpoint instance {} end.", destination);
    }

    /**
     * 把TaskId绑定给GatewayEventParser，复用Task报警功能，随机选一个参与多路复用的TaskId
     */
    private void refreshAlarmHandler() {
        if (workingInstance != null) {
            if (endpointInstances.isEmpty()) {
                workingInstance.setAlarmHandler(new LogAlarmHandler());
            } else {
                String taskId = (String) endpointInstances.keySet().toArray()[0];
                if (workingInstance.getAlarmHandler() instanceof CanalReaderAlarmHandler) {
                    ((CanalReaderAlarmHandler) workingInstance.getAlarmHandler()).setTaskId(taskId);
                } else {
                    workingInstance.setAlarmHandler(new CanalReaderAlarmHandler(taskId));
                }
            }
        }
    }

    private class RegisterItem {
        private CanalInstance instance;
        private KickoutListener kickoutListener;
        private boolean isNewlyRegister;
        private String filter;

        public RegisterItem(CanalInstance instance, KickoutListener kickoutListener, String filter) {
            this.instance = instance;
            this.kickoutListener = kickoutListener;
            this.isNewlyRegister = true;
            this.filter = filter;
        }
    }

    public String getInstanceName() {
        return this.gwCanal.getName();
    }
}
