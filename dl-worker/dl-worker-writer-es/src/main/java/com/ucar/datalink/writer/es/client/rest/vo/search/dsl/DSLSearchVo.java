package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

import com.ucar.datalink.writer.es.client.rest.exception.ElasticSearchException;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.commons.lang.StringUtils;

/**
 *
 * Description: 结构化查询vo
 * All Rights Reserved.
 * Created on 2016-6-30 上午11:09:59
 * @author  孔增（kongzeng@zuche.com）
 */
public class DSLSearchVo extends VoItf{

	/**
	 * 可指定自定义的json串，若已指定searchContext，则以searchContext为准
	 */
	private String content ;

	private String metaType = "_search";

	/**
	 * 结构化查询上下文
	 */
	private SearchContext searchContext;

	private String templateName;

	public DSLSearchVo() {}

	public DSLSearchVo(String clusterName) {
		super.clusterName = clusterName;
	}


	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public SearchContext getSearchContext() {
		return searchContext;
	}

	public void setSearchContext(SearchContext searchContext) {
		this.searchContext = searchContext;
	}

	public String getMetaType() {
		return metaType;
	}

	public void setMetaType(String metaType) {
		this.metaType = metaType;
	}

	public String getCondition() {

		if(searchContext != null) {

			searchContext.checkSearchContext();

			return searchContext.toString();
		}

		return content;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	@Override
	public String getUrl() {
		StringBuffer lastUrl =  new StringBuffer("http://" + host);

		if(index == null && type != null) {
			throw new ElasticSearchException("when type is not null ,the index is required");
		}
		if(index != null) {
			lastUrl.append("/"+index);
		}

		if(!StringUtils.isBlank(type)) {
			lastUrl.append("/"+ type);
		}

		lastUrl.append("/").append(metaType);

		return lastUrl.toString();
	}


}
