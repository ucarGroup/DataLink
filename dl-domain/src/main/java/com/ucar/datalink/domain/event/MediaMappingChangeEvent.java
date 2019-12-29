package com.ucar.datalink.domain.event;

import com.ucar.datalink.common.event.CallbackEvent;
import com.ucar.datalink.common.utils.FutureCallback;

/**
 * Created by qianqian.shi on 2018/8/23.
 */
public class MediaMappingChangeEvent extends CallbackEvent{

    private Long taskId;

    public MediaMappingChangeEvent(FutureCallback callback, Long taskId) {
        super(callback);
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
