package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.DSLFieldKeyEnum;

/**
 * 
 * Description:创建域结点 
 * All Rights Reserved.
 * Created on 2016-6-29 下午12:11:04
 * @author  孔增（kongzeng@zuche.com）
 */
public class FieldNode extends Node {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3634169758351555211L;
	
	public FieldNode() {}
	
	public FieldNode (String fieldName) {
		super.setNodeName(fieldName);
	}
	
	public FieldNode addField(DSLFieldKeyEnum fieldKeyEnum, Object value) {
		this.put(fieldKeyEnum.getName(), value);
		return this;
	}
	
	public FieldNode addField(String fieldKey, Object value) {
		this.put(fieldKey, value);
		return this;
	}

}
