package com.ucar.datalink.flinker.plugin.reader.esreader.client.rest.vo.search.dsl;

public class DSLScrollVo extends DSLSearchVo{

    @Override
	public String getUrl() {
		return "http://" + host + "/_search/scroll";
	}
}
