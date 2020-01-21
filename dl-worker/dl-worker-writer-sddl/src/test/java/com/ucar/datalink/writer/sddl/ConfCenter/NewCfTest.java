package com.ucar.datalink.writer.sddl.ConfCenter;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.common.errors.TaskExecuteException;
import com.zuche.confcenter.bean.vo.ClientDataSource;
import com.zuche.framework.extend.manager.SddlCfcenterManager;
import com.zuche.framework.extend.manager.SddlCfcenterManagerFactory;
import com.zuche.framework.sddl.datasource.SddlLogicCluster;
import com.zuche.framework.utils.SddlConfChange;
import org.apache.commons.lang.StringUtils;

/**
 * Created by lubiao on 2018/10/31.
 */
public class NewCfTest {

    public static void main(String args[]) {
        initCfCent();
        initDataSource("ucarcdms");
    }

    private static void initCfCent() {
        SddlCFContext cfContext = new SddlCFContext("order-service",
                "07ec3241f7ab7ff39b5645bb9784f488",
                "http://gaeatest.10101111.com",
                "ucar");
        ConfCenterApiSingleton.getInstance(cfContext);

    }

    private static SddlLogicCluster initDataSource(String projectName){

        SddlCfcenterManager sddlCfcenterManager = SddlCfcenterManagerFactory.getSddlCfcenterManager(projectName);

        ClientDataSource clientDataSource = sddlCfcenterManager.getShardingDB(projectName);
        String shardingDBNames = clientDataSource.getSourceValue();

        if (StringUtils.isEmpty(shardingDBNames)) {
            String errorInfo = "sddlWriter_work get datasource from cfCenter is error, info:"+ JSON.toJSONString(shardingDBNames);
            throw new TaskExecuteException(errorInfo);
        }

        SddlLogicCluster sddlLogicCluster = SddlConfChange.selectedInitCluster(projectName, shardingDBNames);

        if (sddlLogicCluster == null
                || sddlLogicCluster.getSddlClusters() == null) {
            String errorInfo = "sddlWriter_work get SddlLogicCluster from cfCenter is error, info:"+ shardingDBNames
                    + ","
                    + sddlLogicCluster == null ? "sddlLogicCluster为null" :
                    sddlLogicCluster.getSddlClusters() == null ? "getSddlClusters为null" :
                            String.valueOf(sddlLogicCluster.getSddlClusters().size());
            throw new TaskExecuteException(errorInfo);
        }

        return sddlLogicCluster;
    }
}
