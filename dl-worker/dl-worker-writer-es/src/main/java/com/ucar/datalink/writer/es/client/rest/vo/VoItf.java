
package com.ucar.datalink.writer.es.client.rest.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ucar.datalink.writer.es.client.rest.loadBalance.ESMultiClusterManage;
import com.ucar.datalink.writer.es.exception.ESClientException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;

/**
 *  
 * <br/> Created on 2016-6-1 上午10:43:37
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public abstract class VoItf {
	
	protected String index ;
	
	protected String type;
	
	protected String clusterName = ESMultiClusterManage.DEFAULT_CLUSTER_NAME;
	//可人为指定，否则将默认走配置中的信息
	protected String host;
	//用户名，可人为指定，默认走配置中的信息
	private String user;
	//密码，可人为指定，默认走配置中的信息
	private String pass;
	//转化消息体为json串时，保留值为null的字段，使其能将值为null的字段传递到服务端,默认自动过滤掉值为null的字段
	private boolean retainNullValue = false;
	
	/**
	 * vo 接口
	 * 
	 * <br/> Created on 2016-6-1 上午10:44:20
	 * @author  李洪波(hb.li@zhuche.com)
	 * @since 4.1
	 * @return 返回 url
	 */
	public abstract String getUrl();
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}
	
	public boolean isRetainNullValue() {
		return retainNullValue;
	}

	public void setRetainNullValue(boolean retainNullValue) {
		this.retainNullValue = retainNullValue;
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

	public String getUserPass() {
		if (!StringUtils.isBlank(user) && !StringUtils.isBlank(pass)) {
            String userpass = user+":"+pass;
            try {
            	return Base64.encodeBase64String(userpass.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new ESClientException("es访问权限信息，加密失败", e);
			}
        }
		
		return null;
	}
	/**
	 * 
	 * Description: 将消息体转换为json串
	 * Created on 2016-7-29 下午3:24:09
	 * @author  孔增（kongzeng@zuche.com）
	 * @return
	 */
	public String toJsonString(Object content) {
		String json = null;
		 if(retainNullValue) {
         	json = JSONObject.toJSONString(content, SerializerFeature.WriteMapNullValue);
         }else {
			json = JSONObject.toJSONString(content);
         }
		 return json;
	}
	
}
