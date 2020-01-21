package com.ucar.datalink.writer.rdbms.extend;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.intercept.Interceptor;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by lubiao on 2017/6/8.
 */
public class OrderEntRecordInterceptor implements Interceptor<RdbEventRecord> {

    private static final Logger logger = LoggerFactory.getLogger(OrderEntRecordInterceptor.class);
    private static final String PREFIX = "ENT_ORDER_";

    private List<String> subTables = Arrays.asList("t_scd_order_extend_info", "t_scd_order_detail", "t_scd_order_finance",
            "t_scd_order_detail_extend_info", "t_scd_order_disagree", "t_scd_order_finance_extend_info", "t_scd_order_coordinate",
            "t_scd_order_driver_message_h");

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        String tableName = record.getTableName();
        EventType eventType = record.getEventType();

        //直接判断Insert就ok了，因为insert如果被忽略了，则update和delete在目标端也是空转一圈
        if (eventType.equals(EventType.INSERT)) {
            Optional<EventColumn> entIdColumn = record.getColumns()
                    .stream()
                    .filter(i -> "ent_id".equals(i.getColumnName()))
                    .findFirst();
            Optional<EventColumn> orderIdColumn = record.getColumns()
                    .stream()
                    .filter(i -> "order_id".equals(i.getColumnName()))
                    .findFirst();
            String entId = entIdColumn.isPresent() ? entIdColumn.get().getColumnValue() : null;
            String orderId = orderIdColumn.isPresent() ? orderIdColumn.get().getColumnValue() : null;

            if ("t_scd_order".equals(tableName)) {
                if (!(StringUtils.isNotBlank(entId) && !"0".equals(entId))) {
                    return null;
                } else {
                    String id = record.getKeys().stream().filter(i -> "id".equals(i.getColumnName())).findFirst().get().getColumnValue();
                    context.taskWriterSession().setData(PREFIX + id, id);
                }
            } else if (subTables.contains(tableName) && !isOrderExists(orderId, context)) {
                return null;
            }
        }

        return record;
    }

    private boolean isOrderExists(String orderId, TaskWriterContext context) {
        String id = context.taskWriterSession().getData(PREFIX + orderId);
        if (StringUtils.isNotBlank(id)) {
            return true;
        } else {
            return false;
        }
    }
}
