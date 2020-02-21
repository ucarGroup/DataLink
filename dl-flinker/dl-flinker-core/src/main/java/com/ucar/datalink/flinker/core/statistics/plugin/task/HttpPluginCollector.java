package com.ucar.datalink.flinker.core.statistics.plugin.task;

import com.ucar.datalink.flinker.api.constant.PluginType;
import com.ucar.datalink.flinker.api.element.Record;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.core.statistics.communication.Communication;

/**
 * Created by jingxing on 14-9-9.
 */
public class HttpPluginCollector extends AbstractTaskPluginCollector {
    public HttpPluginCollector(Configuration configuration, Communication Communication,
                               PluginType type) {
        super(configuration, Communication, type);
    }

    @Override
    public void collectDirtyRecord(Record dirtyRecord, Throwable t,
                                   String errorMessage) {
        super.collectDirtyRecord(dirtyRecord, t, errorMessage);
    }

}
