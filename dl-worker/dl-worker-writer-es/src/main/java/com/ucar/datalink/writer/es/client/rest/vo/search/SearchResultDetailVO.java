package com.ucar.datalink.writer.es.client.rest.vo.search;

import java.io.Serializable;

/**
 * 
 * Description: 查询结果详细信息vo
 * All Rights Reserved.
 * Created on 2016-7-20 下午3:38:28
 * @author  孔增（kongzeng@zuche.com）
 */
public class SearchResultDetailVO implements Serializable {
	
	private String index;
	
	private String type;
	
	private String id;
	
	private Float score;
	
	private Long version;

    private String result;

    private String highlight;

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

    @Override
    public String toString() {
        return "SearchResultDetailVO{" +
                "index='" + index + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", score=" + score +
                ", version=" + version +
                ", result='" + result + '\'' +
                ", highlight='" + highlight + '\'' +
                '}';
    }
}
