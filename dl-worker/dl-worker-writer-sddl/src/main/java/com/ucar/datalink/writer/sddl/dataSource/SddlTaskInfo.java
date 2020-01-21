package com.ucar.datalink.writer.sddl.dataSource;

import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.task.TaskInfo;

/**
 * @Description:
 * @Author : yongwang.chen@ucarinc.com
 * @Created: 20/11/2017.
 */
public class SddlTaskInfo {

    private TaskInfo taskInfo;
    private MediaSourceInfo sddlMediaSourceInfo; // sddl类型的mediasource
    private MediaSourceInfo readerMediaSourceInfo;
    private boolean isProxyDb;


    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public MediaSourceInfo getSddlMediaSourceInfo() {
        return sddlMediaSourceInfo;
    }

    public void setSddlMediaSourceInfo(MediaSourceInfo sddlMediaSourceInfo) {
        this.sddlMediaSourceInfo = sddlMediaSourceInfo;
    }

    public MediaSourceInfo getReaderMediaSourceInfo() {
        return readerMediaSourceInfo;
    }

    public void setReaderMediaSourceInfo(MediaSourceInfo readerMediaSourceInfo) {
        this.readerMediaSourceInfo = readerMediaSourceInfo;
    }

    public boolean isProxyDb() {
        return isProxyDb;
    }

    public void setProxyDb(boolean proxyDb) {
        isProxyDb = proxyDb;
    }
}
