/**
 * 
 */
package com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.loadBalance;


import com.ucar.datalink.flinker.plugin.writer.eswriter.client.rest.utils.Assert;

/**
 * @author lihongbo
 * 
 * 集群对象
 *
 */
public class Cluster {
	
	private String host ;
	
	private int httpPort ;
	
	private int tcpPort ;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}
	/**
	 * 获取http url
	 * @return
	 */
	public String getHttpUrl(){
		
		Assert.notNull(host);
		
		return "http://"+host+":"+httpPort ;
	}
	
}
