package com.ucar.datalink.reader.hbase;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.ucar.datalink.common.event.EventBusFactory;
import com.ucar.datalink.domain.event.*;
import com.ucar.datalink.domain.plugin.PluginListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by user on 2017/6/28.
 */
public class HBaseTaskReaderListener implements PluginListener {

    private static final Logger logger = LoggerFactory.getLogger(HBaseTaskReaderListener.class);

    @Override
    public void init() {
        EventBus eventBus = EventBusFactory.getEventBus();

        //获取所有table
        eventBus.register(new Object() {
            @Subscribe
            public void listener(HBaseTablesEvent event) {
                HBasePluginUtil.executeTableEvent(event);
            }
        });


        //获取table下的列
        eventBus.register(new Object() {
            @Subscribe
            public void listener(HBaseColumnsEvent event) {
                HBasePluginUtil.executeColumnEvent(event);
            }
        });


        //检查连接是否正常
        eventBus.register(new Object(){
            @Subscribe
            public void listener(HBaseConnCheckEvent event) {
                HBasePluginUtil.checkHBaseConnection(event);
            }
        });


        //一个表下有多少region
        eventBus.register(new Object(){
            @Subscribe
            public void listener(HBaseRegionCountEvent event) {
                HBasePluginUtil.caclRegionCount(event);
            }
        });


        //根据传入的切分数量，比如10，返回一个大小为10的列表，这个列表就是等待切分的region集合，包含startKey和 endKey
        eventBus.register(new Object(){
            @Subscribe
            public void listener(HBaseSplitEvent event) {
                HBasePluginUtil.generateHBaseSplitInfo(event);
            }
        });


    }


}