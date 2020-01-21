package com.ucar.datalink.writer.sddl.dataSource;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.ucar.datalink.biz.service.DoubleCenterService;
import com.ucar.datalink.biz.service.MediaSourceRelationService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.biz.utils.ddl.DdlUtils;
import com.ucar.datalink.domain.media.*;
import com.ucar.datalink.writer.sddl.exception.SddlInitException;
import com.ucar.datalink.common.errors.TaskExecuteException;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.zuche.confcenter.bean.vo.ClientDataSource;
import com.zuche.framework.enums.IdcEnum;
import com.zuche.framework.extend.manager.SddlCfcenterManager;
import com.zuche.framework.extend.manager.SddlCfcenterManagerFactory;
import com.zuche.framework.sddl.datasource.SddlLogicCluster;
import com.zuche.framework.utils.SddlConfChange;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.ddlutils.model.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 03/11/2017.
 */
public class DataSourceCluster {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceCluster.class);

    public static final DefaultLobHandler defaultLobHandler;

    private static LoadingCache<MediaSourceInfo, SddlDataLinkCluster> dataSources;  // key: sddlMediaSourceInfo
    private static LoadingCache<DataSource, SddlJdbcTemplate>         jdbcTemplates;
    private static LoadingCache<List<Object>, Table>                  tables;       // key: [0]DataSource,[1]schemaName,[2]tableName
    static {
        dataSources = CacheBuilder.newBuilder().build(new CacheLoader<MediaSourceInfo, SddlDataLinkCluster>() {
            @Override
            public SddlDataLinkCluster load(MediaSourceInfo sourceInfo) throws Exception {

                // init datasource for sddl
                SddlDataLinkCluster sddlDataLinkCluster = new SddlDataLinkCluster();
                SddlLogicCluster sddlLogicCluster = initDataSource(sourceInfo);
                sddlDataLinkCluster.setSddlLogicCluster(sddlLogicCluster);;

                return sddlDataLinkCluster;
            }
        });

        jdbcTemplates = CacheBuilder.newBuilder().build(new CacheLoader<DataSource, SddlJdbcTemplate>() {
            @Override
            public SddlJdbcTemplate load(DataSource dataSource) throws Exception {

                Connection connection = null;
                String schemaName = null;
                try {
                    connection = dataSource.getConnection();
                    schemaName = connection.getCatalog();
                } catch (Exception e) {
                    throw new SddlInitException("init SddlJdbcTemplate is error， datasource:"+JSON.toJSONString(dataSource), e);
                }finally {
                    connection.close();
                }
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

                return new SddlJdbcTemplate(jdbcTemplate, schemaName);
            }
        });

        tables = CacheBuilder.newBuilder().softValues().build(new CacheLoader<List<Object>, Table>() {
            @Override
            public Table load(List<Object> names) throws Exception {
                Assert.isTrue(names.size() == 3);
                try {
                    DataSource dataSource = (DataSource) names.get(0);
                    String schemaName         = (String) names.get(1);
                    String tableName          = (String) names.get(2);

                    JdbcTemplate jdbcTemplate = jdbcTemplates.getUnchecked(dataSource).getJdbcTemplate();

                    Table table = DdlUtils.findTable(
                            jdbcTemplate,
                            getActualSchemaName(schemaName),
                            getActualSchemaName(schemaName),
                            tableName,
                            null);

                    if (table == null) {
                        throw new NestableRuntimeException("no found table [" + names.get(1) + "." + names.get(2)
                                + "] , pls check");
                    } else {
                        return table;
                    }
                } catch (Exception e) {
                    throw new NestableRuntimeException("find table [" + names.get(1) + "." + names.get(1) + "] error",
                            e);
                }
            }
        });

        defaultLobHandler = new DefaultLobHandler();
        defaultLobHandler.setStreamAsLob(true);
    }

    public static Map<String, String> getProjectNameByDss() {

        Map<String, String> resultMap = Maps.newConcurrentMap();
        for (MediaSourceInfo keyInfo : dataSources.asMap().keySet()) {
            String projectName = ((SddlMediaSrcParameter)keyInfo.getParameterObj()).getProjectName();

            resultMap.put(projectName, projectName);
        }

        return resultMap;
    }

    public static SddlDataLinkCluster getSddlDs(MediaSourceInfo sddlMSI) {
        return dataSources.getUnchecked(sddlMSI);
    }

    public static SddlJdbcTemplate getSddlJdbcTemplate (DataSource dataSource) {
        return jdbcTemplates.getUnchecked(dataSource);
    }

    /**
     * @Description: 入参为List，且必须包含三个参数，分别为：
     *                  0：DataSource
     *                  1：schemaNmae
     *                  2：tableName
     *
     * @Author : yongwang.chen@ucarinc.com
     * @Date   : 7:26 PM 22/11/2017
     */
    public static Table getTable (final List<Object> paramList) {
        return tables.getUnchecked(paramList);
    }

    private static SddlLogicCluster initDataSource(MediaSourceInfo sourceInfo){

        String projectName = ((SddlMediaSrcParameter)sourceInfo.getParameterObj()).getProjectName();

        SddlCfcenterManager sddlCfcenterManager = SddlCfcenterManagerFactory.getSddlCfcenterManager(projectName);

        ClientDataSource clientDataSource = sddlCfcenterManager.getShardingDB(projectName);
        String shardingDBNames = clientDataSource.getSourceValue();

        if (StringUtils.isEmpty(shardingDBNames)) {
            String errorInfo = "sddlWriter_work get datasource from cfCenter is error, info:"+ JSON.toJSONString(shardingDBNames);
            LOG.error(errorInfo);
            throw new TaskExecuteException(errorInfo);
        }


        /**
         * 以下注释的是双中心代码
         *
         *
         * 注意事项
         *      1 从A切到B时，首先sddl关闭A机房数据库连接，这个关闭连接，因为datalink没有使用JndiIdcSwitchCallBack这个类，则不会关闭datalink使用的数据库连接
         *      2 SddlConfChange.selectedInitCluster接口设计不合理（1）datalink需要用当前中心来创建数据库连接，而sddl设计的是物理中心
         *                                                        （2）idcEnum只用来控制创建读连接，创建写连接还是sddl自己控制的，创建连接理应统一一处控制
         */
        MediaSourceRelationInfo relationInfo = DataLinkFactory.getObject(MediaSourceRelationService.class).getOneByRealMsId(sourceInfo.getId());
        SddlLogicCluster sddlLogicCluster;
        //真实
        if(relationInfo == null){
            sddlLogicCluster = SddlConfChange.selectedInitCluster(projectName, shardingDBNames,IdcEnum.LOGIC_A);
        }
        //虚拟
        else{
            String centerLab = DataLinkFactory.getObject(DoubleCenterService.class).getCenterLab(relationInfo.getVirtualMsId());
            IdcEnum idcEnum = IdcEnum.getIdcEnumFromCode(centerLab);
            if(idcEnum == null){
                throw new TaskExecuteException("sddlWriter_work get centerLab is error，mediaSourceId : " + sourceInfo.getId() + " , centerLab : " + centerLab);
            }
            sddlLogicCluster = SddlConfChange.selectedInitCluster(projectName, shardingDBNames,idcEnum);
        }

        //原代码
        //SddlLogicCluster sddlLogicCluster = SddlConfChange.selectedInitCluster(projectName, shardingDBNames);

        if (sddlLogicCluster == null
                || sddlLogicCluster.getSddlClusters() == null) {
            String errorInfo = "sddlWriter_work get SddlLogicCluster from cfCenter is error, info:"+ shardingDBNames
                                + ","
                                + sddlLogicCluster == null ? "sddlLogicCluster为null" :
                                    sddlLogicCluster.getSddlClusters() == null ? "getSddlClusters为null" :
                                            String.valueOf(sddlLogicCluster.getSddlClusters().size());
            LOG.error(errorInfo);
            throw new TaskExecuteException(errorInfo);
        }

        return sddlLogicCluster;
    }

    private static String getActualSchemaName(String schemaName) {
        if (StringUtils.isBlank(schemaName)) {
            return schemaName;
        }

        MediaInfo.ModeValue modeValue = ModeUtils.parseMode(schemaName);
        if (modeValue.getMode().isMulti()) {
            return modeValue.getMultiValue().get(0);//针对分库分表的场景，获取第一个库的信息使用即可
        }
        return schemaName;
    }

    public static void reloadSddlDataSource (final MediaSourceInfo mediaSourceInfo) {
        if (null == mediaSourceInfo) {
            dataSources.invalidateAll();
        } else {
            dataSources.invalidate(mediaSourceInfo);
        }
    }

    public static void reloadJdbcTemplate (final DataSource dataSource) {
        if (null == dataSource) {
            jdbcTemplates.invalidateAll();
        } else {
            jdbcTemplates.invalidate(dataSource);
        }
    }

    public static void reloadTable(final List<Object> tableParamKey) {
        if (CollectionUtils.isNotEmpty(tableParamKey) && tableParamKey.size() == 3) {
            tables.invalidate(tableParamKey);
        } else {
            // 如果没有存在表名，则直接清空所有的table，重新加载
            tables.invalidateAll();
        }

    }

}
