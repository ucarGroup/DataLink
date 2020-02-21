package com.ucar.datalink.flinker.api.spi;

import com.ucar.datalink.flinker.api.util.Configuration;
import java.util.Map;

/**
 * Created by xiafei.qiuxf on 14/12/17.
 */
public interface Hook {

    /**
     * 返回名字
     *
     * @return
     */
    public String getName();

    /**
     * TODO 文档
     *
     * @param jobConf
     * @param msg
     */
    public void invoke(Configuration jobConf, Map<String, Number> msg);

}
