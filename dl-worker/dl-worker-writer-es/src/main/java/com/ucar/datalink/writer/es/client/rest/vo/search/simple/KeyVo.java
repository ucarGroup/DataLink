package com.ucar.datalink.writer.es.client.rest.vo.search.simple;

import com.ucar.datalink.writer.es.client.rest.constant.CharacterConstant;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.ComparisonEnum;

/**
 * 
 * Description: 关键字对象
 * All Rights Reserved.
 * Created on 2016-6-13 上午10:41:40
 * @author  孔增（kongzeng@zuche.com）
 */
public class KeyVo {
	/**
	 * 关键字名称
	 */
	private String name;
	/**
	 * 关键字的值
	 */
	private String value;
	/**
	 * true 条件必须满足 false 条件必须不满足  null 可满足
	 */
	private Boolean required;
	/**
	 * 比较规则
	 */
	private ComparisonEnum comparisonEnum;
	
	public KeyVo(){}
	
	public KeyVo(String value){
		this.value = value;
	}
	
	public KeyVo(String name , String value){
		this.name = name;
		this.value = value;
	}
	
    public KeyVo(String name , String value, Boolean required){
		this.name = name;
		this.value = value;
		this.required = required;
	}
    
    public KeyVo(String name , String value, ComparisonEnum comparisonEnum){
		this.name = name;
		this.value = value;
		this.comparisonEnum = comparisonEnum;
	}
    
    public KeyVo(String name , String value, ComparisonEnum comparisonEnum, Boolean required){
		this.name = name;
		this.value = value;
		this.required = required;
		this.comparisonEnum = comparisonEnum;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Boolean getRequired() {
		return required;
	}
	public void setRequired(Boolean required) {
		this.required = required;
	}
	
	public ComparisonEnum getComparisonEnum() {
		return comparisonEnum;
	}

	public void setComparisonEnum(ComparisonEnum comparisonEnum) {
		this.comparisonEnum = comparisonEnum;
	}
	

	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
        if(required != null) {
        	if(required) {
        		sb.append(CharacterConstant.PLUS);
        	}else {
        		sb.append(CharacterConstant.REDUCE);
        	}
        }
        if(name != null) {
        	sb.append(name).append(CharacterConstant.COLON);
        }
        appendComparison(sb);
        
		return sb.toString();
	}
	
	private void appendComparison(StringBuilder sb) {
		if(comparisonEnum == null) {
			sb.append(value);
			return;
		}
		switch (comparisonEnum) {
		case LESS:
			sb.append(CharacterConstant.LESS).append(value);
			break;
		case LESS_OR_EQUAL:
			sb.append(CharacterConstant.LESSOR_EQUAL).append(value);
			break;
		case GREATER:
			sb.append(CharacterConstant.GREATER).append(value);
			break;
		case GREATER_OR_EQUAL:
			sb.append(CharacterConstant.GREATER_EQUAL).append(value);
			break;
		case CONTAIN:
			sb.append(CharacterConstant.RIGHT_BRACE).append(value).append(CharacterConstant.LEFT_BRACE);
			break;
		}
	}
	
}
