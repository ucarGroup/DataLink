package com.ucar.datalink.writer.es.client.rest.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.alias.SearchAlias;
import com.ucar.datalink.writer.es.client.rest.batch.BatchDoc;
import com.ucar.datalink.writer.es.client.rest.cat.CatMeta;
import com.ucar.datalink.writer.es.client.rest.cluster.Cluster;
import com.ucar.datalink.writer.es.client.rest.create.CreateDoc;
import com.ucar.datalink.writer.es.client.rest.del.DelDoc;
import com.ucar.datalink.writer.es.client.rest.exist.ExistDoc;
import com.ucar.datalink.writer.es.client.rest.exist.ExistIndex;
import com.ucar.datalink.writer.es.client.rest.index.UpdateMappingIndex;
import com.ucar.datalink.writer.es.client.rest.index.ViewMappingIndex;
import com.ucar.datalink.writer.es.client.rest.search.DSLSearchDocument;
import com.ucar.datalink.writer.es.client.rest.search.ScrollLimitor;
import com.ucar.datalink.writer.es.client.rest.search.SimpleSearchDocument;
import com.ucar.datalink.writer.es.client.rest.search.listener.ScrollQueryListener;
import com.ucar.datalink.writer.es.client.rest.update.UpdateAllDoc;
import com.ucar.datalink.writer.es.client.rest.update.UpdatePartDoc;
import com.ucar.datalink.writer.es.client.rest.update.UploadSearchTemplate;
import com.ucar.datalink.writer.es.client.rest.vo.*;
import com.ucar.datalink.writer.es.client.rest.vo.alias.SearchAliasReslut;
import com.ucar.datalink.writer.es.client.rest.vo.alias.SearchAliasVo;
import com.ucar.datalink.writer.es.client.rest.vo.search.SearchResultDetailVO;
import com.ucar.datalink.writer.es.client.rest.vo.search.SearchResultVo;
import com.ucar.datalink.writer.es.client.rest.vo.search.dsl.DSLScrollVo;
import com.ucar.datalink.writer.es.client.rest.vo.search.dsl.DSLSearchVo;
import com.ucar.datalink.writer.es.client.rest.vo.search.simple.SimpleSearchDocumentVo;
import com.ucar.datalink.writer.es.client.rest.vo.stat.CollectInfoVo;
import com.ucar.datalink.writer.es.client.rest.vo.stat.ESExecuteLogVo;
import com.ucar.datalink.writer.es.client.rest.vo.template.TemplateSearchVo;
import com.ucar.datalink.writer.es.util.Assert;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 *  访问  ES  入口类
 * <br/> Created on 2016-5-23 下午2:58:38
 * @author  李洪波(hb.li@zhuche.com)
 * @since 4.1
 */
public class EsClient {
    private static final Logger LOG = LoggerFactory.getLogger(EsClient.class);

    private static final int ES_COMMANDS_STAT_TYPE = 75100;

    public static final ThreadLocal<CollectInfoVo> COMMAND_STAT_THREADLOCAL = new ThreadLocal<CollectInfoVo>() ;

    private static ScrollLimitor scrollLimitor = new ScrollLimitor();

    /**
     * 创建文档
     *
     * <br/> Created on 2016-5-23 下午3:03:29
     * @author  李洪波(hb.li@zhuche.com)
     * @since 4.1
     * @param vo
     * @return ，json
     * @throws UnsupportedEncodingException
     */
    public static String createDoc(VoItf vo , Object document) throws UnsupportedEncodingException {
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        String json = null;
        boolean success = false;
        try {
            json = JSONObject.toJSONString(document);
            String result = CreateDoc.getInstance().create(vo, json).getJsonString();
            success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;

            executeLog("createDoc", vo, json, executeTime, success, 1);
        }
    }

    /**
     * 更新整个文档
     *
     * <br/> Created on 2016-5-23 下午3:03:29
     * @author  李洪波(hb.li@zhuche.com)
     * @since 4.1
     * @param vo
     * @param content
     * @return ，json
     * @throws UnsupportedEncodingException
     */
    public static String updateAllDoc(VoItf vo , Object content) throws UnsupportedEncodingException {
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        String json = null;
        boolean success = false;
        try {
            json = JSONObject.toJSONString(content);
            String result =  UpdateAllDoc.getInstance().updateAllDoc(vo, json).getJsonString();
            success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("updateAllDoc", vo, json, executeTime, success, 1);
        }
    }

