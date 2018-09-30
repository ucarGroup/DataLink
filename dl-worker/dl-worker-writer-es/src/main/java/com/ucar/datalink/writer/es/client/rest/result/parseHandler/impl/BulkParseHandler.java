package com.ucar.datalink.writer.es.client.rest.result.parseHandler.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.exception.ElasticSearchException;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;
import com.ucar.datalink.writer.es.client.rest.vo.BulkResultVo;

/**
 * 
 * Description: 批量操作返回结果解析器
 * All Rights Reserved.
 * Created on 2016-7-22 下午6:18:58
 * @author  孔增（kongzeng@zuche.com）
 */
public class BulkParseHandler extends ParseHandler {
	
	@Override
	public BulkResultVo parseData(JSONObject json) {
		
		//校验是否有服务端级别异常
		Object errorMsg = super.checkError(json);
		if(errorMsg == null) {
			throw new ElasticSearchException(json.toString());
		}else {
			throw new ElasticSearchException(errorMsg.toString());
		}
		
	}
	
	@Override
	public Object parseError(JSONObject originalData, Object error) {
		
		BulkResultVo vo = new BulkResultVo();
		
		if(originalData == null) {
			return vo;
		}
		
		vo.setJsonString(originalData.toJSONString());
		vo.setJsonObject(originalData);
		
		if(error instanceof Boolean) {
			vo.setSuccess(!(Boolean)error);
		}
		
		return vo;
	}

	@Override
	public Object checkError(JSONObject json) {
		return json.getBoolean(ParseHandler.ERRORS_NAME);
	}
	
	

}
