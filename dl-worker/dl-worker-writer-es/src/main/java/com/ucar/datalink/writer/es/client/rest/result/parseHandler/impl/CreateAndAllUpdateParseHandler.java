package com.ucar.datalink.writer.es.client.rest.result.parseHandler.impl;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;
import com.ucar.datalink.writer.es.client.rest.vo.CRDResultVo;

/**
 * 
 * Description: 插入和全部更新返回结果解析器
 * All Rights Reserved.
 * Created on 2016-7-22 下午6:18:58
 * @author  孔增（kongzeng@zuche.com）
 */
public class CreateAndAllUpdateParseHandler extends ParseHandler {
	
	@Override
	public CRDResultVo parseData(JSONObject json) {
		
		CRDResultVo vo = new CRDResultVo();
		
		if(json == null) {
			return vo;
		}
		
		vo.setJsonString(json.toJSONString());
		
		boolean isCreate = json.getBooleanValue(CREATED_NAME);
		
		if(isCreate) {
			vo.setOperateType(ESEnum.OperateTypeEnum.CREATE);
		}else {
			vo.setOperateType(ESEnum.OperateTypeEnum.ALLUPDATE);
		}
		
		vo.setIndex(json.getString(INDEX_NAME));
		vo.setType(json.getString(TYPE_NAME));
		vo.setId(json.getString(ID_NAME));
		
		vo.setSuccess(true);
		
		return vo;
		
	}

}
