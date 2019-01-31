package com.ucar.datalink.writer.hbase;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.event.HBaseConfigClearEvent;
import com.ucar.datalink.domain.plugin.PluginListener;
import com.ucar.datalink.writer.hbase.handle.util.HBaseConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sqq on 2017/11/29.
 */
public class HBaseTaskWriterListener implements PluginListener {
    public static final Logger logger = LoggerFactory.getLogger(HBaseTaskWriterListener.class);

    @Override
    public void init() {
        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new Object() {
            @Subscribe
            public void listener(HBaseConfigClearEvent event) {
                logger.info("Receive an event for clear hbase-config with media-source-id " + event.getMediaSourceInfo().getId());
                try {
                    HBaseConfigurationFactory.invalidate(event.getMediaSourceInfo());
                    event.getCallback().onCompletion(null, null);
                } catch (Throwable t) {
                    logger.error("something goes wrong when invalidate hbase-config.", t);
                    event.getCallback().onCompletion(t, null);
                }
            }
        });
    }
}
