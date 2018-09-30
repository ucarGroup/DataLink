package com.ucar.datalink.worker.api.util.priority;

/**
 * Created by lubiao on 2018/5/13.
 */
public interface PriorityTaskCallback<T> {

    void call(PriorityBucket<T> bucket);
}
