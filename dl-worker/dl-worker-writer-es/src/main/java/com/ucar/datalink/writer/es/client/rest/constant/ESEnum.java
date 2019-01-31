package com.ucar.datalink.writer.es.client.rest.constant;

import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.impl.*;


public class ESEnum {
	public static enum ComparisonEnum {
		/** less than */
	    LESS("lt"),
	    /** less than or equal to */
	    LESS_OR_EQUAL("lte"),
	    /** greater than or equal to */
	    GREATER_OR_EQUAL("gte"),
	    /** greater than */
	    GREATER("gt"),
	    /** contain */
	    CONTAIN("ct");

	    private String acronym;

	    ComparisonEnum(String acronym) {
	    	this.acronym = acronym;
	    }

		public String getAcronym() {
			return acronym;
		}

		public void setAcronym(String acronym) {
			this.acronym = acronym;
		}

	}

	public interface DSLEnum {
		public String getName() ;
		public void setName(String name) ;
	}
	/**
	 *
	 * Description: es结点关键字枚举
	 * All Rights Reserved.
	 * Created on 2016-6-16 下午4:46:35
	 * @author  孔增（kongzeng@zuche.com）
	 */
	public static enum DSLKeyEnum implements DSLEnum{
		QUERY("query"),
		FILTERED("filtered"),
		FILTER("filter"),
		BOOL("bool"),
		SHOULD("should",true),
		MUST("must",true),
		MUST_NOT("must_not",true),
		SORT("sort"),
		RANGE("range"),
		MULTI_MATCH("multi_match"),
		TERMS("terms"),
		TERM("term"),
		MATCH("match"),
        MATCH_ALL("match_all"),
        MATCH_PHRASE("match_phrase"),
        MATCH_PHRASE_PREFIX("match_phrase_prefix"),
        PREFIX("prefix"),
        QUERY_STRING("query_string");

		/**
		 * 名称
		 */
		String name;
		/**
		 * 子节点是否为数组
		 */
		boolean isArray = false;

		DSLKeyEnum(String name , boolean isArray){
			this.name = name;
			this.isArray = isArray;
		}

		DSLKeyEnum(String name){
			this.name = name;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public boolean isArray() {
			return isArray;
		}

		public void setArray(boolean isArray) {
			this.isArray = isArray;
		}

	}


	/**
	 *
	 * Description: es域关键字枚举
	 * All Rights Reserved.
	 * Created on 2016-6-16 下午4:46:35
	 * @author  孔增（kongzeng@zuche.com）
	 */
	public static enum DSLFieldKeyEnum implements DSLEnum{
		FROM("from"),
		SIZE("size"),
		VALUE("value"),//用于过滤时，指定过滤内容
		QUERY("query"),//用于查询时，指定值查询内容
		BOOST("boost"),//用于文档得分的加权值
		MINIMUM_MATCH("minimum_match"),//用于指定至少匹配数
		CUTOFF_FREQUENCY("cutoff_frequency"),//用于构建高低频词组 0.01（1%）
        LOW_FREQ_OPERATOR("low_freq_operator"),//用于指定低频词组的逻辑运算符 （or（默认）、and）
		HIGH_FREQ_OPERATOR("high_freq_operator"),//用于指定高频词组的逻辑运算符 （or（默认）、and）
		ANALYZER("analyzer"),//用于定义分析查询文本时用到的分析器名称
		DISABLE_COORD("disable_coord"),//是否禁用分数因子的计算，默认为false
		OPERATOR("operator"),//控制用来连接创建的布尔条件的布尔运算符（or（默认）、and）
		FUZZINESS("fuzziness"),//构造模糊查询时，该参数将用来设置相似性(0.0-1.0)
		PREFIX_LENGTH("prefix_length"),//指定差分词条的公共前缀长度，默认值为 0
		ZERO_TERMS_QUERY("zero_terms_query"),//指定当所有的词条都被分析器移除时，查询的行为。它可以被设置为 none(无文档返回) 或 all（返回所有文档） ，默认值是 none 
		SLOP("slop"),//该值定义了文本查询中的词条和词条之间可以有多少个未知词条，以被视为跟一个短语匹配,默认为0
		MAX_EXPANSIONS("max_expansions"),//与match_phrase_prefix配套使用，用于指定有多少前缀将被重写成最后的词条
		DEFAULT_FIELD("default_field"),//与query_string配套使用，用于指定默认的查询字段，默认为_all
        FIELDS("fields"),//用于设置需要返回的值从自身store取
		SOURCE("_source");//用于设置需要返回的值从source中取
		/**
		 * 名称
		 */
		String name;

		DSLFieldKeyEnum(String name){
			this.name = name;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

	}

	public static enum BatchActionEnum {
		INDEX("index"),
	    CREATE("create"),
	    UPDATE("update"),
	    DELETE("delete"),
		UPSERT("upsert");

	    private String name;

	    BatchActionEnum(String name) {
	    	this.name = name;
	    }

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
	/**
	 *
	 * Description: 返回结果解析器枚举
	 * All Rights Reserved.
	 * Created on 2016-7-20 下午2:44:53
	 * @author  孔增（kongzeng@zuche.com）
	 */
	public static enum ParseEnum {
	    DEFAULT(new DefaultParseHandler()),//默认解析器
	    RETAINDETAIL(new RetainDetailParseHandler()),//包含除结果外的其他信息解析器
	    SEARCHALIAS(new SearchAliasParseHandler()),//查询别名解析器
	    CREATEANDALLUPDATE(new CreateAndAllUpdateParseHandler()),//
	    PARTUPDATE(new PartUpdateParseHandler()),
	    DELETE(new DeleteParseHandler()),
	    BULK(new BulkParseHandler()),
		SINGLE(new SingleSearchParseHandler());

	    private ParseHandler parseHandler;

	    ParseEnum(ParseHandler parseHandler) {
	    	this.parseHandler = parseHandler;
	    }

		public ParseHandler getParseHandler() {
			return parseHandler;
		}

		public void setParseHandler(ParseHandler parseHandler) {
			this.parseHandler = parseHandler;
		}
	}
	/**
	 *
	 * Description: 操作类型
	 * All Rights Reserved.
	 * Created on 2016-8-11 下午3:55:12
	 * @author  孔增（kongzeng@zuche.com）
	 */
	public static enum OperateTypeEnum {
	    CREATE,
	    ALLUPDATE,
	    UPDATE,
	    DELETE;
	}
}
