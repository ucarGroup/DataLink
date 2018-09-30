package com.ucar.datalink.writer.es.handle;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucar.datalink.common.errors.DataLoadException;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.rdbms.RdbEventRecord;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.RecordChunk;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.worker.api.transform.Transformer;
import com.ucar.datalink.worker.api.util.BatchSplitter;
import com.ucar.datalink.writer.es.client.rest.client.EsClient;
import com.ucar.datalink.writer.es.client.rest.constant.ESEnum;
import com.ucar.datalink.writer.es.client.rest.vo.BatchContentVo;
import com.ucar.datalink.writer.es.client.rest.vo.BatchDocVo;
import com.ucar.datalink.writer.es.client.rest.vo.BulkResultVo;
import com.ucar.datalink.writer.es.intercept.DdlEventInterceptor;
import com.ucar.datalink.writer.es.util.EsConfigManager;
import com.ucar.datalink.writer.es.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

/**
 * 将RdbEventRecord同步到ElasticSearch
 * Created by lubiao on 2017/6/15.
 */
public class RdbEventRecordHandler extends AbstractHandler<RdbEventRecord> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdbEventRecordHandler.class);
    private static final int MAX_TRIES = 3;

    public RdbEventRecordHandler() {
        super();
        this.addInterceptorBefore(new DdlEventInterceptor());
    }

    private Transformer<RdbEventRecord> transformer = new RdbEventRecordTransformer();

    @Override
    protected void doWrite(List<RdbEventRecord> records, TaskWriterContext context) {
        if (records == null || records.isEmpty()) {
            return;
        }

        List<Future> results = new ArrayList<>();
        records.stream()
                .collect(Collectors.groupingBy(RdbEventRecord::getTableName))
                .entrySet()
                .stream()
                .forEach(i ->
                                results.add(executorService.submit(() -> {
                                            if (context.getWriterParameter().isUseBatch()) {
                                                List<List<RdbEventRecord>> list = BatchSplitter.splitForBatch(i.getValue(),
                                                        context.getWriterParameter().getBatchSize());
                                                for (List<RdbEventRecord> item : list) {
                                                    writeData(item);
                                                }
                                            } else {
                                                for (RdbEventRecord record : i.getValue()) {
                                                    writeData(Lists.newArrayList(record));
                                                }
                                            }
                                        })
                                )
                );

        Throwable ex = null;
        for (int i = 0; i < results.size(); i++) {
            Future result = results.get(i);
            try {
                Object obj = result.get();
                if (obj instanceof Throwable) {
                    ex = (Throwable) obj;
                }
            } catch (Throwable e) {
                ex = e;
            }
        }
        if (ex != null) {
            throw new DatalinkException("something goes wrong when do writing to elasticsearch.", ex);
        }
    }

    @Override
    protected RecordChunk<RdbEventRecord> transform(RecordChunk<RdbEventRecord> recordChunk, TaskWriterContext context) {
        return transformer.transform(recordChunk, context);
    }

    private void writeData(List<RdbEventRecord> records) {
        //框架层会保证传入的records都是同一个数据源的，so，取第一条数据的即可
        MediaSourceInfo targetMediaSource = RecordMeta.mediaMapping(records.get(0)).getTargetMediaSource();
        // 构建批量提交 动作描述信息
        List<BatchContentVo> contents = BatchContentBuilder.buildContents(records);

        if (contents != null && !contents.isEmpty()) {
            LOGGER.debug("Batch Content is :" + JSONObject.toJSONString(contents.get(0)));
        }

        // 批量提交到ES，并处理返回值
        syncToElasticsearch(contents, targetMediaSource);
    }


    private void syncToElasticsearch(List<BatchContentVo> contents, MediaSourceInfo targetMediaSource) {

        for (int i = 0; i < MAX_TRIES; i++) {
            if (contents.isEmpty()) {
                return;
            }

            try {
                //准备数据
                BatchDocVo route = new BatchDocVo(EsConfigManager.getESConfig(targetMediaSource).getClusterName());
                route.setBatchType("_bulk");

                //发送数据
                BulkResultVo bulkResultVo = EsClient.batchDocWithResultParse(route, contents);
                bulkResultVo.checkFailed();

                //解析结果
                String response = bulkResultVo.getJsonString();
                LOGGER.debug("The response is :" + response);
                List<JsonNode> nothingUpdatedActionList = Lists.newArrayList();
                List<JsonNode> explicitErrorActionList = Lists.newArrayList();
                handleResult(response, nothingUpdatedActionList, explicitErrorActionList);

                //处理结果
                if (nothingUpdatedActionList.isEmpty() && explicitErrorActionList.isEmpty()) {
                    return;
                } else {
                    LOGGER.debug("sync to elasticsearch failed,The response is :{}", response);
                    if (i == (MAX_TRIES - 1)) {
                        throw new DataLoadException("sync to elasticsearch failed , the reason is :" + response);
                    }

                    //准备重试
                    List<BatchContentVo> needRetryContents = Lists.newArrayList();
                    Set<String> idSet = Sets.newHashSet();
                    for (BatchContentVo content : contents) {
                        //对于（更新）找不到记录的第一条进行index，其余维持原操作（更新）
                        if (match(content, nothingUpdatedActionList) && !idSet.contains(content.getId())) {
                            content.setBatchActionEnum(ESEnum.BatchActionEnum.INDEX);
                            idSet.add(content.getId());
                        }
                        needRetryContents.add(content);
                    }
                    contents = needRetryContents;
                }
            } catch (Exception e) {
                LOGGER.error("sync to elasticsearch failed,the error is :", e);
                if (i == (MAX_TRIES - 1)) {
                    //最后一次还有错，抛异常
                    throw new DataLoadException("sync to elasticsearch failed.", e);
                }
            }
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(i)); // 重试间隔
        }
    }

    private void handleResult(String response, List<JsonNode> nothingUpdatedActionList,
                              List<JsonNode> explicitErrorActionList) {
        JsonNode json = JsonUtils.parse(response);
        if (!json.path("errors").asBoolean()) {
            if (!(json.path("errors") instanceof BooleanNode)) {
                LOGGER.error("@@@@@@@@ this is a node " + json.path("errors").toString());
                LOGGER.error("######## this is a error " + json.toString());
                LOGGER.error("======== this is a error " + response);
            }
            return;
        }

        for (JsonNode action : json.path("items")) {
            if (action.path("index").hasNonNull("error")) {
                // 写入失败
                explicitErrorActionList.add(action.path("index"));
            } else if (action.path("update").hasNonNull("error")) {
                // 更新失败
                int status = action.path("update").path("status").asInt();
                if (status == 404) {
                    // 0 rows affected, 这种错误以写入模式重试
                    nothingUpdatedActionList.add(action.path("update"));
                } else {
                    explicitErrorActionList.add(action.path("update"));
                }
            }
        }
    }

    private boolean match(BatchContentVo content, List<JsonNode> actionList) {
        for (JsonNode action : actionList) {
            boolean match = action.path("_type").asText().equals(content.getType())
                    && action.path("_id").asText().equals(content.getId());
            // action.path("_index").asText().equals(content.getIndex())
            // _index可能涉及alias问题，不能这么对比
            if (match) {
                actionList.remove(action);
                return true;
            }
        }
        return false;
    }

}
