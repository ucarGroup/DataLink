package com.ucar.datalink.writer.es.client.rest.vo.search.dsl;

import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum.DSLFieldKeyEnum;
import com.ucar.datalink.writer.es.client.rest.exception.ElasticSearchException;
import org.apache.commons.lang.StringUtils;

import java.util.List;


public class SearchContext {

	protected KeyNode root = new KeyNode();

	protected KeyNode queryNode ;

	protected KeyNode filterNode ;

	protected KeyNode sortNode ;

	/**
	 *
	 * Description:
	 * Created on 2016-6-17 下午3:09:11
	 * @author  孔增（kongzeng@zuche.com）
	 * @param orderDesc  格式：排序列:[asc|desc](:mode) ,(mode 为可选项 <P/>
	 * 此选项主要是针对一列有多个值是的排序策略，主要有min, max, avg 或 sum) <P/>
	 * 若如需要多列组合排序，则用逗号分隔<P/>
	 * 例如：name:asc或name:desc:min或name:asc:max,value:desc
	 *
	 */
	public void addShortNode(String orderDesc) {
		if(StringUtils.isBlank(orderDesc)) {
			return ;
		}
		if(sortNode == null) {
			sortNode = new KeyNode();
			root.addField("sort", sortNode);
		}

		String[] orders = orderDesc.split(",");

		for(String order : orders) {
			String[] infos = order.split(":");
			if(infos.length < 2 || infos.length > 3) {
				throw new ElasticSearchException("排序格式不正确:问题出现在"+orderDesc+"的"+order+"处");
			}
			FieldNode infoNode = new FieldNode();
			sortNode.addNode(infos[0], infoNode);
			infoNode.addField("order", infos[1]);
			if(infos.length == 3) {
				infoNode.addField("mode", infos[2]);
			}
		}

	}
	/**
	 *
	 * Description: 设置分页
	 * Created on 2016-6-30 上午11:55:40
	 * @author  孔增（kongzeng@zuche.com）
	 * @param from
	 * @param size
	 */
	public void setPage(int from ,int size) {
		root.addField(DSLFieldKeyEnum.FROM.getName(), from);
		root.addField(DSLFieldKeyEnum.SIZE.getName(), size);
	}
	/**
	 *
	 * Description: 设置返回值，服务端将从每列自身存储中取,一般适用于返回列少的情况
	 * Created on 2016-6-30 上午11:55:52
	 * @author  孔增（kongzeng@zuche.com）
	 * @param fields
	 */
	public void setNeedReturnFields(List<String> fields) {
		root.addField(DSLFieldKeyEnum.FIELDS, fields);
	}

	public void setNeedReturnFields(String...fields) {
		root.addField(DSLFieldKeyEnum.FIELDS, fields);
	}

	/**
	 *
	 * Description: 设置返回值,服务端将从source源文件中取锁需要的列,一般适用于索引开启_source存储且返回列多的情况
	 * Created on 2016-6-30 上午11:55:52
	 * @author  孔增（kongzeng@zuche.com）
	 * @param fields
	 */
	public void setNeedReturnFieldsFromSource(List<String> fields) {
		root.addField(DSLFieldKeyEnum.SOURCE, fields);
	}

	public void setNeedReturnFieldsFromSource(String...fields) {
		root.addField(DSLFieldKeyEnum.SOURCE, fields);
	}

	public KeyNode addFieldToRoot(String field , Object value) {
		root.put(field, value);
		return root;
	}

	public KeyNode addNodeToRoot(String nodeName , Node node) {
		root.put(nodeName, node);
		return root;
	}
    @Override
	public String toString () {
		if(root != null && root.size() >0) {
			return root.toJSONString();
		}

		return null;
	}


	/**
	 * 检查合法性
	 */
	protected  void checkSearchContext() {
		//添加查询默认值
		if(queryNode.size()  == 0) {
			queryNode.addNode(new KeyNode(ESEnum.DSLKeyEnum.MATCH_ALL));
		}
		if(filterNode != null && filterNode.size() == 0) {
			throw new ElasticSearchException("过滤条件不能为空");
		}
	}

}
