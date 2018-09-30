package com.ucar.datalink.writer.es.client.rest.vo.alias;
/**
 * 
 * Description: 别名信息
 * All Rights Reserved.
 * Created on 2016-7-22 下午4:27:56
 * @author  孔增（kongzeng@zuche.com）
 */
public class AliasInfo {
	/**
	 * 别名名称
	 */
	private String alias;
	/**
	 * 别名其他信息
	 */
	private String otherInfo;
	
	
	public AliasInfo (String alias, String otherInfo) {
		this.alias = alias;
		this.otherInfo = otherInfo;
	}
	
	
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getOtherInfo() {
		return otherInfo;
	}
	public void setOtherInfo(String otherInfo) {
		this.otherInfo = otherInfo;
	}


	@Override
	public String toString() {
		return "AliasInfo [alias=" + alias + ", otherInfo=" + otherInfo + "]";
	}
	
	
	
}
