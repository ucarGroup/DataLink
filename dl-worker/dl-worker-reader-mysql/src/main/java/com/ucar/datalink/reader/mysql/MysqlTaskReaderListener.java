package com.ucar.datalink.reader.mysql;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.plugin.PluginListener;

/**
 * Created by sqq on 2017/6/22.
 */
public class MysqlTaskReaderListener implements PluginListener {

    @Override
    public void init() {
        /**
         * 演示ClassLoader机制用
         * 1. 如果调用service.getMonitorCahce().invalidateAll()，会报java.lang.LinkageError错误
         * 2. 如果调用service.clearCache()，则可正常执行
         * 3. 1和2最终都是调用的invalidateAll()方法，但前者会触发PluginClassLoader的类加载
         * 4. PS:调用service.getMonitorCahce()，只取对象，不调用其方法也不会触发LinkageError
         */
        //MonitorService service = DataLinkFactory.getObject(MonitorService.class);
        //service.getMonitorCahce().invalidateAll();
        //service.clearCache();
        //service.getMonitorCahce();

        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new Object() {
            @Subscribe
            public void listener(String s) {

            }
        });
    }
}
