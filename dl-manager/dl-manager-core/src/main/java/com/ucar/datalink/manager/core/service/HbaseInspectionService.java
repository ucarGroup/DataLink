package com.ucar.datalink.manager.core.service;

public interface HbaseInspectionService {

    /**
     * 定时检测hbase表复制
     */
    void inspection();

}
