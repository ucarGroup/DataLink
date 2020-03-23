package com.ucar.datalink.flinker.plugin.reader.esreader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.Base64;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ucar.datalink.flinker.api.element.*;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.plugin.RecordSender;
import com.ucar.datalink.flinker.api.spi.Reader;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.api.util.DateUtils;
import com.ucar.datalink.flinker.plugin.reader.esreader.client.rest.client.EsClient;
import com.ucar.datalink.flinker.plugin.reader.esreader.client.rest.loadBalance.ESConfigVo;
import com.ucar.datalink.flinker.plugin.reader.esreader.client.rest.loadBalance.ESMultiClusterManage;
import com.ucar.datalink.flinker.plugin.reader.esreader.client.rest.search.listener.ScrollQueryListener;
import com.ucar.datalink.flinker.plugin.reader.esreader.client.rest.vo.MappingIndexVo;
import com.ucar.datalink.flinker.plugin.reader.esreader.client.rest.vo.search.SearchResultDetailVO;
import com.ucar.datalink.flinker.plugin.reader.esreader.client.rest.vo.search.SearchResultVo;
import com.ucar.datalink.flinker.plugin.reader.esreader.client.rest.vo.search.dsl.DSLSearchVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;


/**
 * Created by hechaoyi on 16/8/22.
 */
public class EsReader extends Reader {

    private static final Logger logger = LoggerFactory.getLogger(EsReader.class);

    public static class Job extends Reader.Job {

        private Configuration originConfig;

        @Override
        public void init() {
            this.originConfig = super.getPluginJobConf();
            this.originConfig.getNecessaryValue(Key.ES_HOSTS, EsReaderErrorCode.REQUIRED_VALUE);
            this.originConfig.getNecessaryValue(Key.ES_INDEX, EsReaderErrorCode.REQUIRED_VALUE);
            this.originConfig.getNecessaryValue(Key.ES_TYPE, EsReaderErrorCode.REQUIRED_VALUE);
            this.originConfig.getNecessaryValue(Key.COLUMNS, EsReaderErrorCode.REQUIRED_VALUE);
        }

        @Override
        public void destroy() {
        }

        @Override
        public List<Configuration> split(int adviceNumber) {
            return Lists.newArrayList(this.originConfig); // 忽略adviceNumber,不做切分
            // 理由是,我打算使用scroll命令来拉取ES中的全量数据,目前并不支持并行
            // 参考:
            // scroll的使用文档:
            // https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-scroll.html
            // 对同一个scroll_id的并发获取下一页会有问题:
            // https://github.com/elastic/elasticsearch/issues/14954
            // ES计划在v5.0.0版本支持并发,proposal:
            // https://github.com/elastic/elasticsearch/issues/13494
        }
    }

    public static class Task extends Reader.Task {
//        private static final String MATCH_ALL = "{" +
//                " \"query\": {" +
//                "   \"match_all\": {}" +
//                " } " +
//                "}";
        private static final String MATCH_ALL = "{   \"match_all\": {} }";
        private static final String SEARCH_PARAMS = "_search?scroll=%s&size=%d";
        private static final String SCROLL_PARAMS = "{\"scroll\":\"%s\",\"scroll_id\":\"%s\"}";
        private String esIndex;
        private String esType;
        private String esQuery;
        private String esScrollTimeout;
        private int esBatchSize;
        private Map<String, DataType> columns;

