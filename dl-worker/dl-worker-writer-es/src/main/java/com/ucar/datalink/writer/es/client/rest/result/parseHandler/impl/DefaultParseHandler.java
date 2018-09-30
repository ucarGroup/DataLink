package com.ucar.datalink.writer.es.client.rest.result.parseHandler.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Description: 默认查询解析器
 * All Rights Reserved.
 * Created on 2016-7-22 下午6:18:58
 * @author  孔增（kongzeng@zuche.com）
 */
public class DefaultParseHandler extends ParseHandler {
	
	@Override
	public List<String> parseData(JSONObject json) {
		
		List<String> list = new ArrayList<String>();
		
		if(json == null) {
			return list;
		}
		
		JSONObject hits = json.getJSONObject(HITS_NAME);
		
		if(hits == null) {
			return list;
		}
		
		JSONArray results = hits.getJSONArray(HITS_NAME);
		
		if(results == null) {
			return list;
		}
		
		for(Object obj : results) {
			JSONObject jb = (JSONObject)obj;
			String fields = jb.getString(SOURCE_NAME);
			if(fields == null) {
				fields = jb.getString(FIELDS_NAME);
			}
			if(fields != null) {
				list.add(fields);
			}
		}
		return list;
	}

}
