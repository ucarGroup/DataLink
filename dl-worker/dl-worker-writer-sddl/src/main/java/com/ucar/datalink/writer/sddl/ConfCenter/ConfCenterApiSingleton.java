package com.ucar.datalink.writer.sddl.ConfCenter;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.writer.sddl.dataSource.DataSourceCluster;
import com.ucar.datalink.writer.sddl.exception.SddlCfCentException;
import com.ucar.datalink.writer.sddl.util.PropertyCheckUtil;
import com.zuche.confcenter.bean.vo.ClientDataSource;
import com.zuche.confcenter.client.manager.DefaultConfigCenterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * @Description:
 *              ⚠️注意：sddl_write的配置中心client是动态生成的，所以同一个进程中的其他task不能使用使用配置中心
 *
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 03/11/2017.
 */
public class ConfCenterApiSingleton {
    private static Logger LOG = LoggerFactory.getLogger(ConfCenterApiSingleton.class);

    private volatile static ConfCenterApiSingleton cfApi;

    // 防重复属性
    private String cfProjectName;
    private String key;
    private String serverDomain;


    public static ConfCenterApiSingleton getInstance (SddlCFContext context) {
        if (cfApi == null) {
            if (context == null || PropertyCheckUtil.hasNullProperty(context)) {
                String errorInfo = "sddlWriter_work init datasource is error(maybe obj/property is null), context_info:"+ JSON.toJSONString(context);
                throw new SddlCfCentException(errorInfo);
            }

            synchronized (ConfCenterApiSingleton.class) {
                if (cfApi == null) {
                    ConfCenterApiSingleton cfApiTemp = new ConfCenterApiSingleton();

                    Properties properties = new Properties();
                    properties.setProperty("projectName", context.getCfProjectName());
                    properties.setProperty("key", context.getCfKey());
                    properties.setProperty("serverDomain", context.getServerDomain());
                    properties.setProperty("businessLine", context.getBusinessLine().toLowerCase());
                    DefaultConfigCenterManager.getInstance().dynamicLoad(properties);

                    cfApiTemp.setCfProjectName(context.getCfProjectName());
                    cfApiTemp.setKey(context.getCfKey());
                    cfApiTemp.setServerDomain(context.getServerDomain());

                    // customise cf_listener
                    customiseCallBackListener();

                    cfApi = cfApiTemp;
                    LOG.info("new parameters is:"+ JSON.toJSONString(context)+ ", old parameters is:" +JSON.toJSONString(cfApi));
                    return cfApi;
                }
            }
        }

        // if cfApi already exists, it needs to be check verity whether the configuration is the same;
        if (!cfApi.getCfProjectName().equals(context.getCfProjectName())
                || !cfApi.getKey().equals(context.getCfKey())
                || !cfApi.getServerDomain().equals(context.getServerDomain())) {
            throw new SddlCfCentException("init DefaultConfigCenterManager already exists, and the incoming parameter dose not match, new parameters is:"
                    +JSON.toJSONString(context)+ ", old parameters is:" +JSON.toJSONString(cfApi));
        }

        return cfApi;
    }


    private static void customiseCallBackListener () {
        try {
            DefaultConfigCenterManager configCenterManager = DefaultConfigCenterManager.getInstance();
            configCenterManager.addBatchCallBackListener(dataSourceBatchTransport -> {

                ClientDataSource fristClientDataSource = dataSourceBatchTransport.get(0).getClientDataSource();
                LOG.info("sddl_writer,数据源触发回调start, value:{};", JSON.toJSONString(fristClientDataSource));

                String sourceName = fristClientDataSource.getSourceName();
                if (sourceName.indexOf(".fwdb.") > 0
                        && sourceName.startsWith("sddladmin.")) {

                    String[] sourceNameSubs = sourceName.split("\\.");
                    Map<String, String> projectNames = DataSourceCluster.getProjectNameByDss();
                    LOG.info("sddl_writer,数据源配置发生改动时，静态里的项目名s：{}", JSON.toJSONString(projectNames));

                    if (projectNames.containsKey(sourceNameSubs[1])) {
                        DataSourceCluster.reloadSddlDataSource(null);
                        DataSourceCluster.reloadJdbcTemplate(null);
                        DataSourceCluster.reloadTable(null);
                        LOG.info("sddl_writer,数据源触发改动回调结束");
                    }
                }
            });

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public String getCfProjectName() {
        return cfProjectName;
    }

    public void setCfProjectName(String cfProjectName) {
        this.cfProjectName = cfProjectName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getServerDomain() {
        return serverDomain;
    }

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }
}
