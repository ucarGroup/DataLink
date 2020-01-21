package com.ucar.datalink.manager.core.service.impl;

import com.ucar.datalink.manager.core.service.HbaseInspectionService;
import com.ucar.datalink.manager.core.web.util.HbaseSyncUtil;

public class HbaseInspectionServiceImpl implements HbaseInspectionService {

    @Override
    public void inspection() {

        HbaseSyncUtil.checkReplicateTables();

    }

}
