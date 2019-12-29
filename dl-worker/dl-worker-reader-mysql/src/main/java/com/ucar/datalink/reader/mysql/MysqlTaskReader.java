package com.ucar.datalink.reader.mysql;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.biz.service.TaskShadowService;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderParameter;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.plugin.reader.mysql.MysqlReaderPosition;
import com.ucar.datalink.domain.task.TaskShadowInfo;
import com.ucar.datalink.reader.mysql.utils.Constants;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * A implemention for reading binlogs from mysql.
 * <p>
 * 关于TaskShadow的说明：
 * 1. TaskShadow用来对Task的部分mapping进行单独处理而不影响其它mapping的正常同步，使用场景如：追数据、补数据
 * 2. 当没有待执行的taskShadow时，MainCanalReader负责执行Task的所有mapping映射
 * 3. 当存在待执行的taskShadow时，MainCanalReader负责执行taskShadow之外的mapping映射，ShadowCanalReader负责执行taskShadow指定的mapping映射
 * 4. 创建taskShadow时，指定的时间戳不能大于MainPosition的Timestamp的值，否则会有丢数据的风险，程序处理时发现这样的情况会将TaskShadow的状态主动置为DISCARD
 * > 如：Main恰好正在追数据，假设追到了10:00，此时发起了一次shadow请求指定的是从11:00开始取binlog，那么10:00到11:00之间shadow中指定的mapping数据就会丢失
 * 5. 当MainCanalReader和ShadowCanalReader的位点相差无几的时候(默认10s)，Task会结束本次shadow并进行自动重启，重启时以main和shadow中最小的时间戳进行位点初始化
 * 6. Task会不断轮询是否有新的TaskShadow请求，如果有的话自动触发Shadow任务(TaskShadow会按请求顺序，被线性处理)
 * 7. 执行Shadow任务期间，mainCanalReader的位点可任意切换，当main和shadow进行merge后，数据总会最终一致
 * <p>
 * Created by lubiao on 2017/2/6.
 */
@SuppressWarnings("unchecked")
public class MysqlTaskReader extends TaskReader<MysqlReaderParameter, RdbEventRecord> {
    private static final Logger logger = LoggerFactory.getLogger(MysqlTaskReader.class);
    private static final int MAX_EMPTY_TIMES = 10;
    private static final int SHADOW_CHECK_INTERVAL = 10000;//单位ms

    private TaskShadowInfo taskShadow;
    private MysqlTaskPositionManager taskPositionManager;
    private CanalReader mainCanalReader;
    private CanalReader shadowCanalReader;
    private CanalReaderSimulator canalReaderSimulator;
    private long lastShadowCheckTime;

    @Override
    public void start() {
        if (isStart()) {
            return;
        }
        startInternal();
        super.start();
    }

    @Override
    public void close() {
        stopInternal();
    }

    @Override
    public RecordChunk<RdbEventRecord> fetch() throws InterruptedException {
        checkRestart();

        int emptyTimes = 0;
        CanalReaderMsg mainMsg = null;
        CanalReaderMsg shadowMsg = null;

        while (isStart()) {
            mainMsg = mainCanalReader.get();
            if (shadowCanalReader != null) {
                shadowMsg = shadowCanalReader.get();
            }
            if (shadowCanalReader != null && mainMsg == null && shadowMsg == null) {
                applyWait(emptyTimes++);
            } else if (shadowCanalReader == null && mainMsg == null) {
                applyWait(emptyTimes++);
            } else {
                break;
            }
        }
        if (!isStart()) {
            throw new InterruptedException();
        }

        RecordChunk<RdbEventRecord> result;
        if (mainMsg != null) {
            result = new RecordChunk<>(mainMsg.getRecords(), mainMsg.getFirstEntryTime(), mainMsg.getPayloadSize());
            result.putMetaData(Constants.MAIN_BATCH_ID, mainMsg.getBatchId());
            if (shadowMsg != null) {
                result.getRecords().addAll(shadowMsg.getRecords());
                result.setPayloadSize(mainMsg.getPayloadSize() + shadowMsg.getPayloadSize());
                result.putMetaData(Constants.SHADOW_BATCH_ID, shadowMsg.getBatchId());
            }
        } else {
            if (shadowMsg != null) {
                //为了避免延迟报警，firstEntrytime的时间设置为当前时间
                result = new RecordChunk<>(shadowMsg.getRecords(), System.currentTimeMillis(), shadowMsg.getPayloadSize());
                result.putMetaData(Constants.SHADOW_BATCH_ID, shadowMsg.getBatchId());
            } else {
                throw new IllegalStateException("illegal state : both mainMsg and shadowMsg are null.");
            }
        }

        List<RdbEventRecord> simulateRecords = canalReaderSimulator.get();
        if (simulateRecords != null) {
            result.getRecords().addAll(simulateRecords);
        }
        return result;
    }

    @Override
    public void commit(RecordChunk<RdbEventRecord> recordChunk) throws InterruptedException {
        Long mainBatchId = recordChunk.getMetaData(Constants.MAIN_BATCH_ID);
        Long shadowBatchId = recordChunk.getMetaData(Constants.SHADOW_BATCH_ID);

        canalReaderSimulator.ack();
        if (mainBatchId != null) {
            mainCanalReader.ack(mainBatchId, !recordChunk.getRecords().isEmpty());
        }
        if (shadowBatchId != null) {
            shadowCanalReader.ack(shadowBatchId, !recordChunk.getRecords().isEmpty());
        }
    }

