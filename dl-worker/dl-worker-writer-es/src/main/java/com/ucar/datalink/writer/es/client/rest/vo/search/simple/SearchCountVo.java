/**
 * 
 */
package com.ucar.datalink.writer.es.client.rest.vo.search.simple;

import com.ucar.datalink.writer.es.client.rest.constant.CharacterConstant;
import com.ucar.datalink.writer.es.client.rest.exception.ElasticSearchException;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * 
 * Description: 简单查询
 * All Rights Reserved.
 * Created on 2016-6-13 上午10:35:58
 * @author  孔增（kongzeng@zuche.com）
 */
public class SearchCountVo extends VoItf{
	
	private String key ;
	/**
	 * 请求超时时间（单位:ms）
	 */
	private Integer timeout;
	/**
	 * 起始位置
	 */
	private int from;
	/**
	 * 返回条数
	 */
	private int size;
	
    public SearchCountVo() {}
	
	public SearchCountVo(String clusterName) {
		super.clusterName = clusterName;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public void addKey(KeyVo keyVo) {
		if(key == null) {
			key = keyVo.toString();
		}else {
			key += CharacterConstant.SPACE + keyVo.toString();
		}
	}
	
	public String getUrl(){

		StringBuilder lastUrl =  new StringBuilder("http://" + host);
		
		if(StringUtils.isBlank(index) && !StringUtils.isBlank(type)) {
			throw new ElasticSearchException("when type is not null ,the index is required");
		}
		
		lastUrl.append("/"+index);
		
		if(!StringUtils.isBlank(type)) {
			lastUrl.append("/"+ type);
		}
		
		lastUrl.append("/_search");
		
		lastUrl.append(CharacterConstant.QUEST);
		
		if(timeout != null) {
			lastUrl.append("timeout="+timeout+"ms").append(CharacterConstant.AND);
		}
		
		if(size > 0) {
			lastUrl.append("from="+from).append(CharacterConstant.AND).append("size="+size).append(CharacterConstant.AND);
		}
		
		if(!StringUtils.isBlank(key)) {
			//替换url编码
			try {
				key = URLEncoder.encode(key, "utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new ElasticSearchException("url编码转换错误",e);
			}
			lastUrl.append("q="+key);
		}

		return lastUrl.toString() ;
	}
	
	
}
