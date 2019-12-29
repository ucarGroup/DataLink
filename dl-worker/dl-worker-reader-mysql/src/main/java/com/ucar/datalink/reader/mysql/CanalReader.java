package com.ucar.datalink.reader.mysql;

import com.alibaba.otter.canal.instance.core.CanalInstance;
import com.alibaba.otter.canal.instance.core.CanalInstanceGenerator;
import com.alibaba.otter.canal.instance.manager.CanalInstanceWithManager;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.canal.instance.manager.model.CanalParameter;
import com.alibaba.otter.canal.parse.CanalEventParser;
import com.alibaba.otter.canal.parse.inbound.mysql.MysqlEventParser;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.ClientIdentity;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.server.embedded.CanalServerWithEmbedded;
import com.alibaba.otter.canal.sink.entry.EntryEventSink;
import com.alibaba.otter.canal.sink.entry.group.GroupEventSink;
import com.ucar.datalink.biz.service.MediaService;
import com.ucar.datalink.biz.service.SysPropertiesService;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.MediaSourceType;
import com.ucar.datalink.domain.plugin.reader.mysql.GroupSinkMode;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderParameter;
import com.ucar.datalink.domain.sysProperties.SysPropertiesInfo;
import com.ucar.datalink.domain.task.TaskShadowInfo;
import com.ucar.datalink.reader.mysql.extend.CustomCanalInstanceWithManager;
import com.ucar.datalink.reader.mysql.multiplexing.FakeEventParser;
import com.ucar.datalink.reader.mysql.multiplexing.FakeEventSink;
import com.ucar.datalink.worker.api.task.TaskReaderContext;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * CanalReader：对canal的访问进行封装和改造。
 * <p>
 * <p>
 * 关于initEventSinkNormal()方法的说明：
 * <p>
 * 组模式下，需要重设transactionInterval和transctionThresold，不能用默认值，
 * 因为默认值设置的值偏大，空事务会等待较长时间才doSink一次，在子库没什么流量
 * 的情况下，GroupEventSink会因为等待子库的doSink而等待较长时间，造成较大的延迟。
 * <p>
 * 所以为了解决GroupEventSink的卡住和延迟的问题，我们需要做的总共有三点：
 * 1. 强制产生binlog，通过制定update类型的心跳sql解决，如：
 * insert into datalink_util.t_dl_dual values(1,now()) on duplicate key update x_time=now()
 * 2. 调高心跳频率，参见类 com.ucar.datalink.reader.mysql.extend.CustomInsideGroupMysqlEventParser
 * 3. 就是此处的参数调整
 * <p>
 * 说明：
 * canal在LogEventConvert里虽然会基于黑白名单过滤数据，但不会过滤TransactionBegin和TransactionEnd，
 * 所以GroupEventSink里也能拿到不在名单范围内的event事件(即：也能拿到通过心跳sql强制产生的begin和end事件)，
 * 之前没有考虑到sink里还有一个空事务的阈值控制机制，所以只进行了1和2的优化，但没有达到效果，仍然有卡住
 * 和延迟的现象。所以，此处在Group模式下需要对这两个参数，进行特殊设置。
 * <p>
 * <p>
 * Created by lubiao on 2019/7/31.
 */
public class CanalReader {
    private static final Logger logger = LoggerFactory.getLogger(CanalReader.class);
    private static final String MESSAGE_KEY = "MESSAGES";

    private final String destination;
    private final TaskReaderContext context;
    private final MysqlReaderParameter parameter;
    private final MysqlTaskPositionManager taskPositionManager;
    private final CanalReaderType readerType;
    private final TaskShadowInfo taskShadow;

    private String filter;
    private int batchSize = 10000;
    private long batchTimeout = -1L;
    private CanalServerWithEmbedded canalServer;
    private ClientIdentity clientIdentity;
    private CanalReaderDownStreamHandler handler;
    private CanalReaderMessageParser messageParser;
    private volatile CanalReaderEffectSyncPosition effectSyncPosition;
    private CanalReaderMsg latestReaderMsg;
    private volatile boolean hasKickoutMultiplexingRead;
    private volatile boolean running = false;
    private AtomicBoolean needRestartDestination = new AtomicBoolean(false);

