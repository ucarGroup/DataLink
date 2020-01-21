package com.ucar.datalink.common.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.utils.FutureCallback;

/**
 * Created by sqq on 2017/6/21.
 */
public class EventBusTest {

    public static void main(String[] args) {
        EventBus eventBus = EventBusFactory.getEventBus();
        CallbackEvent event = new CallbackEvent(new FutureCallback());
        eventBus.register(new Object() {
            @Subscribe
            public void lister(CallbackEvent event) {
                System.out.printf("2222222");
                event.getCallback().onCompletion(null, null);
            }
        });

        try {
            eventBus.post(event);
            event.getCallback().get();
            System.out.printf("11111111");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
