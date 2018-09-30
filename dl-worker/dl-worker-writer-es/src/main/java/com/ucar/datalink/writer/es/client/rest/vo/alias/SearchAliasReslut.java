package com.ucar.datalink.writer.es.client.rest.vo.alias;

import java.util.List;

/**
 * 
 * Description: 查询索引别名结果集
 * All Rights Reserved.
 * Created on 2016-7-22 下午4:27:56
 * @author  孔增（kongzeng@zuche.com）
 */
public class SearchAliasReslut {
	/**
	 * 索引名称
	 */
	private String index;
	/**
	 * 索引下的所有别名信息
	 */
	private List<AliasInfo> aliases;
	
	public SearchAliasReslut (String index) {
		this.index = index;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public List<AliasInfo> getAliases() {
		return aliases;
	}

	public void setAliases(List<AliasInfo> aliases) {
		this.aliases = aliases;
	}

	@Override
	public String toString() {
		return "SearchAliasReslut [index=" + index + ", aliases=" + aliases
				+ "]";
	}
	
}
