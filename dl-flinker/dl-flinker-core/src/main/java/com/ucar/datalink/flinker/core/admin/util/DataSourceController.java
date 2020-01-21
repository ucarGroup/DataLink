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

    private IZkDataListener dataCenterListener;

    private IZkDataListener dataCenterSwitchListener;

    private volatile boolean isCenterLabNewChange = false;

    /**
     * /datalink/doublecenter/centerLabNew 节点下的key，表示整个机房
     */
    private static final String OVERALL_DATACENTER_INFO_SIGN = "-1";

    /**
     * /datalink/doublecenter/centerLabNew 节点下的key，表示datalink自身
     */
    private static final String DATALINK_INFO_SIGN = "-2";

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
        checkZKCenterLabNewNode();

        //当 /doublecenter/centerLabNew 节点有变化时会触发接口中的函数
        dataCenterSwitchListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                checkZKCenterLabNewNode();
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {

            }
        };

        String centerLab = ZookeeperPathUtils.DATA_LINK_CENTER_LAB_NEW;
        zkClient.subscribeDataChanges(centerLab, dataCenterSwitchListener);
        //String switchLab = ZookeeperPathUtils.getDataLinkLabSwitchProcessingParentZnode();
        //zkClient.subscribeChildChanges(switchLab,dataCenterSwitchListener);
        //zkClient.subscribeDataChanges(centerLab, dataCenterSwitchListener));
    }

    /**
     * 获取 /datalink/doublecenter/centerLabNew 节点下的数据并解析
     */
    private void checkZKCenterLabNewNode() {
        byte[] centerLabNew = this.zkClient.readData(ZookeeperPathUtils.DATA_LINK_CENTER_LAB_NEW,true);
        Map<String,String> jsonMap = JSONObject.parseObject(centerLabNew,Map.class);
        logger.info("[checkZKCenterLabNewNode]->"+jsonMap.toString());
        String centerLabNewStr = jsonMap.get(DATALINK_INFO_SIGN);
        if(StringUtils.isBlank(centerLabNewStr)) {
            centerLabNewStr = jsonMap.get(OVERALL_DATACENTER_INFO_SIGN);

        }
        logger.info("[checkZKCenterLabNewNode]->"+centerLabNewStr);
        parseCenterLabInfo(centerLabNewStr.getBytes());
    }

    public void destroy() {
        String centerLab = ZookeeperPathUtils.DATA_LINK_CENTER_LAB_NEW;
        zkClient.unsubscribeDataChanges(centerLab, dataCenterSwitchListener);
        //String switchLab = ZookeeperPathUtils.getDataLinkLabSwitchProcessingParentZnode();
        //zkClient.unsubscribeChildChanges(switchLab,dataCenterSwitchListener);
    }

    /**
     * 目前切机房不频繁，如果切换频繁的话，可以考虑做如下优化
     * 1.记录下当前机房的标签(如logicA或者logicB),下次触发到这个函数时发现跟保存的机房标签一样，就可以跳过不执行
     * 2.如果解析的ip和port和当前保存的ip，port一样，可以不用重新reload数据源
     * @param buf
     */
    public void parseCenterLabInfo(byte[] buf) {
        if(buf == null) {
            logger.info("buf is null");
            return;
        }
        String labName = new String(buf);
        logger.info("current lab_name -> "+labName);
        if(StringUtils.isBlank(labName)) {
            return;
        }
        if(labName.contains("\"")) {
            labName = labName.replaceAll("\"","");
        }
        byte[] arrBytes = this.zkClient.readData(ZookeeperPathUtils.getDataLinkLabInfoList(),true);
        if(arrBytes==null) {
            return;
        }
        List<LabInfo> labInfolist = JSONObject.parseArray(new String(arrBytes), LabInfo.class);
        logger.info("lab info list ->"+labInfolist.toString());
        for(LabInfo i : labInfolist) {
            if(StringUtils.equals(labName,i.getLabName())) {
                //获取到zk上的一条数据库信息，将这个信息重新设置到类中的变量中
                logger.info("lab-name"+labName+"  equals ->"+i.toString());
                parseDbInfo(i);
                break;
            }
        }
    }

    public void parseDbInfo(LabInfo info) {
        try {
            String ipAndPort = info.getDbIpPort();
            logger.info("ip-port:"+ipAndPort);
            if (StringUtils.isBlank(ipAndPort)) {
                return;
            }
            String[] arr = ipAndPort.split(":");
            if (arr == null | arr.length < 2) {
                return;
            }
            String ip_str= arr[0];
            String port_str = arr[1];
            address = ip_str.trim();
            port = Integer.parseInt(port_str.trim());
            logger.info("parse address->"+address);
            logger.info("parse port->"+port);

            //关闭连接重新初始化
            UserDbUtils.reConnect();
            JobExecutionDbUtils.reConnect();
            JobConfigDbUtils.reConnect();
        } catch(Exception e) {
            logger.error(e.getMessage(),e);
        }
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

/*
    public boolean canStartDataxInstance() {
        return !isDataCenterSwith;
    }

    public boolean canStartDataxWithExcept() {
        if(!isDataCenterSwith) {
            return true;
        }
        throw new RuntimeException("data center in switch, cannot start datax instance.");
    }
*/

}