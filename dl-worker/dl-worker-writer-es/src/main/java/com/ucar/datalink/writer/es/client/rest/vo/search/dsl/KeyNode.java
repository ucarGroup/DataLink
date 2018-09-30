package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.DSLFieldKeyEnum;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.DSLKeyEnum;
import com.ucar.datalink.writer.es.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Description: 创建es关键字结点
 * All Rights Reserved.
 * Created on 2016-6-29 下午12:11:30
 * @author  孔增（kongzeng@zuche.com）
 */
public class KeyNode extends Node {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3634169758351555211L;
	
	public KeyNode (DSLKeyEnum nodeName) {
		super.setNodeName(nodeName);
	}
	
	public KeyNode () {
	}
    /**
     * 
     * Description: 添加子节点
     * Created on 2016-6-30 上午11:12:55
     * @author  孔增（kongzeng@zuche.com）
     * @param node
     * @return
     */
	public KeyNode addNode(Node node) {
		Assert.notNull(node.getNodeName(),"在创建构造方法时，需要指定nodeName");
		return this.addNode(node.getNodeName(), node, node.isArray);
	}
	
	/**
	 * 
	 * Description: 可指定自定义结点名称
	 * Created on 2016-6-17 下午2:18:05
	 * @author  孔增（kongzeng@zuche.com）
	 * @param nodeName
	 * @param node
	 * @return
	 */
	public  KeyNode addNode(String nodeName, Node node) {
		this.put(nodeName, node);
		return this;
	}
	/**
	 * 
	 * Description: 可指定自定义结点名称,并指定此结点类型是否为数组
	 * Created on 2016-6-30 上午11:36:34
	 * @author  孔增（kongzeng@zuche.com）
	 * @param nodeName
	 * @param node
	 * @param isArray
	 * @return
	 */
	public  KeyNode addNode(String nodeName, Node node, boolean isArray) {
		if(isArray) {
			@SuppressWarnings("unchecked")
            List<Node> nodes = (List<Node>) this.get(nodeName);
			if(nodes == null) {
				nodes = new ArrayList<Node>();
				this.put(nodeName, nodes);
			}
			nodes.add(node);
		}else {
			this.put(nodeName, node);
		}
		return this;
	}
	

	public KeyNode addField(String name, Object value) {
		this.put(name, value);
		return this;
	}
	
	public KeyNode addField(DSLFieldKeyEnum fieldKeyEnum, Object value) {
		this.addField(fieldKeyEnum.getName(), value);
		return this;
	}
	
	public KeyNode addFieldWithValues(String name, Object...value) {
		this.put(name, value);
		return this;
	}
	
	public static void main(String[] args) {
		KeyNode node = new KeyNode();
		
		node.addNode(new KeyNode(DSLKeyEnum.QUERY)
		                    .addNode( new KeyNode(DSLKeyEnum.BOOL).addNode(new KeyNode(DSLKeyEnum.MUST).addNode(new FieldNode("leafNode1").addField("leafNode1", "121")))
		                    		                     .addNode(new KeyNode(DSLKeyEnum.MUST_NOT).addNode(new KeyNode(DSLKeyEnum.MATCH).addField("leafNode2","hahah")))))
		    .addNode(new KeyNode(DSLKeyEnum.FILTER)
		                            .addNode(new KeyNode(DSLKeyEnum.TERM)
		                                      .addField("aaa1", "1asdsad")))
		    .addNode(new RangeNode().less("c1", 1).lessOrEqual("c2", "212").greater("c1", 23))
		    .addField("asdas", "qqqq");                		                     
	
		
		System.out.println(node.toJSONString());
	}

}
