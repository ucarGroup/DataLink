package com.ucar.datalink.worker.api.intercept;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.task.TaskWriterContext;


/**
 * 对DdlEventRecord的默认处理器
 * 如果此拦截器拦截到了DdlEventRecord，则说明对应的writer插件对ddl没有做处理，那么直接丢弃该record
 *
 * Created by lubiao on 2017/7/21.
 */
public class BuiltInDdlEventInterceptor implements Interceptor {

    @Override
    public Record intercept(Record record, TaskWriterContext context) {
        if ((record instanceof RdbEventRecord) &&
                ((RdbEventRecord) record).getEventType().isDdl()) {
            return null;
        }
        return record;
    }
}
