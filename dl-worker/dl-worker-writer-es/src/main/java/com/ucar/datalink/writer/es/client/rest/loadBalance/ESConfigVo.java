package com.ucar.datalink.writer.es.client.rest.loadBalance;
/**
 * 
 * Description: es配置vo
 * All Rights Reserved.
 * Created on 2016-7-26 下午9:53:00
 * @author  孔增（kongzeng@zuche.com）
 */
public class ESConfigVo {
	
	private String hosts;
	
	private Integer http_port = 9200;
	
	private Integer tcp_port = 9300;
	
	private String user;
	
	private String pass;
	
	private String clusterName;
	/**
	 * 此集群配置是否使用,为兼容线上已有配置,不指定时默认为true
	 */
	private Boolean isUsed = true;

    /** 是否经过esproxy访问esserver*/
	private Boolean isByProxy = false;



    public Boolean getByProxy() {
        return isByProxy;
    }

    public void setByProxy(Boolean byProxy) {
        isByProxy = byProxy;
    }

    public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public Integer getHttp_port() {
		return http_port;
	}

	public void setHttp_port(Integer http_port) {
		this.http_port = http_port;
	}

	public Integer getTcp_port() {
		return tcp_port;
	}

	public void setTcp_port(Integer tcp_port) {
		this.tcp_port = tcp_port;
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

	public String getClusterName() {
		if(clusterName == null) {
			return ESMultiClusterManage.DEFAULT_CLUSTER_NAME;
		}
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public Boolean getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(Boolean isUsed) {
		this.isUsed = isUsed;
	}
	
}
