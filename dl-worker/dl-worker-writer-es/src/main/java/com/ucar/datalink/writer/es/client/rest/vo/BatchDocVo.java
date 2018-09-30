/**
 * Description: BulkDocumentVo.java
 * All Rights Reserved.
 * @version 4.1  2016-6-1 上午10:49:50  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest.vo;


import com.ucar.datalink.writer.es.util.Assert;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 *  批量提交vo
 * <br/> Created on 2016-6-1 上午10:49:50
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class BatchDocVo extends VoItf implements Serializable {
	
	private String batchType ;
	/**
	 * 记录拼装后的批量操作内容，监控统计使用
	 */
	private String contents;
	
	public BatchDocVo() {}
	
	public BatchDocVo(String clusterName) {
		super.clusterName = clusterName;
	}
	

	public String getBatchType() {
		return batchType;
	}

	public void setBatchType(String batchType) {
		this.batchType = batchType;
	}
	
	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	/* (non-Javadoc)
	 * @see com.ucar.datalink.writer.es.vo.VoItf#getUrl()
	 */
	@Override
	public String getUrl() {
		if(StringUtils.isEmpty(this.batchType)){
			throw new IllegalArgumentException("batchType is not null!");
		}
		
		String url = "http://" + host ;
		if(!StringUtils.isEmpty(index)){
			url = url + "/" + index ;
		}
		if(!StringUtils.isEmpty(type)) {
			Assert.notNull(index, "when type is not null,index con't be null！");
			url = url + "/" + type ;
		}
		url = url + "/" + this.batchType;
		return url;
	}

    @Override
    public String toString() {
        return "BatchDocVo{" +
                "batchType='" + batchType + '\'' +
                ", contents='" + contents + '\'' +
                ", index='" + index + '\'' +
                ", type='" + type + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
