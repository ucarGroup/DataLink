package com.ucar.datalink.worker.api.intercept;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.worker.api.task.TaskWriterContext;

/**
 * 记录拦截器,当为MediaMappingInfo配置了interceptor时，系统会调用该interceptor
 * <p>
 * 可以在拦截器中进行数据过滤和相关的转换操作,多个mapping可能会复用同一个拦截器，所以拦截器并不是线程安全的
 * <p>
 * Created by lubiao on 2017/3/22.
 */
public interface Interceptor<T extends Record> {
    /**
     * @param record
     * @return return null,if the record  need filtering.
     */
    T intercept(T record, TaskWriterContext context);
}
