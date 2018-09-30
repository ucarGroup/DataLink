package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;

import java.util.List;

/**
 * 
 * Description: 多域匹配
 * All Rights Reserved.
 * Created on 2016-6-30 上午11:52:10
 * @author  孔增（kongzeng@zuche.com）
 */
public class MultiMatchNode extends Node {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3634169758351555211L;
	
	public MultiMatchNode(List<String> fields, Object value) {
		super.setNodeName(ESEnum.DSLKeyEnum.MULTI_MATCH);
		this.put(ESEnum.DSLFieldKeyEnum.QUERY.getName(), value);
		this.put(ESEnum.DSLFieldKeyEnum.FIELDS.getName(), fields);
	}
	
}
