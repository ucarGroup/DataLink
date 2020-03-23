/**
 * 
 */
package com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.vo;

import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.constant.CharacterConstant;
import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.utils.Assert;
import org.apache.commons.lang.StringUtils;
import java.io.Serializable;

/**
 * document 的一个描述对象
 *  
 * <br/> Created on 2016-5-20 上午8:32:45
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class SimpleDocVo extends VoItf implements Serializable {

	private static final long serialVersionUID = 600186545130286317L;

	private String id ;
	
	//版本号，update/del 乐观锁
	private String version ;

	/**
	 * routing值
	 */
	private String routingValue;

    public SimpleDocVo() {}
	
	public SimpleDocVo(String clusterName) {
		super.clusterName = clusterName;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
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

	public String getUrl(){
		
		Assert.notNull(index, "index不能为空");
		
		Assert.notNull(type, "type不能为空");
		
		String lastUrl =  "http://" + host +"/" + index + "/"+ type;
		
		if(id != null) {
			lastUrl += "/"+ id;
		}

		lastUrl += CharacterConstant.QUEST;

		if (!StringUtils.isBlank(routingValue)) {
			lastUrl += "routing="+routingValue;
		}

		return lastUrl;
	}
	
}
