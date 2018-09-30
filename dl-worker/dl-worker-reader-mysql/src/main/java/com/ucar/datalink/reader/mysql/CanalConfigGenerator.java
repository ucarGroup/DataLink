package com.ucar.datalink.reader.mysql;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import com.google.common.collect.Lists;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.rdb.RdbMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.sddl.SddlMediaSrcParameter;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderParameter;
import com.ucar.datalink.worker.api.task.TaskReaderContext;
import org.apache.commons.lang.StringUtils;

import java.net.InetSocketAddress;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * 将MysqlReaderParameter的配置转化为CanalConfig.
 * Created by lubiao on 2017/3/20.
 */
public class CanalConfigGenerator {

    public static Canal buildCanalConfig(String destination, MysqlReaderParameter parameter, TaskReaderContext context) {
        Canal canal = new Canal();
        canal.setId(Long.valueOf(destination));
        canal.setName(destination);
        canal.setCanalParameter(buildCanalParameter(parameter, context));
        return canal;
    }

    @SuppressWarnings("unchecked")
    private static CanalParameter buildCanalParameter(MysqlReaderParameter parameter, TaskReaderContext context) {
        CanalParameter canalParameter = new CanalParameter();

        //zookeeper相关信息不用配置，因为CanalTaskMetaManager已经重载了metaManager，靠Datalink框架层提供位点查询和持久化功能
        canalParameter.setZkClusterId(null);
        canalParameter.setZkClusters(null);

        //数据库配置，页面上直接选数据库，然后在此处进行组装
        MediaSourceInfo mediaSrc = context.getService(MediaService.class).getMediaSourceById(parameter.getMediaSourceId());
        if (mediaSrc.getParameterObj() instanceof RdbMediaSrcParameter) {

            RdbMediaSrcParameter mediaSrcParameter = mediaSrc.getParameterObj();
            List<CanalParameter.DataSourcing> masterStandby = buildOnePair(parameter, context, mediaSrcParameter);
            if (masterStandby.size() == 2) {
                canalParameter.setGroupDbAddresses(Lists.<List<CanalParameter.DataSourcing>>newArrayList(
                        Lists.newArrayList(masterStandby.get(0)),
                        Lists.newArrayList(masterStandby.get(1))
                ));
            } else {
                canalParameter.setGroupDbAddresses(Lists.<List<CanalParameter.DataSourcing>>newArrayList(
                        Lists.newArrayList(masterStandby.get(0))
                ));
            }

            canalParameter.setDbUsername(mediaSrcParameter.getReadConfig().getUsername());
            canalParameter.setDbPassword(mediaSrcParameter.getReadConfig().getDecryptPassword());
            canalParameter.setConnectionCharset(mediaSrcParameter.getEncoding());
            canalParameter.setDefaultDatabaseName(mediaSrcParameter.getNamespace());

        } else if (mediaSrc.getParameterObj() instanceof SddlMediaSrcParameter) {

            SddlMediaSrcParameter mediaSrcParameter = mediaSrc.getParameterObj();
            List<CanalParameter.DataSourcing> masters = new ArrayList<>();
            List<CanalParameter.DataSourcing> standbys = new ArrayList<>();
            for (Long id : mediaSrcParameter.getPrimaryDbsId()) {
                RdbMediaSrcParameter item = DataLinkFactory.getObject(MediaService.class).getMediaSourceById(id).getParameterObj();
                List<CanalParameter.DataSourcing> masterStandby = buildOnePair(parameter, context, item);
                masters.add(masterStandby.get(0));
                if (masterStandby.size() == 2) {
                    standbys.add(masterStandby.get(1));
                }
            }
            if (!standbys.isEmpty()) {
                canalParameter.setGroupDbAddresses(Lists.<List<CanalParameter.DataSourcing>>newArrayList(
                        masters, standbys
                ));
            } else {
                canalParameter.setGroupDbAddresses(Lists.<List<CanalParameter.DataSourcing>>newArrayList(
                        masters
                ));
            }

            RdbMediaSrcParameter proxyParameter = DataLinkFactory.getObject(MediaService.class).getMediaSourceById(mediaSrcParameter.getProxyDbId()).getParameterObj();
            canalParameter.setDbUsername(proxyParameter.getReadConfig().getUsername());
            canalParameter.setDbPassword(proxyParameter.getReadConfig().getDecryptPassword());
            canalParameter.setConnectionCharset(proxyParameter.getEncoding());
            canalParameter.setDefaultDatabaseName(proxyParameter.getNamespace());
        } else {
            throw new InvalidParameterException("Unknown MediaSource :" + mediaSrc.getParameterObj().getClass());
        }

        //绝大部分情况下只指定Timestamps就可满足需求
        canalParameter.setPositions(
                Lists.newArrayList(
                        JSON.toJSONString(new EntryPosition(parameter.getStartTimeStamps())),
                        JSON.toJSONString(new EntryPosition(parameter.getStartTimeStamps()))
                ));

        //位点配置考虑多个库的情况
        canalParameter.setStorageMode(CanalParameter.StorageMode.MEMORY);//限定为MEMORY,有需要时再改为可支持其它模式
        canalParameter.setStorageBatchMode(CanalParameter.BatchMode.MEMSIZE);//限定为MEMORY,有需要时再改为可支持其它模式
        canalParameter.setMemoryStorageBufferSize(parameter.getMemoryStorageBufferSize());
        canalParameter.setMemoryStorageBufferMemUnit(parameter.getMemoryStorageBufferMemUnit());
        canalParameter.setHaMode(CanalParameter.HAMode.HEARTBEAT);//限定为HEARTBEAT，有需要时再改为可支持其它模式
        canalParameter.setDetectingEnable(true);
        canalParameter.setHeartbeatHaEnable(true);
        canalParameter.setDetectingSQL(parameter.getDetectingSQL());
        canalParameter.setDetectingIntervalInSeconds(parameter.getDetectingIntervalInSeconds());
        canalParameter.setDetectingRetryTimes(parameter.getDetectingRetryTimes());
        canalParameter.setDetectingTimeoutThresholdInSeconds(parameter.getDetectingTimeoutThresholdInSeconds());
        canalParameter.setMetaMode(CanalParameter.MetaMode.MIXED);//重写了MetaManager，此处随便设置一个值即可，不设置会报异常
        canalParameter.setIndexMode(CanalParameter.IndexMode.MEMORY_META_FAILBACK);//限定为MEMORY_META_FAILBACK，有需要时再改为可支持其它模式
        canalParameter.setDefaultConnectionTimeoutInSeconds(parameter.getDefaultConnectionTimeoutInSeconds());
        canalParameter.setSendBufferSize(parameter.getSendBufferSize());
        canalParameter.setReceiveBufferSize(parameter.getReceiveBufferSize());
        canalParameter.setFallbackIntervalInSeconds(parameter.getFallbackIntervalInSeconds());
        canalParameter.setBlackFilter(parameter.getBlackFilter());
        canalParameter.setDdlIsolation(true);//必须设置为true，让ddl和dml分属不同的batch
        return canalParameter;
    }