    @Override
    public void rollback(RecordChunk<RdbEventRecord> recordChunk, Throwable t) {
        Long mainBatchId = recordChunk.getMetaData(Constants.MAIN_BATCH_ID);
        Long shadowBatchId = recordChunk.getMetaData(Constants.SHADOW_BATCH_ID);

        canalReaderSimulator.rollback();
        if (mainBatchId != null) {
            mainCanalReader.rollback(mainBatchId);
        }
        if (shadowBatchId != null) {
            shadowCanalReader.rollback(shadowBatchId);
        }
    }

    @Override
    protected void dump(RecordChunk<RdbEventRecord> recordChunk) {
        Long mainBatchId = recordChunk.getMetaData(Constants.MAIN_BATCH_ID);
        Long shadowBatchId = recordChunk.getMetaData(Constants.SHADOW_BATCH_ID);

        if (mainBatchId != null) {
            mainCanalReader.dump();
        }
        if (shadowBatchId != null) {
            shadowCanalReader.dump();
        }
    }

    private void prepareTaskShadow() {
        TaskShadowService taskShadowService = context.getService(TaskShadowService.class);
        TaskShadowInfo executingTaskShadow = taskShadowService.getExecutingTaskShadow(Long.valueOf(context.taskId()));
        if (executingTaskShadow != null) {
            this.taskShadow = executingTaskShadow;
            logger.info("find an executing task shadow, and the content is {}", JSONObject.toJSONString(taskShadow));
        } else {
            TaskShadowInfo initTaskShadow = taskShadowService.takeOneTaskShadow(Long.valueOf(context.taskId()));
            if (initTaskShadow != null) {
                MysqlReaderPosition currentPosition = context.positionManager().getPosition(context.taskId());
                if (currentPosition == null || currentPosition.getTimestamp() < initTaskShadow.getParameterObj().getTimeStamp()) {
                    logger.info("find an initial task shadow but it is invalid with current position, prepare to discard it." +
                                    "The current position is {} ,shadow content is {}.",
                            currentPosition == null ? "NULL" : JSONObject.toJSONString(currentPosition),
                            JSONObject.toJSONString(initTaskShadow));
                    taskShadowService.discardTaskShadow(initTaskShadow.getId());
                } else {
                    logger.info("find an initial task shadow and prepare to start it, the shadow content is {}", JSONObject.toJSONString(initTaskShadow));
                    this.taskShadow = initTaskShadow;
                    taskShadowService.startTaskShadow(taskShadow);
                }
            }
        }

        if (taskShadow == null) {
            logger.info("there is no task shadow to process.");
        } else {
            if (!context.taskId().equals(taskShadow.getTaskId().toString())) {
                throw new IllegalArgumentException(String.format("taskshadow`s task id is mismatch with mysql reader task, %s vs %s",
                        taskShadow.getTaskId(), context.taskId()));
            }
        }
    }

    private void startInternal() {
        //初始化
        prepareTaskShadow();
        taskPositionManager = new MysqlTaskPositionManager(context.taskId(), context.positionManager(), taskShadow);
        mainCanalReader = new CanalReader(context.taskId() + "-main", context, parameter, taskPositionManager, CanalReaderType.MAIN, taskShadow);
        if (taskShadow != null) {
            shadowCanalReader = new CanalReader(context.taskId() + "-shadow", context, parameter, taskPositionManager, CanalReaderType.SHADOW, taskShadow);
        }
        canalReaderSimulator = new CanalReaderSimulator(context);
        lastShadowCheckTime = System.currentTimeMillis();

        //启动
        taskPositionManager.start();
        mainCanalReader.start();
        if (shadowCanalReader != null) {
            shadowCanalReader.start();
        }
        canalReaderSimulator.start();
    }

    private void stopInternal() {
        //停止
        mainCanalReader.shutdown();
        if (shadowCanalReader != null) {
            shadowCanalReader.shutdown();
        }
        taskPositionManager.shutdown();
        canalReaderSimulator.shutdown();

        //重置
        mainCanalReader = null;
        shadowCanalReader = null;
        taskPositionManager = null;
        canalReaderSimulator = null;
        taskShadow = null;
        lastShadowCheckTime = 0;
    }

    private void restartInternal() {
        logger.info("canal reader restart begin.");
        stopInternal();
        startInternal();
        logger.info("canal reader restart end.");
    }

    private void checkRestart() {
        if (taskShadow != null && taskPositionManager.isCanMerge()) {
            logger.info("find a merge instruction, prepare to restart canal reader.");
            TaskShadowService taskShadowService = context.getService(TaskShadowService.class);
            taskShadowService.completeTaskShadow(taskShadow);
            restartInternal();
            return;
        }

        if (taskShadow == null) {
            try {
                long interval = System.currentTimeMillis() - lastShadowCheckTime;
                if (interval >= SHADOW_CHECK_INTERVAL) {
                    TaskShadowService taskShadowService = context.getService(TaskShadowService.class);
                    if (taskShadowService.takeOneTaskShadow(Long.valueOf(context.taskId())) != null) {
                        logger.info("find a shadow request, prepare to restart canal reader.");
                        restartInternal();
                    } else {
                        lastShadowCheckTime = System.currentTimeMillis();
                    }
                }
            } catch (Throwable t) {
                //出现异常打日志，但不抛出，保证即使出现datalink数据库不可用时，任务也能正常执行
                logger.error("something goes wrong when do shadow check.", t);
            }
        }
    }

    /**
     * 处理无数据的情况，避免空循环挂死
     */
    private void applyWait(int emptyTimes) {
        int newEmptyTimes = emptyTimes > MAX_EMPTY_TIMES ? MAX_EMPTY_TIMES : emptyTimes;
        if (emptyTimes <= 3) { // 3次以内
            Thread.yield();
        } else { // 超过3次，最多只sleep 10ms
            LockSupport.parkNanos(1000 * 1000L * newEmptyTimes);
        }
    }
}
