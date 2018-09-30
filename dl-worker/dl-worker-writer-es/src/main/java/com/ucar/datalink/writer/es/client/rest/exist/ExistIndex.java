/**
 * 
 */
package com.ucar.datalink.writer.es.client.rest.exist;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;


public class ExistIndex extends AbstractRequestEs {

	private static final ExistIndex EC = new ExistIndex();

	private ExistIndex(){}
	
	public static ExistIndex getInstance(){
		return EC ;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.AbstractRequestEs#getHttpUriRequest()
	 */
	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		return new HttpGet(vo.getUrl());
	}

}
