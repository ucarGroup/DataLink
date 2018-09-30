package com.ucar.datalink.writer.es.client.rest.vo.alias;

import com.ucar.datalink.writer.es.client.rest.vo.VoItf;

/**
 * 
 * Description: 查询索引关联的别名，无别名的索引将不显示
 * All Rights Reserved.
 * Created on 2016-7-22 下午1:29:08
 * @author  孔增（kongzeng@zuche.com）
 */
public class SearchAliasVo extends VoItf {
	
    public SearchAliasVo() {}
	
	public SearchAliasVo(String clusterName) {
		super.clusterName = clusterName;
	}
	
	
	@Override
	public String getUrl() {
		String url = "http://" + host ;
		
		if(index != null) {
			url = url + "/" + index ;
		}
		
		url = url + "/_aliases";
		
		return url;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

}
