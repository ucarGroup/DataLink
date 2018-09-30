package com.ucar.datalink.writer.es.client.rest.loadBalance;

import com.ucar.datalink.writer.es.exception.ESClientException;
import com.ucar.datalink.writer.es.util.Assert;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 * Description: es集群对象
 * All Rights Reserved.
 * Created on 2016-7-27 下午1:50:26
 * @author  孔增（kongzeng@zuche.com）
 */
public class ESCluster {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ESCluster.class);
	
	private List<Cluster> clusterList = new ArrayList<Cluster>();
	//权限信息
	private String userPass;
	
	private String clusterName;
	
	private volatile List<String> urls = null;

    private boolean isByProxy = false;

    public ESCluster(ESConfigVo esConfig) {
		init(esConfig);
	}
	
	public void init(ESConfigVo ev){
		
		clusterName = ev.getClusterName();
		isByProxy = ev.getByProxy();

		String hosts = ev.getHosts();
		Assert.notNull(hosts);
		Integer httpPort = ev.getHttp_port();
		if(httpPort == null){
			httpPort = 9200 ;
		}
		Integer tcpPort = ev.getTcp_port();
		if(tcpPort == null){
			tcpPort = 9300 ;
		}
		
		String[] hostArray = hosts.split(",");
		List<String> list = Arrays.asList(hostArray);
		Collections.shuffle(list);
		for(String host : list ){
			Cluster c = new Cluster();
			c.setHost(host);
			c.setHttpPort(httpPort);
			c.setTcpPort(tcpPort);
			clusterList.add(c);
		}
		
		loadAuthInfo(ev);
		
		urls = new ArrayList<String>();
		
		for(Cluster c:clusterList) {
			if(c != null){
				urls.add(c.getHost() + ":" + c.getHttpPort());
			}
		}
	}
	
	/**
	 * 
	 * Description: 加载权限信息
	 * Created on 2016-7-21 下午3:54:57
	 * @author  孔增（kongzeng@zuche.com）
	 * @throws UnsupportedEncodingException
	 */
	private void loadAuthInfo(ESConfigVo ev) {
        String user = ev.getUser();
        String pass = ev.getPass();

        if (!StringUtils.isBlank(user) && !StringUtils.isBlank(pass)) {
            String userpass = user+":"+pass;
            try {
            	userPass = Base64.encodeBase64String(userpass.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new ESClientException("es访问权限信息，加密失败", e);
			}
            return;
        }
        
        LOGGER.error("Not loaded into the AuthInfo,user:{},pass:{}",user,pass);
    }
	
    public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public List<String> getUrls(){
		return urls ;
	}

	public String getUserPass() {
		return userPass;
	}

	public List<Cluster> getClusterList() {
		return clusterList;
	}

	public void setClusterList(List<Cluster> clusterList) {
		this.clusterList = clusterList;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

    public boolean isByProxy() {
        return isByProxy;
    }

    public void setByProxy(boolean byProxy) {
        isByProxy = byProxy;
    }
}
