package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.meta.CanalMetaManager;
import com.alibaba.otter.canal.meta.MemoryMetaManager;
import com.alibaba.otter.canal.meta.exception.CanalMetaManagerException;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.alibaba.otter.canal.protocol.position.Position;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MigrateMap;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.task.TaskShadowInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 自定义MetaManager，重载canal自带的metaManager,将canal的消费位点纳入统一管理.
 * <p>
 * Created by user on 2017/2/27.
 */
public class CanalReaderMetaManager extends MemoryMetaManager implements CanalMetaManager {

    private static final Logger logger = LoggerFactory.getLogger(CanalReaderMetaManager.class);

    private String filter;
    private final MysqlTaskPositionManager taskPositionManager;
    private final CanalReader canalReader;

    private final Position nullCursor = new Position() {
    };

    public CanalReaderMetaManager(MysqlTaskPositionManager taskPositionManager, CanalReader canalReader) {
        this.taskPositionManager = taskPositionManager;
        this.canalReader = canalReader;
    }

    @Override
    public void start() {
        super.start();

        cursors = MigrateMap.makeComputingMap(new Function<ClientIdentity, Position>() {
            @Override
            public Position apply(ClientIdentity clientIdentity) {
                MysqlReaderPosition readerPosition = taskPositionManager.getPosition(canalReader.getReaderType());
                if (readerPosition == null) {
                    return nullCursor; // 返回一个空对象标识，避免出现异常
                } else {
                    return taskPositionManager.translateToLogPosition(readerPosition);
                }
            }
        });

        destinations = MigrateMap.makeComputingMap(new Function<String, List<ClientIdentity>>() {
            @Override
            public List<ClientIdentity> apply(String destination) {
                logger.info("ClientIdentity is initialized for destination {}.", destination);
                return Lists.newArrayList(new ClientIdentity(destination, (short) 1001, ""));
            }
        });
    }

    @Override
    public Position getCursor(ClientIdentity clientIdentity) throws CanalMetaManagerException {
        Position position = super.getCursor(clientIdentity);
        if (position == nullCursor) {
            return null;
        } else {
            return position;
        }
    }

    @Override
    public void updateCursor(ClientIdentity clientIdentity, Position position) throws CanalMetaManagerException {
        super.updateCursor(clientIdentity, position);
        taskPositionManager.updatePosition(canalReader.getReaderType(), buildPosition(position));
    }

    private MysqlReaderPosition buildPosition(Position position) {
        MysqlReaderPosition mysqlReaderPosition = taskPositionManager.translateToMysqlReaderPosition((LogPosition) position);
        CanalReaderEffectSyncPosition effectSyncPosition = canalReader.getEffectSyncPosition();
        TaskShadowInfo taskShadow = canalReader.getTaskShadow();
        if (effectSyncPosition != null) {
            mysqlReaderPosition.setLatestEffectSyncLogFileName(effectSyncPosition.getLatestEffectSyncLogFileName());
            mysqlReaderPosition.setLatestEffectSyncLogFileOffset(effectSyncPosition.getLatestEffectSyncLogFileOffset());
        }
        if (taskShadow != null) {
            mysqlReaderPosition.setShadowId(taskShadow.getId());
        }
        return mysqlReaderPosition;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
