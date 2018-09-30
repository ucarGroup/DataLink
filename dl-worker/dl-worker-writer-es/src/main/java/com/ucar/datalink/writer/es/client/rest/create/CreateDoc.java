/**
 * 
 */
package com.ucar.datalink.writer.es.client.rest.create;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.ParseEnum;
import com.ucar.datalink.writer.es.client.rest.result.ProcessResult;
import com.ucar.datalink.writer.es.client.rest.vo.CRDResultVo;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.UnsupportedEncodingException;

/**
 * create document
 *  
 * <br/> Created on 2016-5-23 上午9:49:48
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class CreateDoc extends AbstractRequestEs {
	
	private static final CreateDoc CD = new CreateDoc();
	
	private CreateDoc(){}
	
	public static CreateDoc getInstance(){
		return CD ;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.AbstractRequestEs#getHttpUriRequest()
	 */
	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {

		HttpPut put = new HttpPut(vo.getUrl() );

		return put;
	}
	/**
	 * 
	 * Description: 插入文档
	 * Created on 2016-8-11 下午5:23:56
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public CRDResultVo create(VoItf vo,String json) throws UnsupportedEncodingException {
		
		CRDResultVo resultVO = (CRDResultVo) ProcessResult.parseResult(processRequest(vo, json.getBytes("utf-8")), ParseEnum.CREATEANDALLUPDATE);
		
		return resultVO;
	}

}
