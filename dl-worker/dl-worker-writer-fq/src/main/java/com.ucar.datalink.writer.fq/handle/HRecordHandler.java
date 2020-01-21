package com.ucar.datalink.writer.fq.handle;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ucar.datalink.biz.service.MediaSourceService;
import com.ucar.datalink.biz.utils.DataLinkFactory;
import com.ucar.datalink.contract.log.hbase.HRecord;
import com.ucar.datalink.contract.log.hbase.HUtil;
import com.ucar.datalink.domain.RecordMeta;
import com.ucar.datalink.domain.media.MediaMappingInfo;
import com.ucar.datalink.domain.media.MediaSourceInfo;
import com.ucar.datalink.domain.media.parameter.fq.FqMediaSrcParameter;
import com.ucar.datalink.domain.media.parameter.zk.ZkMediaSrcParameter;
import com.ucar.datalink.domain.plugin.writer.fq.FqWriterParameter;
import com.ucar.datalink.domain.plugin.writer.fq.PartitionMode;
import com.ucar.datalink.domain.plugin.writer.fq.SerializeMode;
import com.ucar.datalink.worker.api.handle.AbstractHandler;
import com.ucar.datalink.worker.api.task.TaskWriterContext;
import com.zuche.confcenter.client.manager.DefaultConfigCenterManager;
import com.zuche.framework.metaq.manager.ConfigCenterManager;
import com.zuche.framework.metaq.send.CommonMessageSend;
import com.zuche.framework.metaq.send.MqSendParVo;
import com.zuche.framework.remote.nio.codec.HessianSerializerUtils;

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
        System.setProperty("sz.framework.projectName", "datalink");
    }

    @Override
    public void initialize(TaskWriterContext context) {

        //初始化gaea,fq不关心业务线
        DefaultConfigCenterManager.getInstance();

        //fq要求必须要初始化，且调用多次也没有关系，只生效第一次,参数值也没有关系
        ConfigCenterManager.initDefaultPrefixOnce("ucar");

        super.initialize(context);
    }

    @Override
    protected void doWrite(List<HRecord> records, TaskWriterContext context) {
        if (records.size() > 0) {
            MediaMappingInfo mappingInfo = RecordMeta.mediaMapping(records.get(0));
            MediaSourceInfo sourceInfo = mappingInfo.getTargetMediaSource();
            FqMediaSrcParameter fq = sourceInfo.getParameterObj();
            MediaSourceInfo zkSourceInfo = DataLinkFactory.getObject(MediaSourceService.class).getById(fq.getZkMediaSourceId());
            ZkMediaSrcParameter zk = zkSourceInfo.getParameterObj();
            FqWriterParameter fqWriterParameter = (FqWriterParameter) context.getWriterParameter();

            Map<String,List<HRecord>> hrecordByTables = new HashMap<>();
            for(HRecord record  : records){
                String key = record.getNamespace() + record.getTableName();
                if(!hrecordByTables.containsKey(key)){
                    hrecordByTables.put(key,new ArrayList<>());
                }
                hrecordByTables.get(key).add(record);
            }

            List<Future> results = new ArrayList<>();
            for(Map.Entry<String, List<HRecord>> hrecordByTable :  hrecordByTables.entrySet()){
                List<HRecord> hRecords = hrecordByTable.getValue();
                results.add(this.executorService.submit(() ->{
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
                        submitToMQ(data, fq, zk, fqWriterParameter);
                    });

                }));
            }
            this.checkFutures(results,"something goes wrong when do writing to elasticsearch.");
        }
    }


    void submitToMQ(Map<String, Object> map, FqMediaSrcParameter fq, ZkMediaSrcParameter zk, FqWriterParameter fqWriterParameter) {
        String namespace = map.get("namespace").toString();
        String tableName = map.get("tableName").toString();

        //兼容老数据，为空时为id方式
        String haseKey = namespace + tableName + map.get("rowkey");
        if (fqWriterParameter.getPartitionMode() == PartitionMode.TABLE) {
            haseKey = namespace + tableName;
        }

        MqSendParVo mqSendParVo = new MqSendParVo();
        mqSendParVo.setServers(zk.getServers());
        mqSendParVo.setPrefix(fq.getClusterPrefix());

        String topic = fq.getTopic();
        if (topic.indexOf("${dbTable}") > -1) {
            topic = topic.substring(0, topic.indexOf("${dbTable}")) + namespace + "_" + tableName;
        }
        mqSendParVo.setTopic(topic);


        if (fqWriterParameter.getSerializeMode() == SerializeMode.Hessian) {
            mqSendParVo.setData(HessianSerializerUtils.serialize(map));
        } else if (fqWriterParameter.getSerializeMode() == SerializeMode.Json) {
            mqSendParVo.setData(JSON.toJSONBytes(map));
        } else {
            throw new UnsupportedOperationException("Invalid SerializeMode " + fqWriterParameter.getSerializeMode());
        }
        mqSendParVo.setS(1);
        mqSendParVo.setTimeout("30000");

        CommonMessageSend.sendMessage(haseKey, mqSendParVo);
    }

    private static long getMaxTimestamp(HRecord record) {
        return record.getColumns().stream().mapToLong(c -> c.getTimestamp()).max().getAsLong();
    }

}
