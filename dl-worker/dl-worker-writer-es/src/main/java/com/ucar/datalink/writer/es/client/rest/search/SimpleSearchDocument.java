package com.ucar.datalink.writer.es.client.rest.search;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.vo.SimpleDocVo;
import com.ucar.datalink.writer.es.client.rest.vo.search.SearchResultDetailVO;
import com.ucar.datalink.writer.es.client.rest.vo.search.SearchResultVo;
import com.ucar.datalink.writer.es.client.rest.result.ProcessResult;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import com.ucar.datalink.writer.es.client.rest.vo.search.simple.SimpleSearchDocumentVo;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.List;

/**
 * 
 * Description: 根据id检索文档
 * All Rights Reserved.
 * Created on 2016-6-12 下午3:47:58
 * @author  孔增（kongzeng@zuche.com）
 */
public class SimpleSearchDocument extends AbstractRequestEs {
	
    private static final SimpleSearchDocument RD = new SimpleSearchDocument();
    
	
	private SimpleSearchDocument(){}
	
	public static SimpleSearchDocument getInstance(){
		return RD ;
	}

	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		HttpGet get = new HttpGet(vo.getUrl() );
		return get;
	}
	
	/**
	 * 
	 * Description: 检索文档
	 * Created on 2016-6-12 下午5:08:45
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	public SearchResultDetailVO searchDocumentById(SimpleDocVo vo) {
		return (SearchResultDetailVO) ProcessResult.parseResult(super.processRequest(vo, null), ESEnum.ParseEnum.SINGLE);
	}
	/**
	 * 
	 * Description: 根据关键字查询
	 * Created on 2016-6-12 下午5:55:51
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<String> searchDocumentByKey(SimpleSearchDocumentVo vo) {
		
		return (List<String>) ProcessResult.parseResult(super.processRequest(vo, null));
	}
	
	/**
	 * 
	 * Description: 根据关键字查询,包含其他信息
	 * Created on 2016-6-12 下午5:55:51
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	public SearchResultVo searchByKey(SimpleSearchDocumentVo vo) {
		
		return (SearchResultVo) ProcessResult.parseResult(super.processRequest(vo, null), ESEnum.ParseEnum.RETAINDETAIL);
	}

}
