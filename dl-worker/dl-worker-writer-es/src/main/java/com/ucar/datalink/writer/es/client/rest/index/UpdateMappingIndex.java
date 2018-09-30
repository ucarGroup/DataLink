/**
 * Description: BatchDocument.java
 * All Rights Reserved.
 * @version 4.1  2016-6-1 上午11:01:09  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest.index;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

/**
 *  UPDATE index mapping 关系
 * <br/> Created on 2016-6-1 上午11:01:09
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class UpdateMappingIndex extends AbstractRequestEs {
	
	private static final UpdateMappingIndex UI = new UpdateMappingIndex();
	
	private UpdateMappingIndex(){}
	
	public static UpdateMappingIndex getInstance(){
		return UI ;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.AbstractRequestEs#getHttpUriRequest(com.ucar.datalink.writer.es.vo.VoItf)
	 */
	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		return new HttpPost(vo.getUrl());
	}

}
