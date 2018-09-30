/**
 * 
 */
package com.ucar.datalink.writer.es.client.rest.del;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.vo.SimpleDocVo;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.ParseEnum;
import com.ucar.datalink.writer.es.client.rest.result.ProcessResult;
import com.ucar.datalink.writer.es.client.rest.vo.CRDResultVo;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import com.ucar.datalink.writer.es.util.Assert;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * del documents
 *  
 * <br/> Created on 2016-5-23 上午9:49:48
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class DelDoc extends AbstractRequestEs {
	
	private static final DelDoc DC = new DelDoc();
	
	private DelDoc(){}
	
	public static DelDoc getInstance(){
		return DC ;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.AbstractRequestEs#getHttpUriRequest()
	 */
	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		
		HttpDelete del = new HttpDelete(vo.getUrl());
		
		return del;
	}
	
	public CRDResultVo deleteDoc(SimpleDocVo vo) {
		Assert.notNull(vo.getId(), "id不能为空");
		
		CRDResultVo resultVO =  (CRDResultVo) ProcessResult.parseResult(processRequest(vo, null), ParseEnum.DELETE);
	   
		return resultVO;
	}

}
