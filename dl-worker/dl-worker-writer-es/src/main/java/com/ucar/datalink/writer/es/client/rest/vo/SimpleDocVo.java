/**
 * 
 */
package com.ucar.datalink.writer.es.client.rest.vo;

import com.ucar.datalink.writer.es.util.Assert;

import java.io.Serializable;

/**
 * document 的一个描述对象
 *  
 * <br/> Created on 2016-5-20 上午8:32:45
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class SimpleDocVo extends VoItf implements Serializable {
	
	private String id ;
	
	//版本号，update/del 乐观锁
	private String version ;
	
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
	public String getUrl(){
		
		Assert.notNull(index, "index不能为空");
		
		Assert.notNull(type, "type不能为空");
		
		String lastUrl =  "http://" + host +"/" + index + "/"+ type;
		
		if(id != null) {
			lastUrl += "/"+ id;
		}

		return lastUrl;
	}
	
}
