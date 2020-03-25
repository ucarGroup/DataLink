package com.ucar.datalink.flinker.core.admin.util;

import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.common.zookeeper.ZkClientX;
import com.ucar.datalink.flinker.core.util.container.CoreConstant;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by yang.wang09 on 2018-11-20 14:18.
 */
public final class DataSourceInitController {

    private static final DataSourceInitController INSTANCE = new DataSourceInitController();

    private DataSourceInitController() {

    }

    public static DataSourceInitController getInstance() {
        return INSTANCE;
    }

    public void initialize() throws IOException {
        //initialize admin.properties
        Properties properties = new Properties();
        properties.load(new FileInputStream(CoreConstant.DATAX_ADMIN_CONF));
        ZkClientX zkClient = DLinkZkUtils.get().zkClient();
        DataSourceController.getInstance().initialize(properties,zkClient);
    }

    public void destroy() {

    }

    private String getProperty(Properties properties, String key) {
        return StringUtils.trim(properties.getProperty(StringUtils.trim(key)));
    }
}
