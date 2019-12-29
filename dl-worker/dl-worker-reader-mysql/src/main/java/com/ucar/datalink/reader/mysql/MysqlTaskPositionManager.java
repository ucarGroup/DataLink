package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.protocol.position.LogIdentity;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.task.TaskShadowInfo;
import com.ucar.datalink.worker.api.position.PositionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.net.InetSocketAddress;

/**
 * MysqlTaskPositionManager:负责对某个Task的位点进行组织和管理。
 * <p>
 * Shadow-Reader使用的频次不会特别高，所以getPosition和updatePosition直接通过互斥锁进行并发控制即可，
 * 不会有太大的性能问题。
 * </p>
 * Created by lubiao on 2017/3/20.
 */
public class MysqlTaskPositionManager {
    private static final Logger logger = LoggerFactory.getLogger(MysqlTaskPositionManager.class);

    private final String taskId;
    private final PositionManager positionManager;
    private final TaskShadowInfo taskShadow;

    private volatile MysqlReaderPosition mainPosition;
    private volatile MysqlReaderPosition shadowPosition;
    private volatile boolean initialized;
    private volatile boolean canMerge;

    public MysqlTaskPositionManager(String taskId, PositionManager positionManager, TaskShadowInfo taskShadow) {
        this.taskId = taskId;
        this.positionManager = positionManager;
        this.taskShadow = taskShadow;
    }

    public void start() {
        Assert.notNull(positionManager);
        if (!positionManager.isStart()) {
            positionManager.start();
        }

        logger.info("Mysql Task Position Manager started.");
    }

    public void shutdown() {
        //do nothing
        logger.info("Mysql Task Position Manager shutdown.");
    }

    public synchronized MysqlReaderPosition getPosition(CanalReaderType readerType) {
        initializePosition();
        if (readerType == CanalReaderType.MAIN) {
            return mainPosition;
        } else {
            return shadowPosition;
        }
    }

    public synchronized void updatePosition(CanalReaderType readerType, MysqlReaderPosition position) {
        if (!initialized) {
            throw new IllegalStateException("please initialize position first before update it.");
        }

        if (taskShadow != null && readerType == CanalReaderType.SHADOW && mainPosition == null) {
            throw new IllegalStateException("main position can not be null when update positions.");
        }

        if (taskShadow == null && readerType == CanalReaderType.SHADOW) {
            throw new IllegalStateException("receive a shadow position update request, but there is no shadow task.");
        }

        if (canMerge) {
            logger.info("update operation discarded, because canMerge switch is true. ");
            return;
        }

        if (readerType == CanalReaderType.MAIN) {
            mainPosition = position;
        } else {
            shadowPosition = position;
        }

        if (taskShadow != null && mainPosition != null && shadowPosition != null
                && Math.abs(mainPosition.getTimestamp() - shadowPosition.getTimestamp()) <= 15000) {
            long minTimeStamp = Math.min(mainPosition.getTimestamp(), shadowPosition.getTimestamp());
            //将ip设置为"0.0.0.0",下次启动时按照时间戳启动
            mainPosition.setSourceAddress(new InetSocketAddress("0.0.0.0", mainPosition.getSourceAddress().getPort()));
            mainPosition.setTimestamp(minTimeStamp);
            shadowPosition.setSourceAddress(new InetSocketAddress("0.0.0.0", shadowPosition.getSourceAddress().getPort()));
            shadowPosition.setTimestamp(minTimeStamp);
            canMerge = true;
        }

        mainPosition.setShadowPosition(shadowPosition);
        positionManager.updatePosition(taskId, mainPosition);
    }

    public LogPosition translateToLogPosition(MysqlReaderPosition cp) {
        LogIdentity logIdentity = new LogIdentity(cp.getSourceAddress(), cp.getSlaveId());

        EntryPosition entryPosition = new EntryPosition(cp.getJournalName(), cp.getPosition(), cp.getTimestamp(), cp.getServerId());
        entryPosition.setIncluded(cp.isIncluded());
        entryPosition.setGtid(cp.getGtid());

        LogPosition logPosition = new LogPosition();
        logPosition.setIdentity(logIdentity);
        logPosition.setPostion(entryPosition);

        return logPosition;
    }

    public MysqlReaderPosition translateToMysqlReaderPosition(LogPosition lp) {
        MysqlReaderPosition mysqlReaderPosition = new MysqlReaderPosition();
        mysqlReaderPosition.setIncluded(lp.getPostion().isIncluded());
        mysqlReaderPosition.setJournalName(lp.getPostion().getJournalName());
        mysqlReaderPosition.setPosition(lp.getPostion().getPosition());
        mysqlReaderPosition.setServerId(lp.getPostion().getServerId());
        mysqlReaderPosition.setTimestamp(lp.getPostion().getTimestamp());
        mysqlReaderPosition.setSlaveId(lp.getIdentity().getSlaveId());
        mysqlReaderPosition.setSourceAddress(lp.getIdentity().getSourceAddress());
        mysqlReaderPosition.setGtid(lp.getPostion().getGtid());
        return mysqlReaderPosition;
    }

    public boolean isCanMerge() {
        return canMerge;
    }

    private void initializePosition() {
        if (!initialized) {
            MysqlReaderPosition readerPosition = positionManager.getPosition(taskId);
            if (readerPosition == null) {
                mainPosition = null;
                shadowPosition = null;
            } else {
                mainPosition = readerPosition;
                shadowPosition = buildShadowPosition();
            }
            initialized = true;
        }
    }

    /**
     * 四种组合情况：
     * 1. temp==null && taskShadow==null，代表之前没有执行过影子任务且当前没有影子任务要执行，shadowPosition保持为null即可
     * 2. temp==null && taskShadow!=null, 代表之前没有执行过影子任务且当前有影子任务要执行，shadowPosition保持为null即可，
     * 靠CanalReaderConfigGenerator进行位点初始化
     * 3. temp!=null && taskShadow==null, 代表之前执行过影子任务且当前没有影子任务要执行，shadowPosition保持原样即可
     * 4. temp!=null && taskShadow!=null, 代表之前执行过影子任务且当前有影子任务要执行，需要比对之前的任务ID和当前的任务ID是否相同，
     * 如果相同沿用之前的position，如果不同将之前的position置为null，靠CanalReaderConfigGenerator进行位点初始化
     */
    private MysqlReaderPosition buildShadowPosition() {
        MysqlReaderPosition temp = mainPosition.getShadowPosition();
        //taskShadow如果为null，说明当前没有影子任务需要执行,shadowPosition保持不变即可
        if (temp != null && taskShadow != null) {
            if (temp.getShadowId() < taskShadow.getId()) {
                //上一轮的影子任务已经结束，不能用之前的位点进行初始化，需要设置为null，让CanalReader从Canal配置中取值
                temp = null;
            } else if (temp.getShadowId() > taskShadow.getId()) {
                // 影子任务的id是单调递增的，如果出现前序id比后序id还大的情况，抛异常
                throw new IllegalStateException("previous shadow id should not be greater than subsequent one.");
            }
        }
        return temp;
    }
}
