/**
 * Description: ViewMappingDocumentVo.java
 * All Rights Reserved.
 * @version 4.1  2016-6-2 下午3:29:57  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest.vo;


import org.apache.commons.lang.StringUtils;

public class MetaVo extends VoItf {

	private String metaType ;
	private String metaSubType ;

    public MetaVo() {}

	public MetaVo(String clusterName) {
		super.clusterName = clusterName;
	}

	public String getMetaType() {
		return metaType;
	}

	public void setMetaType(String metaType) {
		this.metaType = metaType;
	}

    public String getMetaSubType() {
        return metaSubType;
    }

    public void setMetaSubType(String metaSubType) {
        this.metaSubType = metaSubType;
    }

    public void setIndex(String index) {
        //("no need to set indexname");
    }

	@Override
	public String getUrl() {

		String url = "http://" + host;
		if(!StringUtils.isEmpty(metaType)){
			url = url + "/"+metaType ;
		}
		if(!StringUtils.isEmpty(metaSubType)){
			url = url +"/" + metaSubType ;
		}
		return url;
	}
}
