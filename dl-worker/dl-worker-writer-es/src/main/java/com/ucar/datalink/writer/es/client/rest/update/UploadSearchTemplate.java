/**
 * Description: UpdatePartDocument.java
 * All Rights Reserved.
 * @version 4.0  2016-5-25 下午5:00:12  by 李洪波（hb.li@zhuche.com）创建
 */
package com.ucar.datalink.writer.es.client.rest.update;

import com.ucar.datalink.writer.es.client.rest.vo.VoItf;

/**
 *
 */
public class UploadSearchTemplate extends VoItf {

    private String content ;

    private String templateName ;

    @Override
    public String getUrl() {
        return "http://" + host + "/_search/template/"+templateName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
