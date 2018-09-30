/**
 * Description: ClusterVo.java
 * All Rights Reserved.
 * @version 4.0  2016-6-25 下午5:42:10  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest.vo;


import java.io.Serializable;

/**
 *  cluster 集群vo
 * <br/> Created on 2016-6-25 下午5:42:10
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class ClusterVo extends VoItf implements Serializable {
	
	private String messageType ;
	
    public ClusterVo() {}
	
	public ClusterVo(String clusterName) {
		super.clusterName = clusterName;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	@Override
	public String getUrl() {
		
		return "http://"+host + "/" + messageType;
	}
	
	
}
