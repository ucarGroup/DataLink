package com.ucar.datalink.writer.sddl;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.event.CommonEvent;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.plugin.PluginListener;
import com.ucar.datalink.writer.sddl.manager.checkData.SddlCheckData;
import com.ucar.datalink.writer.sddl.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 27/10/2017.
 */
public class SddlTaskWriterListener implements PluginListener {
    private static final Logger LOG = LoggerFactory.getLogger(SddlTaskWriterListener.class);

    @Override
    public void init() {


        // 监控sddl数据检查
        checkDataComplete();
    }

    private void checkDataComplete() {
        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new Object() {

            @AllowConcurrentEvents
            @Subscribe
            public void listener(CommonEvent event) {
                try {
                    LOG.info("sddl checkData:" + event.getEventName());
                    if (event.getEventName().equals(Constants.EVENT_SDDL_CHECKDATA_SWITCH)) {
                        if (SddlCheckData.checkDataSwitch.get()) {
                            SddlCheckData.checkSyncShardingCompleteEnd();

                            LOG.info("sddl checkData for {}, is off!", event.getEventName());
                        } else {
                            SddlCheckData.checkSyncShardingCompleteStart();

                            LOG.info("sddl checkData for {}, is on!", event.getEventName());
                        }


                    }
                } catch (Throwable t) {
                    LOG.error("sddl checkData is error, eventName:{}!.", event.getEventName(), t);
                }
            }
        });

    }
}
