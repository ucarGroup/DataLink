/**
 * Description: BatchDocument.java
 * All Rights Reserved.
 * @version 4.1  2016-6-1 上午11:01:09  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest.batch;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.result.ProcessResult;
import com.ucar.datalink.writer.es.client.rest.vo.BatchContentVo;
import com.ucar.datalink.writer.es.client.rest.vo.BatchDocVo;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import com.ucar.datalink.writer.es.client.rest.vo.BulkResultVo;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 *  
 * <br/> Created on 2016-6-1 上午11:01:09
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class BatchDoc extends AbstractRequestEs {
	
	private static final BatchDoc BI = new BatchDoc();
	
	private BatchDoc(){}
	
	public static BatchDoc getInstance(){
		return BI ;
	}


	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		
		HttpPost post = new HttpPost(vo.getUrl());
		
		return post;
	}
	
	public BulkResultVo batchDoc(BatchDocVo vo , List<BatchContentVo> contents) throws UnsupportedEncodingException {
		
		if(contents == null ||contents.size() == 0) {
			return new BulkResultVo();
		}
		
		StringBuilder sb = new StringBuilder();
		for(BatchContentVo contentVo : contents) {
			sb.append(contentVo.toString());
		}
		
		vo.setContents(sb.toString());
		
		BulkResultVo resultVo = (BulkResultVo) ProcessResult.parseResult(processRequest(vo, vo.getContents().getBytes("utf-8")), ESEnum.ParseEnum.BULK);
	   
	    return resultVo;
	}

}