    /**
     * 文档是否存在
     *
     * <br/> Created on 2016-5-23 下午3:03:29
     * @author  李洪波(hb.li@zhuche.com)
     * @since 4.1
     * @param vo
     * @return ，json
     */
    public static boolean existDoc(SimpleDocVo vo){
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;

        try {
            Assert.notNull(vo.getId(), "id不能为空");
            String result =  ExistDoc.getInstance().processRequest(vo, null);
            success = true;
            if(result.contains("200")){
                return true ;
            }

            if(result.contains("404")){
                return false ;
            }
            success = false;
            throw new RuntimeException("未知的状态  "+result);
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("existDoc", vo, vo.getId(), executeTime, success, 1);
        }
    }

    /**
     * 删除document
     *
     * <br/> Created on 2016-5-23 下午3:03:29
     * @author  李洪波(hb.li@zhuche.com)
     * @since 4.1
     * @param vo
     * @return ，json
     */
    public static String delDoc(SimpleDocVo vo){
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        try {
            String result = DelDoc.getInstance().deleteDoc(vo).getJsonString();
            success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("delDoc", vo, vo.getId(), executeTime, success, 1);
        }
    }

    /**
     * 更新文档局部
     *
     * <br/> Created on 2016-5-27 下午3:03:29
     * @author  李洪波(hb.li@zhuche.com)
     * @since 4.1
     * @param vo
     * @return ，json
     */
    public static String updatePartDoc(VoItf vo , Map<String , Object> row){
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        try {
            String result = UpdatePartDoc.getInstance().updateDoc(vo, row).getJsonString();
            success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("updatePartDoc", vo, vo.toJsonString(row), executeTime, success, 1);
        }
    }
    /**
     * 批量操作文档
     *
     * <br/> Created on 2016-6-1 上午11:33:26
     * @author  李洪波(hb.li@zhuche.com)
     * @since 4.1
     * @param vo
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String batchDoc(BatchDocVo vo , List<BatchContentVo> contents) throws UnsupportedEncodingException {
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        try {
            String result = BatchDoc.getInstance().batchDoc(vo, contents).getJsonString();
            success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            Integer resultNum = 0;
            if(contents != null) {
                resultNum = contents.size();
            }
            executeLog("batchDoc", vo, vo.getContents(), executeTime, success, resultNum);
        }
    }

    public static BulkResultVo batchDocWithResultParse(BatchDocVo vo , List<BatchContentVo> contents) throws UnsupportedEncodingException {
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        try {
            BulkResultVo result;

            result = BatchDoc.getInstance().batchDoc(vo, contents);

            success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            Integer resultNum = 0;
            if(contents != null) {
                resultNum = contents.size();
            }
            executeLog("batchDocWithResultParse", vo, vo.getContents(), executeTime, success, resultNum);
        }
    }

    /**
     * 查看索引映射关系
     *
     * <br/> Created on 2016-6-2 下午3:44:40
     * @author  李洪波(hb.li@zhuche.com)
     * @since 4.1
     * @param vo
     * @return
     */
    public static String viewMappingIndex(VoItf vo){
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        try {
            String result =   ViewMappingIndex.getInstance().processRequest(vo, null);
            success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("viewMappingIndex", vo, null, executeTime, success, 1);
        }
    }

    public static List<List<String>> cat(MetaVo vo){
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        try {
            List<List<String>> result = null;
            result = CatMeta.getInstance().processRequest(vo);
            success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("cat", vo, null, executeTime, success, 1);
        }
    }

    public static boolean existIndex(String indexName){
        return existIndex(new ExsitIndexVo(indexName));
    }

