package com.ucar.datalink.writer.dummy;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.plugin.PluginListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sqq on 2017/6/22.
 */
public class DummyTaskWriterListener implements PluginListener {
    private static final Logger logger = LoggerFactory.getLogger(DummyTaskWriterListener.class);

    @Override
    public void init() {
        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new Object() {
            @Subscribe
            public void listener(String s) {
                logger.info("receive info is : " + s);
            }
        });
    }
}
