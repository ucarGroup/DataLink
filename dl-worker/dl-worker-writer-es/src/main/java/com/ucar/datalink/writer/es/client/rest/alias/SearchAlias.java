package com.ucar.datalink.writer.es.client.rest.alias;

import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.ParseEnum;
import com.ucar.datalink.writer.es.client.rest.result.ProcessResult;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import com.ucar.datalink.writer.es.client.rest.vo.alias.SearchAliasVo;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * 
 * Description: 查询别名
 * All Rights Reserved.
 * Created on 2016-7-22 下午3:14:43
 * @author  孔增（kongzeng@zuche.com）
 */
public class SearchAlias extends AbstractRequestEs {
	
	private static final SearchAlias SA = new SearchAlias();
	
	private SearchAlias(){}
	
	public static SearchAlias getInstance() {
		return SA;
	}

	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		HttpGet get = new HttpGet(vo.getUrl());
		return get;
	}

	public Object searchAlias(SearchAliasVo vo) {
		return ProcessResult.parseResult(super.processRequest(vo, null), ParseEnum.SEARCHALIAS);
	}
	
}