    /**
     * 判断索引是否存在
     *
     * @param
     * @return true 表示已存在 false 反之
     *
     */
    public static boolean existIndex(ExsitIndexVo vo){
        Long start = System.currentTimeMillis();

        try {
            Assert.isTrue(!StringUtils.isBlank(vo.getIndex()));
            String response = ExistIndex.getInstance().processRequest(vo, null);

            if (StringUtils.isBlank(response)) {
                return false;
            }
            if (response.indexOf("index_not_found_exception") != -1) {
                return false;
            }

            String[] metas = response.split(" ");
            boolean result = metas != null && metas.length > 3 && metas[2].equals(vo.getIndex());
            return result;
        }finally {
        }
    }

    /**
     * 更新索引映射方法
     *
     * <br/> Created on 2016-6-6 下午3:00:42
     * @author  李洪波(hb.li@zhuche.com)
     * @since 4.1
     * @param vo ,MappingIndexVo
     * @param content , json utf-8 序列化 内容
     * @return
     */
    public static String updateMappingIndex(VoItf vo , byte[] content){
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;

        try {
            String result = UpdateMappingIndex.getInstance().processRequest(vo, content);
            success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            try {
                executeLog("updateMappingIndex", vo, new String(content,"utf-8"), executeTime, success, 1);
            } catch (UnsupportedEncodingException e) {
                LOG.error("es客户端更新索引处理字符串字节数组异常",e);
            }
        }
    }

    /**
     *
     * Description: 根据id查询document
     * Created on 2016-6-12 下午4:01:03
     * @author  孔增（kongzeng@zuche.com）
     * @param vo
     * @return
     */
    public static String searchDocumentById(SimpleDocVo vo) {
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        Integer resultNum = 0;
        try {
            Assert.notNull(vo.getId(), "id不能为空");
            String result =  SimpleSearchDocument.getInstance().searchDocumentById(vo).getResult();
            success = true;
            resultNum = 1;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("searchDocumentById", vo, vo.getId(), executeTime,success, resultNum);
        }
    }

    /**
     *
     * Description: 根据id查询document详情
     * Created on 2016-6-12 下午4:01:03
     * @author  孔增（kongzeng@zuche.com）
     * @param vo
     * @return
     */
    public static SearchResultDetailVO searchDocumentDetailById(SimpleDocVo vo) {
        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        Integer resultNum = 0;

        try {
            Assert.notNull(vo.getId(), "id不能为空");
            SearchResultDetailVO result =  SimpleSearchDocument.getInstance().searchDocumentById(vo);
            success = true;
            resultNum = 1;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
			executeLog("searchDocumentDetailById", vo, vo.getId(), executeTime, success, resultNum);
		}
	}