    private static List<CanalParameter.DataSourcing> buildOnePair(MysqlReaderParameter parameter, TaskReaderContext context,
                                                                  RdbMediaSrcParameter mediaSrcParameter) {
        //如果心跳检测sql是dml操作，那么canal的ip只能选择写库ip，因为读库开启了read-only模式，无法执行dml操作
        //写库ip是虚ip，也可以支持canal的高可用，缺点就是会给写库增加一些压力
        //所以，在心跳sql不是dml操作时，优先使用读库ip取binlog

        String detectingSQL = parameter.getDetectingSQL();
        if (StringUtils.startsWithIgnoreCase(detectingSQL.trim(), "select")
                || StringUtils.startsWithIgnoreCase(detectingSQL.trim(), "show")
                || StringUtils.startsWithIgnoreCase(detectingSQL.trim(), "explain")
                || StringUtils.startsWithIgnoreCase(detectingSQL.trim(), "desc")) {
            List<String> hosts = mediaSrcParameter.getReadConfig().getHosts();
            int seed1 = (int) (Long.valueOf(context.taskId()) + hosts.size());//加一个host.size(),保证seed值比size值大
            int seed2 = seed1 + 1;

            ArrayList<CanalParameter.DataSourcing> result = new ArrayList<>();
            result.add(
                    new CanalParameter.DataSourcing(
                            CanalParameter.SourcingType.MYSQL,
                            new InetSocketAddress(hosts.get(seed1 % hosts.size()), mediaSrcParameter.getPort())));
            result.add(
                    new CanalParameter.DataSourcing(
                            CanalParameter.SourcingType.MYSQL,
                            new InetSocketAddress(hosts.get(seed2 % hosts.size()), mediaSrcParameter.getPort())));
            return result;
        } else {
            //vip模式下，只能配master-address，不能配standby-address
            //详见com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser的findStartPositionInternal()方法
            ArrayList<CanalParameter.DataSourcing> result = new ArrayList<>();
            result.add(
                    new CanalParameter.DataSourcing(
                            CanalParameter.SourcingType.MYSQL,
                            new InetSocketAddress(mediaSrcParameter.getWriteConfig().getWriteHost(), mediaSrcParameter.getPort())));
            return result;
        }
    }
}
