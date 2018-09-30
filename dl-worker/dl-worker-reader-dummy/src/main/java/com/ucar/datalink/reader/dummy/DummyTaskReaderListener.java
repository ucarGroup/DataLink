package com.ucar.datalink.reader.dummy;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.plugin.PluginListener;

/**
 * Created by sqq on 2017/6/22.
 */
public class DummyTaskReaderListener implements PluginListener {

    @Override
    public void init() {
        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new Object() {
            @Subscribe
            public void listener(String s) {

            }
        });
    }
}
