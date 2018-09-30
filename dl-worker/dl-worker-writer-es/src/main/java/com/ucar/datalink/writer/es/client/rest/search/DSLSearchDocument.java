package com.ucar.datalink.writer.es.client.rest.search;

import com.alibaba.fastjson.JSONObject;
import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.result.ProcessResult;
import com.ucar.datalink.writer.es.client.rest.result.parseHandler.ParseHandler;
import com.ucar.datalink.writer.es.client.rest.update.UploadSearchTemplate;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import com.ucar.datalink.writer.es.client.rest.vo.search.SearchResultVo;
import com.ucar.datalink.writer.es.client.rest.vo.search.dsl.DSLSearchVo;
import com.ucar.datalink.writer.es.client.rest.vo.template.TemplateSearchVo;
import com.ucar.datalink.writer.es.util.Assert;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 
 * Description: 根据id检索文档
 * All Rights Reserved.
 * Created on 2016-6-12 下午3:47:58
 * @author  孔增（kongzeng@zuche.com）
 */
public class DSLSearchDocument extends AbstractRequestEs {
	
    private static final DSLSearchDocument RD = new DSLSearchDocument();
	
	private DSLSearchDocument(){}
	
	public static DSLSearchDocument getInstance(){
		return RD ;
	}

	@Override
	public HttpRequestBase getHttpUriRequest(VoItf vo) {
		HttpPost post = new HttpPost(vo.getUrl() );
		return post;
	}	
	
	/**
	 * 
	 * Description: 结构化查询、过滤,仅包含结果
	 * Created on 2016-6-14 上午9:47:29
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public List<String> dslSearchDocument(DSLSearchVo vo) throws UnsupportedEncodingException {
		Assert.notNull(vo, "DSLSearchVo is requeired");
		byte [] bytes = null;
		if(vo.getCondition() != null) {
			bytes = vo.getCondition().getBytes("utf-8");
		}
		vo.setMetaType("_search");
		return (List<String>) ProcessResult.parseResult(super.processRequest(vo,bytes));
	}
	/**
	 * 
	 * Description: 结构化查询、过滤,还包含其他信息
	 * Created on 2016-6-14 上午9:47:29
	 * @author  孔增（kongzeng@zuche.com）
	 * @param vo
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public SearchResultVo dslSearch(DSLSearchVo vo) throws UnsupportedEncodingException {
		Assert.notNull(vo, "DSLSearchVo is requeired");
		
		byte [] bytes = null;
		if(vo.getCondition() != null) {
			bytes = vo.getCondition().getBytes("utf-8");
		}
		vo.setMetaType("_search");
		return (SearchResultVo) ProcessResult.parseResult(super.processRequest(vo,bytes), ESEnum.ParseEnum.RETAINDETAIL);
	}


    /**
     * 结构化模板查询,结果包含元信息和命中的文档
     * @param vo
     * @return
     * @throws UnsupportedEncodingException
     */
    public SearchResultVo dslTemplateSearch(DSLSearchVo vo) throws UnsupportedEncodingException {
        Assert.notNull(vo, "DSLSearchVo is requeired");

        if(vo.getSearchContext() != null) {
            throw new RuntimeException("模板查询不支持查询域,请在context属性中设置自定义json串");
        }
        byte [] bytes = vo.getContent().getBytes("utf-8");
        vo.setMetaType("_search/template");
        return (SearchResultVo) ProcessResult.parseResult(super.processRequest(vo,bytes), ESEnum.ParseEnum.RETAINDETAIL);
    }

    /**
     *
     * Description: 结构化查询、过滤,还包含其他信息
     * Created on 2016-6-14 上午9:47:29
     * @author  孔增（kongzeng@zuche.com）
     * @param vo
     * @return
     * @throws UnsupportedEncodingException
     */
    public SearchResultVo dslScrollSearch(DSLSearchVo vo) throws UnsupportedEncodingException {
        Assert.notNull(vo, "DSLSearchVo is requeired");
        byte [] bytes = null;
        if(vo.getCondition() != null) {
            bytes = vo.getCondition().getBytes("utf-8");
        }
        return (SearchResultVo) ProcessResult.parseResult(super.processRequest(vo,bytes), ESEnum.ParseEnum.RETAINDETAIL);
    }

    /**
     * 上传查询模板
     * @param vo
     * @return
     * @throws UnsupportedEncodingException
     */
    public boolean uploadSearchTemplate(UploadSearchTemplate vo) throws UnsupportedEncodingException {
        Assert.notNull(vo, "DSLSearchVo is requeired");

        if(StringUtils.isBlank(vo.getTemplateName())) {
            throw new RuntimeException("查询模板的名称不能空");
        }
        byte [] bytes = vo.getContent().getBytes("utf-8");

        JSONObject json = JSONObject.parseObject(super.processRequest(vo, bytes));
        ProcessResult.checkError(json);
        if(json == null) {
            return false;
        }
        Boolean created = json.getBooleanValue(ParseHandler.CREATED_NAME);
        return created != null;
    }


    /**
     * 模板渲染结果查询
     * @param vo
     * @return
     * @throws UnsupportedEncodingException
     */
    public String dslTemplateRender(TemplateSearchVo vo) throws UnsupportedEncodingException {
        Assert.notNull(vo, "TemplateSearchVo is requeired");
        byte[] bytes = vo.getContent().getBytes("utf-8");
        return super.processRequest(vo,bytes);
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
	public long dslSearchDocumentCount(DSLSearchVo vo) throws UnsupportedEncodingException {
		Assert.notNull(vo, "DSLSearchVo is requeired");
		
		vo.setMetaType("_count");
		
		byte [] bytes = null;
		if(vo.getCondition() != null) {
			bytes = vo.getCondition().getBytes("utf-8");
		}
		JSONObject json = JSONObject.parseObject(super.processRequest(vo, bytes));
		if(json == null) {
			return 0;
		}
		ProcessResult.checkError(json);
		return json.getLongValue(ParseHandler.COUNT_NAME);
	}

}
