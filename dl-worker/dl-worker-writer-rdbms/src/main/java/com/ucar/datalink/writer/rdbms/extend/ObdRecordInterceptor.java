package com.ucar.datalink.writer.rdbms.extend;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

/**
 * Created by lubiao on 2017/7/18.
 */
public class ObdRecordInterceptor implements Interceptor<RdbEventRecord> {

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        String tableName = record.getTableName();
        if ("t_scd_vehicle".equals(tableName) && record.getEventType().equals(EventType.INSERT)) {
            EventColumn column = getEventColumn(record, "part_time_driver");
            if (!("0".equals(column.getColumnValue()) || "2".equals(column.getColumnValue()) || "4".equals(column.getColumnValue()))) {
                return null;
            }
        } else if ("t_vehicle_state".equals(tableName) && record.getEventType().equals(EventType.INSERT)) {
            EventColumn column = getEventColumn(record, "register_time");
            if (!StringUtils.isBlank(column.getColumnValue())) {
                return null;
            }
        } else if ("t_scd_vehicle_model".equals(tableName) && record.getEventType().equals(EventType.INSERT)) {
            EventColumn column1 = getEventColumn(record, "is_self");
            EventColumn column2 = getEventColumn(record, "status");
            if (!("1".equals(column1.getColumnValue()) && "1".equals(column2.getColumnValue()))) {
                return null;
            }
        }

        return record;
    }

    private EventColumn getEventColumn(RdbEventRecord record, String columnName) {
        Optional<EventColumn> optional = record.getColumns().stream().filter(i -> i.getColumnName().equals(columnName)).findFirst();
        return optional.isPresent() ? optional.get() : null;
    }
}
