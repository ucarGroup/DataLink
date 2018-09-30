/**
 * Description: LocalUtil.java
 * All Rights Reserved.
 * @version 3.2  2013-11-21 下午2:35:51  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 *  本地资源工具类
 * <br/> Created on 2013-11-21 下午2:35:51
 * @author  李洪波(hb.li@zhuche.com)
 * @since 3.2 
 */
public final class LocalUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalUtil.class);
	
	public static final String LOCAL_IP = getLocalIp();
	
	public static final String HOST_NAME = getLocalHostName();
	
	private LocalUtil(){}
	
	/**
	 * 获取本机ip地址
	 * 此方法为重量级的方法，不要频繁调用
	 * <br/> Created on 2013-11-21 下午2:36:27
	 * @author  李洪波(hb.li@zhuche.com)
	 * @since 3.2 
	 * @return
	 */
	public static String getLocalIp(){
		try{
			//根据网卡取本机配置的IP  
			Enumeration<NetworkInterface> netInterfaces= NetworkInterface.getNetworkInterfaces();
			String ip = null;
			a: while(netInterfaces.hasMoreElements()){  
			   NetworkInterface ni=netInterfaces.nextElement();
	           Enumeration<InetAddress> ips = ni.getInetAddresses();
	           while (ips.hasMoreElements()) {
	               InetAddress ipObj = ips.nextElement();
	               if (ipObj.isSiteLocalAddress()) {
	           			ip =  ipObj.getHostAddress();
	           			break a;
	               }
	           }
			}
			return ip;
		}catch (Exception e){
			LOGGER.error("", e);
			return null;
		}
	}
	
	/**
	 * 获取本地机器名
	 * 此方法为重量级的方法，不要频繁调用
	 * 一般耗时在百毫秒，缓存使用
	 * @return
	 * @throws UnknownHostException
	 */
	public static String getLocalHostName(){
		
		String hostName = null ;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOGGER.error("get hostname error", e);
		}
		LOGGER.info("get local hostName ：" + hostName );
		
		return hostName ;
	}
	
	public static void main(String[] args) throws UnknownHostException {
		
		long s = System.currentTimeMillis();
		System.out.println(getLocalHostName());
		long e = System.currentTimeMillis();
		System.out.println(e-s);
	}
	
}
