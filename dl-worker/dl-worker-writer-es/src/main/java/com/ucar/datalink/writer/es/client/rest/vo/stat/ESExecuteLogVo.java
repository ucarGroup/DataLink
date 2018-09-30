package com.ucar.datalink.writer.es.client.rest.vo.stat;

import com.ucar.datalink.writer.es.client.rest.vo.SimpleDocVo;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;

import java.io.Serializable;


public class ESExecuteLogVo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4787095558949919114L;
    /**
     * 执行方法名
     */
	private String methodName;
	/**
	 * 索引名称
	 */
	private String index;
	/**
	 * 类型名称
	 */
	private String type;
	/**
	 * 数据id
	 */
	private String id;
	/**
	 * 请求体
	 */
	private String content;
	/**
	 * es服务端ip
	 */
	private String esServerIp;
	/**
	 * 执行总耗时
	 */
	private long executeTime;
	/**
	 * 结果解析耗时
	 */
	private long parseTime;
	/**
	 * 服务端执行耗时
	 */
	private Long serverTime;
	/**
	 * 相应体大小
	 */
	private long responseSize;
	/**
	 * 是否成功
	 */
	private boolean success = false;
	/**
	 * 客户端请求url
	 */
	//private String requestPath;
	/**
	 * 返回结果条数
	 */
	private Integer resultNum;

	public ESExecuteLogVo(String methodName, VoItf esVO, String content, long executeTime, CollectInfoVo cio , boolean success) {

		try {
			this.methodName = methodName;
			if(esVO != null) {
				this.index = esVO.getIndex();
				this.type = esVO.getType();
				this.esServerIp = esVO.getHost();

				if(esVO instanceof SimpleDocVo) {
					this.id = ((SimpleDocVo)esVO).getId();
				}
			}

			this.content = content;
			this.executeTime = executeTime;
			this.parseTime = cio.getParseTime();
			this.responseSize = cio.getResponseSize();
			this.serverTime = cio.getServerTime();
			this.success = success;


		} catch (Throwable e) {
			//e.printStackTrace();
		}
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEsServerIp() {
		return esServerIp;
	}

	public void setEsServerIp(String esServerIp) {
		this.esServerIp = esServerIp;
	}

	public long getExecuteTime() {
		return executeTime;
	}

	public void setExecuteTime(long executeTime) {
		this.executeTime = executeTime;
	}

	public long getParseTime() {
		return parseTime;
	}

	public void setParseTime(long parseTime) {
		this.parseTime = parseTime;
	}

	public Long getServerTime() {
		return serverTime;
	}

	public void setServerTime(Long serverTime) {
		this.serverTime = serverTime;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public long getResponseSize() {
		return responseSize;
	}

	public void setResponseSize(long responseSize) {
		this.responseSize = responseSize;
	}


	public Integer getResultNum() {
		return resultNum;
	}

	public void setResultNum(Integer resultNum) {
		this.resultNum = resultNum;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
