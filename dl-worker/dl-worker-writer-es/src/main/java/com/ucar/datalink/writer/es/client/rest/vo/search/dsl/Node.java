package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;

public abstract class Node extends JSONObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8173783528909022699L;
	
	private String nodeName;
	
	protected ESEnum.DSLEnum nodeNameEnum;
	
	protected boolean isArray = false;
	
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(ESEnum.DSLEnum nodeNameEnum) {
		this.nodeNameEnum = nodeNameEnum;
		if(nodeNameEnum instanceof ESEnum.DSLKeyEnum) {
			isArray = ((ESEnum.DSLKeyEnum)nodeNameEnum).isArray();
		}
		this.nodeName = nodeNameEnum.getName();
	}
	
	protected void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public ESEnum.DSLEnum getNodeNameEnum() {
		return nodeNameEnum;
	}


	public void setNodeNameEnum(ESEnum.DSLKeyEnum nodeNameEnum) {
		this.nodeNameEnum = nodeNameEnum;
	}
	
}
