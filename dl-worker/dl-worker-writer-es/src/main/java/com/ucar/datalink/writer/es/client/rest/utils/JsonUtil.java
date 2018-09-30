package com.ucar.datalink.writer.es.client.rest.utils;

import com.alibaba.fastjson.JSONObject;

public class JsonUtil {
	
	public static Object getValueByPath(JSONObject obj, String path) {
		
		String[] keys = path.split("/");
		Object tem = obj;
		
		for(String key : keys) {
			
			if(tem instanceof JSONObject) {
				tem = ((JSONObject)tem).get(key);
			}else{
				return null;
			}
			
		}
		
		return tem;
	}

}
