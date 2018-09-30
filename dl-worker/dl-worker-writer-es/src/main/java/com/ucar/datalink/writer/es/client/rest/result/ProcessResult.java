package com.ucar.datalink.writer.es.client.rest.result;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.client.EsClient;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.exception.ElasticSearchException;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;
import com.ucar.datalink.writer.es.client.rest.vo.stat.CollectInfoVo;

/**
 * 
 * Description: 处理返回结果
 * All Rights Reserved.
 * Created on 2016-6-12 下午3:47:58
 * @author  孔增（kongzeng@zuche.com）
 */
public class ProcessResult {
	
	public static Object parseResult(String result) {
		return parseResult(result, ESEnum.ParseEnum.DEFAULT);
	}
	
	public static Object parseResult(String result, ESEnum.ParseEnum parseEnum) {
		long start = System.currentTimeMillis();
        Long serverTime = null;
		try {
			JSONObject json = JSONObject.parseObject(result);
			if(json != null) {
				serverTime = json.getLong(ParseHandler.TOOK_NAME);
			}
			return parseEnum.getParseHandler().parse(json);
		}catch (Exception e) {
			throw new ElasticSearchException(result);
		} finally {
			CollectInfoVo collectInfoVo = EsClient.COMMAND_STAT_THREADLOCAL.get();
			if(collectInfoVo != null) {
				collectInfoVo.setServerTime(serverTime);
				collectInfoVo.setParseTime(System.currentTimeMillis() - start);
			}
		}
		
	}
	
	/**
	 * 
	 * Description: 异常处理
	 * Created on 2016-6-13 下午1:40:11
	 * @author  孔增（kongzeng@zuche.com）
	 * @param json
	 */
	public static void checkError(JSONObject json) {
		if(json == null) {
			return ;
		}
		JSONObject error = json.getJSONObject(ParseHandler.ERROR_NAME);
		if(error == null) {
			return;
		}
		throw new ElasticSearchException(error.toJSONString());
	}
}
