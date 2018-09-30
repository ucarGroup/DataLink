package com.ucar.datalink.writer.es.client.rest.result.parseHandler.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.vo.alias.AliasInfo;
import com.ucar.datalink.writer.es.client.rest.vo.alias.SearchAliasReslut;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * 
 * Description: 查询别名返回结果解析
 * All Rights Reserved.
 * Created on 2016-7-22 下午4:36:21
 * @author  孔增（kongzeng@zuche.com）
 */
public class SearchAliasParseHandler extends ParseHandler {
	
	@Override
	public List<SearchAliasReslut> parseData(JSONObject json) {
		
		List<SearchAliasReslut> list = new ArrayList<SearchAliasReslut>();
		
		if(json == null) {
			return list;
		}
		
		Iterator<Entry<String,Object>> ito = json.entrySet().iterator();
		while(ito.hasNext()) {
			Entry<String,Object> entry = ito.next();
			String index = entry.getKey();
			JSONObject obj = json.getJSONObject(index).getJSONObject("aliases");
			if(obj.size() == 0) {
				continue;
			}
			SearchAliasReslut sar = new SearchAliasReslut(index);
			List<AliasInfo> aliasList = new ArrayList<AliasInfo>();
			sar.setAliases(aliasList);
			Iterator<Entry<String,Object>> aliasIto = obj.entrySet().iterator();
			while(aliasIto.hasNext()) {
				Entry<String,Object> aliasEntry = aliasIto.next();
				aliasList.add(new AliasInfo(aliasEntry.getKey(), String.valueOf(aliasEntry.getValue())));
			}
			
			list.add(sar);
			
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		long t = System.currentTimeMillis();
		for(int i =0 ;i< 10000;i++) {
			String ss = "{\"cdms_test\":{\"aliases\":{\"test1\":{\"index_routing\":\"cdms_test\",\"search_routing\":\"cdms_test\"},\"test2\":{\"index_routing\":\"cdms_test\",\"search_routing\":\"cdms_test\"}}}}";
			JSONObject json = JSONObject.parseObject(ss);
			SearchAliasParseHandler a = new SearchAliasParseHandler();
			a.parseData(json);
		}
		System.out.println(System.currentTimeMillis()-t);
		
	}

}
