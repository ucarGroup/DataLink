package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.alibaba.otter.canal.protocol.position.LogIdentity;
import com.alibaba.otter.canal.protocol.position.LogPosition;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;

/**
 * Created by user on 2017/3/20.
 */
public class CanalPositionTranslator {

    public static LogPosition translateToLogPosition(MysqlReaderPosition cp) {
        LogIdentity logIdentity = new LogIdentity(cp.getSourceAddress(), cp.getSlaveId());

        EntryPosition entryPosition = new EntryPosition(cp.getJournalName(), cp.getPosition(), cp.getTimestamp(), cp.getServerId());
        entryPosition.setIncluded(cp.isIncluded());

        LogPosition logPosition = new LogPosition();
        logPosition.setIdentity(logIdentity);
        logPosition.setPostion(entryPosition);

        return logPosition;
    }

    public static MysqlReaderPosition translateToMysqlReaderPosition(LogPosition lp) {
        MysqlReaderPosition mysqlReaderPosition = new MysqlReaderPosition();
        mysqlReaderPosition.setIncluded(lp.getPostion().isIncluded());
        mysqlReaderPosition.setJournalName(lp.getPostion().getJournalName());
        mysqlReaderPosition.setPosition(lp.getPostion().getPosition());
        mysqlReaderPosition.setServerId(lp.getPostion().getServerId());
        mysqlReaderPosition.setTimestamp(lp.getPostion().getTimestamp());
        mysqlReaderPosition.setSlaveId(lp.getIdentity().getSlaveId());
        mysqlReaderPosition.setSourceAddress(lp.getIdentity().getSourceAddress());
        return mysqlReaderPosition;
    }
}
