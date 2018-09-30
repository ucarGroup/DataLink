/**
 * Description: UpdatePartDocument.java
 * All Rights Reserved.
 * @version 4.0  2016-5-25 下午5:00:12  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest.update;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.vo.CRDResultVo;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import com.ucar.datalink.writer.es.client.rest.result.ProcessResult;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 *  update  part document
 * <br/> Created on 2016-5-25 下午5:00:12
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class UpdatePartDoc extends AbstractRequestEs {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePartDoc.class);
	
	private static final UpdatePartDoc UC = new UpdatePartDoc();
	
	private UpdatePartDoc(){}
	
	public static UpdatePartDoc getInstance(){
		return UC ;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.AbstractRequestEs#getHttpUriRequest(com.ucar.datalink.writer.es.vo.DocumentVo)
	 */
	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {

		HttpPost post = new HttpPost(vo.getUrl()+"/_update");
		
		return post;
	}
	
	public CRDResultVo updateDoc(VoItf vo , Map<String , Object> row) {
		
		Map<String , Object> doc = new HashMap<String, Object>();
		doc.put("doc", row);
		try {
			String json = vo.toJsonString(doc);
			
			CRDResultVo resultVO =  (CRDResultVo) ProcessResult.parseResult(super.processRequest(vo, json.getBytes("utf-8")), ESEnum.ParseEnum.PARTUPDATE);
			
			return resultVO;
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("部分更新时发生异常", e);
		}
		return null ;
	}
	

}
