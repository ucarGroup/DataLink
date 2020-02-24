package com.ucar.datalink.flinker.core.admin.util;

import com.ucar.datalink.flinker.api.zookeeper.ZkClientx;
import com.ucar.datalink.flinker.api.zookeeper.ZookeeperPathUtils;
import com.ucar.datalink.flinker.core.admin.AdminConstants;
import com.ucar.datalink.flinker.core.admin.bean.FlowControlData;
import com.ucar.datalink.flinker.core.admin.record.Encryption;
import com.ucar.datalink.flinker.core.admin.record.JobConfigDbUtils;
import com.ucar.datalink.flinker.core.admin.record.JobExecutionDbUtils;
import com.ucar.datalink.flinker.core.admin.record.UserDbUtils;
import com.ucar.datalink.flinker.core.util.container.CoreConstant;
import com.alibaba.fastjson.JSONObject;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import java.util.*;

/**
 * Created by yang.wang09 on 2018-11-15 18:16.
 */
public final class DataSourceController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DataSourceController.class);

    private static final DataSourceController INSTANCE = new DataSourceController();

    private ZkClientx zkClient;

    private String address;
    private Integer port;
    private String schema;
    private String userName;
    private String password;

    private DataSourceController() {

    }

    public void initialize(Properties properties, ZkClientx zkClient) {
        address = properties.getProperty(AdminConstants.DATAX_DB_ADDRESS);
        port = Integer.valueOf(properties.getProperty(AdminConstants.DATAX_DB_PORT));
        schema = properties.getProperty(AdminConstants.DATAX_DB_SCHEMA);
        userName = properties.getProperty(AdminConstants.DATAX_DB_USERNAME);
        password = Encryption.decrypt(properties.getProperty(AdminConstants.DATAX_DB_PASSWORD));
        logger.info("initialize address -> "+address);
        logger.info("initialize port -> "+port);
        logger.info("initialize schema -> "+schema);
        logger.info("initialize userName -> "+userName);
        logger.info("initialize password -> "+password);

        this.zkClient = zkClient;

    }


    public static DataSourceController getInstance() {
        return INSTANCE;
    }

    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public String getSchema() {
        return schema;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }


    public void destroy() {
    }
}