    CanalReader(String destination, TaskReaderContext context, MysqlReaderParameter parameter,
                MysqlTaskPositionManager taskPositionManager, CanalReaderType readerType, TaskShadowInfo taskShadow) {
        this.destination = destination;
        this.context = context;
        this.parameter = parameter;
        this.taskPositionManager = taskPositionManager;
        this.readerType = readerType;
        this.taskShadow = taskShadow;
    }

    void kickOut() {
        this.hasKickoutMultiplexingRead = true;
        this.needRestartDestination.compareAndSet(false, true);
    }

    void start() {
        if (running) {
            return;
        }
        this.filter = CanalReaderFilterBuilder.makeFilterExpression(destination, context, readerType, taskShadow);
        this.batchSize = parameter.getMessageBatchSize();
        this.batchTimeout = parameter.getBatchTimeout();
        this.messageParser = new CanalReaderMessageParser(context, parameter);
        this.handler = new CanalReaderDownStreamHandler();
        this.canalServer = new CanalServerWithEmbedded();
        this.canalServer.setCanalInstanceGenerator(new CanalInstanceGenerator() {

            @Override
            public CanalInstance generate(String destination) {
                final boolean isMultiplexingRead = isMultiplexingRead();
                final Canal canal = CanalReaderConfigGenerator.buildCanalConfig(destination, parameter, context, readerType, taskShadow);
                final CanalReaderAlarmHandler alarmHandler = new CanalReaderAlarmHandler(context.taskId());

                CanalInstanceWithManager instance = new CustomCanalInstanceWithManager(canal, filter) {

                    @Override
                    protected void initMetaManager() {
                        CanalReaderMetaManager canalReaderMetaManager = new CanalReaderMetaManager(taskPositionManager, CanalReader.this);
                        canalReaderMetaManager.setFilter(CanalReader.this.filter);
                        metaManager = canalReaderMetaManager;
                    }

                    @Override
                    protected void startEventParserInternal(CanalEventParser parser, boolean isGroup) {
                        if (parser instanceof MysqlEventParser) {
                            MysqlEventParser mysqlEventParser = (MysqlEventParser) parser;
                            mysqlEventParser.setSupportBinlogFormats("ROW,STATEMENT,MIXED");
                            mysqlEventParser.setSupportBinlogImages("FULL,MINIMAL,NOBLOB");
                        }

                        super.startEventParserInternal(parser, isGroup);
                    }

                    @Override
                    protected void initEventSink() {
                        if (isMultiplexingRead) {
                            initEventSinkMultiplexing();
                        } else {
                            initEventSinkNormal();
                        }
                    }

                    @Override
                    protected void initEventParser() {
                        if (isMultiplexingRead) {
                            logger.info("init eventParser begin...");
                            eventParser = new FakeEventParser(canal, this, () -> kickOut(), filter);
                            logger.info("init eventParser end! \n\t load CanalEventParser:{}", eventParser.getClass().getName());
                        } else {
                            super.initEventParser();
                        }
                    }

                    private void initEventSinkNormal() {
                        logger.info("init eventSink begin...");

                        int groupSize = getGroupSize();
                        if (groupSize <= 1) {
                            eventSink = new EntryEventSink();
                        } else {
                            if (parameter.getGroupSinkMode() == GroupSinkMode.Coordinate) {
                                eventSink = new GroupEventSink(groupSize);
                                ((GroupEventSink) eventSink).setEmptyTransactionInterval(10L);//ms
                                ((GroupEventSink) eventSink).setEmptyTransctionThresold(10L);
                            } else {
                                eventSink = new EntryEventSink();
                            }
                        }

                        if (eventSink instanceof EntryEventSink) {
                            ((EntryEventSink) eventSink).setFilterTransactionEntry(false);
                            ((EntryEventSink) eventSink).setEventStore(getEventStore());
                            ((EntryEventSink) eventSink).addHandler(handler);
                        }

                        logger.info("init eventSink end! \n\t load CanalEventSink:{}", eventSink.getClass().getName());
                    }

                    private void initEventSinkMultiplexing() {
                        logger.info("init eventSink begin...");
                        eventSink = new FakeEventSink(canal, this);
                        logger.info("init eventSink end! \n\t load CanalEventSink:{}", eventSink.getClass().getName());
                    }

                    private int getGroupSize() {
                        List<List<CanalParameter.DataSourcing>> groupDbAddresses = parameters.getGroupDbAddresses();
                        if (!org.springframework.util.CollectionUtils.isEmpty(groupDbAddresses)) {
                            return groupDbAddresses.get(0).size();
                        } else {
                            // 可能是基于tddl的启动
                            return 1;
                        }
                    }
                };

                instance.setAlarmHandler(alarmHandler);
                return instance;
            }
        });

        //构造instance的时候已经指定了filter，clientIdentity就不需要指定filter了
        this.clientIdentity = new ClientIdentity(destination, (short) 1001, "");
        this.canalServer.start();
        this.startDestination();

        running = true;
    }

