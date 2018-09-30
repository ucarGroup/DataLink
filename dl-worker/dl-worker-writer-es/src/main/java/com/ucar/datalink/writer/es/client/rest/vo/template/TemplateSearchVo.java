package com.ucar.datalink.writer.es.client.rest.vo.template;

import com.ucar.datalink.writer.es.client.rest.vo.VoItf;

import java.io.Serializable;

/**
 * 模板查询vo
 * @author forest
 * @create 2016-12-20 16:48
 */
public class TemplateSearchVo extends VoItf implements Serializable {

    private String content ;

    @Override
    public String getUrl() {
        return "http://" + host + "/_render/template";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