	/**
	 *
	 * Description: 根据关键字查询,仅返回结果
	 * Created on 2016-6-12 下午5:55:51
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	public static List<String> searchDocumentByKey(SimpleSearchDocumentVo vo) {
		Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
		boolean success = false;
		Integer resultNum = 0;
        try {
            List<String> result =  SimpleSearchDocument.getInstance().searchDocumentByKey(vo);
			success = true;
			resultNum = result.size();
            return result;
		}finally {
            long executeTime = System.currentTimeMillis() - start;
			executeLog("searchDocumentByKey", vo, vo.getUrl(), executeTime, success, resultNum);
		}
	}

	/**
	 *
	 * Description: 根据关键字查询,返回结果包含文档详情
	 * Created on 2016-6-12 下午5:55:51
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	public static List<SearchResultDetailVO> searchDocumentDetailByKey(SimpleSearchDocumentVo vo) {
		Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
		boolean success = false;
		Integer resultNum = 0;

        try {
            List<SearchResultDetailVO> result =  SimpleSearchDocument.getInstance().searchByKey(vo).getResults();
			success = true;
			resultNum = result.size();
			return result;
		}finally {
			long executeTime = System.currentTimeMillis() - start;
			executeLog("searchDocumentDetailByKey", vo, vo.getUrl(), executeTime, success, resultNum);
		}
	}

	/**
	 *
	 * Description: 根据关键字查询,返回结果包含详情
	 * Created on 2016-6-12 下午5:55:51
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	public static SearchResultVo searchByKey(SimpleSearchDocumentVo vo) {
		Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
		boolean success = false;
		Integer resultNum = 0;
        try {
            SearchResultVo result =  SimpleSearchDocument.getInstance().searchByKey(vo);
			success = true;
			resultNum = result.getTotal();
			return result;
		}finally {
			long executeTime = System.currentTimeMillis() - start;
			executeLog("searchByKey", vo, vo.getUrl(), executeTime, success, resultNum);
		}
	}
	/**
	 *
	 * Description: 结构化查询,仅返回结果
	 * Created on 2016-6-12 下午5:55:51
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	public static List<String> dslSearchDocument(DSLSearchVo vo) throws UnsupportedEncodingException {
		Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
		boolean success = false;
		Integer resultNum = 0;
        try {
			List<String> list = DSLSearchDocument.getInstance().dslSearchDocument(vo);
			success = true;
			resultNum = list.size();
			return list;
		}finally {
			long executeTime = System.currentTimeMillis() - start;
			executeLog("dslSearchDocument", vo, vo.getCondition(), executeTime, success, resultNum);
		}
	}
	/**
	 *
	 * Description: 结构化查询,返回结果包含文档详情
	 * Created on 2016-6-12 下午5:55:51
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	public static List<SearchResultDetailVO> dslSearchDocumentDetail(DSLSearchVo vo) throws UnsupportedEncodingException {
		Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
		boolean success = false;
		Integer resultNum = 0;

        try {
            List<SearchResultDetailVO> list =  DSLSearchDocument.getInstance().dslSearch(vo).getResults();
			success = true;
			resultNum = list.size();
			return list;
		}finally {
            long executeTime = System.currentTimeMillis() - start;
			executeLog("dslSearchDocumentDetail", vo, vo.getCondition(), executeTime,success, resultNum);
		}
	}

	/**
	 *
	 * Description: 结构化查询,返回结果包含详情
	 * Created on 2016-6-12 下午5:55:51
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	public static SearchResultVo dslSearch(DSLSearchVo vo) throws UnsupportedEncodingException {
		Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
		boolean success = false;
		Integer resultNum = 0;
        try {
            SearchResultVo result = DSLSearchDocument.getInstance().dslSearch(vo);
			success = true;
			resultNum = result.getTotal();
            return result;
		}finally {
            long executeTime = System.currentTimeMillis() - start;
			executeLog("dslSearch", vo, vo.getCondition(), executeTime, success, resultNum);
		}
	}

    public static void dslScrollDocument(DSLSearchVo vo,ScrollQueryListener scrollQueryListener)
                                                throws UnsupportedEncodingException {
        if (scrollLimitor.isTooMuchCalled()) {
            throw new RuntimeException("called too much times in recent minutes");
        }

        Long start = System.currentTimeMillis();
        COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        Integer resultNum = 0;

        try {
            vo.setMetaType("_search?scroll=1m");
            SearchResultVo resultVo = DSLSearchDocument.getInstance().dslScrollSearch(vo);

            String scrollId = resultVo.getScrollId();
            Assert.isTrue(scrollId != null,"scroll 响应出问题了");
            resultNum = resultVo.getTotal();
            scrollQueryListener.onQuery(resultVo.getResults());

            while (true) {
                String scrollBody = "{\n" +
                        "    \"scroll\" : \"1m\", \n" +
                        "    \"scroll_id\" : \""+scrollId+"\" \n" +
                        "}";

                DSLScrollVo dslScrollVo = new DSLScrollVo();

                dslScrollVo.setContent(scrollBody);

                resultVo = DSLSearchDocument.getInstance().dslScrollSearch(dslScrollVo);

                if (resultVo.getResults() == null || resultVo.getResults().size() == 0) {//最后一页
                    success = true;
                    return;
                }

                scrollQueryListener.onQuery(resultVo.getResults());

                scrollId = resultVo.getScrollId();
            }
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("dslSearch", vo, vo.getCondition(), executeTime, success, resultNum);
        }
    }

    /**
     *
     * Description: 结构化模板查询
     * Created on 2016-12-20 下午18:35:51
     * @author  王唯（forest.wang@zuche.com）
     * @param vo
     * @return
     */
    public static SearchResultVo dslTemplateSearch(DSLSearchVo vo) throws UnsupportedEncodingException {
        Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;
        Integer resultNum = 0;

        try {
        	SearchResultVo result =  DSLSearchDocument.getInstance().dslTemplateSearch(vo);
        	success = true;
        	resultNum = result.getTotal();
            return result;
        }finally {
        	long executeTime = System.currentTimeMillis() - start;
            executeLog("dslTemplateSearch", vo, vo.getCondition(), executeTime, success, resultNum);
        }
    }

