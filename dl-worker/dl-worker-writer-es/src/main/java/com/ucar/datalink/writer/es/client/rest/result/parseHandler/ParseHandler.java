package com.ucar.datalink.writer.es.client.rest.result.parseHandler;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.exception.ElasticSearchException;

/**
 * 
 * Description: 返回结果解析器
 * All Rights Reserved.
 * Created on 2016-7-20 下午2:31:25
 * @author  孔增（kongzeng@zuche.com）
 */
public abstract class ParseHandler {
	public static final String HITS_NAME = "hits";
	public static final String INDEX_NAME = "_index";
	public static final String TYPE_NAME = "_type";
	public static final String ID_NAME = "_id";
	public static final String SCORE_NAME = "_score";
	public static final String SOURCE_NAME = "_source";
	public static final String HIGHLIGHT = "highlight";
    public static final String FIELDS_NAME = "fields";
	public static final String ERROR_NAME = "error";
	public static final String COUNT_NAME = "count";
	public static final String ACKNOWLEDGED_NAME = "acknowledged";
	public static final String TOTAL_NAME = "total";
	public static final String CREATED_NAME = "created";
	public static final String ITEMS_NAME = "items";
	public static final String SHARD_NAME = "_shards";
	public static final String SUCCESS_NAME = "successful";
	public static final String FAILED_NAME = "failed";
	public static final String FOUND_NAME = "found";
	public static final String VERSION_NAME = "_version";
	public static final String STATUS_NAME = "status";
	public static final String ERRORS_NAME = "errors";
	public static final String TOOK_NAME = "took";
	/**
	 * 
	 * Description: 结果解析
	 * Created on 2016-8-19 下午5:40:39
	 * @author  孔增（kongzeng@zuche.com）
	 * @param json
	 * @return
	 */
	public Object parse(JSONObject json) {
		Object error = checkError(json);
		if(error == null) {
			return parseData(json);
		}else {
			return parseError(json, error);
		}
	}
	/**
	 * 
	 * Description: 解析不含error的返回结果
	 * Created on 2016-8-19 下午5:39:40
	 * @author  孔增（kongzeng@zuche.com）
	 * @param json
	 * @return
	 */
	public abstract Object parseData(JSONObject json);
	/**
	 * 
	 * Description: 解析含error的返回结果
	 * Created on 2016-8-19 下午5:39:57
	 * @author  孔增（kongzeng@zuche.com）
	 * @param originalData
	 * @param error
	 * @return
	 */
	public Object parseError(JSONObject originalData, Object error) {
		throw new ElasticSearchException(error.toString());
	}
	
	/**
	 * 
	 * Description: 异常处理
	 * Created on 2016-6-13 下午1:40:11
	 * @author  孔增（kongzeng@zuche.com）
	 * @param json
	 */
	public Object checkError(JSONObject json) {
		
		if(json == null) {
			return null;
		}
		
		return json.getJSONObject(ERROR_NAME);

	}
	
}
