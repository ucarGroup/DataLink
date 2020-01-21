package com.ucar.datalink.domain.decorate;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Date;

/**
 * @author xy.li
 * @date 2019/05/29
 */
public class TaskDecorate {

    private long  id;
    private long  taskId;
    private String  taskName;
    private String  tableName;
    private String  remark;
    private Date  createTime;
    private Date modifyTime;
    private String  statement;
    private boolean deleted;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ");
        sql.append(this.getTableName());
        sql.append(" ");
        statement = statement.trim();
        if(statement.startsWith("[")){
            String[] array = statement.substring(1,statement.length() - 1).split("-");
            if(array.length != 2){
                throw new IllegalArgumentException(String.format("数据格式异常[%s]",this.statement));
            }
            String beginId = array[0].trim();
            String endId = array[1].trim();
            try {
                Long.parseLong(beginId);
                Long.parseLong(endId);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format("转换long类型异常[%s] [%s]",beginId,endId));
            }
            sql.append(" where id >= ");
            sql.append(beginId);
            sql.append(" and ");
            sql.append(" id <= ");
            sql.append(endId);
        }else{
            sql.append(" where id in (");
            sql.append(statement);
            sql.append(")");
        }
        return sql.toString();
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }

    public static void main(String[] args) {
        TaskDecorate taskDecorate = new TaskDecorate();
        taskDecorate.setStatement("1,2,3");
        taskDecorate.setTableName("t_dl_task_decorate_detail");
        System.out.println(taskDecorate.getSql());

        TaskDecorate taskDecorate2 = new TaskDecorate();
        taskDecorate2.setStatement("[1-10]");
        taskDecorate2.setTableName("t_dl_task_decorate_detail");
        System.out.println(taskDecorate2.getSql());

        TaskDecorate taskDecorate3 = new TaskDecorate();
        taskDecorate3.setStatement("1");
        taskDecorate3.setTableName("t_dl_task_decorate_detail");
        System.out.println(taskDecorate3.getSql());


    }

}
