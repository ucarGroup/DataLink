package com.ucar.datalink.writer.es.client.rest.vo.search;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.utils.JsonUtil;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * Description: 查询返回结果信息
 * All Rights Reserved.
 * Created on 2016-7-28 上午11:26:19
 * @author  孔增（kongzeng@zuche.com）
 */
public class SearchResultVo implements Serializable {
	/**
	 * 符合条件的总记录数
	 */
	private int total;
	/**
	 * 返回的文档信息
	 */
	private List<SearchResultDetailVO> results;
	/**
	 * 原始数据
	 */
	private JSONObject originalData;

    /** 游标 */
    private String scrollId;

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public int getTotal() {
		return total;
	}
	
	public void setTotal(int total) {
		this.total = total;
	}
	
	public List<SearchResultDetailVO> getResults() {
		return results;
	}
	
	public void setResults(List<SearchResultDetailVO> results) {
		this.results = results;
	}
	

	public JSONObject getOriginalData() {
		return originalData;
	}

	public void setOriginalData(JSONObject originalData) {
		this.originalData = originalData;
	}
	/**
	 * 根据key路径从源数据中获取值  eg: _shards/failed
	 * @param path
	 * @return
	 */
	public Object getValueByPath(String path) {
		return JsonUtil.getValueByPath(originalData, path);
	}

	@Override
	public String toString() {
		return "SearchResultVo [total=" + total + ", results=" + results
				+ ", originalData=" + originalData + "]";
	}

}
