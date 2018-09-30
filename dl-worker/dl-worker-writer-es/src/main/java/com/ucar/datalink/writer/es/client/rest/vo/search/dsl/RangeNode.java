package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.ComparisonEnum;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.DSLFieldKeyEnum;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.DSLKeyEnum;

/**
 * 
 * Description:创建范围查询结点 
 * All Rights Reserved.
 * Created on 2016-6-29 下午12:10:33
 * @author  孔增（kongzeng@zuche.com）
 */
public class RangeNode extends Node{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3634169758351555211L;
	
	public RangeNode() {
		super.setNodeName(DSLKeyEnum.RANGE);
	}
	
	
	public RangeNode less(String fieldName, Object compareValue) {
		
		addComparison(ComparisonEnum.LESS, fieldName, compareValue);
		
		return this;
		
	}

	public RangeNode lessOrEqual(String fieldName, Object compareValue) {
		addComparison(ComparisonEnum.LESS_OR_EQUAL, fieldName, compareValue);
		
		return this;
		
	}
	
	public RangeNode greater(String fieldName, Object compareValue) {
		
		addComparison(ComparisonEnum.GREATER, fieldName, compareValue);
		
		return this;
		
	}
	
	public RangeNode greaterOrEqual(String fieldName, Object compareValue) {
		
		addComparison(ComparisonEnum.GREATER_OR_EQUAL, fieldName, compareValue);
		
		return this;
		
	}

	private void addComparison(ComparisonEnum comparisonEnum, String fieldName, Object compareValue) {
		FieldNode ln = (FieldNode) this.get(fieldName);
		if(ln == null) {
			ln = new FieldNode(fieldName);
			super.put(fieldName,ln);
		}
		
		ln.addField(comparisonEnum.getAcronym() , compareValue);
	}
	
	public RangeNode addConfig(String fieldName, DSLFieldKeyEnum fieldKeyEnum, Object compareValue) {
		return this.addConfig(fieldName, fieldKeyEnum.getName(), compareValue);
	}
	
	public RangeNode addConfig(String fieldName, String fieldKey, Object compareValue) {
		FieldNode ln = (FieldNode) this.get(fieldName);
		if(ln == null) {
			ln = new FieldNode(fieldName);
			super.put(fieldName,ln);
		}
		
		ln.addField(fieldKey , compareValue);
		
		return this;
	}

}
