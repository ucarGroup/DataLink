/**
 * Description: BatchDocument.java
 * All Rights Reserved.
 * @version 4.1  2016-6-1 上午11:01:09  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest.index;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

/**
 *  查看index mapping 关系
 * <br/> Created on 2016-6-1 上午11:01:09
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class ViewMappingIndex extends AbstractRequestEs {
	
	private static final ViewMappingIndex VI = new ViewMappingIndex();
	
	private ViewMappingIndex(){}
	
	public static ViewMappingIndex getInstance(){
		return VI ;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.AbstractRequestEs#getHttpUriRequest(com.ucar.datalink.writer.es.vo.VoItf)
	 */
	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		
		HttpGet get = new HttpGet(vo.getUrl());
		
		return get;
	}

}
