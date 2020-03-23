package com.ucar.datalink.flinker.core.statistics.plugin;

import com.ucar.datalink.flinker.api.plugin.JobPluginCollector;
import com.ucar.datalink.flinker.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.ucar.datalink.flinker.core.statistics.communication.Communication;

import java.util.List;
import java.util.Map;

/**
 * Created by jingxing on 14-9-9.
 */
public final class DefaultJobPluginCollector implements JobPluginCollector {
    private AbstractContainerCommunicator jobCollector;

    public DefaultJobPluginCollector(AbstractContainerCommunicator containerCollector) {
        this.jobCollector = containerCollector;
    }

    @Override
    public Map<String, List<String>> getMessage() {
        Communication totalCommunication = this.jobCollector.collect();
        return totalCommunication.getMessage();
    }

    @Override
    public List<String> getMessage(String key) {
        Communication totalCommunication = this.jobCollector.collect();
        return totalCommunication.getMessage(key);
    }
}
