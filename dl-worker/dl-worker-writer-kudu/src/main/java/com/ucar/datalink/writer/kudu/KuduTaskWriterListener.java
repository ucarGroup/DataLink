package com.ucar.datalink.writer.kudu;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.errors.ErrorException;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.event.KuduColumnSyncEvent;
import com.ucar.datalink.domain.event.KuduConfigClearEvent;
import com.ucar.datalink.domain.plugin.PluginListener;
import com.ucar.datalink.domain.vo.ResponseVo;
import com.ucar.datalink.writer.kudu.util.KuduColumnSyncManager;
import com.ucar.datalink.writer.kudu.util.KuduTableFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sqq on 2017/6/22.
 */
public class KuduTaskWriterListener implements PluginListener {
    private static final Logger logger = LoggerFactory.getLogger(KuduTaskWriterListener.class);

    @Override
    public void init() {
        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new Object() {
            @Subscribe
            public void listener(KuduConfigClearEvent event) {
                logger.info("Receive an event for clear KuduConfigClearEvent");
                try {
                    KuduTableFactory.invalidate(event.getMediaSourceInfo());
                    event.getCallback().onCompletion(null, null);
                } catch (Throwable t) {
                    logger.error("something goes wrong when invalidate kudu-config.", t);
                    event.getCallback().onCompletion(t, null);
                }
            }

            @Subscribe
            public void columnSyncEvent(KuduColumnSyncEvent event) {
                logger.info("Receive an kuduColumnSyncEvent {}", event.toString());

                ResponseVo response = new ResponseVo();
                try {
                    KuduColumnSyncManager.syncColumnDefinition(event);
                    event.getCallback().onCompletion(null, response);
                } catch (Throwable t) {
                    logger.error("something goes wrong when columnSyncEvent{}",event.toString(), t);
                    if(t instanceof ErrorException){
                        response.setCode(((ErrorException) t).getCode());
                    }else{
                        response.setCode(400);
                    }
                    response.setMessage(t.getMessage());
                    event.getCallback().onCompletion(null, response);
                }



            }

        });


    }
}
