package com.ucar.datalink.writer.es.client.rest.result.parseHandler.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.exception.ElasticSearchException;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;
import com.ucar.datalink.writer.es.client.rest.vo.CRDResultVo;

/**
 * 
 * Description: 部分更新返回结果解析器
 * All Rights Reserved.
 * Created on 2016-7-22 下午6:18:58
 * @author  孔增（kongzeng@zuche.com）
 */
public class PartUpdateParseHandler extends ParseHandler {
	
	@Override
	public CRDResultVo parseData(JSONObject json) {
		
		CRDResultVo vo = new CRDResultVo();
		
		vo.setOperateType(ESEnum.OperateTypeEnum.UPDATE);
		
		if(json == null) {
			return vo;
		}
		
		vo.setJsonString(json.toJSONString());
		
		JSONObject shard = json.getJSONObject(SHARD_NAME);
		
		int failed = shard.getIntValue(FAILED_NAME);
		int success = shard.getIntValue(SUCCESS_NAME);
		
		if(success > 0 && failed == 0) {
			vo.setSuccess(true);
		}
		
		vo.setIndex(json.getString(INDEX_NAME));
		vo.setType(json.getString(TYPE_NAME));
		vo.setId(json.getString(ID_NAME));
		
		return vo;
		
	}
	
	
	@Override
	public Object parseError(JSONObject originalData, Object error) {
        
		CRDResultVo vo = new CRDResultVo();
		
		if(originalData == null) {
			return vo;
		}
		
		vo.setJsonString(originalData.toJSONString());
		
		vo.setSuccess(false);
		
		int status = originalData.getIntValue(STATUS_NAME);
		if(status != 404) {
			throw new ElasticSearchException(error.toString());
		}
		
		return vo;
	}
	
	

}
