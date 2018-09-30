/**
 * 
 */
package com.ucar.datalink.writer.es.client.rest.cluster;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * es 集群信息
 *  
 * <br/> Created on 2016-6-25 下午5:41:06
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class Cluster extends AbstractRequestEs {
	
	private static final Cluster C = new Cluster();
	
	private Cluster(){}
	
	public static Cluster getInstance(){
		return C ;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.AbstractRequestEs#getHttpUriRequest()
	 */
	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		
		HttpGet put = new HttpGet(vo.getUrl() );
		
		return put;
	}

}
