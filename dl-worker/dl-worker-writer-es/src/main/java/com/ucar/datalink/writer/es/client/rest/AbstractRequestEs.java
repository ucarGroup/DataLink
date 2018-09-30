/**
 * Description: AbstractRequestEs.java
 * All Rights Reserved.
 * @version 4.0  2016-5-20 上午8:45:04  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest;

import com.ucar.datalink.writer.es.client.rest.client.EsClient;
import com.ucar.datalink.writer.es.client.rest.exception.ElasticAuthException;
import com.ucar.datalink.writer.es.client.rest.exception.ElasticSearchException;
import com.ucar.datalink.writer.es.client.rest.loadBalance.BalanceUtil;
import com.ucar.datalink.writer.es.client.rest.loadBalance.ESCluster;
import com.ucar.datalink.writer.es.client.rest.loadBalance.ESMultiClusterManage;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import com.ucar.datalink.writer.es.client.rest.vo.stat.CollectInfoVo;
import com.ucar.datalink.writer.es.util.LocalUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *  请求es的抽象类
 * <br/> Created on 2016-5-20 上午8:45:04
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public abstract class AbstractRequestEs {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRequestEs.class);
	
	private static final int SOCKET_TIMEOUT = 15000;
	
	private static final int CONNECTION_TIMEOUT = 5000;
	
	private static final int MAX_HTTP_CONNECTION = 1000;
	
	private static final int MAX_CONNECTION_PER_HOST  = 200;
	
	private static final CloseableHttpClient httpClient;
	
	static {
    	
        RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(CONNECTION_TIMEOUT)
				.setSocketTimeout(SOCKET_TIMEOUT)
				.build();
    	
    	PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    	// Increase max total connection to 200
    	cm.setMaxTotal(MAX_HTTP_CONNECTION);
    	// Increase default max connection per route to 20
    	cm.setDefaultMaxPerRoute(MAX_CONNECTION_PER_HOST);

    	// Increase max connections for localhost:80 to 50
		// HttpHost localhost = new HttpHost("locahost", 80);
		// cm.setMaxPerRoute(new HttpRoute(localhost), 50);

    	httpClient = HttpClients.custom().setConnectionManager(cm).disableAutomaticRetries()
    			.setDefaultRequestConfig(config).build();
    }

	public String processRequest(VoItf vo , byte[] content){
		
		  ESCluster mc = ESMultiClusterManage.getMonitorCluster(vo.getClusterName());
		  if(mc == null) {
			 throw new NullPointerException("can't find es cluster "+vo.getClusterName());
		  }
		
		  //若未指定host，则采用负载策略，自动获取host
		  if(StringUtils.isBlank(vo.getHost())) {
			  vo.setHost(BalanceUtil.getRandomHost(mc));
		  }

		  HttpRequestBase request = getHttpUriRequest(vo);
		  //
		  if(request == null){
			  throw new NullPointerException("获取 HttpRequestBase is error, HttpRequestBase is null!");
		  }
		  HttpEntity entity = null;
		  CloseableHttpResponse response = null;
		  try {
			    setHeader(request,vo, mc);
			    if(content != null) {
			    	setEntity(request, content);
			    }
	            response = httpClient.execute(request);
			    if (response == null || response.getStatusLine() == null) {
					throw new ElasticSearchException("get http line error");
				}
			    int statusCode = response.getStatusLine().getStatusCode();
			    if (statusCode == 401) {
					throw new ElasticAuthException("401 authority error:username or password not correct");
				} else if (statusCode == 403) {
					throw new ElasticAuthException("403 pemission error:you have right to take current option");
				}
	            entity = response.getEntity();

			    String result = null;
	            if (entity != null ){
	                result = EntityUtils.toString(entity, "UTF-8");

					CollectInfoVo collectInfoVo = EsClient.COMMAND_STAT_THREADLOCAL.get();
					if(collectInfoVo != null) {
						collectInfoVo.setResponseSize(entity.getContentLength());
					}
	            }else{
	            	result = String.valueOf(response.getStatusLine());
	            }
	           return result ;
	        } catch (Exception e) {
			  	throw new RuntimeException("访问"+vo.getUrl()+"失败",e);
	        } finally{
      		    try {
					EntityUtils.consume(entity);
				} catch (IOException e) {
					LOGGER.error("", e);
				}
	        	if(response != null){
	        		try {
						response.close();
					} catch (IOException e) {
						LOGGER.error("连接关闭失败:" , e);
					}
	        	}
	        	if(request != null){
	        		request.releaseConnection();
	        	}
	        }
		  
	  }
	
	
	public abstract HttpRequestBase getHttpUriRequest(VoItf vo);
	
	protected void setHeader(HttpRequestBase request, VoItf vo , ESCluster mc){
		request.setHeader("Content-Type", "application/json;charset=utf-8");
		request.setHeader("x-forwarded-for", LocalUtil.LOCAL_IP);
		
		String userPass = vo.getUserPass();
		
		if(StringUtils.isBlank(userPass)) {
			userPass = mc.getUserPass();
		}
		
        if(userPass != null) {
        	request.setHeader("Authorization"," Basic "+userPass);
        }
	}

    protected void setEntity(HttpRequestBase request , byte[] content){
		
		if((request instanceof HttpEntityEnclosingRequestBase) && content != null){
			 ContentType contentType = ContentType.create("application/json", "UTF-8");
			 HttpEntity httpEntity = new ByteArrayEntity(content, contentType);
			 ((HttpEntityEnclosingRequestBase)request).setEntity(httpEntity);
		}
	}

}
