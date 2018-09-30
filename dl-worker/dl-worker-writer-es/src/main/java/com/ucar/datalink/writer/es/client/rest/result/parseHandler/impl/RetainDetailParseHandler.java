package com.ucar.datalink.writer.es.client.rest.result.parseHandler.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;
import com.ucar.datalink.writer.es.client.rest.vo.search.SearchResultDetailVO;
import com.ucar.datalink.writer.es.client.rest.vo.search.SearchResultVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Description: 带有详细信息的查询结果解析器
 * All Rights Reserved.
 * Created on 2016-7-22 下午6:19:16
 * @author  孔增（kongzeng@zuche.com）
 */
public class RetainDetailParseHandler extends ParseHandler {
	
	@Override
	public SearchResultVo parseData(JSONObject json) {
		
		SearchResultVo resultVo = new SearchResultVo();
		
		List<SearchResultDetailVO> list = new ArrayList<SearchResultDetailVO>();
		
		if(json == null) {
			return resultVo;
		}
		
		resultVo.setOriginalData(json);
		
		resultVo.setResults(list);
		
		JSONObject hits = json.getJSONObject(HITS_NAME);
		
		if(hits == null) {
			return resultVo;
		}
		
		Integer total = hits.getInteger(TOTAL_NAME);
		
		if(total != null) {
			resultVo.setTotal(total);
		}

        String scrollId = json.getString("_scroll_id");

        if(scrollId != null) {
            resultVo.setScrollId(scrollId);
        }

		JSONArray results = hits.getJSONArray(HITS_NAME);
		
		if(results == null) {
			return resultVo;
		}
		
		for(Object obj : results) {
			JSONObject jb = (JSONObject)obj;
			SearchResultDetailVO detailVo = new SearchResultDetailVO();
			detailVo.setIndex(jb.getString(INDEX_NAME));
			detailVo.setType(jb.getString(TYPE_NAME));
			detailVo.setId(jb.getString(ID_NAME));
			detailVo.setScore(jb.getFloat(SCORE_NAME));
			detailVo.setVersion(jb.getLong(VERSION_NAME));
			detailVo.setHighlight(jb.getString(HIGHLIGHT));
			String fields = jb.getString(SOURCE_NAME);
			if(fields == null) {
				fields = jb.getString(FIELDS_NAME);
			}
		    detailVo.setResult(fields);
		    list.add(detailVo);
		}
		
		return resultVo;
	}

}
