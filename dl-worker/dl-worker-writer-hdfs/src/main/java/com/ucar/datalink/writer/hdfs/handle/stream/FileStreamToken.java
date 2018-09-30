package com.ucar.datalink.writer.hdfs.handle.stream;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;


/**
 * HDFS文件流令牌类
 *
 * @author lubiao
 */
public class FileStreamToken {

    private volatile String pathString;
    private volatile Path path;
    private volatile DistributedFileSystem fileSystem;
    private volatile FSDataOutputStream fileStream;
    private volatile long lastUpdateTime;
    private volatile long lastHSyncTime;

    public FileStreamToken(String pathString, Path path, DistributedFileSystem fileSystem, FSDataOutputStream fileStream) {
        this.pathString = pathString;
        this.path = path;
        this.fileSystem = fileSystem;
        this.fileStream = fileStream;
        this.lastUpdateTime = System.currentTimeMillis();
        this.lastHSyncTime = 0;
    }

    public String getPathString() {
        return pathString;
    }

    public void setPathString(String pathString) {
        this.pathString = pathString;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public DistributedFileSystem getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(DistributedFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public FSDataOutputStream getFileStream() {
        return fileStream;
    }

    public void setFileStream(FSDataOutputStream fileStream) {
        this.fileStream = fileStream;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public long getLastHSyncTime() {
        return lastHSyncTime;
    }

    public void setLastHSyncTime(long lastHSyncTime) {
        this.lastHSyncTime = lastHSyncTime;
    }
}
