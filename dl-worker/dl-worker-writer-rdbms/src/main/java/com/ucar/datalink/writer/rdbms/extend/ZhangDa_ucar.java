package com.ucar.datalink.writer.rdbms.extend;

import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.List;

/**
 * Created by yang.wang09 on 2018-06-26 16:45.
 */
public class ZhangDa_ucar extends AbstractTriggerSqlInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ZhangDa_ucar.class);

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        String schemaName = record.getSchemaName();
        String tableName = record.getTableName();

        if("config_center_client_list".equals(tableName)) {
            return change(record);
        }
        else if("config_center_data_source".equals(tableName)) {
            return change(record);
        }
        else if("config_center_notice_log".equals(tableName)) {
            return change(record);
        }
        else if("config_center_oper_log".equals(tableName)) {
            return change(record);
        }
        else if("config_center_project".equals(tableName)) {
            return change(record);
        }
        else if("config_center_user_relation".equals(tableName)) {
            return change(record);
        }
        else if("config_center_project_relation".equals(tableName)) {
            return change(record);
        }

        return record;

    }


    private RdbEventRecord change(RdbEventRecord record) {
        List<EventColumn> columns = record.getColumns();
        List<EventColumn> keys = record.getKeys();
        if(record.getEventType()==EventType.INSERT || record.getEventType()==EventType.UPDATE || record.getEventType()==EventType.DELETE) {
            for (EventColumn ec : keys) {
                if ("ID".equalsIgnoreCase(ec.getColumnName())) {
                    String oldValue = ec.getColumnValue();
                    String newValue = "ucar_" + oldValue;
                    ec.setColumnValue(newValue);
                    ec.setColumnType(Types.VARCHAR);
                    logger.info("ucarinc_gps_cfcenter " + record.getTableName() + " new value->" + newValue);
                }
            }
            logger.info(" record type -> "+record.getEventType());
            dumpColumns("primary keys columns", keys);
            dumpColumns("normal columns",columns);
        }
        return record;
    }

    private void dumpColumns(String comment, List<EventColumn> columns) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append(comment).append("\n");
        for(EventColumn ec : columns) {
            sb.append("name=").append(ec.getColumnName()).append("\t").append("value=").append(ec.getColumnValue());
            sb.append("\n");
        }
        logger.info(sb.toString());
    }



}
