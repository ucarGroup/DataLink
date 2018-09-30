package com.ucar.datalink.writer.rdbms;

import com.ucar.datalink.contract.Record;
import com.ucar.datalink.domain.plugin.writer.rdbms.RdbmsWriterParameter;
import com.ucar.datalink.worker.api.handle.Handler;
import com.ucar.datalink.worker.api.task.TaskWriter;
import com.ucar.datalink.writer.rdbms.handle.RdbEventRecordHandler;

/**
 * 将其它数据源数据导入到关系型数据库.
 * <p>
 * Created by lubiao on 2017/3/3.
 */
public class RdbmsTaskWriter extends TaskWriter<RdbmsWriterParameter> {

    @Override
    protected Handler getHandler(Class<? extends Record> clazz) {
        return new RdbEventRecordHandler();
    }
}
