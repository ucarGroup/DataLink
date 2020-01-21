package com.ucar.datalink.domain.plugin.writer.hdfs;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sqq on 2017/7/18.
 */
public class FileSplitStrategy {
    @JSONField(format = "yyyy-MM-dd")
    private Date effectiveDate;
    private FileSplitMode fileSplitMode;

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public FileSplitMode getFileSplitMode() {
        return fileSplitMode;
    }

    public void setFileSplitMode(FileSplitMode fileSplitMode) {
        this.fileSplitMode = fileSplitMode;
    }

}
