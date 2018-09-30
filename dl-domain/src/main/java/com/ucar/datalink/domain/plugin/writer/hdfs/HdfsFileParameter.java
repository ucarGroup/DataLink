package com.ucar.datalink.domain.plugin.writer.hdfs;

import com.ucar.datalink.domain.Parameter;

import java.util.List;

/**
 * Created by sqq on 2017/7/28.
 */
public class HdfsFileParameter extends Parameter{
    private List<FileSplitStrategy> fileSplitStrategieList;

    public List<FileSplitStrategy> getFileSplitStrategieList() {
        return fileSplitStrategieList;
    }

    public void setFileSplitStrategieList(List<FileSplitStrategy> fileSplitStrategieList) {
        this.fileSplitStrategieList = fileSplitStrategieList;
    }
}
