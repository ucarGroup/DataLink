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
 * 其它系统数据同步到ucarinc_gps数据库时，一些特殊处理逻辑
 * Created by lubiao on 2017/6/14.
 */
public class UcarincGpsInterceptor extends AbstractTriggerSqlInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UcarincGpsInterceptor.class);

    private static final String FCAR_ADMIN_T_DEPARTMENT_INSERT = "INSERT INTO t_dept(project_dept_id, project_cid, project_type, dept_name, dept_type, parent_id, status)VALUES(#id#, #city_id#, 4, #name#, CASE WHEN #type# = 100 THEN 1 WHEN #type# = 150 THEN 2 WHEN #type# = 200 THEN 3 WHEN #type# = 300 THEN 4 ELSE #type# END, #parent_id#, #status#)";
    private static final String FCAR_ADMIN_T_DEPARTMENT_UPDATE = "update t_dept set project_cid=#city_id#, dept_name=#name#, dept_type=CASE WHEN #type# = 100 THEN 1 WHEN #type# = 150 THEN 2 WHEN #type# = 200 THEN 3 WHEN #type# = 300 THEN 4 ELSE #type# END, parent_id=#parent_id#, status=#status# where project_dept_id=#id# AND project_type=4";
    private static final String FCAR_ADMIN_T_DEPARTMENT_DELETE = "delete from t_dept where project_dept_id=#id# AND project_type=4";

    private static final String FCAR_ADMIN_T_EMPLOYEE_INSERT = "INSERT INTO t_user(project_user_id, login_name, name, dept_id, project_type) VALUES (#id#, #emp_login_id#, #name#, #department_id#, 4)";
    private static final String FCAR_ADMIN_T_EMPLOYEE_UPDATE = "UPDATE t_user SET login_name=#emp_login_id#, name=#name#, dept_id=#department_id# where project_user_id=#id# AND project_type=4";
    private static final String FCAR_ADMIN_T_EMPLOYEE_DELETE = "DELETE FROM t_user WHERE project_user_id=#id# AND project_type=4";

    private static final String FCAR_LOAN_T_ASSET_VEHICLE_INSERT = "INSERT INTO t_vehicle(project_vid,vehicle_no, vin, dept_id, status_1st, status, project_type)VALUES(#id#,CASE WHEN (#vehicle_no# IS NULL OR TRIM(#vehicle_no#) = '') THEN #vin# ELSE #vehicle_no# END, #vin#, #department_id#, #vehicle_status#, #status#, 4)";
    private static final String FCAR_LOAN_T_ASSET_VEHICLE_UPDATE = "update t_vehicle set vehicle_no=CASE WHEN (#vehicle_no# IS NULL OR TRIM(#vehicle_no#) = '') THEN #vin# ELSE #vehicle_no# END,vin=#vin#, dept_id=#department_id#, status_1st=#vehicle_status#, status=#status# WHERE project_vid=#id# AND project_type=4";
    private static final String FCAR_LOAN_T_ASSET_VEHICLE_DELETE = "delete from t_vehicle where project_vid=#id# AND project_type=4";

    private static final String FCAR_LOAN_T_LOAN_APPLY_INSERT = "INSERT INTO t_frm_apply_monitor(apply_id,vehicle_id, apply_no, vehicle_no)VALUES(#id#,#vehicle_id#,#loan_apply_no#,#vehicle_no#)";
    private static final String FCAR_LOAN_T_LOAN_APPLY_UPDATE = "update t_frm_apply_monitor set vehicle_id=#vehicle_id#,apply_no=#loan_apply_no#, vehicle_no=#vehicle_no# WHERE apply_id=#id# ";
    private static final String FCAR_LOAN_T_LOAN_APPLY_DELETE = "delete from t_frm_apply_monitor where apply_id=#id#";

    private static final String UC_DB_T_DEPARTMENT_INSERT = "INSERT INTO t_dept(project_dept_id, project_cid, project_type, dept_name, dept_type, parent_id, status,virtual)VALUES(#id#, CASE WHEN (#local_city# IS NULL OR TRIM(#local_city#) = '') THEN 0 ELSE #local_city# END, 5, #name#, CASE WHEN #type# = 100 THEN 1 WHEN #type# = 120 THEN 2 WHEN #type# = 150 THEN 3 WHEN #type# = 200 THEN 4 WHEN #type# = 230 THEN 9 ELSE #type# END, #parent_id#, #status#, #virtual#)";
    private static final String UC_DB_T_DEPARTMENT_UPDATE = "update t_dept set project_cid=CASE WHEN (#local_city# IS NULL OR TRIM(#local_city#) = '') THEN 0 ELSE #local_city# END, dept_name=#name#, dept_type=CASE WHEN #type# = 100 THEN 1 WHEN #type# = 120 THEN 2 WHEN #type# = 150 THEN 3 WHEN #type# = 200 THEN 4 WHEN #type# = 230 THEN 9 ELSE #type# END, parent_id=#parent_id#, status=#status#, virtual=#virtual# where project_dept_id=#id# AND project_type=5";
    private static final String UC_DB_T_DEPARTMENT_DELETE = "delete from t_dept where project_dept_id=#id# AND project_type=5";

    private static final String UC_DB_T_EMPLOYEE_INSERT = "INSERT INTO t_user(project_user_id, login_name, name, dept_id, project_type) VALUES (#id#, #emp_login_id#, #name#, #department_id#, 5)";
    private static final String UC_DB_T_EMPLOYEE_UPDATE = "UPDATE t_user SET login_name=#emp_login_id#, name=#name#, dept_id=#department_id# where project_user_id=#id# AND project_type=5";
    private static final String UC_DB_T_EMPLOYEE_DELETE = "DELETE FROM t_user WHERE project_user_id=#id# AND project_type=5";

    private static final String UC_DB_T_VEHICLE_BASE_INSERT = "INSERT INTO t_vehicle(project_vid,dept_id,project_type,vin,vehicle_type_id) values(#id#,#store#,5,#vin#,#model_id#)";
    private static final String UC_DB_T_VEHICLE_BASE_UPDATE = "update t_vehicle set dept_id=#store#, vin=#vin#,vehicle_type_id=#model_id# where project_vid=#id# and project_type=5";
    private static final String UC_DB_T_VEHICLE_BASE_DELETE = "delete from t_vehicle where project_vid=#id#  and project_type=5";

    private static final String UC_DB_T_VEHICLE_STATUS_UPDATE = "update t_vehicle set vehicle_no=CASE " +
            "WHEN (#carno# IS NULL OR TRIM(#carno#) = '') THEN #frame_no# ELSE #carno# END,vin=#frame_no#, " +
            "status_1st=#car_status#, status=#valid#, " +
            "status_2nd=#stock_status#, status_3rd=#emerge_mark#, rent_time=#sell_out_time# " +
            "WHERE project_vid=#vehicle_id# AND project_type=5";
    private static final String UCAR_VEHICLE_T_SCD_VEHICLE_INSERT = "INSERT INTO t_vehicle(project_vid,vehicle_no,vin,dept_id,status_1st,status,project_type,car_order,car_order_name)values(#id#,#vehicle_no#,#vehicle_no#,1,1,1,0,17,'加盟专车')";

    private static final String UC_VRMS_T_VRMS_MONITOR_INFO_INSERT_1 = "INSERT INTO t_device(id,device_no, production_time, fountain, protocol_key, model_name, dept_id, expriy_time," +
            "status, sim_no, imei, month_exchange, communication, is_cancel, remark, create_time, " +
            "create_emp, modify_time, modify_emp) " +
            "VALUES(#id#,#monitor_code#, #production_date#, #battery_type#, " +
            " #agreement#, #model_name#, #monitor_location#, #is_expiryDate#, #monitor_status#, #sim_cardId#, #iccid#, #month_flow#" +
            " ,#communication_company#, #sim_valid#, #remark#, #create_time#, #create_emp#" +
            " ,#modify_time#, #modify_emp#)";
    private static final String UC_VRMS_T_VRMS_MONITOR_INFO_UPDATE_1 = "update t_device set " +
            "device_no=#monitor_code#, " +
            "production_time=#production_date#, " +
            "fountain=#battery_type#," +
            "protocol_key=#agreement#," +
            "model_name=#model_name#," +
            "dept_id=#monitor_location#," +
            "expriy_time=#is_expiryDate#," +
            "status=#monitor_status#," +
            "sim_no=#sim_cardId#," +
            "imei=#iccid#," +
            "month_exchange=#month_flow#," +
            "communication=#communication_company#," +
            "is_cancel=#sim_valid#," +
            "remark=#remark#," +
            "create_time=#create_time#," +
            "create_emp=#create_emp#," +
            "modify_time=#modify_time#," +
            "modify_emp=#modify_emp#" +
            " where id=#id#";
    private static final String UC_VRMS_T_VRMS_MONITOR_INFO_DELETE_1 = "delete from t_device where id=#id#";

    private static final String UC_VRMS_T_VRMS_MONITOR_INFO_INSERT_2 = "INSERT INTO t_device_now(device_id,device_no) VALUES(#id#, #monitor_code#)";
    private static final String UC_VRMS_T_VRMS_MONITOR_INFO_UPDATE_2 = "update t_device_now set device_no=#monitor_code# where device_id = #id#";
    private static final String UC_VRMS_T_VRMS_MONITOR_INFO_DELETE_2 = "delete from t_device_now where device_id=#id#";

    private static final String AMP_VMS_T_VEHICLE_INFO_INSERT = "INSERT INTO t_vehicle(project_vid,vehicle_no, vin, dept_id, status_1st, status_2nd, status_3rd, status, project_type)VALUES(#id#,CASE WHEN (#vehicle_no# IS NULL OR TRIM(#vehicle_no#) = '') THEN #frame_no# ELSE #vehicle_no# END, #frame_no#, #department_id#, #v_first_status_id#, #v_second_status_id#, #v_third_status_id#, #is_deleted#, 6)";
    private static final String AMP_VMS_T_VEHICLE_INFO_UPDATE = "update t_vehicle set vehicle_no=CASE WHEN (#vehicle_no# IS NULL OR TRIM(#vehicle_no#) = '') THEN #frame_no# ELSE #vehicle_no# END,vin=#frame_no#, dept_id=#department_id#, status_1st=#v_first_status_id#, status_2nd=#v_second_status_id#, status_3rd=#v_third_status_id#, status=#is_deleted# WHERE project_vid=#id# AND project_type=6";
    private static final String AMP_VMS_T_VEHICLE_INFO_DELETE = "delete from t_vehicle where project_vid=#id# AND project_type=6";

    @Override
    public RdbEventRecord intercept(RdbEventRecord record, TaskWriterContext context) {
        try {
            String schema = record.getSchemaName();
            String tableName = record.getTableName();

            if ("fcar_admin".equalsIgnoreCase(schema)) {
                if ("t_department".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        executeSql(FCAR_ADMIN_T_DEPARTMENT_INSERT, record);
                    } else if (record.getEventType() == EventType.UPDATE) {
                        executeSql(FCAR_ADMIN_T_DEPARTMENT_UPDATE, record);
                    } else if (record.getEventType() == EventType.DELETE) {
                        executeSql(FCAR_ADMIN_T_DEPARTMENT_DELETE, record);
                    }
                    return null;
                } else if ("t_employee".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        executeSql(FCAR_ADMIN_T_EMPLOYEE_INSERT, record);
                    } else if (record.getEventType() == EventType.UPDATE) {
                        executeSql(FCAR_ADMIN_T_EMPLOYEE_UPDATE, record);
                    } else if (record.getEventType() == EventType.DELETE) {
                        executeSql(FCAR_ADMIN_T_EMPLOYEE_DELETE, record);
                    }
                    return null;
                }
            } else if ("fcar_loan".equalsIgnoreCase(schema)) {
                if ("t_asset_vehicle".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        executeSql(FCAR_LOAN_T_ASSET_VEHICLE_INSERT, record);
                    } else if (record.getEventType() == EventType.UPDATE) {
                        executeSql(FCAR_LOAN_T_ASSET_VEHICLE_UPDATE, record);
                    } else if (record.getEventType() == EventType.DELETE) {
                        executeSql(FCAR_LOAN_T_ASSET_VEHICLE_DELETE, record);
                    }
                    return null;
                }else if ("t_loan_apply".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        executeSql(FCAR_LOAN_T_LOAN_APPLY_INSERT, record);
                    } else if (record.getEventType() == EventType.UPDATE) {
                        executeSql(FCAR_LOAN_T_LOAN_APPLY_UPDATE, record);
                    } else if (record.getEventType() == EventType.DELETE) {
                        executeSql(FCAR_LOAN_T_LOAN_APPLY_DELETE, record);
                    }
                    return record;
                }
            } else if ("uc_db".equalsIgnoreCase(schema)) {
                if ("t_department".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        executeSql(UC_DB_T_DEPARTMENT_INSERT, record);
                    } else if (record.getEventType() == EventType.UPDATE) {
                        executeSql(UC_DB_T_DEPARTMENT_UPDATE, record);
                    } else if (record.getEventType() == EventType.DELETE) {
                        executeSql(UC_DB_T_DEPARTMENT_DELETE, record);
                    }
                    return null;
                } else if ("t_employee".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        executeSql(UC_DB_T_EMPLOYEE_INSERT, record);
                    } else if (record.getEventType() == EventType.UPDATE) {
                        executeSql(UC_DB_T_EMPLOYEE_UPDATE, record);
                    } else if (record.getEventType() == EventType.DELETE) {
                        executeSql(UC_DB_T_EMPLOYEE_DELETE, record);
                    }
                    return null;
                } else if ("t_vehicle_base".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        executeSql(UC_DB_T_VEHICLE_BASE_INSERT, record);
                    } else if (record.getEventType() == EventType.UPDATE) {
                        executeSql(UC_DB_T_VEHICLE_BASE_UPDATE, record);
                    } else if (record.getEventType() == EventType.DELETE) {
                        executeSql(UC_DB_T_VEHICLE_BASE_DELETE, record);
                    }
                    return null;
                } else if ("t_vehicle_status".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.UPDATE) {
                        executeSql(UC_DB_T_VEHICLE_STATUS_UPDATE, record);
                    }
                    return null;
                }
            } else if ("ucar_vehicle".equalsIgnoreCase(schema)) {
                if ("t_scd_vehicle".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        EventColumn cell1 = getEventColumn(record, "status");
                        EventColumn cell2 = getEventColumn(record, "part_time_driver");
                        if ("1".equals(cell1.getColumnValue()) && "3".equals(cell2.getColumnValue())) {
                            executeSql(UCAR_VEHICLE_T_SCD_VEHICLE_INSERT, record);
                        }
                    }
                    return null;
                }
            }else if ("uc_vrms".equalsIgnoreCase(schema)) {
                if ("t_vrms_monitor_info".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        executeSql(UC_VRMS_T_VRMS_MONITOR_INFO_INSERT_1, record);
                        executeSql(UC_VRMS_T_VRMS_MONITOR_INFO_INSERT_2, record);
                    } else if (record.getEventType() == EventType.UPDATE) {
                        executeSql(UC_VRMS_T_VRMS_MONITOR_INFO_UPDATE_1, record);
                        executeSql(UC_VRMS_T_VRMS_MONITOR_INFO_UPDATE_2, record);
                    } else if (record.getEventType() == EventType.DELETE) {
                        executeSql(UC_VRMS_T_VRMS_MONITOR_INFO_DELETE_1, record);
                        executeSql(UC_VRMS_T_VRMS_MONITOR_INFO_DELETE_2, record);
                    }
                    return null;
                }
            } else if ("amp_vms".equalsIgnoreCase(schema)) {
                if ("t_vehicle_info".equalsIgnoreCase(tableName)) {
                    if (record.getEventType() == EventType.INSERT) {
                        executeSql(AMP_VMS_T_VEHICLE_INFO_INSERT, record);
                    } else if (record.getEventType() == EventType.UPDATE) {
                        executeSql(AMP_VMS_T_VEHICLE_INFO_UPDATE, record);
                    } else if (record.getEventType() == EventType.DELETE) {
                        executeSql(AMP_VMS_T_VEHICLE_INFO_DELETE, record);
                    }
                    return null;
                }
            }

            return record;
        } catch (Throwable t) {
            logger.error("Record Info is:" + record);
            throw new DataLoadException("Record Info is:" + record, t);
        }
    }

    private EventColumn getEventColumn(RdbEventRecord record, String columnName) {
        Optional<EventColumn> optional = record.getColumns().stream().filter(i -> i.getColumnName().equals(columnName)).findFirst();
        return optional.isPresent() ? optional.get() : null;
    }
}
