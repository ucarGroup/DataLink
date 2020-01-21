package com.ucar.datalink.writer.rdbms.extend;

import com.google.common.collect.Sets;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;

import java.util.Set;

/**
 * Created by lubiao on 2018/2/12.
 */
public class YccOrder2EsInterceptor implements Interceptor<RdbEventRecord> {

    private static Set<String> tables = Sets.newHashSet("t_rap_order", "t_vins_order", "t_mt_order", "t_peccancy_order");

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        String tableName = record.getTableName();
        EventType eventType = record.getEventType();

        if (tables.contains(tableName)) {
            if (EventType.INSERT.equals(eventType) || EventType.UPDATE.equals(eventType)) {
                EventColumn typeC = record.getColumn("type");
                EventColumn statusC = record.getColumn("status");
                int type = Integer.valueOf(typeC.getColumnValue());
                int status = Integer.valueOf(statusC.getColumnValue());
                int type_status = type * 1000 + status;

                EventColumn extendC = new EventColumn();
                extendC.setUpdate(true);
                extendC.setColumnValue(String.valueOf(type_status));
                extendC.setKey(false);
                extendC.setColumnName("type_status");
                extendC.setColumnType(typeC.getColumnType());
                extendC.setIndex(Integer.MAX_VALUE);
                extendC.setNull(false);

                record.getColumns().add(extendC);
                RecordMeta.attachExtendColumns(record, Sets.newHashSet("type_status"));
            }
        }
        return record;
    }
}
