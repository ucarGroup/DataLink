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
import com.ucar.datalink.worker.api.position.PositionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 自定义MetaManager，重载canal自带的metaManager,将canal的消费位点纳入统一管理.
 * <p>
 * Created by user on 2017/2/27.
 */
public class CanalTaskMetaManager extends MemoryMetaManager implements CanalMetaManager {

    private static final Logger logger = LoggerFactory.getLogger(CanalTaskMetaManager.class);

    private String filter;
    private PositionManager positionManager;
    private MysqlTaskReader mysqlTaskReader;

    private final Position nullCursor = new Position() {
    };

    public CanalTaskMetaManager(MysqlTaskReader mysqlTaskReader) {
        this.mysqlTaskReader = mysqlTaskReader;
    }

    @Override
    public void start() {
        super.start();
        Assert.notNull(positionManager);
        if (!positionManager.isStart()) {
            positionManager.start();
        }

        cursors = MigrateMap.makeComputingMap(new Function<ClientIdentity, Position>() {

            public Position apply(ClientIdentity clientIdentity) {
                MysqlReaderPosition readerPosition = positionManager.getPosition(clientIdentity.getDestination());
                if (readerPosition == null) {
                    return nullCursor; // 返回一个空对象标识，避免出现异常
                } else {
                    return CanalPositionTranslator.translateToLogPosition(readerPosition);
                }
            }
        });

        destinations = MigrateMap.makeComputingMap(new Function<String, List<ClientIdentity>>() {
            public List<ClientIdentity> apply(String destination) {
                logger.info("ClientIdentity is initialized for destination {}.", destination);
                return Lists.newArrayList(new ClientIdentity(destination, Short.valueOf(destination), filter));
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
        positionManager.updatePosition(clientIdentity.getDestination(), buildPosition(position));
    }

    private MysqlReaderPosition buildPosition(Position position) {
        MysqlReaderPosition mysqlReaderPosition = CanalPositionTranslator.translateToMysqlReaderPosition((LogPosition) position);
        EffectSyncPosition effectSyncPosition = mysqlTaskReader.getEffectSyncPosition();
        if (effectSyncPosition != null) {
            mysqlReaderPosition.setLatestEffectSyncLogFileName(effectSyncPosition.getLatestEffectSyncLogFileName());
            mysqlReaderPosition.setLatestEffectSyncLogFileOffset(effectSyncPosition.getLatestEffectSyncLogFileOffset());
        }
        return mysqlReaderPosition;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public PositionManager getPositionManager() {
        return positionManager;
    }

    public void setPositionManager(PositionManager positionManager) {
        this.positionManager = positionManager;
    }
}
