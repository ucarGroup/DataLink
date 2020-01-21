package com.ucar.datalink.writer.rdbms.extend;

import com.ucar.datalink.common.errors.DataLoadException;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by lubiao on 2017/7/18.
 */
public class ZucheGpsInterceptor extends AbstractTriggerSqlInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ZucheGpsInterceptor.class);

    private static final String UCAR_VEHICLE_T_SCD_VEHICLE_INSERT = "insert into vehicle_info(extend_vid,vehicle_number,vin,dept_id,status_1st,status,project_type,car_order,car_order_name)values(#id#,#vehicle_no#,#vehicle_no#,1,1,1,0,17,'加盟专车')";

    private static final String UC_DB_T_VEHICLE_BASE_INSERT = "INSERT INTO vehicle_info(extend_vid,dept_id,project_type,vin) values(#id#,#store#,5,#vin#)";
    private static final String UC_DB_T_VEHICLE_BASE_UPDATE = "update vehicle_info set dept_id=#store# where extend_vid=#id# and project_type=5";
    private static final String UC_DB_T_VEHICLE_BASE_DELETE = "delete from vehicle_info where extend_vid=#id#  and project_type=5";

    private static final String UC_DB_T_VEHICLE_STATUS_UPDATE = "update vehicle_info set vehicle_number=CASE WHEN (#carno# IS NULL OR TRIM(#carno#) = '') THEN #frame_no# ELSE #carno# END,vin=#frame_no#, status_1st=#car_status#, status=#valid# WHERE extend_vid=#vehicle_id# AND project_type=5";

    private static final String UC_DB_T_DEPARTMENT_INSERT = "INSERT INTO company_info(erp_id, company_name, parent_id, city_name, company_type_id, STATUS, project_type)VALUES(#id#, #name#, #parent_id#, #local_city#, CASE WHEN #type# = 100 THEN 1 WHEN #type# = 120 THEN 2 WHEN #type# = 150 THEN 3 WHEN #type# = 200 THEN 4 END, #status#, 5)";
    private static final String UC_DB_T_DEPARTMENT_UPDATE = "update company_info set company_name=#name#, parent_id=#parent_id#, city_name=#local_city#, company_type_id=CASE WHEN #type# = 100 THEN 1 WHEN #type# = 120 THEN 2 WHEN #type# = 150 THEN 3 WHEN #type# = 200 THEN 4 END, status=#status# where erp_id=#id# AND project_type=5";
    private static final String UC_DB_T_DEPARTMENT_DELETE = "delete from company_info where erp_id=#id# AND project_type=5";

    private static final String UC_DB_T_EMPLOYEE_INSERT = "INSERT INTO user_info(erp_id, user_loginname, user_name, company_id, project_type, type_id) VALUES (#id#, #emp_login_id#, #name#, #department_id#, 5, 5)";
    private static final String UC_DB_T_EMPLOYEE_UPDATE = "UPDATE user_info SET user_loginname=#emp_login_id#, user_name=#name#, company_id=#department_id# where erp_id=#id# AND project_type=5";
    private static final String UC_DB_T_EMPLOYEE_DELETE = "DELETE FROM user_info WHERE erp_id=#id# AND project_type=5";

    private static final String FCAR_ADMIN_T_DEPARTMENT_INSERT = "INSERT INTO company_info(erp_id, company_name, parent_id, city_name, company_type_id, STATUS, project_type)VALUES(#id#, #name#, #parent_id#, #city_id#, CASE WHEN #type#=100 THEN 1 WHEN #type#=150 THEN 2 WHEN #type#=200 THEN 3 WHEN #type#=300 THEN 4 END, #status#, 4)";
    private static final String FCAR_ADMIN_T_DEPARTMENT_UPDATE = "update company_info set company_name=#name#, parent_id=#parent_id#, city_name=#city_id#, company_type_id = CASE WHEN #type#=100 THEN 1 WHEN #type#=150 THEN 2 WHEN #type#=200 THEN 3 WHEN #type#=300 THEN 4 END, status=#status# where erp_id=#id# AND project_type=4";
    private static final String FCAR_ADMIN_T_DEPARTMENT_DELETE = "delete from company_info where erp_id=#id# AND project_type=4";

    private static final String FCAR_ADMIN_T_EMPLOYEE_INSERT = "INSERT INTO user_info(erp_id, user_loginname, user_name, company_id, project_type, type_id) VALUES (#id#, #emp_login_id#, #name#, #department_id#, 4, 4)";
    private static final String FCAR_ADMIN_T_EMPLOYEE_UPDATE = "UPDATE user_info SET user_loginname=#emp_login_id#, user_name=#name#, company_id=#department_id# where erp_id=#id# AND project_type=4";
    private static final String FCAR_ADMIN_T_EMPLOYEE_DELETE = "DELETE FROM user_info WHERE erp_id=#id# AND project_type=4";

    private static final String FCAR_LOAN_T_ASSET_VEHICLE_INSERT = "INSERT INTO vehicle_info(extend_vid,vehicle_number, vin, dept_id, status_1st, STATUS, project_type)VALUES(#id#,CASE WHEN (#vehicle_no# IS NULL OR TRIM(#vehicle_no#) = '') THEN #vin# ELSE #vehicle_no# END, #vin#, #department_id#, #vehicle_status#, #status#, 4)";
    private static final String FCAR_LOAN_T_ASSET_VEHICLE_UPDATE = "update vehicle_info set vehicle_number=CASE WHEN (#vehicle_no# IS NULL OR TRIM(#vehicle_no#) = '') THEN #vin# ELSE #vehicle_no# END,vin=#vin#, dept_id=#department_id#, status_1st=#vehicle_status#, status=#status# WHERE extend_vid=#id# AND project_type=4";
    private static final String FCAR_LOAN_T_ASSET_VEHICLE_DELETE = "delete from vehicle_info where extend_vid=#id# AND project_type=4";

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        try {
            String schemaName = record.getSchemaName();
            String tableName = record.getTableName();
            if ("ucar_vehicle".equals(schemaName) && "t_scd_vehicle".equals(tableName)) {
                if (record.getEventType().equals(EventType.INSERT)) {
                    EventColumn cell1 = getEventColumn(record, "status");
                    EventColumn cell2 = getEventColumn(record, "part_time_driver");
                    EventColumn cell3 = getEventColumn(record, "use_driver_type_2");
                    if ("1".equals(cell1.getColumnValue()) && "3".equals(cell2.getColumnValue()) && ("31".equals(cell3.getColumnValue()) || "32".equals(cell3.getColumnValue()))) {
                        executeSql(UCAR_VEHICLE_T_SCD_VEHICLE_INSERT, record);
                    }
                }
                return null;
            } else if ("uc_db".equals(schemaName) && "t_vehicle_base".equals(tableName)) {
                if (record.getEventType() == EventType.INSERT) {
                    executeSql(UC_DB_T_VEHICLE_BASE_INSERT, record);
                } else if (record.getEventType() == EventType.UPDATE) {
                    executeSql(UC_DB_T_VEHICLE_BASE_UPDATE, record);
                } else if (record.getEventType() == EventType.DELETE) {
                    executeSql(UC_DB_T_VEHICLE_BASE_DELETE, record);
                }
                return null;
            } else if ("uc_db".equals(schemaName) && "t_vehicle_status".equals(tableName)) {
                if (record.getEventType() == EventType.UPDATE) {
                    executeSql(UC_DB_T_VEHICLE_STATUS_UPDATE, record);
                }
                return null;
            } else if ("uc_db".equals(schemaName) && "t_department".equals(tableName)) {
                if (record.getEventType() == EventType.INSERT) {
                    executeSql(UC_DB_T_DEPARTMENT_INSERT, record);
                } else if (record.getEventType() == EventType.UPDATE) {
                    executeSql(UC_DB_T_DEPARTMENT_UPDATE, record);
                } else if (record.getEventType() == EventType.DELETE) {
                    executeSql(UC_DB_T_DEPARTMENT_DELETE, record);
                }
                return null;
            } else if ("uc_db".equals(schemaName) && "t_employee".equals(tableName)) {
                if (record.getEventType() == EventType.INSERT) {
                    executeSql(UC_DB_T_EMPLOYEE_INSERT, record);
                } else if (record.getEventType() == EventType.UPDATE) {
                    executeSql(UC_DB_T_EMPLOYEE_UPDATE, record);
                } else if (record.getEventType() == EventType.DELETE) {
                    executeSql(UC_DB_T_EMPLOYEE_DELETE, record);
                }
                return null;
            } else if ("fcar_admin".equals(schemaName) && "t_department".equals(tableName)) {
                if (record.getEventType() == EventType.INSERT) {
                    executeSql(FCAR_ADMIN_T_DEPARTMENT_INSERT, record);
                } else if (record.getEventType() == EventType.UPDATE) {
                    executeSql(FCAR_ADMIN_T_DEPARTMENT_UPDATE, record);
                } else if (record.getEventType() == EventType.DELETE) {
                    executeSql(FCAR_ADMIN_T_DEPARTMENT_DELETE, record);
                }
                return null;
            } else if ("fcar_admin".equals(schemaName) && "t_employee".equals(tableName)) {
                if (record.getEventType() == EventType.INSERT) {
                    executeSql(FCAR_ADMIN_T_EMPLOYEE_INSERT, record);
                } else if (record.getEventType() == EventType.UPDATE) {
                    executeSql(FCAR_ADMIN_T_EMPLOYEE_UPDATE, record);
                } else if (record.getEventType() == EventType.DELETE) {
                    executeSql(FCAR_ADMIN_T_EMPLOYEE_DELETE, record);
                }
                return null;
            } else if ("fcar_loan".equals(schemaName) && "t_asset_vehicle".equals(tableName)) {
                if (record.getEventType() == EventType.INSERT) {
                    executeSql(FCAR_LOAN_T_ASSET_VEHICLE_INSERT, record);
                } else if (record.getEventType() == EventType.UPDATE) {
                    executeSql(FCAR_LOAN_T_ASSET_VEHICLE_UPDATE, record);
                } else if (record.getEventType() == EventType.DELETE) {
                    executeSql(FCAR_LOAN_T_ASSET_VEHICLE_DELETE, record);
                }
                return null;
            }

            return record;
        } catch (Throwable t) {
            logger.error("Reord Info is:" + record);
            throw new DataLoadException("Record Info is:" + record, t);
        }
    }

    private EventColumn getEventColumn(RdbEventRecord record, String columnName) {
        Optional<EventColumn> optional = record.getColumns().stream().filter(i -> i.getColumnName().equals(columnName)).findFirst();
        return optional.isPresent() ? optional.get() : null;
    }
}
