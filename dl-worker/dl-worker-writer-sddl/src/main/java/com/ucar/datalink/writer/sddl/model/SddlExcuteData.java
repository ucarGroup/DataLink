package com.ucar.datalink.writer.sddl.model;

import com.alibaba.fastjson.JSON;
import com.ucar.datalink.contract.log.rdbms.EventColumn;
import com.ucar.datalink.contract.log.rdbms.EventType;
import com.zuche.framework.sddl.datasource.SddlDataSource;

import java.util.List;

/**
 * @Description:
 *
 * @Author : yongwang.chen@ucarinc.com
 * @Date   : 4:06 PM 15/11/2017
 */
public class SddlExcuteData {

    private SddlDataSource sddlDs;
    
    private String clusterName;
    
    private String tableName;


    private PreparedSqlInfo preparedSqlInfo;

    private EventType eventType;


    public SddlExcuteData(String clusterName, EventType eventType){
    	this.clusterName=clusterName;
    	this.eventType = eventType;
    }

    public SddlExcuteData(String clusterName, SddlDataSource sddlDs, String tableName, EventType eventType) {
        this.sddlDs = sddlDs;
        this.clusterName = clusterName;
        this.tableName = tableName;
        this.eventType = eventType;
    }

    public SddlDataSource getSddlDs() {
		return sddlDs;
	}

	public void setSddlDs(SddlDataSource sddlDs) {
		this.sddlDs = sddlDs;
	}

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public PreparedSqlInfo getPreparedSqlInfo() {
        return preparedSqlInfo;
    }

    public void setPreparedSqlInfo(PreparedSqlInfo preparedSqlInfo) {
        this.preparedSqlInfo = preparedSqlInfo;
    }

    @Override
    public String toString() {
        return "SddlExcuteData{" +
                "sddlDs=" + sddlDs +
                ", clusterName='" + clusterName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", preparedSqlInfo=" + JSON.toJSONString(preparedSqlInfo) +
                ", eventType=" + eventType +
                '}';
    }

    public class PreparedSqlInfo {
        private String sql;
        private List<EventColumn> preparedColumns;

        public PreparedSqlInfo() {
        }

        public PreparedSqlInfo(String sql, List<EventColumn> preparedColumns) {
            this.sql = sql;
            this.preparedColumns = preparedColumns;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public List<EventColumn> getPreparedColumns() {
            return preparedColumns;
        }

        public void setPreparedColumns(List<EventColumn> preparedColumns) {
            this.preparedColumns = preparedColumns;
        }
    }

}
