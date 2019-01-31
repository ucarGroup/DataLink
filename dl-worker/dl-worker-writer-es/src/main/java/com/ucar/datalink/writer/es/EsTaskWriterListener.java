package com.ucar.datalink.writer.es;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.event.EsConfigClearEvent;
import com.ucar.datalink.domain.plugin.PluginListener;
import com.ucar.datalink.writer.es.util.EsConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sqq on 2017/6/22.
 */
public class EsTaskWriterListener implements PluginListener {
    private static final Logger logger = LoggerFactory.getLogger(EsTaskWriterListener.class);

    @Override
    public void init() {
        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new Object() {
            @Subscribe
            public void listener(EsConfigClearEvent event) {
                logger.info("Receive an event for clear es-config with media-source-id " + event.getMediaSourceInfo().getId());
                try {
                    EsConfigManager.invalidate(event.getMediaSourceInfo());
                    event.getCallback().onCompletion(null, null);
                } catch (Throwable t) {
                    logger.error("something goes wrong when invalidate es-config.", t);
                    event.getCallback().onCompletion(t, null);
                }
            }
        });
    }
}
