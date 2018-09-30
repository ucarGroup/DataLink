package com.ucar.datalink.writer.es.client.rest.vo;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;

import java.io.Serializable;

public class BulkResultItem implements Serializable {
	//指定的操作类型
    public String action;
    //es服务端真正的操作类型
    public ESEnum.OperateTypeEnum realAction;
    public String index;
    public String type;
    public String id;
    public int status;
    private String error;
    private Integer version;
    
    public BulkResultItem(String action, JSONObject values) {
        this.action = action;
        this.index = values.getString(ParseHandler.INDEX_NAME);
        this.type = values.getString(ParseHandler.TYPE_NAME);
        this.id = values.getString(ParseHandler.ID_NAME);
        this.status = values.getIntValue(ParseHandler.STATUS_NAME);
        this.error = values.getString(ParseHandler.ERROR_NAME);
        this.version = values.getInteger(ParseHandler.VERSION_NAME);
        setRealAction(action, status);
    }
    
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public ESEnum.OperateTypeEnum getRealAction() {
		return realAction;
	}

	private void setRealAction(String action, int status) {
		if(ESEnum.BatchActionEnum.CREATE.getName().equals(action)) {
			realAction = ESEnum.OperateTypeEnum.CREATE;
		}else if(ESEnum.BatchActionEnum.UPDATE.getName().equals(action)) {
			realAction = ESEnum.OperateTypeEnum.UPDATE;
		}else if(ESEnum.BatchActionEnum.DELETE.getName().equals(action)) {
			realAction = ESEnum.OperateTypeEnum.DELETE;
		}else if(ESEnum.BatchActionEnum.INDEX.getName().equals(action)) {
			if(status == 201) {
				realAction = ESEnum.OperateTypeEnum.CREATE;
			}else if(status == 200){
				realAction = ESEnum.OperateTypeEnum.ALLUPDATE;
			}
		}
	}
	
}
