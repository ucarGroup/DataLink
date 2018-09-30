package com.ucar.datalink.writer.es.client.rest.cat;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.ucar.datalink.writer.es.client.rest.AbstractRequestEs;
import com.ucar.datalink.writer.es.client.rest.vo.VoItf;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.Iterator;
import java.util.List;


public class CatMeta extends AbstractRequestEs {

    private static final CatMeta RD = new CatMeta();

    private CatMeta() {
    }

    public static CatMeta getInstance() {
        return RD;
    }

    @Override
    public HttpRequestBase getHttpUriRequest(VoItf vo) {
        return new HttpGet(vo.getUrl());
    }

    public List<List<String>> processRequest(VoItf vo) {
        List<List<String>> results = Lists.newArrayList();

        final String resp = super.processRequest(vo, null);
        if (!StringUtils.isBlank(resp)) {

            if (resp.trim().startsWith("{")) {//正常响应不会是一个json串
                throw new RuntimeException("sth wrong:" + resp);
            }

            String[] rows = resp.split("\n");
            for (int i = 0; i < rows.length; i++) {
                List<String> eachRow = Lists.newArrayList();
                Splitter splitter = Splitter.on(" ").omitEmptyStrings().trimResults();
                Iterable<String> cols = splitter.split(rows[i]);
                Iterator<String> iterator = cols.iterator();
                while (iterator.hasNext()) {
                    eachRow.add(iterator.next());
                }
                results.add(eachRow);
            }
        }
        return results;
    }
}
