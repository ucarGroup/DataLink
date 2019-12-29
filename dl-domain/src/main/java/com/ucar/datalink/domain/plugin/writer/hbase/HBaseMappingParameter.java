package com.ucar.datalink.domain.plugin.writer.hbase;

import com.ucar.datalink.domain.Parameter;

/**
 * Created by lubiao on 2019/12/28.
 */
public class HBaseMappingParameter extends Parameter {
    /**
     * 同步到HBase的列族名
     */
    private String familyName = "default";

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
}