    /**
     *
     * Description: 模板渲染结果预览
     * Created on 2016-12-20 下午18:35:51
     * @author  王唯（forest.wang@zuche.com）
     * @param vo
     * @return
     */
    public static String dslTemplateRender(TemplateSearchVo vo) throws UnsupportedEncodingException {
        Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;

        try {
            String result =  DSLSearchDocument.getInstance().dslTemplateRender(vo);
        	success = true;
            return result;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("dslTemplateRender", vo, vo.getContent(), executeTime, success,1);
        }
    }

    /**
     *
     * Description: 模板
     * Created on 2016-12-20 下午18:35:51
     * @author  王唯（forest.wang@zuche.com）
     * @param vo
     * @return
     */
    public static boolean uploadSearchTemplate(UploadSearchTemplate vo) throws UnsupportedEncodingException {
        Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
        boolean success = false;

        try {
            success =  DSLSearchDocument.getInstance().uploadSearchTemplate(vo);
            return success;
        }finally {
            long executeTime = System.currentTimeMillis() - start;
            executeLog("uploadSearchTemplate", vo, vo.getContent(), executeTime, success,1);
        }
    }

	/**
	 *
	 * Description: 查询符合条件的文档条数
	 * Created on 2016-7-21 上午10:07:23
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static long dslSearchDocumentCount(DSLSearchVo vo) throws UnsupportedEncodingException {
		Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
		boolean success = false;

        try {
            long num =  DSLSearchDocument.getInstance().dslSearchDocumentCount(vo);
			success = true;
            return num;
		}finally {
            long executeTime = System.currentTimeMillis() - start;
			executeLog("dslSearchDocumentCount", vo, vo.getCondition(), executeTime, success,1);
		}
	}
	/**
	 * 获取es 集群信息
	 *
	 * <br/> Created on 2016-6-25 下午5:45:39
	 * @author  李洪波(hb.li@zhuche.com)
	 * @since 4.1
	 * @param vo
	 * @return
	 */
	public static String getClusterMessage(ClusterVo vo ){

        String result =  Cluster.getInstance().processRequest(vo, null);
        return result;

	}

	/**
	 *
	 * Description: 查询别名
	 * Created on 2016-7-22 下午3:26:29
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<SearchAliasReslut> searchAlias(SearchAliasVo vo) {
		Long start = System.currentTimeMillis();
		COMMAND_STAT_THREADLOCAL.set(new CollectInfoVo());
		boolean success = false;
		Integer resultNum = 0;

        try {
            List<SearchAliasReslut> list =  (List<SearchAliasReslut>) SearchAlias.getInstance().searchAlias(vo);
			success = true;
			resultNum = list.size();
			return list;
		}finally {
            long executeTime = System.currentTimeMillis() - start;
			executeLog("searchAlias", vo, vo.getUrl(), executeTime, success,resultNum);
		}
	}

	/**
	 *
	 * Description: 记录客户端执行日志
	 * Created on 2016-10-31 下午6:27:59
	 * @author  孔增（kongzeng@zuche.com）
	 * @param methodName 方法名
	 * @param executeTime 总执行时间
	 * @param content 执行内容
	 */
	private static void executeLog(String methodName, VoItf esVO, String content, long executeTime, boolean success, Integer resultNum) {

		CollectInfoVo cio = null;

		COMMAND_STAT_THREADLOCAL.remove();

		if(cio == null) {
             return;
		}

		ESExecuteLogVo logVo = new ESExecuteLogVo(methodName, esVO, content, executeTime, cio, success);
		logVo.setResultNum(resultNum);
		LOG.warn("es客户端执行日志：param:{}, type:{}, key:{}!",
                JSON.toJSONString(logVo), ES_COMMANDS_STAT_TYPE, logVo.getEsServerIp());
	}

}
