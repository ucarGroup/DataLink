package com.ucar.datalink.writer.rdbms;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.event.bi.RdbmsCountEvent;
import com.ucar.datalink.domain.plugin.PluginListener;
import com.ucar.datalink.writer.rdbms.operator.RdbmsOperatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sqq on 2017/6/22.
 */
public class RdbmsTaskWriterListener implements PluginListener {

    private static final Logger logger = LoggerFactory.getLogger(RdbmsTaskWriterListener.class);


    @Override
    public void init() {

        EventBus eventBus = EventBusFactory.getEventBus();

        //获取所有执行count操作
        eventBus.register(new Object() {
            @Subscribe
            public void listener(RdbmsCountEvent event) {
                logger.debug(event.toString());
                RdbmsOperatorUtil.count(event);
            }
        });

    }
}
