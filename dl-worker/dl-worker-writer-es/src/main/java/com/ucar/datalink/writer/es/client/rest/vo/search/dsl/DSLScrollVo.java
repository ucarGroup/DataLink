package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

public class DSLScrollVo extends DSLSearchVo{

    @Override
	public String getUrl() {
		return "http://" + host + "/_search/scroll";
	}
}
