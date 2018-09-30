package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;

/**
 * 
 * Description: 查询上下文
 * All Rights Reserved.
 * Created on 2016-6-16 下午7:21:41
 * @author  孔增（kongzeng@zuche.com）
 */
public class QueryContext extends SearchContext{
	
	
	public QueryContext () {
		super.queryNode = new KeyNode(ESEnum.DSLKeyEnum.QUERY);
		super.root.addNode(queryNode);
	}
	
	
	public KeyNode addNode(Node customNode) {
	    queryNode.addNode(customNode);
		return queryNode;
	}

	public KeyNode addFiled(String name, Object value) {
		queryNode.addField(name, value);
		return queryNode;
	}
	
	public KeyNode addNode(String name, Node customNode) {
		queryNode.addNode(name, customNode);
		return queryNode;
	}
	
	
	public KeyNode addFiled(ESEnum.DSLFieldKeyEnum fieldKeyEnum, Object value) {
		queryNode.addField(fieldKeyEnum, value);
		return queryNode;
	}
	
	public static void main(String[] args) {
		QueryContext qc = new QueryContext();
		
		qc.addNode( new KeyNode(ESEnum.DSLKeyEnum.BOOL).addNode(new KeyNode(ESEnum.DSLKeyEnum.MUST).addNode(new KeyNode(ESEnum.DSLKeyEnum.MATCH).addField("leafNode1","hahah")))
                .addNode(new KeyNode(ESEnum.DSLKeyEnum.MUST_NOT).addNode(new KeyNode(ESEnum.DSLKeyEnum.MATCH).addField("leafNode2","hahah"))))
          .addField("asdas", "asd");
        qc.addShortNode("detail:asc:min,aa:desc");
        qc.addNode("prefix", new KeyNode());
	    System.out.println(qc.toString());
	}

}
