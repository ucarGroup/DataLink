package com.alibaba.datax.core.plugin;

import com.ucar.datalink.flinker.api.plugin.TaskPluginCollector;
import com.ucar.datalink.flinker.core.transport.channel.Channel;

public class BufferedRecordExchanger extends com.ucar.datalink.flinker.core.transport.exchanger.BufferedRecordExchanger{
    public BufferedRecordExchanger(Channel channel, TaskPluginCollector pluginCollector) {
        super(channel, pluginCollector);
    }
}
