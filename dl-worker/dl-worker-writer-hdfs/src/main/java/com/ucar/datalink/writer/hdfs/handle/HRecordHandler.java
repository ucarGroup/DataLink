package com.ucar.datalink.writer.hdfs.handle;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.contract.log.hbase.HUtil;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.plugin.writer.hdfs.HdfsWriterParameter;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.ucar.datalink.writer.hdfs.HdfsTaskWriter;
import com.ucar.datalink.writer.hdfs.handle.config.HdfsConfig;
import com.ucar.datalink.writer.hdfs.handle.config.HdfsConfigManager;
import com.ucar.datalink.writer.hdfs.handle.util.Dict;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lubiao on 2017/12/13.
 */
public class HRecordHandler extends BaseRecordHandler<HRecord> {

    public HRecordHandler(HdfsTaskWriter hdfsTaskWriter, TaskWriterContext taskWriterContext, HdfsWriterParameter hdfsWriterParameter) {
        super(hdfsTaskWriter, taskWriterContext, hdfsWriterParameter);
    }

    @Override
    protected Map<String, List<String>> buildData(List<HRecord> records, TaskWriterContext context) {
        HRecord firstRecord = records.get(0);
        MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(firstRecord);
        String namespace = firstRecord.getNamespace();
        String name = firstRecord.getTableName();
        HdfsConfig hdfsConfig = HdfsConfigManager.getHdfsConfig(mappingInfo.getTargetMediaSource(), this.hdfsWriterParameter);
        String hdfsFilePath = hdfsConfig.getHdfsAddress() + hdfsFilePathGenerator.getHdfsFilePath(namespace, name, mappingInfo, Dict.HDFS_DB_TYPE_HBASE, Dict.HDFS_TRANSFER_DATA_TYPE_NORMAL);
        List<String> values = records.stream().map(r -> {
            Map<String, String> map = Maps.newHashMap();
            map.put("rowkey", HUtil.toString(r.getRowKey()));
            r.getColumns().stream().forEach(c -> {
                String columnName = HUtil.toString(c.getFamily()) + "_" + HUtil.toString(c.getQualifier());
                String columnValue = HUtil.toString(c.getValue());
                map.put(columnName, columnValue);
            });
            return JSONObject.toJSONString(map);
        }).collect(Collectors.toList());

        Map<String, List<String>> result = Maps.newHashMap();
        result.put(hdfsFilePath, values);
        return result;
    }
}
