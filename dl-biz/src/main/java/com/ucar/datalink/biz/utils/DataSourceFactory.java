package com.ucar.datalink.biz.utils;

import com.google.common.cache.*;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.media.parameter.MediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.rdb.BasicDataSourceConfig;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * DataSource是J2EE标准，不需要插件化
 * <p>
 * Created by user on 2017/3/15.
 */
public class DataSourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);

    private static final LoadingCache<MediaSourceInfo, DataSource> dataSources;

    static {
        dataSources = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).removalListener(new RemovalListener<MediaSourceInfo, DataSource>() {
            @Override
            public void onRemoval(RemovalNotification<MediaSourceInfo, DataSource> notification) {
                DataSource ds = notification.getValue();
                if (ds instanceof org.apache.tomcat.jdbc.pool.DataSource) {
                    try {
                        ((org.apache.tomcat.jdbc.pool.DataSource) ds).close();
                        logger.info("RemovalListener close datasource succeeded.");
                    } catch (Exception e) {
                        logger.error("RemovalListener close datasource failed, DataSource is " + ds, e);
                    }
                }
            }
        }).build(new CacheLoader<MediaSourceInfo, DataSource>() {
            @Override
            public DataSource load(MediaSourceInfo sourceInfo) throws Exception {
                MediaSrcParameter parameter = sourceInfo.getParameterObj();
                RdbMediaSrcParameter rdbParameter;
                if (parameter instanceof RdbMediaSrcParameter) {
                    rdbParameter = (RdbMediaSrcParameter) parameter;
                } else if (parameter instanceof SddlMediaSrcParameter) {
                    Long proxyMsId = ((SddlMediaSrcParameter) parameter).getProxyDbId();
                    rdbParameter = DataLinkFactory.getObject(MediaSourceService.class).getById(proxyMsId).getParameterObj();
                } else {
                    throw new DatalinkException("Unknown MediaSrcParameter Type.");
                }

                Object object = rdbParameter.getDataSourceConfig();
                if (object instanceof BasicDataSourceConfig) {
                    return createTomcatDataSource(rdbParameter, (BasicDataSourceConfig) object);
                } else {
                    throw new DatalinkException("Unknown DataSourceConfig Type:" + object.getClass().getCanonicalName());
                }
            }
        });
    }

    public static DataSource createTomcatDataSource(RdbMediaSrcParameter parameter, BasicDataSourceConfig dsConfig) {
        org.apache.tomcat.jdbc.pool.DataSource tomcatDs = new org.apache.tomcat.jdbc.pool.DataSource();

        tomcatDs.setInitialSize(dsConfig.getInitialSize());// 初始化连接池时创建的连接数
        tomcatDs.setMaxActive(dsConfig.getMaxActive());// 连接池允许的最大并发连接数，值为非正数时表示不限制
        tomcatDs.setMaxIdle(dsConfig.getMaxIdle());// 连接池中的最大空闲连接数，超过时，多余的空闲连接将会被释放，值为负数时表示不限制
        tomcatDs.setMinIdle(dsConfig.getMinIdle());// 连接池中的最小空闲连接数，低于此数值时将会创建所欠缺的连接，值为0时表示不创建
        tomcatDs.setMaxWait(dsConfig.getMaxWait());// 以毫秒表示的当连接池中没有可用连接时等待可用连接返回的时间，超时则抛出异常，值为-1时表示无限等待
        tomcatDs.setRemoveAbandoned(true);// 是否清除已经超过removeAbandonedTimeout设置的无效连接
        tomcatDs.setLogAbandoned(true);// 当清除无效链接时是否在日志中记录清除信息的标志
        tomcatDs.setRemoveAbandonedTimeout(dsConfig.getRemoveAbandonedTimeout()); // 以秒表示清除无效链接的时限
        tomcatDs.setNumTestsPerEvictionRun(dsConfig.getNumTestsPerEvictionRun());// 确保连接池中没有已破损的连接
        tomcatDs.setTestOnBorrow(false);// 指定连接被调用时是否经过校验
        tomcatDs.setTestOnReturn(false);// 指定连接返回到池中时是否经过校验
        tomcatDs.setTestWhileIdle(true);// 指定连接进入空闲状态时是否经过空闲对象驱逐进程的校验
        tomcatDs.setTimeBetweenEvictionRunsMillis(dsConfig.getTimeBetweenEvictionRunsMillis()); // 以毫秒表示空闲对象驱逐进程由运行状态进入休眠状态的时长，值为非正数时表示不运行任何空闲对象驱逐进程
        tomcatDs.setMinEvictableIdleTimeMillis(dsConfig.getMinEvictableIdleTimeMillis()); // 以毫秒表示连接被空闲对象驱逐进程驱逐前在池中保持空闲状态的最小时间

        // 动态的参数
        tomcatDs.setDriverClassName(parameter.getDriver());
        tomcatDs.setUrl(getJdbcUrl(
                parameter.getMediaSourceType(),
                parameter.getWriteConfig().getWriteHost(),
                parameter.getPort(),
                parameter.getNamespace()));
        tomcatDs.setUsername(parameter.getWriteConfig().getUsername());
        tomcatDs.setPassword(parameter.getWriteConfig().getDecryptPassword());

        if (parameter.getMediaSourceType() == MediaSourceType.ORACLE) {
            Properties props = new Properties();
            props.setProperty("restrictGetTables", "true");
            tomcatDs.setDbProperties(props);

            tomcatDs.setValidationQuery("select 1 from dual");
        } else if (parameter.getMediaSourceType() == MediaSourceType.MYSQL) {
            // open the batch mode for mysql since 5.1.8
            Properties props = new Properties();
            props.setProperty("useServerPrepStmts", "false");
            props.setProperty("rewriteBatchedStatements", "true");
            props.setProperty("zeroDateTimeBehavior", "convertToNull");// 将0000-00-00的时间类型返回null
            props.setProperty("yearIsDateType", "false");// 直接返回字符串，不做year转换date处理
            props.setProperty("noDatetimeStringSync", "true");// 返回时间类型的字符串,不做时区处理
            if (StringUtils.isNotEmpty(parameter.getEncoding())) {
                if (StringUtils.equalsIgnoreCase(parameter.getEncoding(), "utf8mb4")) {
                    props.setProperty("characterEncoding", "utf8");
                    tomcatDs.setInitSQL("set names utf8mb4");
                } else {
                    props.setProperty("characterEncoding", parameter.getEncoding());
                }
            }
            tomcatDs.setDbProperties(props);
            tomcatDs.setValidationQuery("select 1");

        } else if (parameter.getMediaSourceType() == MediaSourceType.SQLSERVER) {
            tomcatDs.setValidationQuery("select 1");
        } else if (parameter.getMediaSourceType() == MediaSourceType.POSTGRESQL) {
            tomcatDs.setValidationQuery("select 1");
        } else {
            logger.error("ERROR ## Unknow database type");
        }

        return tomcatDs;
    }

    /**
     * get jdbc prefix if the source type is rdbms.
     */
    private static String getJdbcUrl(MediaSourceType type, String host, int port, String schema) {
        if (type == MediaSourceType.MYSQL) {
            return String.format("jdbc:mysql://%s:%s/%s", host, port, schema);
        } else if (type == MediaSourceType.SQLSERVER) {
            return String.format("jdbc:sqlserver://%s:%s;DatabaseName=%s", host, port, schema);
        } else if (type == MediaSourceType.ORACLE) {
            return String.format("jdbc:oracle:thin:@//%s:%s/%s", host, port, schema);
        } else if (type == MediaSourceType.POSTGRESQL) {
            return String.format("jdbc:postgresql://%s:%s/%s", host, port, schema);
        }
        return null;
    }

    public static DataSource getDataSource(MediaSourceInfo mediaSourceInfo) {
        return dataSources.getUnchecked(mediaSourceInfo);
    }

    public static void invalidate(MediaSourceInfo mediaSourceInfo, Supplier preCloseAction) {
        DataSource ds = dataSources.getIfPresent(mediaSourceInfo);
        if (ds != null) {
            dataSources.invalidate(mediaSourceInfo);
            preCloseAction.get();
            if (ds instanceof org.apache.tomcat.jdbc.pool.DataSource) {
                try {
                    ((org.apache.tomcat.jdbc.pool.DataSource) ds).close();
                } catch (Exception e) {
                    logger.error("Datasource close failed,MediaSource id is " + mediaSourceInfo.getId(), e);
                }
            }
            logger.info("Datasource invalidate successfully");
        }
        logger.info("No Datasource to be invalidated");
    }
}
