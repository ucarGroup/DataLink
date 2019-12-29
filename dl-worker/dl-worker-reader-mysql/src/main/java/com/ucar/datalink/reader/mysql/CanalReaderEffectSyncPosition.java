package com.ucar.datalink.reader.mysql;

/**
 * 区别于MysqlReaderPosition中的journalName和position，CanalReaderEffectSyncPosition代表实际发生同步的消费位点
 * 何谓"实际发生同步的消费位点"，举例说明：
 *      1. 数据库A有两张表，T1和T2
 *      2. 一个同步任务配置了一条映射，将T1从A库同步到B库
 *      3. 假设当前binlog已经消费到了12:00，但T1在A库的最后写入时间是10:00
 *      4. 那么MysqlReaderPosition中的journalName和position的值对应的是12:00，CanalReaderEffectSyncPosition对应的则是10:00
 *      5. 即EffectSyncPosition存储的是最后一次实际发生同步的位点，10:00以后不再有T1的数据，所以停留在了10:00这一时刻
 *
 * Created by lubiao on 2018/12/17.
 */
public class CanalReaderEffectSyncPosition {
    private String latestEffectSyncLogFileName;
    private long latestEffectSyncLogFileOffset;

    public CanalReaderEffectSyncPosition(String latestEffectSyncLogFileName, long latestEffectSyncLogFileOffset) {
        this.latestEffectSyncLogFileName = latestEffectSyncLogFileName;
        this.latestEffectSyncLogFileOffset = latestEffectSyncLogFileOffset;
    }

    public String getLatestEffectSyncLogFileName() {
        return latestEffectSyncLogFileName;
    }

    public void setLatestEffectSyncLogFileName(String latestEffectSyncLogFileName) {
        this.latestEffectSyncLogFileName = latestEffectSyncLogFileName;
    }

    public long getLatestEffectSyncLogFileOffset() {
        return latestEffectSyncLogFileOffset;
    }

    public void setLatestEffectSyncLogFileOffset(long latestEffectSyncLogFileOffset) {
        this.latestEffectSyncLogFileOffset = latestEffectSyncLogFileOffset;
    }
}
