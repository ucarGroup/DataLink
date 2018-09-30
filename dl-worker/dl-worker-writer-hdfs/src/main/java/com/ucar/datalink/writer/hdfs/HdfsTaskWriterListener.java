package com.ucar.datalink.writer.hdfs;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.event.CommonEvent;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.plugin.PluginListener;
import com.ucar.datalink.writer.hdfs.handle.stream.FileStreamKeeper;
import com.ucar.datalink.writer.hdfs.handle.util.Dict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sqq on 2017/7/12.
 */
public class HdfsTaskWriterListener implements PluginListener {
    private static final Logger logger = LoggerFactory.getLogger(HdfsTaskWriterListener.class);

    @Override
    public void init() {
        FileStreamKeeper.start();
        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new Object() {
            @Subscribe
            public void listener(CommonEvent event) {
                try {
                    if (event.getEventName().equals(Dict.EVENT_CLOSE_STREAM)) {
                        String fileName = (String) event.getPayload().get(Dict.EVENT_CLOSE_STREAM_FILE_NAME);
                        FileStreamKeeper.closeStreamLocal(fileName);
                        logger.info("common event process succeeded for " + event.getEventName());
                    }
                } catch (Throwable t) {
                    logger.error("common event process failed.", t);
                }
            }
        });
    }
}
