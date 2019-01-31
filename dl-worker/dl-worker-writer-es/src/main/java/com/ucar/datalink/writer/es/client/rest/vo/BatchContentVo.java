package com.ucar.datalink.writer.es.client.rest.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.BatchActionEnum;
import com.ucar.datalink.writer.es.util.Assert;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BatchContentVo implements Serializable {

	private BatchActionEnum batchActionEnum;

	private String index;

	private String type;

	private String id;
	//转化json串时，保留值为null的字段，使其能将值为null的字段传递到服务端,默认自动过滤掉值为null的字段
	private boolean retainNullValue = false;
	//指定其他支持的配置
	private Map<String,Object> selfConfig = new HashMap<String, Object>();

	private Object content;

	public BatchActionEnum getBatchActionEnum() {
		return batchActionEnum;
	}

	public void setBatchActionEnum(BatchActionEnum batchActionEnum) {
		this.batchActionEnum = batchActionEnum;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public boolean isRetainNullValue() {
		return retainNullValue;
	}

	public void setRetainNullValue(boolean retainNullValue) {
		this.retainNullValue = retainNullValue;
	}

	public BatchContentVo putConfig(String configName, Object configValue) {
		selfConfig.put(configName, configValue);
		return this;
	}

	@Override
	public String toString() {
		Assert.notNull(batchActionEnum, "batchActionEnum 不能为空！");
		if(!BatchActionEnum.DELETE.equals(batchActionEnum)) {
			Assert.notNull(content, "content 不能为空！");
		}
		StringBuilder sb = new StringBuilder();
		JSONObject actionJson = new JSONObject();
		JSONObject actionBodyJson =  new JSONObject();

		if(!StringUtils.isBlank(index)) {
			actionBodyJson.put("_index", index);
		}

		if(!StringUtils.isBlank(type)) {
			actionBodyJson.put("_type", type);
		}

		if(!StringUtils.isBlank(id)) {
			actionBodyJson.put("_id", id);
		}

		actionBodyJson.putAll(selfConfig);

		if(batchActionEnum == BatchActionEnum.UPSERT){
			actionJson.put(BatchActionEnum.UPDATE.getName(), actionBodyJson);
		}else{
			actionJson.put(batchActionEnum.getName(), actionBodyJson);
		}

		sb.append(actionJson.toJSONString()).append("\n");
		if(content != null) {

            String json = null;
            if(retainNullValue) {
            	json = JSONObject.toJSONString(content, SerializerFeature.WriteMapNullValue);
            }else {
				json = JSONObject.toJSONString(content);
            }

			if(BatchActionEnum.UPDATE.equals(batchActionEnum)) {
				json = "{ \"doc\" : " + json + " }";
			}

			if(BatchActionEnum.UPSERT.equals(batchActionEnum)) {
				json = "{ \"doc\" : " + json + " ,\"doc_as_upsert\" : true}";
			}

			sb.append(json).append("\n");
		}
		return sb.toString();
	}

}
