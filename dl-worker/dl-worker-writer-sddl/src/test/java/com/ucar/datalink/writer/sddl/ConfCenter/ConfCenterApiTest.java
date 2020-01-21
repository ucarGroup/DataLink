/*
package com.ucar.datalink.writer.sddl.ConfCenter;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.writer.sddl.SddlTestException;
import com.zuche.confcenter.bean.vo.ClientDataSource;
import com.zuche.confcenter.client.api.ConfCenterApi;
import com.zuche.framework.common.ConfigCenterApiSingleton;
import com.zuche.framework.extend.constants.SddlConfigConstant;
import org.junit.Test;

*/
/**
 * @Description: cf_center   test
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 22/11/2017.
 *//*

public class ConfCenterApiTest {
    // 测试项目ucarcdms
    private String projectName = "ucarcdms";
    private String cfKey = "07ec3241f7ab7ff39b5645bb9784f488";
    private String serverDomain = "http://gaeatest.10101111.com/";
    private String businessLine = "ucar";

    @Test
    public void testConfCenterApiInit () {
        // init
        SddlCFContext cfContext = new SddlCFContext(projectName, cfKey, serverDomain, businessLine);
        ConfCenterApiSingleton.getInstance(cfContext);
        ConfCenterApi confCenterApi = ConfigCenterApiSingleton.getSingleton().getConfCenterApi();

        // checking
        String key = SddlConfigConstant.getSddlTakeEffectKey(projectName);
        ClientDataSource clientDataSource = confCenterApi.getDataSourceByKey(key);

        if (clientDataSource == null) {
            throw new SddlTestException("sddl_test 初始化配置中心异常！");
        }
        System.out.println("sddl_writer_test, result:" + JSON.toJSONString(clientDataSource));
    }

}
*/
