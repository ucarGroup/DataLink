package com.ucar.datalink.reader.mysql.extend;

import com.alibaba.otter.canal.common.CanalException;
import com.alibaba.otter.canal.common.utils.JsonUtils;
import com.alibaba.otter.canal.filter.aviater.AviaterRegexFilter;
import com.alibaba.otter.canal.instance.manager.CanalInstanceWithManager;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.parse.CanalEventParser;
import com.alibaba.otter.canal.parse.ha.CanalHAController;
import com.alibaba.otter.canal.parse.inbound.AbstractEventParser;
import com.alibaba.otter.canal.parse.inbound.group.GroupEventParser;
import com.alibaba.otter.canal.parse.inbound.mysql.LocalBinlogEventParser;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.parse.support.AuthenticationInfo;
import com.alibaba.otter.canal.protocol.position.EntryPosition;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 1. 继承自Canal的CanalInstanceWithManager
 * 2. initEventParser()和doInitEventParser()，这两个方法完全copy自CanalInstanceWithManager，与其差异在于：
 *    > 如果在Group模式下，初始化的parser类型为自定义的CustomInsideGroupMysqlEventParser
 *    > 如果是在非Group模型下，初始化的parser类型为Canal自带的MysqlEventParser
 *    > 具体原因参见CustomInsideGroupMysqlEventParser的注释
 *    > 相关代码变更以 "//@@changes" 进行了标记
 * 3. Important：对引用的canal版本进行升级的时候，记得进行代码检查和合并
 *
 * Created by lubiao on 2019/1/29.
 */
public class CustomCanalInstanceWithManager extends CanalInstanceWithManager {
    private static final Logger logger = LoggerFactory.getLogger(CustomCanalInstanceWithManager.class);

    public CustomCanalInstanceWithManager(Canal canal, String filter) {
        super(canal, filter);
    }

    @Override
    protected void initEventParser() {
        logger.info("init eventParser begin...");
        CanalParameter.SourcingType type = parameters.getSourcingType();

        List<List<CanalParameter.DataSourcing>> groupDbAddresses = parameters.getGroupDbAddresses();
        if (!CollectionUtils.isEmpty(groupDbAddresses)) {
            int size = groupDbAddresses.get(0).size();// 取第一个分组的数量，主备分组的数量必须一致
            List<CanalEventParser> eventParsers = new ArrayList<CanalEventParser>();
            for (int i = 0; i < size; i++) {
                List<InetSocketAddress> dbAddress = new ArrayList<InetSocketAddress>();
                CanalParameter.SourcingType lastType = null;
                for (List<CanalParameter.DataSourcing> groupDbAddress : groupDbAddresses) {
                    if (lastType != null && !lastType.equals(groupDbAddress.get(i).getType())) {
                        throw new CanalException(String.format("master/slave Sourcing type is unmatch. %s vs %s",
                                lastType,
                                groupDbAddress.get(i).getType()));
                    }

                    lastType = groupDbAddress.get(i).getType();
                    dbAddress.add(groupDbAddress.get(i).getDbAddress());
                }

                // 初始化其中的一个分组parser
                eventParsers.add(doInitEventParser(lastType, dbAddress, true));//@@changes
            }

            if (eventParsers.size() > 1) { // 如果存在分组，构造分组的parser
                GroupEventParser groupEventParser = new GroupEventParser();
                groupEventParser.setEventParsers(eventParsers);
                this.eventParser = groupEventParser;
            } else {
                this.eventParser = eventParsers.get(0);
            }
        } else {
            // 创建一个空数据库地址的parser，可能使用了tddl指定地址，启动的时候才会从tddl获取地址
            this.eventParser = doInitEventParser(type, new ArrayList<InetSocketAddress>(), true);//@@changes
        }

        logger.info("init eventParser end! \n\t load CanalEventParser:{}", eventParser.getClass().getName());
    }

