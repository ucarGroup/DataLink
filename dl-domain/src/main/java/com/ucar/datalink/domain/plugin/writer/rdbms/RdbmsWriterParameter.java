package com.ucar.datalink.domain.plugin.writer.rdbms;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.PluginWriterParameter;

import java.util.List;
import java.util.Set;

/**
 * Created by lubiao on 2017/3/3.
 */
public class RdbmsWriterParameter extends PluginWriterParameter {

    private SyncMode syncMode = SyncMode.TablePartlyOrdered;

    @Override
    public String initPluginName() {
        return "writer-rdbms";
    }

    @Override
    public String initPluginClass() {
        return "com.ucar.datalink.writer.rdbms.RdbmsTaskWriter";
    }

    @Override
    public String initPluginListenerClass() {
        return "com.ucar.datalink.writer.rdbms.RdbmsTaskWriterListener";
    }

    @Override
    public Set<MediaSourceType> initSupportedSourceTypes() {
        return Sets.newHashSet(MediaSourceType.MYSQL, MediaSourceType.SQLSERVER, MediaSourceType.POSTGRESQL, MediaSourceType.SDDL);
    }

    public SyncMode getSyncMode() {
        return syncMode;
    }

    public void setSyncMode(SyncMode syncMode) {
        this.syncMode = syncMode;
    }

    public static enum SyncMode {
        /**
         * 库级别，全局有序
         */
        DbGlobalOrdered,
        /**
         * 表级别，局部有序
         */
        TablePartlyOrdered;

        public static List<SyncMode> getAllModes() {
            return Lists.newArrayList(TablePartlyOrdered, DbGlobalOrdered);
        }
    }
}
