package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.sink.AbstractCanalEventDownStreamHandler;
import com.alibaba.otter.canal.store.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by lubiao on 2017/2/6.
 */
public class CanalReaderDownStreamHandler extends AbstractCanalEventDownStreamHandler<List<Event>> {

    private static final Logger logger = LoggerFactory.getLogger(CanalReaderDownStreamHandler.class);

    @Override
    public List<Event> before(List<Event> events) {
        if (logger.isDebugEnabled()) {
            events.stream().forEach(e -> {
                logger.debug("Event Dump in Handler : EntryType is {}, EventType is {},SchemaName is {},TableName is {}.",
                        e.getEntry().getEntryType(),
                        e.getEntry().getHeader().getEventType(),
                        e.getEntry().getHeader().getSchemaName(),
                        e.getEntry().getHeader().getTableName());
            });
        }
        return super.before(events);
    }
}

