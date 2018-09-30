package com.ucar.datalink.writer.es.client.rest.vo;


import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 *  查看  _mapping  vo
 * <br/> Created on 2016-6-2 下午3:29:57
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class MappingIndexVo extends VoItf implements Serializable {
	
	//_mapping、_settings
	private String metaType ;
	
    public MappingIndexVo() {}
	
	public MappingIndexVo(String clusterName) {
		super.clusterName = clusterName;
	}

	public String getMetaType() {
		return metaType;
	}

	public void setMetaType(String metaType) {
		this.metaType = metaType;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.vo.VoItf#getUrl()
	 */
	@Override
	public String getUrl() {
		
		String url = "http://" + host +"/"+index ;
		if(!StringUtils.isEmpty(metaType)){
			url = url + "/"+metaType ;
		}
		if(!StringUtils.isEmpty(type)){
			url = url +"/" + type ;
		}
		return url;
	}

}
