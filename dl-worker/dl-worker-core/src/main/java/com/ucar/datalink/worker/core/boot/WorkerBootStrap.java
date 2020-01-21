package com.ucar.datalink.worker.core.boot;

import com.alibaba.fastjson.parser.ParserConfig;
import com.ucar.datalink.biz.datasource.DataSourceHolder;
import com.ucar.datalink.biz.service.*;
import com.ucar.datalink.common.utils.IPUtils;
import com.ucar.datalink.domain.worker.WorkerInfo;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.common.zookeeper.ZkConfig;
import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.worker.api.probe.ProbeManager;
import com.ucar.datalink.worker.core.runtime.*;
import com.ucar.datalink.worker.core.runtime.coordinate.WorkerKeeper;
import com.ucar.datalink.worker.core.runtime.rest.RestServer;
import com.ucar.datalink.worker.core.runtime.standalone.StandaloneWorkerKeeper;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Worker启动引导类
 * Created by lubiao on 2016/12/6.
 */
public class WorkerBootStrap {
    private static final Logger logger = LoggerFactory.getLogger(WorkerBootStrap.class);

    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    static{
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
    }

    public void boot(String[] args) {
        try {
            //boot spring
            final ApplicationContextBootStrap appContextBootStrap = new ApplicationContextBootStrap("worker/spring/datalink-worker.xml");
            appContextBootStrap.boot();

            //initial WorkerConfig
            Map<String, String> workerProps;
            if (args.length > 0) {
                String workerPropsFile = args[0];//if assigned,use the assigned file
                workerProps = !workerPropsFile.isEmpty() ?
                        Utils.propsToStringMap(Utils.loadProps(workerPropsFile)) : Collections.<String, String>emptyMap();
            } else {
                workerProps = Utils.propsToStringMap(buildWorkerProps());
            }

            //initial zkclient
            DLinkZkUtils.init(new ZkConfig(
                    workerProps.get(WorkerConfig.ZK_SERVER_CONFIG),
                    Integer.valueOf(workerProps.get(WorkerConfig.ZK_SESSION_TIMEOUT_MS_CONFIG)),
                    Integer.valueOf(workerProps.get(WorkerConfig.ZK_CONNECTION_TIMEOUT_MS_CONFIG))), workerProps.get(WorkerConfig.ZK_ROOT_CONFIG));

            //initial dataSource
            DataSourceHolder dataSourceHolder = new DataSourceHolder(DLinkZkUtils.get(), workerProps.get("doubleCenter.whoIsCentral.url"));
            dataSourceHolder.init();

            //初始化数据库中参数
            buildWorkerDBProps(workerProps);

            //init WorkerConfig
            WorkerConfig config = WorkerConfig.fromProps(workerProps, true);

            //get boot mode
            String bootMode = config.getString(WorkerConfig.WORKER_BOOT_MODE_CONFIG);

            //initial plugin EventBus（Listener）
            PluginListenersBootStrap.boot(config);

            SigarBootStrap.boot();

            //initial restserver
            RestServer rest = new RestServer(config);
            URI advertisedUrl = rest.advertisedUrl();
            String restUrl = advertisedUrl.getHost() + ":" + advertisedUrl.getPort();
            String workerId = config.getString(CommonClientConfigs.CLIENT_ID_CONFIG);

            //initial worker
            Time time = new SystemTime();
            Worker worker = new Worker(
                    workerId,
                    time,
                    config,
                    new TaskPositionManager(buildTaskPositionService(bootMode), config),
                    new TaskSyncStatusManager(DataLinkFactory.getObject(TaskSyncStatusService.class), config),
                    ProbeManager.getInstance()
            );

            //initial Keeper
            Keeper keeper = buildKeeper(
                    config,
                    worker,
                    time,
                    restUrl,
                    bootMode
            );

            //start datalink worker
            logger.info("## start the datalink worker.");
            final WorkerController controller = new WorkerController(keeper, rest);
            controller.startup();
            logger.info("## the datalink worker is running now ......");
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    try {
                        logger.info("## stop the datalink worker");
                        controller.shutdown();
                        appContextBootStrap.close();
                    } catch (Throwable e) {
                        logger.warn("##something goes wrong when stopping datalink worker", e);
                    } finally {
                        logger.info("## datalink worker is down.");
                    }
                }

            });
        } catch (Throwable e) {
            logger.error("## Something goes wrong when starting up the datalink worker:", e);
            System.exit(1);
        }
    }

    private Properties buildWorkerProps() throws IOException {
        Properties properties = new Properties();

        String conf = System.getProperty("worker.conf");
        if (!StringUtils.isEmpty(conf)) {
            if (conf.startsWith(CLASSPATH_URL_PREFIX)) {
                conf = StringUtils.substringAfter(conf, CLASSPATH_URL_PREFIX);
                properties.load(WorkerBootStrap.class.getClassLoader().getResourceAsStream(conf));
            } else {
                properties.load(new FileInputStream(conf));
            }
        }
        return properties;
    }

    private void buildWorkerDBProps(Map<String, String> workerProps) throws IOException {
        WorkerInfo workerInfo;
        WorkerService service = DataLinkFactory.getObject(WorkerService.class);
        String clientId = workerProps.get(CommonClientConfigs.CLIENT_ID_CONFIG);
        if (!StringUtils.isBlank(clientId)) {
            workerInfo = service.getById(Long.valueOf(clientId));
        } else {
            workerInfo = service.getByAddress(IPUtils.getHostIp());
        }

        if (workerInfo != null) {
            workerProps.put(WorkerConfig.GROUP_ID_CONFIG, String.valueOf(workerInfo.getGroupId()));
            workerProps.put(WorkerConfig.REST_PORT_CONFIG, workerInfo.getRestPort().toString());
            if (StringUtils.isBlank(clientId)) {
                workerProps.put(CommonClientConfigs.CLIENT_ID_CONFIG, String.valueOf(workerInfo.getId()));
            }
            workerProps.put(WorkerConfig.LAB_ID, String.valueOf(workerInfo.getLabId()));
        } else {
            throw new DatalinkException(String.format("Worker is not found for client id [%s] or ip [%s] ", clientId, IPUtils.getHostIp()));
        }

        //ddl_sync、sync_auto_addcolumn参数从数据库中读取
        SysPropertiesService propertiesService = DataLinkFactory.getObject(SysPropertiesService.class);
        Map<String, String> map = propertiesService.map();
        workerProps.put(WorkerConfig.DDL_SYNC_CONFIG, StringUtils.isNotBlank((map.get(WorkerConfig.DDL_SYNC_CONFIG))) ? map.get(WorkerConfig.DDL_SYNC_CONFIG) : String.valueOf(WorkerConfig.DDL_SYNC_DEFAULT));
        workerProps.put(WorkerConfig.SYNC_AUTO_ADD_COLUMN_CONFIG, StringUtils.isNotBlank((map.get(WorkerConfig.SYNC_AUTO_ADD_COLUMN_CONFIG))) ? map.get(WorkerConfig.SYNC_AUTO_ADD_COLUMN_CONFIG) : String.valueOf(WorkerConfig.SYNC_AUTO_ADD_COLUMN_DEFAULT));
    }

    private Keeper buildKeeper(WorkerConfig config, Worker worker, Time time, String restUrl, String bootMode) {
        Keeper keeper;
        if (BootMode.DISTRIBUTED.equals(bootMode)) {
            keeper = new WorkerKeeper(
                    config,
                    time,
                    worker,
                    new TaskStatusManager(DataLinkFactory.getObject(TaskStatusService.class)),
                    new TaskConfigManager(config.getString(WorkerConfig.GROUP_ID_CONFIG), DataLinkFactory.getObject(TaskConfigService.class)),
                    restUrl
            );
        } else if (BootMode.STANDALONE.equals(bootMode)) {
            keeper = new StandaloneWorkerKeeper(
                    config,
                    worker,
                    time,
                    new TaskConfigManager(config.getString(WorkerConfig.GROUP_ID_CONFIG), DataLinkFactory.getObject(TaskConfigService.class))
            );
        } else {
            throw new DatalinkException("invalid boot mode : " + bootMode);
        }

        return keeper;
    }

    private TaskPositionService buildTaskPositionService(String bootMode) {
        if (BootMode.DISTRIBUTED.equals(bootMode)) {
            return DataLinkFactory.getObject("taskPositionServiceZkImpl");
        } else {
            return DataLinkFactory.getObject("taskPositionServiceDbImpl");
        }
    }

    public static void main(String[] args) {
        WorkerBootStrap bootStrap = new WorkerBootStrap();
        bootStrap.boot(args);
    }
}
