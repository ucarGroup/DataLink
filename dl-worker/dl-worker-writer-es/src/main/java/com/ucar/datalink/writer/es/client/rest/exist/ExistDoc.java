/**
 * 
 */
package com.ucar.datalink.writer.es.client.rest.exist;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * exist documents
 *  
 * <br/> Created on 2016-5-23 上午9:49:48
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class ExistDoc extends AbstractRequestEs {
	
	private static final ExistDoc EC = new ExistDoc();
	
	private ExistDoc(){}
	
	public static ExistDoc getInstance(){
		return EC ;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.AbstractRequestEs#getHttpUriRequest()
	 */
	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		
		HttpHead head = new HttpHead(vo.getUrl());
		
		return head;
	}

}
