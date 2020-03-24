package com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.constant.ESEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 批量更新插入提交vo
 * @date  2016-6-1 上午10:49:50
 * @author  李洪波(hb.li@zhuche.com)
 * @since 1.0
 */
public class BatchUpsertContentVo implements Serializable {

    private static final long serialVersionUID = -335566268956888871L;

    private ESEnum.BatchActionEnum batchActionEnum = ESEnum.BatchActionEnum.UPDATE;
	
	private String index;
	
	private String type;

    private String id;

    /**
     * routing值
     */
    private String routingValue;

	private int retryOnConflict = 3;

	/**转化json串时，保留值为null的字段，使其能将值为null的字段传递到服务端,默认自动过滤掉值为null的字段*/
	private boolean retainNullValue = false;
	/**指定其他支持的配置*/
	private Map<String,Object> selfConfig = new HashMap<String, Object>();
	
	private Map<String,Object> updateMap = new HashMap<String, Object>();;

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

    public String getRoutingValue() {
        return routingValue;
    }

    public void setRoutingValue(String routingValue) {
        this.routingValue = routingValue;
    }

    public BatchUpsertContentVo putConfig(String configName, Object configValue) {
        selfConfig.put(configName, configValue);
        return this;
    }

    public BatchUpsertContentVo putUpdateDate(String name, Object value) {
        updateMap.put(name, value);
        return this;
    }

    @Override
	public String toString() {
        Assert.isTrue(updateMap.size() > 0, "updateMap 不能为空！");

        Assert.notNull(type, "type 不能为空！");
        Assert.notNull(id, "id 不能为空！");

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

        if (!StringUtils.isBlank(routingValue)) {
            actionBodyJson.put("routing", routingValue);
        }

		actionBodyJson.put("_retry_on_conflict", retryOnConflict);

		actionBodyJson.putAll(selfConfig);

		actionJson.put(batchActionEnum.getName(), actionBodyJson);
		
		sb.append(actionJson.toJSONString()).append("\n");

        Map<String,Object> wrapperMap = new HashMap<String, Object>(16);
        wrapperMap.put("doc", updateMap);
        wrapperMap.put("doc_as_upsert", true);

        String json = null;
        if(retainNullValue) {
            json = JSONObject.toJSONString(wrapperMap, SerializerFeature.WriteMapNullValue);
        }else {
            json = JSONObject.toJSONString(wrapperMap);
        }

        sb.append(json).append("\n");

		return sb.toString();
	}
	
}