        @Override
        public void init() {
            Configuration conf = super.getPluginJobConf();

            // 初始化ES集群
            ESConfigVo esConfigVo = new ESConfigVo();
            esConfigVo.setHosts(conf.getString(Key.ES_HOSTS)); // 必填
            esConfigVo.setUser(conf.getString(Key.ES_USERNAME, ""));
            esConfigVo.setPass(conf.getString(Key.ES_PASSWORD, ""));
            esConfigVo.setHttp_port(conf.getInt(Key.ES_HTTP_PORT, 9200));
            esConfigVo.setTcp_port(conf.getInt(Key.ES_TCP_PORT, 9300));
            ESMultiClusterManage.addESConfigs(Lists.newArrayList(esConfigVo));

            // 初始一些参数
            this.esIndex = conf.getString(Key.ES_INDEX); // 必填
            this.esType = conf.getString(Key.ES_TYPE); // 必填
            this.esScrollTimeout = conf.getString(Key.ES_SEARCH_TIMEOUT, "5m"); // 服务端游标超时时间
            this.esBatchSize = conf.getInt(Key.ES_BATCH_SIZE, 1000);

            // 改写query参数,增加sort排序,利于服务端scroll性能
            String queryStr = conf.getString(Key.ES_QUERY);
            JSONObject queryObj = JSON.parseObject(StringUtils.isNotBlank(queryStr) && !"{}".equals(queryStr) ? queryStr : MATCH_ALL);
            // Scroll requests have optimizations that make them faster when the sort order is _doc.
            // If you want to iterate over all documents regardless of the order, this is the most efficient option
            // query.put("sort", Lists.newArrayList("_doc"));
            this.esQuery = queryObj.toJSONString();

            // 获取索引元数据
            List<String> columns = conf.getList(Key.COLUMNS, String.class); // 必填
            retrieveIndexMapping(columns);
        }

        private void retrieveIndexMapping(List<String> columns) {
            MappingIndexVo vo = new MappingIndexVo();
            vo.setIndex(this.esIndex);
            vo.setType(this.esType);
            vo.setMetaType("_mapping");
            String response = EsClient.viewMappingIndex(vo);
            JSONObject mappings = JSON.parseObject(response);
            if (mappings.isEmpty()) {
                throw DataXException.asDataXException(EsReaderErrorCode.MAPPING_NOT_FOUND, response);
            }

            mappings = ((JSONObject) mappings.values().iterator().next()).getJSONObject("mappings");
            mappings = ((JSONObject) mappings.values().iterator().next()).getJSONObject("properties");
            this.columns = Maps.newLinkedHashMap(); // 有序的map
            for (String column : columns) {
                JSONObject mapping = mappings.getJSONObject(column);
                this.columns.put(column, DataType.parse(mapping.getString("type")));
            }
        }

        @Override
        public void destroy() {
        }

        @Override
        public void startRead(final RecordSender recordSender) {
            try {
                //SearchResultVo result = EsClient.dslSearch(beginScrollSearch());
                //SearchResultVo result = beginScrollSearchWithTimeRecord();
                /*SearchResultVo result = EsClient.dslSearch(beginScrollSearch());

                while (!result.getResults().isEmpty()) {
                    for (SearchResultDetailVO item : result.getResults()) {
                        handleItems(item, recordSender);
                    }
                    String scrollId = result.getOriginalData().getString("_scroll_id");
                    //result = EsClient.dslSearch(continueScrollSearch(scrollId));
                    result = continueScrollSearchWithTimeRecord(scrollId);
                }*/
                DSLSearchVo ffo = new DSLSearchVo();
                ffo.setIndex(this.esIndex);
                ffo.setType(this.esType);
                String query = "{\"size\":" + esBatchSize + ",\"query\": " + esQuery + "}";
                logger.info("query statement is :" + query);
                ffo.setContent(query);
                EsClient.dslScrollDocument(ffo, new ScrollQueryListener() {
                    @Override
                    public void onQuery(List<SearchResultDetailVO> results) {
                        for (SearchResultDetailVO item : results) {
                            handleItems(item, recordSender);
                        }
                    }
                });
            } catch (UnsupportedEncodingException e) {
                logger.warn("UnsupportedEncodingException: {}", e.getMessage());
            }
        }


        private SearchResultVo beginScrollSearchWithTimeRecord() throws UnsupportedEncodingException {
            long startTime = System.currentTimeMillis();
            SearchResultVo result = EsClient.dslSearch(beginScrollSearch());
            long elapseTime = System.currentTimeMillis() - startTime;
            logger.info("es_start_reader begin scroll search ->"+elapseTime+"  total->"+result.getResults().size());
            return result;
        }