    void shutdown() {
        if (handler != null) {
            try {
                handler.stop();
            } catch (Exception e) {
                logger.warn("failed destory handler", e);
            }
            handler = null;
        }

        //停止的时候可能Server从来没有启动过，所以加非空校验
        if (canalServer != null) {
            canalServer.stop(destination);
            canalServer.stop();
        }

        running = false;
    }

    /**
     * 未取到数据则返回null
     */
    CanalReaderMsg get() throws InterruptedException {
        checkBeforeGet();
        CanalReaderMsg result = doGet();
        if (result != null) {
            checkAfterGet(result);
        }
        return result;
    }

    void ack(long batchId, boolean effectSync) {
        this.canalServer.ack(clientIdentity, batchId);
        if (effectSync) {
            this.effectSyncPosition = new CanalReaderEffectSyncPosition(
                    latestReaderMsg.getLogFileName(),
                    latestReaderMsg.getLogFileOffset()
            );
        }
    }

    void rollback(long batchId) {
        this.canalServer.rollback(clientIdentity, batchId);
    }

    void dump() {
        if (logger.isInfoEnabled()) {
            Message message = (Message) latestReaderMsg.getMetaData().get(MESSAGE_KEY);
            String startPosition = null;
            String endPosition = null;
            if (!CollectionUtils.isEmpty(message.getEntries())) {
                startPosition = CanalReaderRecordsDumper.buildPositionForDump(message.getEntries().get(0));
                endPosition = CanalReaderRecordsDumper.buildPositionForDump(message.getEntries().get(message.getEntries().size() - 1));
            }

            //dump for troubleshoot problems
            CanalReaderMessageDumper.dumpMessages(message, latestReaderMsg.getBatchId(), message.getEntries().size());
            CanalReaderRecordsDumper.dumpRecords(latestReaderMsg.getBatchId(), latestReaderMsg.getRecords(),
                    startPosition, endPosition, message.getEntries().size(), parameter.isDumpDetail());
        }
    }

    private void startDestination() {
        this.canalServer.start(destination);
        this.canalServer.subscribe(clientIdentity);
    }

    private void stopDestination() {
        this.canalServer.stop(destination);
    }

    private void checkBeforeGet() {
        checkRestart();
    }

    private CanalReaderMsg doGet() throws InterruptedException {
        Message message;
        try {
            if (batchTimeout < 0) {
                message = canalServer.getWithoutAck(clientIdentity, batchSize);
            } else { // 超时控制
                message = canalServer.getWithoutAck(clientIdentity, batchSize, batchTimeout, TimeUnit.MILLISECONDS);
            }
        } catch (ZkInterruptedException e) {
            throw new InterruptedException();
        }

        if (message == null || message.getId() == -1L) {
            latestReaderMsg = null;
            return null;
        } else {
            // 获取第一个的entry时间，包括被过滤的数据
            // 获取该批次数据对应的binlog日志大小
            long payloadSize = 0;
            String logFileName = null;
            long logFileOffset = 0;
            if (!org.springframework.util.CollectionUtils.isEmpty(message.getEntries())) {
                payloadSize = message.getEntries().stream().mapToLong(i -> i.getHeader().getEventLength()).summaryStatistics().getSum();
                logFileName = message.getEntries().get(0).getHeader().getLogfileName();
                logFileOffset = message.getEntries().get(0).getHeader().getLogfileOffset();
            }

            latestReaderMsg = new CanalReaderMsg(
                    message.getId(),
                    logFileName,
                    logFileOffset,
                    calcFirstEntryTime(message),
                    payloadSize,
                    messageParser.parse(message.getEntries(), context)
                            .stream()
                            .filter(r -> !parameter.getFilteredEventTypes().contains(r.getEventType()))
                            .collect(Collectors.toList())
            );
            if (parameter.isDump()) {
                latestReaderMsg.getMetaData().put(MESSAGE_KEY, message);
            }
            return latestReaderMsg;
        }
    }

