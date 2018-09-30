package com.ucar.datalink.writer.es.client.rest.vo;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.exception.ElasticSearchException;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 
 * Description: 批量操作返回结果
 * All Rights Reserved.
 * Created on 2016-8-12 下午1:54:04
 * @author  孔增（kongzeng@zuche.com）
 */
public class BulkResultVo implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkResultVo.class);
	//总执行结果
	public boolean isSuccess =  false;
	//原始json串
	private String jsonString;
	//返回结果json对象
	private JSONObject jsonObject;


	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	
	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	/**
	 *
	 * Description: 校验是否有失败的操作
	 * Created on 2017-1-3 下午9:45:47
	 * @author  孔增（kongzeng@zuche.com）
	 */
	public void checkFailed() {
		if (jsonObject != null && jsonObject.containsKey(ParseHandler.ITEMS_NAME)) {
			for (Object obj : jsonObject.getJSONArray(ParseHandler.ITEMS_NAME)) {
				JSONObject jso = (JSONObject)obj;
				for (String key : jso.keySet()) {
					JSONObject item = jso.getJSONObject(key);
					JSONObject shards = item.getJSONObject(ParseHandler.SHARD_NAME);
					if(shards == null) {
						continue;
					}
					Integer failed = shards.getIntValue(ParseHandler.FAILED_NAME);
					if(failed > 0) {
						throw new ElasticSearchException(item.toJSONString());
					}
				}
			}
		}

	}


}