    private CanalEventParser doInitEventParser(CanalParameter.SourcingType type, List<InetSocketAddress> dbAddresses, boolean isGroupMode) {
        CanalEventParser eventParser;
        if (type.isMysql()) {
            //@@changes begin
            MysqlEventParser mysqlEventParser;
            if (isGroupMode) {
                mysqlEventParser = new CustomInsideGroupMysqlEventParser();
            } else {
                mysqlEventParser = new MysqlEventParser();
            }
            //@@changes end

            mysqlEventParser.setDestination(destination);
            // 编码参数
            mysqlEventParser.setConnectionCharset(Charset.forName(parameters.getConnectionCharset()));
            mysqlEventParser.setConnectionCharsetNumber(parameters.getConnectionCharsetNumber());
            // 网络相关参数
            mysqlEventParser.setDefaultConnectionTimeoutInSeconds(parameters.getDefaultConnectionTimeoutInSeconds());
            mysqlEventParser.setSendBufferSize(parameters.getSendBufferSize());
            mysqlEventParser.setReceiveBufferSize(parameters.getReceiveBufferSize());
            // 心跳检查参数
            mysqlEventParser.setDetectingEnable(parameters.getDetectingEnable());
            mysqlEventParser.setDetectingSQL(parameters.getDetectingSQL());
            mysqlEventParser.setDetectingIntervalInSeconds(parameters.getDetectingIntervalInSeconds());
            // 数据库信息参数
            mysqlEventParser.setSlaveId(parameters.getSlaveId());
            if (!CollectionUtils.isEmpty(dbAddresses)) {
                mysqlEventParser.setMasterInfo(new AuthenticationInfo(dbAddresses.get(0),
                        parameters.getDbUsername(),
                        parameters.getDbPassword(),
                        parameters.getDefaultDatabaseName()));

                if (dbAddresses.size() > 1) {
                    mysqlEventParser.setStandbyInfo(new AuthenticationInfo(dbAddresses.get(1),
                            parameters.getDbUsername(),
                            parameters.getDbPassword(),
                            parameters.getDefaultDatabaseName()));
                }
            }

            if (!CollectionUtils.isEmpty(parameters.getPositions())) {
                EntryPosition masterPosition = JsonUtils.unmarshalFromString(parameters.getPositions().get(0),
                        EntryPosition.class);
                // binlog位置参数
                mysqlEventParser.setMasterPosition(masterPosition);

                if (parameters.getPositions().size() > 1) {
                    EntryPosition standbyPosition = JsonUtils.unmarshalFromString(parameters.getPositions().get(0),
                            EntryPosition.class);
                    mysqlEventParser.setStandbyPosition(standbyPosition);
                }
            }
            mysqlEventParser.setFallbackIntervalInSeconds(parameters.getFallbackIntervalInSeconds());
            mysqlEventParser.setProfilingEnabled(false);
            mysqlEventParser.setFilterTableError(parameters.getFilterTableError());
            eventParser = mysqlEventParser;
        } else if (type.isLocalBinlog()) {
            LocalBinlogEventParser localBinlogEventParser = new LocalBinlogEventParser();
            localBinlogEventParser.setDestination(destination);
            localBinlogEventParser.setBufferSize(parameters.getReceiveBufferSize());
            localBinlogEventParser.setConnectionCharset(Charset.forName(parameters.getConnectionCharset()));
            localBinlogEventParser.setConnectionCharsetNumber(parameters.getConnectionCharsetNumber());
            localBinlogEventParser.setDirectory(parameters.getLocalBinlogDirectory());
            localBinlogEventParser.setProfilingEnabled(false);
            localBinlogEventParser.setDetectingEnable(parameters.getDetectingEnable());
            localBinlogEventParser.setDetectingIntervalInSeconds(parameters.getDetectingIntervalInSeconds());
            localBinlogEventParser.setFilterTableError(parameters.getFilterTableError());
            // 数据库信息，反查表结构时需要
            if (!CollectionUtils.isEmpty(dbAddresses)) {
                localBinlogEventParser.setMasterInfo(new AuthenticationInfo(dbAddresses.get(0),
                        parameters.getDbUsername(),
                        parameters.getDbPassword(),
                        parameters.getDefaultDatabaseName()));

            }
            eventParser = localBinlogEventParser;
        } else if (type.isOracle()) {
            throw new CanalException("unsupport SourcingType for " + type);
        } else {
            throw new CanalException("unsupport SourcingType for " + type);
        }

        // add transaction support at 2012-12-06
        if (eventParser instanceof AbstractEventParser) {
            AbstractEventParser abstractEventParser = (AbstractEventParser) eventParser;
            abstractEventParser.setTransactionSize(parameters.getTransactionSize());
            abstractEventParser.setLogPositionManager(initLogPositionManager());
            abstractEventParser.setAlarmHandler(getAlarmHandler());
            abstractEventParser.setEventSink(getEventSink());

            if (StringUtils.isNotEmpty(filter)) {
                AviaterRegexFilter aviaterFilter = new AviaterRegexFilter(filter);
                abstractEventParser.setEventFilter(aviaterFilter);
            }

            // 设置黑名单
            if (StringUtils.isNotEmpty(parameters.getBlackFilter())) {
                AviaterRegexFilter aviaterFilter = new AviaterRegexFilter(parameters.getBlackFilter());
                abstractEventParser.setEventBlackFilter(aviaterFilter);
            }
        }
        if (eventParser instanceof MysqlEventParser) {
            MysqlEventParser mysqlEventParser = (MysqlEventParser) eventParser;

            // 初始化haController，绑定与eventParser的关系，haController会控制eventParser
            CanalHAController haController = initHaController();
            mysqlEventParser.setHaController(haController);
        }
        return eventParser;
    }
}
