package com.ucar.datalink.writer.es.client.rest.vo.stat;

import java.io.Serializable;

/**
 * 
 * Description: 命令执行日志统计
 * All Rights Reserved.
 * Created on 2016-11-9 下午4:34:46
 * @author  孔增（kongzeng@zuche.com）
 */
public class CollectInfoVo implements Serializable {
	
	/**
	 * 结果解析耗时
	 */
	private long parseTime;
	/**
	 * 相应体大小
	 */
	private long responseSize;
	/**
	 * 服务端执行耗时
	 */
	private Long serverTime;
	
	public long getParseTime() {
		return parseTime;
	}
	
	public void setParseTime(long parseTime) {
		this.parseTime = parseTime;
	}
	
	public long getResponseSize() {
		return responseSize;
	}

	public void setResponseSize(long responseSize) {
		this.responseSize = responseSize;
	}

	public Long getServerTime() {
		return serverTime;
	}

	public void setServerTime(Long serverTime) {
		this.serverTime = serverTime;
	}
}
