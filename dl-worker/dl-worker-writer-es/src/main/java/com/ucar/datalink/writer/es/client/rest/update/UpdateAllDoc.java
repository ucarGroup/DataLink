/**
 * 
 */
package com.ucar.datalink.writer.es.client.rest.update;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.vo.SimpleDocVo;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.ParseEnum;
import com.ucar.datalink.writer.es.client.rest.result.ProcessResult;
import com.ucar.datalink.writer.es.client.rest.vo.CRDResultVo;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.UnsupportedEncodingException;

/**
 * update all  document
 *  
 * <br/> Created on 2016-5-23 上午9:49:48
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class UpdateAllDoc extends AbstractRequestEs {
	
	private static final UpdateAllDoc UI = new UpdateAllDoc();
	
	private UpdateAllDoc(){}
	
	public static UpdateAllDoc getInstance(){
		return UI ;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.AbstractRequestEs#getHttpUriRequest()
	 */
	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {

		String url = vo.getUrl();
		if (vo instanceof SimpleDocVo) {
			SimpleDocVo simpleDocVo = (SimpleDocVo)vo;
			if (simpleDocVo.getVersion() != null && !simpleDocVo.getVersion().isEmpty()) {
				url += "?version_type=external&version="+simpleDocVo.getVersion();
			}
		}

		return new HttpPut(url);
	}
	
	
	public CRDResultVo updateAllDoc(VoItf vo , String json) throws UnsupportedEncodingException {
		
		CRDResultVo resultVO =  (CRDResultVo) ProcessResult.parseResult(processRequest(vo, json.getBytes("utf-8")), ParseEnum.CREATEANDALLUPDATE);
	    
		return resultVO;
	}

}