    private void checkAfterGet(CanalReaderMsg canalReaderMsg) {
        if (hasKickoutMultiplexingRead && (System.currentTimeMillis() - canalReaderMsg.getFirstEntryTime() < 1000 * 30)
                && needRestartDestination.compareAndSet(false, true)) {
            hasKickoutMultiplexingRead = false;
        }
    }

    private void checkRestart() {
        if (needRestartDestination.compareAndSet(true, false)) {
            logger.info("restart desination begin.");
            stopDestination();
            startDestination();
            logger.info("restart destination end.");
        }
    }

    private boolean isMultiplexingRead() {
        SysPropertiesInfo sysPropertiesInfo = context.getService(SysPropertiesService.class)
                .getSysPropertiesByKey("multiplexingReadGlobal");
        MediaSourceInfo mediaSrc = context.getService(MediaService.class)
                .getMediaSourceById(parameter.getMediaSourceId());

        boolean result = sysPropertiesInfo != null && "true".equals(sysPropertiesInfo.getPropertiesValue()) &&
                parameter.isMultiplexingRead() && mediaSrc.getType() != MediaSourceType.SDDL && !hasKickoutMultiplexingRead;
        logger.info("multiplexing read switch for destination {} is {}.", destination, result);
        return result;
    }

    /**
     * <p>
     * 设计之初，firstEntryTime的获取方式为：message.getEntries().get(0).getHeader().getExecuteTime()
     * 这样获取不够精确，在有大事务或者长事务时，按此取到的firstEntryTime和事务最终提交的时间会有比较大的差距，
     * 此时，我们通过firstEntryTime去计算延迟时间的时候，算出来的值就会比较大而触发报警。
     * <p>
     * 为什么按此取到的firstEntryTime和事务最终提交的时间会有比较大的差距呢？是因为mysql对于每个操作事件，
     * 以其真正发生的时间作为ExecuteTime，不会因为在一个事务里，而以提交时间为准，如下所示：
     * TRANSACTIONBEGIN   2018-01-01 10:00:00
     * INSERT             2018-01-01 10:01:00
     * UPDATE             2018-01-01 10:02:00
     * TRANSACTIONEND     2018-01-01 10:03:00
     * </p>
     * <p>
     * <p>
     * 而应该以第一个TRANSACTIONEND的时间为准，因为TRANSACTIONEND对应的是最终的commit，代表了实际发生
     * 如果此批次没有TRANSACTIONEND，说明该批次所有数据都在一个事务里，则以最后一条的时间为准
     * 这么改，主要是解决了长事务的问题，此处所说的长事务，是指一个事务从begin到end的跨度比较长，但中间不一定
     * 有非常多的操做，并且这些操作的间隔时间比较长。
     * </p>
     *
     * @param message
     * @return
     */
    private long calcFirstEntryTime(Message message) {
        CanalEntry.Entry lastEntry = null;
        for (CanalEntry.Entry entry : message.getEntries()) {
            lastEntry = entry;
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                break;
            }
        }

        return lastEntry.getHeader().getExecuteTime();
    }

    public CanalReaderEffectSyncPosition getEffectSyncPosition() {
        return effectSyncPosition;
    }

    public CanalReaderType getReaderType() {
        return readerType;
    }

    public TaskShadowInfo getTaskShadow() {
        return taskShadow;
    }
}
