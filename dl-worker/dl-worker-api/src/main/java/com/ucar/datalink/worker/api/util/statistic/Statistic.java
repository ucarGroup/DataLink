package com.ucar.datalink.worker.api.util.statistic;

import com.alibaba.fastjson.JSONObject;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 07/12/2017.
 */
public class Statistic {

    public String toJsonString() {
        return JSONObject.toJSONString(this, true);
    }

}
