package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.DSLFieldKeyEnum;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.DSLKeyEnum;

/**
 * 
 * Description: 过滤查询上下文
 * All Rights Reserved.
 * Created on 2016-6-16 下午7:21:41
 * @author  孔增（kongzeng@zuche.com）
 */
public class FilterQueryContext extends SearchContext{
	
	
	public FilterQueryContext () {
		KeyNode queryContext = new KeyNode(DSLKeyEnum.QUERY);
		super.queryNode = new KeyNode(DSLKeyEnum.QUERY);
		super.filterNode = new KeyNode(DSLKeyEnum.FILTER);
		queryContext.addNode(new KeyNode(DSLKeyEnum.FILTERED).addNode(queryNode).addNode(filterNode));
		super.root.addNode(queryContext);
	}
	
	/**
	 * 
	 * Description: 添加查询结点
	 * Created on 2016-6-17 上午10:38:58
	 * @author  孔增（kongzeng@zuche.com）
	 * @param node
	 * @return
	 */
	public KeyNode addNodeToQuery(Node node) {
		super.queryNode.addNode(node);
		return super.queryNode;
	}
	
	/**
	 * 
	 * Description: 添加查询结点
	 * Created on 2016-6-17 上午10:38:58
	 * @author  孔增（kongzeng@zuche.com）
	 * @param node
	 * @return
	 */
	public KeyNode addNodeToQuery(String nodeName, Node node) {
		super.queryNode.addNode(nodeName, node);
		return super.queryNode;
	}
	/**
	 * 
	 * Description: 添加查询字段 
	 * Created on 2016-6-17 上午10:39:08
	 * @author  孔增（kongzeng@zuche.com）
	 * @param name
	 * @param value
	 * @return
	 */
	public KeyNode addFiledToQuery(String name, Object value) {
		super.queryNode.addField(name, value);
		return super.queryNode;
	}
	
	/**
	 * 
	 * Description: 添加过滤结点
	 * Created on 2016-6-17 上午10:38:42
	 * @author  孔增（kongzeng@zuche.com）
	 * @param node
	 * @return
	 */
	public Node addNodeToFilter(Node node) {
		super.filterNode.addNode(node);
		return super.filterNode;
	}
	
	/**
	 * 
	 * Description: 添加过滤结点
	 * Created on 2016-6-17 上午10:38:42
	 * @author  孔增（kongzeng@zuche.com）
	 * @param nodeName node
	 * @return
	 */
	public Node addNodeToFilter(String nodeName, Node node) {
		super.filterNode.addNode(nodeName,node);
		return super.filterNode;
	}
	
	/**
	 * 
	 * Description:  添加过滤字段
	 * Created on 2016-6-17 上午10:38:47
	 * @author  孔增（kongzeng@zuche.com）
	 * @param name
	 * @param value
	 * @return
	 */
	public Node addFieldToFilter(String name, Object value) {
		super.filterNode.addField(name, value);
		return super.filterNode;
	}

	
	public static void main(String[] args) {
		FilterQueryContext qc = new FilterQueryContext();
		
		qc.addNodeToFilter( new KeyNode(DSLKeyEnum.BOOL).addNode(new KeyNode(DSLKeyEnum.MUST).addNode(new KeyNode(DSLKeyEnum.MATCH).addField("leafNode1","hahah")))
                .addNode(new KeyNode(DSLKeyEnum.MUST_NOT).addNode(new KeyNode(DSLKeyEnum.MATCH).addNode(new FieldNode("leafNode2").addField(DSLFieldKeyEnum.QUERY, "hahah")))));
		qc.addFiledToQuery("asdasd", new KeyNode(DSLKeyEnum.BOOL));
		qc.addNodeToFilter("asdasd", new KeyNode(DSLKeyEnum.BOOL));
		System.out.println(qc.toString());
	}

}
