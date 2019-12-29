package com.ucar.datalink.reader.mysql;

import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.reader.mysql.taskdecorate.TaskDecoreteUtils;
import com.ucar.datalink.worker.api.task.TaskReaderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by lubiao on 2019/8/5.
 */
public class CanalReaderSimulator {
    private static final Logger logger = LoggerFactory.getLogger(CanalReaderSimulator.class);

    private final TaskReaderContext context;
    private final AtomicBoolean taskDecoratedByStart;

    public CanalReaderSimulator(TaskReaderContext context) {
        this.context = context;
        this.taskDecoratedByStart = new AtomicBoolean(true);
    }

    void start() {
        // do nothing
    }

    void shutdown() {
        // do nothing
    }

    List<RdbEventRecord> get() {
        if (taskDecoratedByStart.get()) {
            try {
                return TaskDecoreteUtils.getRecord(this.context);
            } catch (Exception e) {
                logger.error("getTaskDecoratedRecords error:", e);
            }
        }
        return null;
    }

    void ack() {
        if (taskDecoratedByStart.get()) {
            taskDecoratedByStart.set(false);
            try {
                TaskDecoreteUtils.saveExecuteLog(this.context, false, "执行commit，具体补录参看细节!");
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    void rollback() {
        if (taskDecoratedByStart.get()) {
            taskDecoratedByStart.set(false);
            try {
                TaskDecoreteUtils.saveExecuteLog(this.context, true, "执行rollback，异常未知!");
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }
}
