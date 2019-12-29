package com.ucar.datalink.domain.plugin.writer.es;

import com.ucar.datalink.domain.Parameter;

/**
 * Created by lubiao on 2019/12/28.
 */
public class EsMappingParameter extends Parameter {
    /**
     * 同步到ES时，如果esUsePrefix为true，并且配置了prefixName名称，则用prefixName作为每个索引列的前缀
     */
    private String prefixName;

    public String getPrefixName() {
        return prefixName;
    }

    public void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }
}
