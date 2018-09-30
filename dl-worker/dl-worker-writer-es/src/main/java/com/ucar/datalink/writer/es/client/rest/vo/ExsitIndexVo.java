/**
 * Description: ViewMappingDocumentVo.java
 * All Rights Reserved.
 * @version 4.1  2016-6-2 下午3:29:57  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest.vo;

import java.io.Serializable;

public class ExsitIndexVo extends VoItf implements Serializable {

	private String index ;

    public ExsitIndexVo(String indexName) {
        this.index = indexName;
    }

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	@Override
	public String getUrl() {
		return "http://" + host +"/_cat/indices/"+index ;
	}

}
