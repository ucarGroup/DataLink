package com.ucar.datalink.common.event;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sqq on 2017/6/20.
 */
public class EventBusFactory {

    private static class EventBusHolder {
        static String identifier = "default";
        static ExecutorService executor = Executors.newCachedThreadPool();
        private static final EventBus INSTANCE = new AsyncEventBus(identifier, executor);
    }
    public static final EventBus getEventBus() {
        return EventBusHolder.INSTANCE;
    }
}
