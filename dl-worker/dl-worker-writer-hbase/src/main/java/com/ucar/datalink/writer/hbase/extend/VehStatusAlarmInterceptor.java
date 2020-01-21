package com.ucar.datalink.writer.hbase.extend;

import com.ucar.datalink.contract.log.hbase.HColumn;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import org.apache.hadoop.hbase.util.Bytes;
import java.util.List;

public class VehStatusAlarmInterceptor implements Interceptor<HRecord> {
    @Override
    public HRecord intercept(HRecord record, TaskWriterContext context) {
        List<HColumn> list = record.getColumns();
        for(HColumn c : list) {
            String qualifier_name = Bytes.toString(c.getQualifier());
            if("reportTime".equals(qualifier_name)) {
                long v = Bytes.toLong(c.getValue());
                c.setValue( Bytes.toBytes(""+v) );
            }
        }
        return record;
    }
}