        private SearchResultVo continueScrollSearchWithTimeRecord(String scrollId) throws UnsupportedEncodingException {
            long startTime = System.currentTimeMillis();
            SearchResultVo result = EsClient.dslSearch(beginScrollSearch());
            long elapseTime = System.currentTimeMillis() - startTime;
            logger.info("es_start_reader continue scroll search ->"+elapseTime+"  total->"+result.getResults().size());
            return result;
        }

        private DSLSearchVo beginScrollSearch() {
            DSLSearchVo vo = new DSLSearchVo();
            vo.setIndex(this.esIndex);
            vo.setType(this.esType);
            vo.setContent(this.esQuery);
            vo.setMetaType(String.format(SEARCH_PARAMS, this.esScrollTimeout, this.esBatchSize));
            return vo;
        }

        private DSLSearchVo continueScrollSearch(String scrollId) {
            DSLSearchVo vo = new DSLSearchVo();
            vo.setMetaType("_search/scroll");
            vo.setContent(String.format(SCROLL_PARAMS, this.esScrollTimeout, scrollId));
            return vo;
        }

        private void handleItems(SearchResultDetailVO item, RecordSender recordSender) {
            Record record = recordSender.createRecord();
            record.setId(item.getId()); // 主键
            JSONObject source = JSON.parseObject(item.getResult());

            for (Map.Entry<String, DataType> column : this.columns.entrySet()) {
                try {
                    switch (column.getValue()) {
                        case LONG:
                        case INTEGER:
                        case SHORT:
                        case BYTE:
                            record.addColumn(new LongColumn(source.getLong(column.getKey())));
                            break;
                        case DOUBLE:
                        case FLOAT:
                            record.addColumn(new DoubleColumn(source.getDouble(column.getKey())));
                            break;
                        case STRING:
                            record.addColumn(new StringColumn(source.getString(column.getKey())));
                            break;
                        case BOOLEAN:
                            record.addColumn(new BoolColumn(source.getBoolean(column.getKey())));
                            break;
                        case DATE:
                            record.addColumn(new DateColumn(DateUtils.parse(source.getString(column.getKey()))));
                            break;
                        case BINARY:
                            byte[] bytes = source.containsKey(column.getKey()) ? Base64.decodeFast(source.getString(column.getKey())) : null;
                            record.addColumn(new BytesColumn(bytes));
                            break;
                        default:
                            super.getTaskPluginCollector().collectDirtyRecord(record, "column " + column.getKey() + " data type unknown: " + source);
                            break;
                    }
                } catch (Exception e) {
                    super.getTaskPluginCollector().collectDirtyRecord(record, e, "column " + column.getKey() + " data type convert failure: " + source);
                }
            }

            recordSender.sendToWriter(record);
        }
    }


    public static void main(String args[]) throws Exception {
        ESConfigVo esConfigVo = new ESConfigVo();
        esConfigVo.setHosts("10.104.100.89,10.104.100.90"); // 必填
        esConfigVo.setUser("sdk");
        esConfigVo.setPass("pass4Sdk");
        esConfigVo.setHttp_port(9200);
        esConfigVo.setTcp_port(9300);
        ESMultiClusterManage.addESConfigs(Lists.newArrayList(esConfigVo));


        DSLSearchVo ffo = new DSLSearchVo();
        ffo.setIndex("t_b_system");
        ffo.setType("t_b_system");
        String query = "{\n" +
                "    \"size\": 100, \n" +
                "    \"query\": {\n" +
                "        \"match_all\": {}\n" +
                "    }\n" +
                "}";
        ffo.setContent(query);
        EsClient.dslScrollDocument(ffo, new ScrollQueryListener() {
            @Override
            public void onQuery(List<SearchResultDetailVO> results) {
                System.out.println(results);
            }
        });
    }
}
