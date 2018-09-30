package com.ucar.datalink.writer.hdfs.handle.stream;

import com.ucar.datalink.common.errors.TaskClosedException;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.writer.hdfs.handle.config.HdfsConfig;
import com.ucar.datalink.writer.hdfs.handle.config.HdfsConfigManager;
import com.ucar.datalink.writer.hdfs.handle.util.FileLockUtils;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * OutStream-Holder
 *
 * @author lubiao
 */
public class FileStreamHolder {

    private static final Logger logger = LoggerFactory.getLogger(FileStreamHolder.class);

    private final Map<String, FileStreamToken> tokens;
    private final String taskId;
    private final HdfsWriterParameter hdfsWriterParameter;
    private final ReentrantReadWriteLock readWriteLock;
    private volatile boolean running;

    public FileStreamHolder(String taskId, HdfsWriterParameter hdfsWriterParameter) {
        this.tokens = new ConcurrentHashMap<>();
        this.taskId = taskId;
        this.hdfsWriterParameter = hdfsWriterParameter;
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    public void start() {
        this.running = true;
        FileStreamKeeper.register(this);
        logger.info("FileStreamHolder is started.");
    }

    /**
     * close方法和getStreamToken方法会被不同的线程调用，tokens存在并发问题，引入读写锁，进行并发处理
     * 具体的并发场景(问题)为：
     * close方法被TaskWriter主线程调用，getStreamToken方法被AbstractHandler的executorService线程调用
     * 如果不进行并发控制，可能会导致close执行结束后，仍然有新的FileStreamToken(ps：假设其对应的pathString名字为p1)加入进来，那么新加入的token对应的FileStream没有机会被关闭
     * 待Task重新启动之后，会实例化一个新的FileStreamHolder对象，新对象的tokens里面并没有p1对应的FileStreamToken，那么会尝试去创建，因为之前的那个FileStream没有被关闭，
     * 本次创建会导致hdfs的租约异常：because current leaseholder is trying to recreate file，并且无法自动恢复.
     * <p>
     * ps：close方法被调用的时候TaskWriter主线程都已经接近尾声了，AbstractHandler的executorService为什么还有线程在运行？？
     * 参见AbstractHandler的submitAndWait方法，我们用到了ExecutorCompletionService，当出现异常的时候，会尝试执行Future.cancel,但这并不会导致线程的
     * 立即结束(参见Thread.interrupt()方法)，所以当主线程接近尾声的时候，线程池中的操作是有可能还未结束的
     */
    public void close() {
        try {
            this.readWriteLock.writeLock().lock();
            this.running = false;
            if (this.tokens.size() > 0) {
                this.tokens.keySet().forEach(this::closeStreamToken);
            }
        } finally {
            FileStreamKeeper.unRegister(this);
            this.readWriteLock.writeLock().unlock();
        }
        logger.info("FileStreamHolder is closed.");
    }

    public FileStreamToken getStreamToken(String pathString, MediaSourceInfo mediaSourceInfo)
            throws Exception {
        try {
            this.readWriteLock.readLock().lock();
            if (!running) {
                throw new TaskClosedException("FileStreamHolder has closed, StreamToken gotten failed.");
            }

            return getStreamTokenInternal(pathString, mediaSourceInfo);
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    private FileStreamToken getStreamTokenInternal(String pathString, MediaSourceInfo mediaSourceInfo)
            throws Exception {
        HdfsConfig hdfsConfig = HdfsConfigManager.getHdfsConfig(mediaSourceInfo, hdfsWriterParameter);
        DistributedFileSystem hadoopFS = (DistributedFileSystem) FileSystemManager.getFileSystem(hdfsConfig);

        ReentrantLock lock = FileLockUtils.getLock(pathString);
        try {
            lock.lock();
            FileStreamToken token = tokens.get(pathString);
            if (token == null) {
                FSDataOutputStream fileStream;
                Path path = new Path(pathString);

                if (!hadoopFS.exists(path)) {
                    fileStream = hadoopFS.create(path, false,
                            hdfsConfig.getConfiguration().getInt(CommonConfigurationKeysPublic.IO_FILE_BUFFER_SIZE_KEY,
                                    CommonConfigurationKeysPublic.IO_FILE_BUFFER_SIZE_DEFAULT),
                            (short) 3, 64 * 1024 * 1024L);
                    logger.info("stream create succeeded for file : " + pathString);
                } else {
                    fileStream = hadoopFS.append(path);
                    logger.info("stream append succeeded for file : " + pathString);
                }

                token = new FileStreamToken(pathString, path, hadoopFS, fileStream);
                tokens.put(pathString, token);
            }

            return token;
        } finally {
            lock.unlock();
        }
    }

    public void closeStreamToken(String pathString) {
        ReentrantLock lock = FileLockUtils.getLock(pathString);
        try {
            lock.lock();
            FileStreamToken vo = tokens.get(pathString);
            if (vo != null) {
                boolean removeToken = true;
                try {
                    vo.getFileStream().close();
                    logger.info("stream close succeeded for file : " + pathString);
                } catch (Throwable e) {
                    logger.error("stream close failed for file : " + pathString, e);
                    try {
                        vo.getFileSystem().recoverLease(vo.getPath());
                        logger.info("lease recover succeeded for file : " + pathString);
                    } catch (Exception ex) {
                        //RecoverLease失败的情况下，token不能被remove，否则将引发Lease的ReCreate或OtherCreate问题
                        removeToken = false;
                        logger.error("lease recover failed for file : " + pathString, ex);
                    }
                } finally {
                    if (removeToken) {
                        tokens.remove(pathString);
                    }
                }
            }
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    void closeLeisureStreamTokens() {
        tokens.entrySet().stream().forEach(entry -> {
            try {
                FileStreamToken vo = entry.getValue();
                if (vo.getLastUpdateTime() + hdfsWriterParameter.getStreamLeisureLimit() < System.currentTimeMillis()) {
                    closeStreamToken(entry.getKey());//超时关闭
                }
            } catch (Throwable t) {
                logger.error("leisure stream close failed for file : " + entry.getKey());
            }
        });
    }

    void closeAllStreamTokens() {
        if (this.tokens.size() > 0) {
            this.tokens.keySet().forEach(this::closeStreamToken);
        }
    }

    int tokenSize() {
        return tokens.size();
    }

    public String getTaskId() {
        return taskId;
    }
}
