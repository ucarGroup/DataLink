package com.ucar.datalink.domain.plugin.writer.hbase;

import com.ucar.datalink.domain.Parameter;

/**
 * Created by sqq on 2017/12/1.
 */
public class HBaseSyncParameter extends Parameter{
    private boolean syncDelete;

    public boolean isSyncDelete() {
        return syncDelete;
    }

    public void setSyncDelete(boolean syncDelete) {
        this.syncDelete = syncDelete;
    }
}
