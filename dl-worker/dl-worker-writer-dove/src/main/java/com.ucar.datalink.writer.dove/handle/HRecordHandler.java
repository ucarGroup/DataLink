package com.ucar.datalink.writer.dove.handle;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.common.errors.DatalinkException;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.contract.log.hbase.HUtil;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.dove.DoveMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.dove.DoveWriterParameter;
import com.ucar.datalink.domain.plugin.writer.dove.PartitionMode;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucarinc.dove.common.message.Message;
import com.ucarinc.framework.dove.DoveConstants;
import com.ucarinc.framework.dove.producer.ProducerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by liuyifan
 */
public class HRecordHandler extends AbstractHandler<HRecord> {

    static {
        System.setProperty("project.name", "datalink");
    }

    @Override
    public void initialize(TaskWriterContext context) {
        super.initialize(context);
    }

    @Override
    protected void doWrite(List<HRecord> records, TaskWriterContext context) {
        if (records.size() > 0) {
            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(records.get(0));
            MediaSourceInfo sourceInfo = mappingInfo.getTargetMediaSource();
            DoveMediaSrcParameter dove = sourceInfo.getParameterObj();
            MediaSourceInfo zkSourceInfo = DataLinkFactory.getObject(MediaSourceService.class).getById(dove.getZkMediaSourceId());
            ZkMediaSrcParameter zk = zkSourceInfo.getParameterObj();
            DoveWriterParameter doveWriterParameter = (DoveWriterParameter) context.getWriterParameter();

            Map<String, List<HRecord>> hrecordByTables = new HashMap<>();
            for (HRecord record : records) {
                String key = record.getNamespace() + record.getTableName();
                if (!hrecordByTables.containsKey(key)) {
                    hrecordByTables.put(key, new ArrayList<>());
                }
                hrecordByTables.get(key).add(record);
            }

            List<Future> results = new ArrayList<>();
            for (Map.Entry<String, List<HRecord>> hrecordByTable : hrecordByTables.entrySet()) {
                List<HRecord> hRecords = hrecordByTable.getValue();
                results.add(this.executorService.submit(() -> {
                    hRecords.stream().forEach(r -> {
                        Map<String, Object> data = Maps.newHashMap();
                        data.put("rowkey", HUtil.toString(r.getRowKey()));
                        data.put("namespace", r.getNamespace());
                        data.put("tableName", r.getTableName());
                        data.put("binlog_eventtime", String.valueOf(getMaxTimestamp(r)));
                        List<Map<String, String>> columns = Lists.newArrayList();
                        r.getColumns().stream().forEach(c -> {
                            Map<String, String> nameValue = Maps.newHashMap();
                            String columnName = HUtil.toString(c.getFamily()) + "_" + HUtil.toString(c.getQualifier());
                            String columnValue = HUtil.toString(c.getValue());
                            nameValue.put(columnName, columnValue);
                            columns.add(nameValue);
                        });
                        data.put("columns", columns);
                        submitToMQ(data, dove, zk, doveWriterParameter);
                    });

                }));
            }
            this.checkFutures(results, "something goes wrong when do writing to elasticsearch.");
        }
    }


    void submitToMQ(Map<String, Object> map, DoveMediaSrcParameter dove, ZkMediaSrcParameter zk, DoveWriterParameter doveWriterParameter) {
        System.setProperty(DoveConstants.SPRING_DOVE_META_SERVER, zk.getServers());

        String namespace = map.get("namespace").toString();
        String tableName = map.get("tableName").toString();
        String haseKey = namespace + "_" + tableName + "_" + map.get("rowkey");
        if (doveWriterParameter.getPartitionMode() == PartitionMode.TABLE) {
            haseKey = namespace + "_" + tableName;
        }

        String topic = dove.getTopic();
        if (topic.indexOf("${dbTable}") > -1) {
            topic = topic.substring(0, topic.indexOf("${dbTable}")) + namespace + "_" + tableName;
        }

        try {
            ProducerManager.send(new Message<Object, Object>(topic, haseKey, JSON.toJSONString(map)));
        } catch (InterruptedException e) {
            throw new DatalinkException(e);
        }
    }

    private static long getMaxTimestamp(HRecord record) {
        return record.getColumns().stream().mapToLong(c -> c.getTimestamp()).max().getAsLong();
    }

}
